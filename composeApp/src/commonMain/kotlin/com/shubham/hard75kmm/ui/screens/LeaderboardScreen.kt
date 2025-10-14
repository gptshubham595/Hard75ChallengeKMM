package com.shubham.hard75kmm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shubham.hard75kmm.data.models.LeaderboardEntry
import com.shubham.hard75kmm.data.models.toInstant
import com.shubham.hard75kmm.ui.models.LeaderboardState
import com.shubham.hard75kmm.ui.viewmodel.LeaderboardViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object LeaderboardScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<LeaderboardViewModel>()
        val leaderboardState by viewModel.leaderboardState.collectAsState()

        LeaderboardScreenContent(
            onNavigateBack = { navigator.pop() },
            leaderboardState = leaderboardState
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreenContent(
    onNavigateBack: () -> Unit,
    leaderboardState: LeaderboardState
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                leaderboardState.isLoading -> {
                    CircularProgressIndicator()
                }

                leaderboardState.error != null -> {
                    Text(
                        text = "Error: ${leaderboardState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                leaderboardState.entries.isEmpty() -> {
                    Text(text = "No one has completed the challenge yet!")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(leaderboardState.entries) { entry ->
                            LeaderboardItem(entry)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LeaderboardScreenContentPreviewLoading() {
    LeaderboardScreenContent(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(isLoading = true, entries = emptyList(), error = null)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LeaderboardScreenContentPreviewError() {
    LeaderboardScreenContent(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(
            isLoading = false,
            entries = emptyList(),
            error = "Failed to load leaderboard"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LeaderboardScreenContentPreviewEmpty() {
    LeaderboardScreenContent(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(isLoading = false, entries = emptyList(), error = null)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Preview
@Composable
fun LeaderboardScreenContentPreviewWithData() {
    val entries = listOf(
        LeaderboardEntry(
            userId = "user1",
            userName = "Alice",
            totalScore = 1500,
            completedDate = null
        ),
        LeaderboardEntry(
            userId = "user2",
            userName = "Bob",
            totalScore = 1250,
            completedDate = null
        ),
        LeaderboardEntry(
            userId = "user3",
            userName = "Charlie",
            totalScore = 1100,
            completedDate = null
        )
    )
    LeaderboardScreenContent(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(isLoading = false, entries = entries, error = null)
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = entry.userName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Completed: ${
                        entry.completedDate?.toInstant()?.let {
                            formatDate(it)
                        } ?: "N/A"
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = "${entry.totalScore} pts", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "${dateTime.dayOfMonth} $month ${dateTime.year}"
}
