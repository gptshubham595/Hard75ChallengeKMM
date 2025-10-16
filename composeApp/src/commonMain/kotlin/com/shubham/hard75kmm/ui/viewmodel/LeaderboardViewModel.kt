package com.shubham.hard75kmm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class LeaderboardViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    /**
     * A StateFlow that represents the current state of the leaderboard.
     * It starts in a loading state, then updates with either a list of entries
     * or an error message. It listens for real-time changes from Firestore.
     */
    val leaderboardState: StateFlow<LeaderboardState> = firestore
        .collection("leaderboard")
        // Order the results by score in descending order (highest first)
        .orderBy("totalScore", Direction.DESCENDING)
        .snapshots // This creates a Flow that emits updates in real-time
        .map { snapshot ->
            // Map the Firestore documents to our LeaderboardEntry data class
            val entries = snapshot.documents.map { doc ->
                doc.data<LeaderboardEntry>()
            }
            LeaderboardState(entries = entries, isLoading = false, error = null)
        }
        .catch { e ->
            // Handle any errors during data fetching (e.g., network issues)
            emit(
                LeaderboardState(
                    error = "Failed to load leaderboard: ${e.message}",
                    isLoading = false
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardState(isLoading = true) // Start in a loading state
        )
}