package com.shubham.hard75kmm.data.models

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a single entry in the leaderboard.
 * This data class is used for serialization/deserialization with Cloud Firestore.
 *
 * The empty default values are required for Firestore's automatic data mapping.
 *
 * @param userId The unique ID of the user from Firebase Authentication.
 * @param userName The display name of the user.
 * @param totalScore The final total score the user achieved after 75 days.
 * @param completedDate The server-side timestamp of when the user completed the challenge.
 */

@Serializable
data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val totalScore: Long = 0,
    // This annotation tells the KMM Firebase library to automatically
    // populate this field with the server's timestamp upon creation.
    val completedDate: Timestamp? = null
)

@OptIn(ExperimentalTime::class)
fun Timestamp.toInstant(): Instant {
    return Instant.fromEpochSeconds(this.seconds, this.nanoseconds.toLong())
}