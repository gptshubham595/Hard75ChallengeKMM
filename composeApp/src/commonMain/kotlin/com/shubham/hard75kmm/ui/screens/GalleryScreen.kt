@file:OptIn(ExperimentalTime::class)

package com.shubham.hard75kmm.ui.screens
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shubham.hard75kmm.data.db.entities.DayStatus
import com.shubham.hard75kmm.db.Challenge_days
import com.shubham.hard75kmm.ui.viewmodel.GalleryViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object GalleryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<GalleryViewModel>()
        val photosByAttempt by viewModel.photosByAttempt.collectAsState()

        GalleryScreenContent(
            photosByAttempt = photosByAttempt,
            onNavigateBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreenContent(
    photosByAttempt: Map<Long, List<Challenge_days>>,
    onNavigateBack: () -> Unit
) {
    var selectedDay by remember { mutableStateOf<Challenge_days?>(null) }

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

    if (selectedDay != null) {
        PhotoDetailDialog(
            day = selectedDay!!,
            onDismiss = { selectedDay = null }
        )
    }
}

@Preview
@Composable
fun GalleryScreenContentPreview() {
    val sampleDays1 = listOf(
        Challenge_days(1L, 1, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds()),
        Challenge_days(2L, 2, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds()),
        Challenge_days(3L, 3, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds()),
    )
    val sampleDays2 = listOf(
        Challenge_days(1L, 1, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds()),
        Challenge_days(2L, 2, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds()),
    )
    val photos = mapOf(
        1L to sampleDays1,
        2L to sampleDays2
    )
    GalleryScreenContent(photosByAttempt = photos, onNavigateBack = {})
}

@Preview
@Composable
fun GalleryScreenContentEmptyPreview() {
    GalleryScreenContent(photosByAttempt = emptyMap(), onNavigateBack = {})
}

@Preview
@Composable
fun GalleryScreenContentWithDialogPreview() {
    val day = Challenge_days(1L, 1, DayStatus.getRandomStatus(), 100, 10,listOf("selfie"), "","",Clock.System.now().toEpochMilliseconds())
    val photos = mapOf(1L to listOf(day))
    GalleryScreenContent(photosByAttempt = photos, onNavigateBack = {})
}

@Composable
private fun AttemptSection(
    attemptNumber: Long,
    days: List<Challenge_days>,
    onPhotoClick: (Challenge_days) -> Unit
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

    // A simple FlowRow is often better for a small number of items than a nested lazy list.
    // However, for potentially 75 photos, a grid is better. We estimate the height.
    val gridHeight = ((days.size + 1) / 2 * 220).dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(gridHeight)
    ) {
        items(days, key = { it.dayNumber }) { day ->
            PostalCardItem(day, onClick = { onPhotoClick(day) })
        }
    }
}

@Composable
fun PostalCardItem(day: Challenge_days, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            KamelImage(
                resource = { asyncPainterResource(data = day.selfieImageUrl!!) },
                contentDescription = "Selfie for Day ${day.dayNumber}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                onLoading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
            )
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

@Composable
private fun PhotoDetailDialog(day: Challenge_days, onDismiss: () -> Unit) {
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
                KamelImage(
                    resource = { asyncPainterResource(data = day.selfieImageUrl!!) },
                    contentDescription = "Full screen selfie for Day ${day.dayNumber}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Fit,
                    onLoading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.headlineMedium
                )
                day.timestamp?.let {
                    Text(
                        text = formatDate(Instant.fromEpochMilliseconds(it), "dd MMM yyyy, hh:mm a"),
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

fun formatDate(instant: Instant, format: String): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    // Basic formatting. For more complex needs, a library like ThreeTenBP on KMM is an option.
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return when (format) {
        "dd MMM yyyy" -> "${dateTime.day} $month ${dateTime.year}"
        "dd MMM yyyy, hh:mm a" -> {
            val hour = if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
            val minute = dateTime.minute.toString().padStart(2, '0')
            val ampm = if (dateTime.hour < 12) "AM" else "PM"
            "${dateTime.day} $month ${dateTime.year}, $hour:$minute $ampm"
        }
        else -> dateTime.toString()
    }
}