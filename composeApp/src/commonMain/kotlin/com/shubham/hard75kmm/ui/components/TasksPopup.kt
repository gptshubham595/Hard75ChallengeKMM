package com.shubham.hard75kmm.ui.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val checkedStates = remember(dayData) {
        tasks.map { task ->
            dayData?.completedTaskIds?.contains(task.id) ?: false
        }.toMutableStateList()
    }

    val cameraManager = rememberCameraManager { imageData ->
        showNoteDialog = imageData
    }

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
                            .clickable { if (task.id != "selfie") checkedStates[index] = !checkedStates[index] }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { isChecked -> if (task.id != "selfie") checkedStates[index] = isChecked },
                                enabled = task.id != "selfie"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = task.name)
                        }
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
                val manuallyCheckedIds = tasks.filterIndexed { index, task ->
                    task.id != "selfie" && checkedStates[index]
                }.map { it.id }
                val finalCompletedIds = if (dayData?.completedTaskIds?.contains("selfie") == true) {
                    (manuallyCheckedIds + "selfie").distinct()
                } else {
                    manuallyCheckedIds
                }
                onFinish(finalCompletedIds)
            }) { Text("Update Tasks") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showNoteDialog != null) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = null },
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
                    showNoteDialog = null
                }) { Text("Save Selfie") }
            }
        )
    }
}
