package com.shubham.hard75kmm.data.repositories

import com.benasher44.uuid.uuid4
import com.russhwolf.settings.Settings
import com.shubham.hard75kmm.data.models.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class TaskRepository(
    private val settings: Settings
) {
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }

    private val defaultTasks = listOf(
        Task(id = "gym", name = "Go to the Gym"),
        Task(id = "water_1l", name = "Drink 1L Water"),
        Task(id = "water_2l", name = "Drink 2L Water"),
        Task(id = "water_3l", name = "Drink 3L Water"),
        Task(id = "walk", name = "Outdoor Walk"),
        Task(id = "read", name = "Read 10 pages"),
        Task(id = "steps_5k", name = "Complete 5k steps"),
        Task(id = "steps_10k", name = "Complete 10k steps"),
        Task(id = "no_junk", name = "No Junk Food")
    )

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())

    init {
        // Load initial tasks from storage
        loadTasks()
    }

    /**
     * Exposes the current list of tasks as a Flow that the UI can observe.
     */
    fun getAllTasks(): Flow<List<Task>> = _taskList.asStateFlow()

    /**
     * Adds a new task to the user's list and saves it.
     */
    suspend fun addTask(taskName: String) {
        val newTask = Task(id = uuid4().toString(), name = taskName)
        _taskList.update { it + newTask }
        saveTasks()
    }

    /**
     * Deletes a task from the user's list and saves the changes.
     */
    suspend fun deleteTask(task: Task) {
        _taskList.update { it.filterNot { it.id == task.id } }
        saveTasks()
    }

    /**
     * Loads the task list from settings. If no tasks are saved,
     * it initializes the list with default tasks.
     */
    private fun loadTasks() {
        val tasksJson = settings.getString(TASKS_KEY, "")
        if (tasksJson.isNotBlank()) {
            try {
                _taskList.update { json.decodeFromString(tasksJson) }
            } catch (e: Exception) {
                // If decoding fails (e.g., corrupted data), fallback to default
                _taskList.update { defaultTasks }
            }
        } else {
            // This is the first launch, so load and save the default tasks
            _taskList.update { defaultTasks }
            // Launch a coroutine to save without blocking the init block
            CoroutineScope(Dispatchers.IO).launch { saveTasks() }
        }
    }

    /**
     * Saves the current task list to settings as a JSON string.
     */
    private suspend fun saveTasks() = withContext(Dispatchers.IO) {
        val tasksJson = json.encodeToString(_taskList.value)
        settings.putString(TASKS_KEY, tasksJson)
    }

    companion object {
        private const val TASKS_KEY = "user_tasks_list"
    }
}