package com.shubham.hard75kmm.ui.models

import com.shubham.hard75kmm.data.db.entities.ChallengeDay
import com.shubham.hard75kmm.data.models.Task

data class ChallengeUiState(
    val days: List<ChallengeDay> = emptyList(),
    val taskList: List<Task> = emptyList(),
    val currentDayNumber: Long = 1,
    val isChallengeActive: Boolean = false,
    val hasFailed: Boolean = false,
    val userPhotoUrl: String? = null
)