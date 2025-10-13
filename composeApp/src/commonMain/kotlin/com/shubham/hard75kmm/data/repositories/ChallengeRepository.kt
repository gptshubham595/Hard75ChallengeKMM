package com.shubham.hard75kmm.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.russhwolf.settings.Settings
import com.shubham.hard75kmm.db.AppDatabase
import com.shubham.hard75kmm.db.Challenge_days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ChallengeRepository(
    db: AppDatabase,
    private val settings: Settings
) {
    private val queries = db.challengeDayQueries

    /**
     * Retrieves a flow of all days for the user's current attempt.
     */
    fun getDaysForCurrentAttempt(): Flow<List<Challenge_days>> {
        val currentAttempt = settings.getInt(ATTEMPT_KEY, 1)
        return queries.selectByAttempt(currentAttempt.toLong()).asFlow().mapToList(Dispatchers.IO)
    }

    /**
     * Retrieves a flow of all days from all attempts, for the Gallery screen.
     */
    fun getAllDays(): Flow<List<Challenge_days>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    /**
     * Fetches a specific day's data for the current attempt.
     */
    suspend fun getDay(dayNumber: Int): Challenge_days? = withContext(Dispatchers.IO) {
        val currentAttempt = getCurrentAttemptNumber()
        queries.selectByAttemptAndDay(currentAttempt.toLong(), dayNumber.toLong())
            .executeAsOneOrNull()
    }

    /**
     * Inserts or updates a Challenge_days in the database.
     */
    suspend fun upsertDay(day: Challenge_days) = withContext(Dispatchers.IO) {
        queries.insertDay(
            attemptNumber = day.attemptNumber,
            dayNumber = day.dayNumber,
            status = day.status,
            score = day.score,
            totalTasks = day.totalTasks,
            completedTaskIds = day.completedTaskIds,
            selfieImageUrl = day.selfieImageUrl,
            selfieNote = day.selfieNote,
            timestamp = day.timestamp
        )
    }

    /**
     * Gets the current attempt number from settings.
     */
    fun getCurrentAttemptNumber(): Int {
        return settings.getInt(ATTEMPT_KEY, 1)
    }

    /**
     * Increments the attempt number and saves the start date for the new attempt.
     */
    @OptIn(ExperimentalTime::class)
    fun startNewAttempt() {
        val newAttempt = getCurrentAttemptNumber() + 1
        settings.putInt(ATTEMPT_KEY, newAttempt)
        settings.putLong(getStartDateKey(newAttempt), Clock.System.now().toEpochMilliseconds())
    }

    /**
     * Retrieves the start date for the current attempt from settings.
     */
    @OptIn(ExperimentalTime::class)
    fun getStartDateForCurrentAttempt(): LocalDate {
        val currentAttempt = getCurrentAttemptNumber()
        val timestamp = settings.getLong(getStartDateKey(currentAttempt), 0L)
        return if (timestamp > 0) {
            Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        } else {
            // This is the first ever launch for this attempt, set and return today's date
            val todayTimestamp = Clock.System.now().toEpochMilliseconds()
            settings.putLong(getStartDateKey(currentAttempt), todayTimestamp)
            Instant.fromEpochMilliseconds(todayTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }

    private fun getStartDateKey(attemptNumber: Int) = "start_date_for_attempt_$attemptNumber"

    companion object {
        private const val ATTEMPT_KEY = "current_attempt"
    }
}

