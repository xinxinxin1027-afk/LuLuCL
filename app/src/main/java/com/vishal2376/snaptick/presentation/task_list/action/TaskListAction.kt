package com.vishal2376.snaptick.presentation.task_list.action

import com.vishal2376.snaptick.domain.model.Task
import java.time.LocalDate

sealed interface TaskListAction {
    data class ToggleCompletion(val taskId: Int, val isCompleted: Boolean) : TaskListAction

    /**
     * Calendar-specific completion action. Repeating tasks must be completed for
     * the day selected in the calendar rather than always mutating today's row.
     */
    data class ToggleCompletionForDate(
        val taskId: Int,
        val date: LocalDate,
        val isCompleted: Boolean,
    ) : TaskListAction

    data class SwipeTask(val task: Task) : TaskListAction
    data class DeleteTask(val taskId: Int) : TaskListAction
    data object UndoDelete : TaskListAction
}
