package com.shubham.hard75kmm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.shubham.hard75kmm.data.db.entities.ChallengeDay
import com.shubham.hard75kmm.ui.viewmodel.GalleryViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun GalleryScreenRoot(navController: NavController) {
    val viewModel: GalleryViewModel = koinViewModel()
    val photosByAttempt by viewModel.photosByAttempt.collectAsState()

    GalleryScreenContent(
        photosByAttempt = photosByAttempt,
        onNavigateBack = { navController.popBackStack() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreenContent(
    photosByAttempt: Map<Long, List<ChallengeDay>>,
    onNavigateBack: () -> Unit
) {
    var selectedDay by remember { mutableStateOf<ChallengeDay?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("75 Day Gallery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (photosByAttempt.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't taken any photos yet!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Sort attempts in descending order (newest first)
                val sortedEntries = photosByAttempt.entries.sortedByDescending { it.key }

                sortedEntries.forEach { (attemptNumber, days) ->
                    item {
                        AttemptSection(
                            attemptNumber = attemptNumber,
                            days = days,
                            onPhotoClick = { day -> selectedDay = day }
                        )
                    }
                }
            }
        }
    }

    // Show the full-screen photo detail dialog when a day is selected
    if (selectedDay != null) {
        PhotoDetailDialog(
            day = selectedDay!!,
            onDismiss = { selectedDay = null }
        )
    }
}


@Composable
private fun AttemptSection(
    attemptNumber: Long,
    days: List<ChallengeDay>,
    onPhotoClick: (ChallengeDay) -> Unit
) {
    val totalScore = days.sumOf { it.score }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attempt $attemptNumber",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "$totalScore pts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }

    // Estimate the grid height to allow the parent LazyColumn to scroll smoothly.
    // This prevents nesting a lazy layout inside another without a fixed height.
    val gridHeight = ((days.size + 1) / 2 * 220).dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(gridHeight)
    ) {
        items(days, key = { "${it.attemptNumber}-${it.dayNumber}" }) { day ->
            PostalCardItem(day, onClick = { onPhotoClick(day) })
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun PostalCardItem(day: ChallengeDay, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            var imageLoadResult by remember { mutableStateOf<Result<Painter>?>(null) }
            val painter = rememberAsyncImagePainter(model = day.selfieImageUrl, onSuccess = {
                if (it.painter.intrinsicSize.width > 1 && it.painter.intrinsicSize.height > 1) {
                    imageLoadResult = Result.success(it.painter)
                } else {
                    imageLoadResult =
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
                        contentDescription = "Selfie for Day ${day.dayNumber}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
            }
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                day.timestamp?.let {
                    Text(
                        text = formatDate(Instant.fromEpochMilliseconds(it), "dd MMM yyyy"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                day.selfieNote?.let { note ->
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
private fun PhotoDetailDialog(day: ChallengeDay, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                var imageLoadResult by remember { mutableStateOf<Result<Painter>?>(null) }
                val painter = rememberAsyncImagePainter(model = day.selfieImageUrl, onSuccess = {
                    imageLoadResult =
                        if (it.painter.intrinsicSize.width > 1 && it.painter.intrinsicSize.height > 1) {
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
                            contentDescription = "Selfie for Day ${day.dayNumber}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.headlineMedium
                )
                day.timestamp?.let {
                    Text(
                        text = formatDate(
                            Instant.fromEpochMilliseconds(it),
                            "dd MMM yyyy, hh:mm a"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                day.selfieNote?.let { note ->
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatDate(instant: Instant, format: String): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    // Basic formatting for dates.
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return when (format) {
        "dd MMM yyyy" -> "${dateTime.dayOfMonth} $month ${dateTime.year}"
        "dd MMM yyyy, hh:mm a" -> {
            val hour = if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
            val minute = dateTime.minute.toString().padStart(2, '0')
            val ampm = if (dateTime.hour < 12) "AM" else "PM"
            "${dateTime.dayOfMonth} $month ${dateTime.year}, $hour:$minute $ampm"
        }

        else -> dateTime.toString()
    }
}