package com.shubham.hard75kmm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shubham.hard75kmm.data.models.Task
import com.shubham.hard75kmm.db.Challenge_days

@Composable
fun TasksPopup(
    tasks: List<Task>,
    dayData: Challenge_days?,
    onDismiss: () -> Unit,
    onFinish: (List<String>) -> Unit,
    onSelfieTaken: (ByteArray, String?) -> Unit
) {
    var showNoteDialog by remember { mutableStateOf<ByteArray?>(null) }

    // Remember the checked state of each task based on the current day's data
    val checkedStates = remember(dayData) {
        tasks.map { task ->
            dayData?.completedTaskIds?.contains(task.id) ?: false
        }.toMutableStateList()
    }

    // This gets the correct platform-specific camera and permission logic
    val cameraManager = rememberCameraManager { imageData ->
        showNoteDialog = imageData
    }

    // ### Main Task List Dialog ###
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Day ${dayData?.dayNumber ?: "-"} Tasks") },
        text = {
            LazyColumn {
                itemsIndexed(tasks) { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Allow toggling checkbox by clicking the row, except for selfie
                                if (task.id != "selfie") {
                                    checkedStates[index] = !checkedStates[index]
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { isChecked ->
                                    if (task.id != "selfie") {
                                        checkedStates[index] = isChecked
                                    }
                                },
                                // The selfie checkbox is only for display; it's checked programmatically
                                enabled = task.id != "selfie"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = task.name)
                        }
                        // Show camera icon only for the selfie task
                        if (task.id == "selfie") {
                            IconButton(onClick = { cameraManager.launch() }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Take Selfie")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Get the list of tasks the user manually checked.
                val manuallyCheckedIds = tasks.filterIndexed { index, task ->
                    task.id != "selfie" && checkedStates[index]
                }.map { it.id }

                // If the selfie was already completed, we must add it back to the list
                // to preserve its state.
                val finalCompletedIds = if (dayData?.completedTaskIds?.contains("selfie") == true) {
                    (manuallyCheckedIds + "selfie").distinct()
                } else {
                    manuallyCheckedIds
                }

                onFinish(finalCompletedIds)
                onDismiss() // Close the dialog after finishing
            }) { Text("Update Tasks") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )

    // ### Note Dialog (appears after a photo is taken) ###
    if (showNoteDialog != null) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = null }, // Dismiss if user clicks outside
            title = { Text("Add a Note (Optional)") },
            text = {
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("How was your day?") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSelfieTaken(showNoteDialog!!, noteText.takeIf { it.isNotBlank() })
                    showNoteDialog = null // Close this dialog
                }) { Text("Save Selfie") }
            }
        )
    }
}