package com.shubham.hard75kmm.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shubham.hard75kmm.data.repositories.ChallengeRepository
import com.shubham.hard75kmm.db.Challenge_days
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GalleryViewModel(
    repository: ChallengeRepository
) : ScreenModel {

    /**
     * Exposes a flow of photos grouped by their attempt number.
     * This StateFlow is observed by the GalleryScreen to build its UI.
     */
    val photosByAttempt: StateFlow<Map<Long, List<Challenge_days>>> = repository.getAllDays()
        .map { allDays ->
            // 1. Filter the list to include only days that have a selfie image URL.
            // 2. Group the filtered list into a Map where the key is the attemptNumber.
            allDays
                .filter { !it.selfieImageUrl.isNullOrBlank() }
                .groupBy { it.attemptNumber }
        }
        .stateIn(
            scope = screenModelScope, // Use the provided scope for KMM ViewModels
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap() // The initial state is an empty map.
        )
}