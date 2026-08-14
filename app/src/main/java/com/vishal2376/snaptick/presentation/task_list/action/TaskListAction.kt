package com.vishal2376.snaptick.presentation.task_list.action

import com.vishal2376.snaptick.domain.model.Task
import java.time.LocalDate

sealed interface TaskListAction {
    data class ToggleCompletion(val taskId: Int, val isCompleted: Boolean) : TaskListAction

    data class ToggleCompletionForDate(
        val taskId: Int,
        val date: LocalDate,
        val isCompleted: Boolean,
    ) : TaskListAction

    /** Insert a new calendar task or replace an existing one, including reminders. */
    data class UpsertTask(
        val task: Task,
        val reminderOffsets: List<Int> = task.reminderOffsets(),
    ) : TaskListAction

    data class TogglePin(val taskId: Int) : TaskListAction
    data class SwipeTask(val task: Task) : TaskListAction
    data class DeleteTask(val taskId: Int) : TaskListAction
    data object UndoDelete : TaskListAction
}
