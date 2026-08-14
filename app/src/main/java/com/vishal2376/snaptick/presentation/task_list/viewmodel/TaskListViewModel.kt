package com.vishal2376.snaptick.presentation.task_list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishal2376.snaptick.data.repositories.TaskRepository
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.presentation.task_list.action.TaskListAction
import com.vishal2376.snaptick.presentation.task_list.events.TaskListEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {

    val todayTasks: Flow<List<Task>> = repository.getTodayTasksWithCompletions()
    val allTasks: Flow<List<Task>> = repository.getAllTasks()

    private val _events = MutableSharedFlow<TaskListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var deletedTask: Task? = null
    private var deletedOffsets: List<Int> = emptyList()

    fun onAction(action: TaskListAction) {
        when (action) {
            is TaskListAction.ToggleCompletion -> toggleCompletion(
                taskId = action.taskId,
                date = LocalDate.now(),
                isCompleted = action.isCompleted,
            )

            is TaskListAction.ToggleCompletionForDate -> toggleCompletion(
                taskId = action.taskId,
                date = action.date,
                isCompleted = action.isCompleted,
            )

            is TaskListAction.UpsertTask -> viewModelScope.launch {
                if (action.task.id > 0) {
                    repository.updateTask(action.task, action.reminderOffsets)
                } else {
                    repository.insertTask(action.task, action.reminderOffsets)
                }
            }

            is TaskListAction.TogglePin -> viewModelScope.launch {
                repository.getTaskById(action.taskId)?.let { task ->
                    repository.updateTask(task.copy(isPinned = !task.isPinned))
                }
            }

            is TaskListAction.SwipeTask -> {
                deletedTask = action.task
                deleteTask(action.task)
            }

            is TaskListAction.DeleteTask -> viewModelScope.launch {
                repository.getTaskById(action.taskId)?.let { task ->
                    deletedTask = task
                    deletedOffsets = repository.getReminderOffsets(task.uuid)
                    repository.deleteTask(task)
                }
            }

            is TaskListAction.UndoDelete -> viewModelScope.launch {
                deletedTask?.let { task ->
                    repository.insertTask(task, deletedOffsets.ifEmpty { task.reminderOffsets() })
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            deletedOffsets = repository.getReminderOffsets(task.uuid)
            repository.deleteTask(task)
        }
    }

    private fun toggleCompletion(taskId: Int, date: LocalDate, isCompleted: Boolean) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            if (task.isRepeated) {
                if (isCompleted) repository.markCompletedForDate(task.uuid, date)
                else repository.unmarkCompletedForDate(task.uuid, date)
            } else {
                repository.updateTask(task.copy(isCompleted = isCompleted))
            }
        }
    }
}
