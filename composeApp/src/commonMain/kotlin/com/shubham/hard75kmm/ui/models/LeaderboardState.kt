package com.shubham.hard75kmm.ui.models

import com.shubham.hard75kmm.data.models.LeaderboardEntry

data class LeaderboardState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
