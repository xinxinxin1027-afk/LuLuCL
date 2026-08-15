package com.vishal2376.snaptick.presentation.calender_screen

import java.time.LocalDate

fun List<CalendarTaskItem>.forCalendarDate(date: LocalDate): List<CalendarTaskItem> =
    asSequence()
        .filter { it.occursOn(date) }
        .sortedWith(
            compareBy<CalendarTaskItem> { it.completedOn(date) }
                .thenByDescending { it.isPinned }
                .thenByDescending { it.allDay }
                .thenBy { it.start.toLocalTime() }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
        )
        .toList()

fun List<CalendarTaskItem>.applyAgendaFilter(
    date: LocalDate,
    filter: CalendarAgendaFilter,
): List<CalendarTaskItem> =
    forCalendarDate(date).filter { task ->
        val statusMatches = when (filter.status) {
            AgendaStatusFilter.ALL -> true
            AgendaStatusFilter.INCOMPLETE -> !task.completedOn(date)
            AgendaStatusFilter.COMPLETED -> task.completedOn(date)
            AgendaStatusFilter.PINNED -> task.isPinned
        }
        statusMatches && (filter.quadrant == null || task.quadrant == filter.quadrant)
    }

fun List<CalendarTaskItem>.searchCalendarTasks(query: String): List<CalendarTaskItem> {
    val keyword = query.trim()
    if (keyword.isBlank()) return emptyList()
    return asSequence()
        .filter { it.title.contains(keyword, ignoreCase = true) || it.description.contains(keyword, ignoreCase = true) }
        .sortedWith(
            compareByDescending<CalendarTaskItem> { it.isPinned }
                .thenBy { it.start }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
        )
        .toList()
}

fun CalendarTaskItem.toggleSubtask(subtaskId: String, completed: Boolean): CalendarTaskItem =
    copy(subtasks = subtasks.map { if (it.id == subtaskId) it.copy(completed = completed) else it })
