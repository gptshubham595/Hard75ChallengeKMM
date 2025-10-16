package com.shubham.hard75kmm.data.db.entities

import androidx.room.Entity

@Entity(tableName = "challenge_days", primaryKeys = ["attemptNumber", "dayNumber"])
data class ChallengeDay(
    val attemptNumber: Long,
    val dayNumber: Long,
    val status: DayStatus,
    val score: Long = 0,
    val totalTasks: Long,
    val completedTaskIds: List<String> = emptyList(),
    val selfieImageUrl: String? = null,
    val selfieNote: String? = null,
    val timestamp: Long? = null
)