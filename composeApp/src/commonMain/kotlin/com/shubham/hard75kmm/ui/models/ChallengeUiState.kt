package com.shubham.hard75kmm.ui.models

import com.shubham.hard75kmm.data.models.Task
import com.shubham.hard75kmm.db.Challenge_days

data class ChallengeUiState(
    val days: List<Challenge_days> = emptyList(),
    val taskList: List<Task> = emptyList(),
    val currentDayNumber: Int = 1,
    val isChallengeActive: Boolean = false,
    val hasFailed: Boolean = false,
    val userPhotoUrl: String? = null
)