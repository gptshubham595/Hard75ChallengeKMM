package com.shubham.hard75kmm.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shubham.hard75kmm.ui.components.CalendarView
import com.shubham.hard75kmm.ui.components.TasksPopup
import com.shubham.hard75kmm.ui.models.ChallengeUiState
import com.shubham.hard75kmm.ui.viewmodel.ChallengeViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

object ChallengeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<ChallengeViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        ChallengeScreenContent(
            uiState = uiState,
            onNavigateToLeaderboard = { navigator.push(LeaderboardScreen) },
            onNavigateToEditTasks = { navigator.push(EditTasksScreen) },
            onNavigateToGallery = { navigator.push(GalleryScreen) },
            onStartChallenge = viewModel::startChallenge,
            onStartNewAttempt = { viewModel.startNewAttempt() },
            updateTasksForCurrentDay = viewModel::updateTasksForCurrentDay,
            onSelfieTaken = viewModel::saveSelfie,
            onDismissFailureDialog = viewModel::dismissFailureDialog
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreenContent(
    uiState: ChallengeUiState,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToEditTasks: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onStartChallenge: () -> Unit,
    onStartNewAttempt: () -> Unit,
    updateTasksForCurrentDay: (List<String>) -> Unit,
    onSelfieTaken: (ByteArray, String?) -> Unit,
    onDismissFailureDialog: () -> Unit,
) {
    var showTaskDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showStartFreshDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("75 Hard Challenge") },
                actions = {
                    uiState.userPhotoUrl?.let {
                        KamelImage(
                            resource = { asyncPainterResource(data = it) },
                            contentDescription = "User Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } ?: Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "User Profile",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                    )

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        // Dropdown items for navigation and actions
                        DropdownMenuItem(
                            text = { Text("Edit Tasks") },
                            onClick = { onNavigateToEditTasks(); showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Leaderboard") },
                            onClick = { onNavigateToLeaderboard(); showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Gallery") },
                            onClick = { onNavigateToGallery(); showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Start New Attempt") },
                            onClick = { showStartFreshDialog = true; showMenu = false })
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isChallengeActive) {
                Button(
                    onClick = { showTaskDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = uiState.taskList.isNotEmpty()
                ) {
                    Text("FINISH TODAY'S TASK")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isChallengeActive) {
                CalendarView(days = uiState.days)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ready to start the challenge?")
                    Button(onClick = onStartChallenge, modifier = Modifier.padding(top = 16.dp)) {
                        Text("START DAY 1")
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        val currentDayData = uiState.days.find { it.dayNumber.toInt() == uiState.currentDayNumber }
        TasksPopup(
            tasks = uiState.taskList,
            dayData = currentDayData,
            onDismiss = { showTaskDialog = false },
            onFinish = updateTasksForCurrentDay,
            onSelfieTaken = onSelfieTaken
        )
    }

    if (uiState.hasFailed) {
        AlertDialog(
            onDismissRequest = onDismissFailureDialog,
            title = { Text("Challenge Failed") },
            text = { Text("You missed a day. You can start a new attempt.") },
            confirmButton = { TextButton(onClick = onDismissFailureDialog) { Text("START OVER") } }
        )
    }

    if (showStartFreshDialog) {
        AlertDialog(
            onDismissRequest = { showStartFreshDialog = false },
            title = { Text("Start a New Attempt?") },
            text = { Text("This will start a new attempt on Day 1. Your previous progress will be saved in the gallery.") },
            confirmButton = {
                Button(onClick = { onStartNewAttempt(); showStartFreshDialog = false }) {
                    Text("Yes, Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStartFreshDialog = false
                }) { Text("Cancel") }
            }
        )
    }
}