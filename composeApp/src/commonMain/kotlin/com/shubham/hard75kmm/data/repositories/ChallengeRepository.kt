package com.shubham.hard75kmm.data.repositories

import com.russhwolf.settings.Settings
import com.shubham.hard75kmm.data.db.ChallengeDao
import com.shubham.hard75kmm.data.db.entities.ChallengeDay
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to the data source.
 * It is the single source of truth for the challenge data in the KMM module.
 */
class ChallengeRepository(
    private val dao: ChallengeDao,
    private val settings: Settings // KMM replacement for SharedPreferences
) {

    /**
     * Retrieves a flow of all days for the user's current attempt.
     */
    fun getDaysForCurrentAttempt(): Flow<List<ChallengeDay>> {
        val currentAttempt = getCurrentAttemptNumber()
        return dao.getDaysForAttempt(currentAttempt)
    }

    /**
     * Fetches the most recently updated day from the database for the current attempt.
     * This is the essential function that was missing.
     */
    suspend fun getLatestUpdatedDay(): ChallengeDay? {
        val currentAttempt = getCurrentAttemptNumber()
        return dao.getLatestUpdatedDayForAttempt(currentAttempt)
    }

    /**
     * Retrieves a flow of all days from all attempts.
     * Used by the GalleryViewModel.
     */
    fun getAllDays(): Flow<List<ChallengeDay>> {
        return dao.getAllDays()
    }

    /**
     * Fetches a specific day's data for the current attempt.
     */
    suspend fun getDay(dayNumber: Long): ChallengeDay? {
        val currentAttempt = getCurrentAttemptNumber()
        return dao.getDayForAttempt(currentAttempt, dayNumber)
    }

    /**
     * Inserts or updates a ChallengeDay in the database.
     */
    suspend fun upsertDay(day: ChallengeDay) {
        dao.upsertDay(day)
    }

    /**
     * Gets the current attempt number from multiplatform-settings.
     */
    fun getCurrentAttemptNumber(): Long {
        return settings.getLong(ATTEMPT_KEY, 1)
    }

    /**
     * Increments the attempt number in multiplatform-settings.
     */
    fun startNewAttempt() {
        val newAttempt = getCurrentAttemptNumber() + 1
        settings.putLong(ATTEMPT_KEY, newAttempt)
    }

    companion object {
        private const val ATTEMPT_KEY = "current_attempt"
    }
}