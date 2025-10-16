package com.shubham.hard75kmm.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shubham.hard75kmm.data.db.entities.DayStatus
import com.shubham.hard75kmm.data.models.LeaderboardEntry
import com.shubham.hard75kmm.data.models.Task
import com.shubham.hard75kmm.data.repositories.ChallengeRepository
import com.shubham.hard75kmm.data.repositories.TaskRepository
import com.shubham.hard75kmm.db.Challenge_days
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
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val taskRepository: TaskRepository
) : ScreenModel {

    // KMM Firebase instances
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        // This combine operator creates a reactive stream that updates the UI state
        // whenever the underlying data (days or tasks) changes.
        screenModelScope.launch {
            combine(
                challengeRepository.getDaysForCurrentAttempt(),
                taskRepository.getAllTasks()
            ) { days, tasks ->
                // Ensure the static 'selfie' task is always in the list for the UI
                val fullTaskList = tasks.toMutableList().apply {
                    if (none { it.id == "selfie" }) {
                        add(0, Task(id = "selfie", name = "Attach today's selfie"))
                    }
                }

                // Construct the new UI state
                ChallengeUiState(
                    days = days,
                    taskList = fullTaskList,
                    isChallengeActive = days.isNotEmpty(),
                    userPhotoUrl = auth.currentUser?.photoURL
                )
            }.stateIn(
                scope = screenModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChallengeUiState()
            ).collectLatest { state ->
                // Update the mutable state flow and trigger a check for the day's status
                _uiState.value = state
                if (state.isChallengeActive) {
                    checkDailyStatus()
                }
            }
        }
    }

    // --- Task Management ---
    fun addTask(taskName: String) = screenModelScope.launch {
        taskRepository.addTask(taskName)
    }

    fun deleteTask(task: Task) = screenModelScope.launch {
        taskRepository.deleteTask(task)
    }

    // --- Challenge & Attempt Logic ---
    fun startChallenge() {
        screenModelScope.launch {
            initializeDaysForNewAttempt()
        }
    }

    fun startNewAttempt() {
        screenModelScope.launch {
            challengeRepository.startNewAttempt()
            initializeDaysForNewAttempt()
        }
    }

    private suspend fun initializeDaysForNewAttempt() {
        val newAttemptNumber = challengeRepository.getCurrentAttemptNumber()
        val totalTasks = taskRepository.getAllTasks().first().size

        // Create Day 1 as FAILED (red) to prompt the user to act
        val day1 = Challenge_days(
            attemptNumber = newAttemptNumber.toLong(),
            dayNumber = 1,
            status = DayStatus.FAILED,
            score = 0,
            totalTasks = totalTasks.toLong(),
            completedTaskIds = emptyList(),
            selfieImageUrl = null,
            selfieNote = null,
            timestamp = Clock.System.now().toEpochMilliseconds() // Set timestamp to today
        )
        challengeRepository.upsertDay(day1)

        // Pre-populate the remaining 74 days as LOCKED
        val futureDays = (2..75).map { dayNum ->
            Challenge_days(
                attemptNumber = newAttemptNumber.toLong(),
                dayNumber = dayNum.toLong(),
                status = DayStatus.LOCKED,
                score = 0,
                totalTasks = totalTasks.toLong(),
                completedTaskIds = emptyList(),
                selfieImageUrl = null,
                selfieNote = null,
                timestamp = null
            )
        }
        futureDays.forEach { challengeRepository.upsertDay(it) }
    }


    // --- Task and Selfie Updates ---
    fun updateTasksForCurrentDay(completedIds: List<String>) {
        screenModelScope.launch {
            val currentDayNumber = _uiState.value.currentDayNumber
            val dayToUpdate = challengeRepository.getDay(currentDayNumber) ?: return@launch
            val totalTasks = _uiState.value.taskList.size

            val newStatus = when {
                completedIds.size == totalTasks -> DayStatus.COMPLETED
                completedIds.isNotEmpty() -> DayStatus.IN_PROGRESS
                else -> DayStatus.FAILED
            }

            // Correct scoring: IN_PROGRESS score should not include the selfie
            val newScore = when (newStatus) {
                DayStatus.COMPLETED -> 10
                DayStatus.IN_PROGRESS -> completedIds.count { it != "selfie" }
                else -> 0
            }

            val updatedDay = dayToUpdate.copy(
                status = newStatus,
                score = newScore.toLong(),
                completedTaskIds = completedIds,
                totalTasks = totalTasks.toLong(),
                timestamp = Clock.System.now()
                    .toEpochMilliseconds() // Update timestamp on any change
            )
            challengeRepository.upsertDay(updatedDay)

            if (currentDayNumber == 75 && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    fun saveSelfie(photoData: ByteArray, note: String?) {
        screenModelScope.launch {
            // TODO: Implement expect/actual for saving the byte array to a file
            // and getting the platform-specific file path.
            val localUri = "path/to/saved/image.jpg" // This is a placeholder

            val currentDay = _uiState.value.currentDayNumber
            val dayData = challengeRepository.getDay(currentDay) ?: return@launch

            val updatedDay = dayData.copy(
                selfieImageUrl = localUri,
                selfieNote = note,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            challengeRepository.upsertDay(updatedDay)

            // Programmatically mark the 'selfie' task as complete
            val updatedTaskIds = dayData.completedTaskIds.toMutableList().apply {
                if (!contains("selfie")) add("selfie")
            }
            updateTasksForCurrentDay(updatedTaskIds)
        }
    }

    private fun completeChallenge() {
        screenModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val totalScore = _uiState.value.days.sumOf { it.score }
            val entry = LeaderboardEntry(
                userId = user.uid,
                userName = user.displayName ?: "Anonymous",
                totalScore = totalScore
            )
            // The KMM library automatically adds the server timestamp
            firestore.collection("leaderboard").document(user.uid).set(entry)
        }
    }

    // --- Daily Status and Failure Logic ---
    private suspend fun getLatestUpdatedDay(): Challenge_days? {
        // In SQLDelight, we manually find the most recent day with a timestamp.
        return uiState.value.days
            .filter { it.timestamp != null }
            .maxByOrNull { it.timestamp!! }
    }

    private fun checkDailyStatus() {
        screenModelScope.launch {
            val latestDay = getLatestUpdatedDay() ?: return@launch
            val lastTimestamp = latestDay.timestamp ?: return@launch

            val today = LocalDate.fromEpochDays(
                (Clock.System.now().toEpochMilliseconds() / (1000 * 60 * 60 * 24)).toInt()
            )

            // Case 1: Still the same calendar day (respecting grace period).
            if (today.isSameDay(lastTimestamp)) {
                _uiState.update { it.copy(currentDayNumber = latestDay.dayNumber.toInt()) }
                return@launch
            }

            // Case 2: It's the very next day.
            if (today.isNextDay(lastTimestamp)) {
                if (latestDay.status == DayStatus.COMPLETED || latestDay.status == DayStatus.IN_PROGRESS) {
                    // Success! Unlock the next day.
                    val nextDayNumber = latestDay.dayNumber.toInt() + 1
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

            // Case 3: More than one day has passed.
            if (today.isMoreThanOneDay(lastTimestamp)) {
                if (latestDay.status == DayStatus.COMPLETED || latestDay.status == DayStatus.IN_PROGRESS) {
                    challengeRepository.upsertDay(latestDay.copy(status = DayStatus.FAILED))
                }
                _uiState.update { it.copy(hasFailed = true) }
            }
        }
    }

    fun dismissFailureDialog() {
        _uiState.update { it.copy(hasFailed = false) }
        startNewAttempt()
    }

    // --- Date/Time Helper Functions with Grace Period ---
    private fun isWithinGracePeriod(timestamp: Long): Boolean {
        // 2-hour grace period in milliseconds (2 AM cutoff)
        val twoHoursInMillis = 2 * 60 * 60 * 1000
        val gracePeriodEnd = timestamp + twoHoursInMillis
        return Clock.System.now().toEpochMilliseconds() < gracePeriodEnd
    }

    private fun LocalDate.isSameDay(lastDayTimeStamp: Long): Boolean {
        val systemZone = TimeZone.currentSystemDefault()
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(systemZone).date

        return this == lastDayDate || (this.toEpochDays() == lastDayDate.toEpochDays() + 1 && isWithinGracePeriod(
            lastDayTimeStamp
        ))
    }

    private fun LocalDate.isNextDay(lastDayTimeStamp: Long): Boolean {
        val systemZone = TimeZone.currentSystemDefault()
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(systemZone).date

        return this.toEpochDays() == lastDayDate.toEpochDays() + 1 && !isWithinGracePeriod(
            lastDayTimeStamp
        )
    }

    private fun LocalDate.isMoreThanOneDay(lastDayTimeStamp: Long): Boolean {
        val systemZone = TimeZone.currentSystemDefault()
        val lastDayDate = Instant.fromEpochMilliseconds(lastDayTimeStamp)
            .toLocalDateTime(systemZone).date

        return this.toEpochDays() > lastDayDate.toEpochDays() + 1
    }
}