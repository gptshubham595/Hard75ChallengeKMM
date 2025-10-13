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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class ChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val taskRepository: TaskRepository
) : ScreenModel {

    // Firebase instances for KMM
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    val uiState: StateFlow<ChallengeUiState> = combine(
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
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChallengeUiState()
    )

    init {
        // Initial check for daily status when the ViewModel is created
        screenModelScope.launch {
            if (uiState.value.isChallengeActive) {
                checkDailyStatus()
            }
        }
    }

    // --- Task Management ---
    fun addTask(taskName: String) = screenModelScope.launch { taskRepository.addTask(taskName) }
    fun deleteTask(task: Task) = screenModelScope.launch { taskRepository.deleteTask(task) }

    // --- Photo Management (platform-specific implementation needed) ---
    @OptIn(ExperimentalTime::class)
    fun saveSelfie(photoData: ByteArray, note: String?) {
        // TODO: Implement expect/actual for saving byte array to file and getting the path
        // For now, this logic updates the database entry.
        screenModelScope.launch {
            val localUri = "path/to/saved/image.jpg" // Placeholder
            val currentDay = uiState.value.currentDayNumber
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

    // --- Challenge & Attempt Logic ---
    fun startChallenge() = startNewAttempt(isFirstEverAttempt = true)
    fun startNewAttempt(isFirstEverAttempt: Boolean = false) {
        screenModelScope.launch {
            if (!isFirstEverAttempt) {
                challengeRepository.startNewAttempt()
            }
            val newAttemptNumber = challengeRepository.getCurrentAttemptNumber()
            val initialDays = (1..75).map { dayNum ->
                Challenge_days(
                    attemptNumber = newAttemptNumber.toLong(),
                    dayNumber = dayNum.toLong(),
                    status = if (dayNum == 1) DayStatus.FAILED else DayStatus.LOCKED,
                    score = 0,
                    totalTasks = uiState.value.taskList.size.toLong(),
                    completedTaskIds = emptyList(),
                    selfieImageUrl = null,
                    selfieNote = null,
                    timestamp = null
                )
            }
            initialDays.forEach { challengeRepository.upsertDay(it) }
        }
    }

    fun updateTasksForCurrentDay(completedIds: List<String>) {
        screenModelScope.launch {
            val currentDayNumber = uiState.value.currentDayNumber
            val dayToUpdate = challengeRepository.getDay(currentDayNumber) ?: return@launch
            val totalTasks = uiState.value.taskList.size
            val newStatus = when {
                completedIds.isEmpty() -> DayStatus.FAILED
                completedIds.size < totalTasks -> DayStatus.IN_PROGRESS
                else -> DayStatus.COMPLETED
            }
            val newScore = when (newStatus) {
                DayStatus.COMPLETED -> 10
                DayStatus.IN_PROGRESS -> completedIds.size
                else -> 0
            }
            val updatedDay = dayToUpdate.copy(
                status = newStatus,
                score = newScore.toLong(),
                completedTaskIds = completedIds,
                totalTasks = totalTasks.toLong()
            )
            challengeRepository.upsertDay(updatedDay)
            if (currentDayNumber == 75 && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkDailyStatus() {
        screenModelScope.launch {
            val startDate = challengeRepository.getStartDateForCurrentAttempt()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val daysPassed = startDate.daysUntil(today) + 1
            if (daysPassed > 75 || daysPassed < 1) return@launch

            // Update current day number in a separate state if needed, or derive in UI
            // For simplicity, we can refetch/rely on the flow to update.

            val yesterdayData = challengeRepository.getDay(daysPassed - 1)
            if (yesterdayData != null && yesterdayData.status != DayStatus.COMPLETED) {
                // Handle failure state
            }
            val todayData = challengeRepository.getDay(daysPassed)
            if (todayData != null && todayData.status == DayStatus.LOCKED) {
                challengeRepository.upsertDay(todayData.copy(status = DayStatus.FAILED))
            }
        }
    }

    private fun completeChallenge() {
        screenModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val totalScore = uiState.value.days.sumOf { it.score }
            val entry = LeaderboardEntry(
                userId = user.uid,
                userName = user.displayName ?: "Anonymous",
                totalScore = totalScore
            )
            firestore.collection("leaderboard").document(user.uid).set(entry)
        }
    }

    fun dismissFailureDialog() {
        // Handle failure state and start new attempt
        startNewAttempt()
    }
}
