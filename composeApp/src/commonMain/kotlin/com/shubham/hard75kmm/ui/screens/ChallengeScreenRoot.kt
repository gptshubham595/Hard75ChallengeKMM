package com.shubham.hard75kmm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.shubham.hard75kmm.data.db.entities.ChallengeDay
import com.shubham.hard75kmm.data.db.entities.DayStatus
import com.shubham.hard75kmm.data.models.Task
import com.shubham.hard75kmm.navigation.Route
import com.shubham.hard75kmm.ui.components.CalendarView
import com.shubham.hard75kmm.ui.components.TasksPopup
import com.shubham.hard75kmm.ui.models.ChallengeUiState
import com.shubham.hard75kmm.ui.viewmodel.ChallengeViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ChallengeScreenRoot(
    navController: NavController,
    viewModel: ChallengeViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    ChallengeScreenContent(
        uiState = uiState,
        // Update navigation lambdas to use the NavController
        onNavigateToLeaderboard = { navController.navigate(Route.LeaderboardScreen) },
        onNavigateToEditTasks = { navController.navigate(Route.EditTasksScreen) },
        onNavigateToGallery = { navController.navigate(Route.GalleryScreen) },
        // ... other callbacks remain the same
        onStartChallenge = viewModel::startChallenge,
        onStartNewAttempt = { viewModel.startNewAttempt() },
        updateTasksForCurrentDay = viewModel::updateTasksForCurrentDay,
        onSelfieTaken = viewModel::saveSelfie,
        onDismissFailureDialog = viewModel::dismissFailureDialog
    )
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
                    if (!uiState.userPhotoUrl.isNullOrEmpty()) {
                        var imageLoadResult by remember { mutableStateOf<Result<Painter>?>(null) }
                        val painter =
                            rememberAsyncImagePainter(model = uiState.userPhotoUrl, onSuccess = {
                                imageLoadResult = if (it.painter.intrinsicSize.width > 1 && it.painter.intrinsicSize.height > 1) {
                                    Result.success(it.painter)
                                } else {
                                    Result.failure(Exception("Image load failed: Invalid Image Size!"))
                                }
                            }, onError = {
                                it.result.throwable.printStackTrace()
                                imageLoadResult = Result.failure(it.result.throwable)
                            })

                        when (val result = imageLoadResult) {
                            null -> CircularProgressIndicator()
                            else -> if (result.isSuccess) {
                                Image(
                                    painter = painter,
                                    contentDescription = "User Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(36.dp)
                                        .clip(CircleShape),
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                        )
                    }

                    // Dropdown menu for extra options
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Tasks") },
                            onClick = {
                                onNavigateToEditTasks()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Leaderboard") },
                            onClick = {
                                onNavigateToLeaderboard()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Leaderboard,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gallery") },
                            onClick = {
                                onNavigateToGallery()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Start New Attempt") },
                            onClick = {
                                showStartFreshDialog = true
                                showMenu = false
                            }
                        )
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ready to start the challenge?")
                    Button(onClick = onStartChallenge, modifier = Modifier.padding(top = 16.dp)) {
                        Text("START DAY 1")
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        val currentDayData = uiState.days.find { it.dayNumber == uiState.currentDayNumber }
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
            text = { Text("You missed a day and your streak is broken. You can start a new challenge.") },
            confirmButton = { TextButton(onClick = onDismissFailureDialog) { Text("START OVER") } }
        )
    }

    if (showStartFreshDialog) {
        AlertDialog(
            onDismissRequest = { showStartFreshDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Start a New Attempt?") },
            text = { Text("This will end your current attempt and start a new one on Day 1. Your previous progress will be saved in the gallery.") },
            confirmButton = {
                Button(
                    onClick = {
                        onStartNewAttempt()
                        showStartFreshDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartFreshDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- Previews for Jetpack Compose ---

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun ChallengeScreenContentPreview() {
    ChallengeScreenContent(
        uiState = ChallengeUiState(
            days = (1..75).map {
                ChallengeDay(
                    attemptNumber = 1,
                    dayNumber = it.toLong(),
                    status = DayStatus.getRandomStatus(),
                    score = 100,
                    totalTasks = 10,
                    completedTaskIds = listOf("selfie"),
                    selfieImageUrl = "",
                    selfieNote = "",
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            },
            taskList = listOf(Task(id = "gym", name = "Go to the Gym")),
            currentDayNumber = 5,
            isChallengeActive = true
        ),
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        onNavigateToGallery = {},
        onStartChallenge = {},
        onStartNewAttempt = {},
        updateTasksForCurrentDay = {},
        onSelfieTaken = { _, _ -> },
        onDismissFailureDialog = {}
    )
}

@Preview
@Composable
fun ChallengeScreenContentNotStartedPreview() {
    ChallengeScreenContent(
        uiState = ChallengeUiState(isChallengeActive = false),
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        onNavigateToGallery = {},
        onStartChallenge = {},
        onStartNewAttempt = {},
        updateTasksForCurrentDay = {},
        onSelfieTaken = { _, _ -> },
        onDismissFailureDialog = {}
    )
}