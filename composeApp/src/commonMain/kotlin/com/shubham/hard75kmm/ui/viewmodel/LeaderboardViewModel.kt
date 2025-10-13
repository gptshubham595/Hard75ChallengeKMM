package com.shubham.hard75kmm.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shubham.hard75kmm.data.models.LeaderboardEntry
import com.shubham.hard75kmm.ui.models.LeaderboardState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LeaderboardViewModel : ScreenModel {

    private val firestore = Firebase.firestore

    val leaderboardState: StateFlow<LeaderboardState> = firestore
        .collection("leaderboard")
        // Order the results by score in descending order (highest first)
        .orderBy("totalScore", Direction.DESCENDING)
        .snapshots.map { snapshot ->
            // Map the Firestore documents to our LeaderboardEntry data class
            val entries = snapshot.documents.map { doc ->
                doc.data<LeaderboardEntry>()
            }
            LeaderboardState(entries = entries, isLoading = false)
        }
        .catch { e ->
            // Handle any errors during data fetching
            emit(
                LeaderboardState(
                    error = "Failed to load leaderboard: ${e.message}",
                    isLoading = false
                )
            )
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardState(isLoading = true) // Start in a loading state
        )
}