package com.shubham.hard75kmm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.hard75kmm.data.ImageStorage
import com.shubham.hard75kmm.data.db.entities.ChallengeDay
import com.shubham.hard75kmm.data.db.entities.DayStatus
import com.shubham.hard75kmm.data.db.entities.DayStatus.Companion.stillHasHope
import com.shubham.hard75kmm.data.models.LeaderboardEntry
import com.shubham.hard75kmm.data.models.Task
import com.shubham.hard75kmm.data.repositories.ChallengeRepository
import com.shubham.hard75kmm.data.repositories.TaskRepository
import com.shubham.hard75kmm.ui.models.ChallengeUiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val taskRepository: TaskRepository,
    private val imageStorage: ImageStorage
) : ViewModel() { // Using androidx.lifecycle.ViewModel for viewModelScope

    // KMM Firebase instances
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // This reactive stream updates the UI whenever the database or task list changes
            combine(
                challengeRepository.getDaysForCurrentAttempt(),
                taskRepository.getAllTasks()
            ) { days, tasks ->
                val fullTaskList = tasks.toMutableList().apply {
                    if (none { it.id == "selfie" }) {
                        add(0, Task(id = "selfie", name = "Attach today's selfie"))
                    }
                }
                ChallengeUiState(
                    days = days,
                    taskList = fullTaskList,
                    isChallengeActive = days.isNotEmpty(),
                    userPhotoUrl = auth.currentUser?.photoURL
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChallengeUiState()
            ).collectLatest { state ->
                _uiState.value = state
                if (state.isChallengeActive) {
                    checkDailyStatus()
                }
            }
        }
    }

    // --- Task Management ---
    fun addTask(taskName: String) = viewModelScope.launch { taskRepository.addTask(taskName) }
    fun deleteTask(task: Task) = viewModelScope.launch { taskRepository.deleteTask(task) }

    // --- Daily Status and Failure Logic ---
    private fun checkDailyStatus() {
        viewModelScope.launch {
            val latestDay = challengeRepository.getLatestUpdatedDay() ?: return@launch
            val lastTimestamp = latestDay.timestamp ?: return@launch
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Case 1: Still the same calendar day (with grace period).
            if (today.isSameDay(lastTimestamp)) {
                _uiState.update { it.copy(currentDayNumber = latestDay.dayNumber) }
                return@launch
            }

            // Case 2: It's the very next day.
            if (today.isNextDay(lastTimestamp)) {
                if (latestDay.status.stillHasHope()) {
                    // Success! Unlock the next day.
                    val nextDayNumber = latestDay.dayNumber + 1
                    if (nextDayNumber > 75) return@launch // Challenge finished

                    val nextDay = challengeRepository.getDay(nextDayNumber)
                    if (nextDay != null && nextDay.status == DayStatus.LOCKED) {
                        val unlockedNextDay = nextDay.copy(
                            status = DayStatus.FAILED, // Start as red
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                        challengeRepository.upsertDay(unlockedNextDay)
                        _uiState.update { it.copy(currentDayNumber = nextDayNumber) }
                    }
                } else {
                    // Failed to complete yesterday's tasks.
                    challengeRepository.upsertDay(latestDay.copy(status = DayStatus.FAILED))
                    _uiState.update { it.copy(hasFailed = true) }
                }
                return@launch
            }

            // Case 3: More than one day has passed. You missed a day.
            if (today.isMoreThanOneDay(lastTimestamp)) {
                if (latestDay.status.stillHasHope()) {
                    challengeRepository.upsertDay(latestDay.copy(status = DayStatus.FAILED))
                }
                _uiState.update { it.copy(hasFailed = true) }
            }
        }
    }

    // --- Challenge & Attempt Logic ---
    fun startChallenge() = viewModelScope.launch { initializeDaysForNewAttempt() }

    fun startNewAttempt() = viewModelScope.launch {
        challengeRepository.startNewAttempt()
        initializeDaysForNewAttempt()
    }

    private suspend fun initializeDaysForNewAttempt() {
        val newAttemptNumber = challengeRepository.getCurrentAttemptNumber()
        val totalTasks = taskRepository.getAllTasks().first().size
        val day1 = ChallengeDay(
            attemptNumber = newAttemptNumber,
            dayNumber = 1,
            status = DayStatus.FAILED,
            totalTasks = totalTasks.toLong(),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        challengeRepository.upsertDay(day1)

        val futureDays = (2..75).map { dayNum ->
            ChallengeDay(
                attemptNumber = newAttemptNumber,
                dayNumber = dayNum.toLong(),
                status = DayStatus.LOCKED,
                totalTasks = totalTasks.toLong(),
                timestamp = null
            )
        }
        futureDays.forEach { challengeRepository.upsertDay(it) }
    }

    // --- Task and Selfie Updates ---
    fun updateTasksForCurrentDay(completedIds: List<String>) {
        viewModelScope.launch {
            val currentDayNumber = _uiState.value.currentDayNumber
            val dayToUpdate = challengeRepository.getDay(currentDayNumber) ?: return@launch
            val totalTasks = _uiState.value.taskList.size
            val newStatus = when {
                completedIds.size == totalTasks -> DayStatus.COMPLETED
                completedIds.isNotEmpty() -> DayStatus.IN_PROGRESS
                else -> DayStatus.FAILED
            }
            val newScore = when (newStatus) {
                DayStatus.COMPLETED -> 10L
                DayStatus.IN_PROGRESS -> completedIds.count { it != "selfie" }
                else -> 0L
            }
            val updatedDay = dayToUpdate.copy(
                status = newStatus,
                score = newScore.toLong(),
                completedTaskIds = completedIds,
                totalTasks = totalTasks.toLong(),
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            challengeRepository.upsertDay(updatedDay)

            if (currentDayNumber == 75L && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    fun saveSelfie(photoData: ByteArray, note: String?) {
        viewModelScope.launch {
            val localUri = imageStorage.saveImage(photoData)
            val currentDay = _uiState.value.currentDayNumber
            val dayData = challengeRepository.getDay(currentDay) ?: return@launch

            val updatedDay = dayData.copy(
                selfieImageUrl = localUri,
                selfieNote = note,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            challengeRepository.upsertDay(updatedDay)

            val updatedTaskIds = dayData.completedTaskIds.toMutableList().apply {
                if (!contains("selfie")) add("selfie")
            }
            updateTasksForCurrentDay(updatedTaskIds)
        }
    }

    private fun completeChallenge() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val totalScore = _uiState.value.days.sumOf { it.score }
            val entry = LeaderboardEntry(
                userId = user.uid,
                userName = user.displayName ?: "Anonymous",
                totalScore = totalScore
            )
            firestore.collection("leaderboard").document(user.uid).set(entry)
        }
    }

    fun dismissFailureDialog() {
        _uiState.update { it.copy(hasFailed = false) }
        startNewAttempt()
    }

    // --- Date/Time Helper Functions (using kotlinx-datetime) ---
    private fun isWithinGracePeriod(timestamp: Long): Boolean {
        // 2-hour grace period in milliseconds
        val twoHoursInMillis = 2 * 60 * 60 * 1000
        val gracePeriodEnd = timestamp + twoHoursInMillis
        return Clock.System.now().toEpochMilliseconds() < gracePeriodEnd
    }

    private fun LocalDate.isSameDay(lastDayTimeStamp: Long): Boolean {
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return this == lastDayDate || (this == lastDayDate.plus(
            1,
            kotlinx.datetime.DateTimeUnit.DAY
        ) && isWithinGracePeriod(lastDayTimeStamp))
    }

    private fun LocalDate.isNextDay(lastDayTimeStamp: Long): Boolean {
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return this == lastDayDate.plus(
            1,
            kotlinx.datetime.DateTimeUnit.DAY
        ) && !isWithinGracePeriod(lastDayTimeStamp)
    }

    private fun LocalDate.isMoreThanOneDay(lastDayTimeStamp: Long): Boolean {
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return this > lastDayDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
    }
}