package com.vishal2376.snaptick.presentation.calender_screen

import com.vishal2376.snaptick.domain.model.TaskSubtask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

enum class CalendarViewMode { MONTH, WEEK, AGENDA }
enum class AgendaStatusFilter { ALL, INCOMPLETE, COMPLETED, PINNED }

data class CalendarAgendaFilter(
    val status: AgendaStatusFilter = AgendaStatusFilter.ALL,
    val quadrant: Int? = null,
) {
    val isActive: Boolean
        get() = status != AgendaStatusFilter.ALL || quadrant != null
}

data class CalendarTaskItem(
    val id: Long = 0L,
    val uuid: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean = false,
    val isCompleted: Boolean = false,
    val completedDates: Set<LocalDate> = emptySet(),
    val isPinned: Boolean = false,
    val quadrant: Int = 4,
    val colorHex: String = LuluTaskColors.first(),
    val reminders: List<LocalDateTime> = emptyList(),
    val isRepeated: Boolean = false,
    val repeatWeekdays: Set<DayOfWeek> = emptySet(),
    val subtasks: List<TaskSubtask> = emptyList(),
) {
    init {
        require(end >= start) { "Task end must not be before start." }
        require(quadrant in 1..4) { "Quadrant must be between 1 and 4." }
    }

    fun occursOn(date: LocalDate): Boolean {
        if (isRepeated) {
            val days = repeatWeekdays.ifEmpty { setOf(start.dayOfWeek) }
            return date >= start.toLocalDate() && date.dayOfWeek in days
        }
        return !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate())
    }

    fun completedOn(date: LocalDate): Boolean =
        if (isRepeated) date in completedDates else isCompleted

    fun withCompletion(date: LocalDate, completed: Boolean): CalendarTaskItem {
        return if (isRepeated) {
            copy(completedDates = if (completed) completedDates + date else completedDates - date)
        } else {
            copy(isCompleted = completed)
        }
    }

    val completedSubtasks: Int get() = subtasks.count { it.completed }
}

sealed interface CalendarTaskAction {
    data class Create(val task: CalendarTaskItem) : CalendarTaskAction
    data class Update(val task: CalendarTaskItem) : CalendarTaskAction
    data class Delete(val taskId: Long) : CalendarTaskAction
    data class ToggleCompletion(
        val taskId: Long,
        val date: LocalDate,
        val completed: Boolean,
    ) : CalendarTaskAction
    data class TogglePin(val taskId: Long) : CalendarTaskAction
    data class ToggleSubtask(
        val taskId: Long,
        val subtaskId: String,
        val completed: Boolean,
    ) : CalendarTaskAction
}

data class CalendarEditorState(
    val id: Long = 0L,
    val uuid: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endDate: LocalDate = LocalDate.now(),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val allDay: Boolean = false,
    val isCompleted: Boolean = false,
    val completedDates: Set<LocalDate> = emptySet(),
    val isPinned: Boolean = false,
    val quadrant: Int = 4,
    val colorHex: String = LuluTaskColors.first(),
    val reminders: List<LocalDateTime> = emptyList(),
    val isRepeated: Boolean = false,
    val repeatWeekdays: Set<DayOfWeek> = emptySet(),
    val subtasks: List<TaskSubtask> = emptyList(),
) {
    val startDateTime: LocalDateTime
        get() = LocalDateTime.of(startDate, if (allDay) LocalTime.MIN else startTime)

    val endDateTime: LocalDateTime
        get() = LocalDateTime.of(endDate, if (allDay) LocalTime.MAX else endTime)

    fun normalized(): CalendarEditorState {
        val safeEndDate = endDate.coerceAtLeast(startDate)
        val safeEndTime = when {
            allDay -> LocalTime.MAX
            safeEndDate == startDate && endTime <= startTime -> startTime.plusHours(1)
            else -> endTime
        }
        val safeStartTime = if (allDay) LocalTime.MIN else startTime
        val safeWeekdays = if (isRepeated) repeatWeekdays.ifEmpty { setOf(startDate.dayOfWeek) } else emptySet()
        return copy(
            title = title.trim(),
            description = description.trim(),
            startTime = safeStartTime,
            endDate = safeEndDate,
            endTime = safeEndTime,
            quadrant = quadrant.coerceIn(1, 4),
            colorHex = normalizeHexColor(colorHex),
            reminders = reminders.distinct().sorted().filter { it <= LocalDateTime.of(startDate, safeStartTime) },
            repeatWeekdays = safeWeekdays,
            subtasks = subtasks
                .map { it.copy(title = it.title.trim()) }
                .filter { it.title.isNotBlank() }
                .distinctBy { it.id },
        )
    }

    fun validate(): List<CalendarEditorError> {
        val errors = mutableListOf<CalendarEditorError>()
        if (title.isBlank()) errors += CalendarEditorError.TitleRequired
        if (title.trim().length > 120) errors += CalendarEditorError.TitleTooLong
        if (description.length > 1000) errors += CalendarEditorError.DescriptionTooLong
        if (endDateTime < startDateTime) errors += CalendarEditorError.EndBeforeStart
        if (isRepeated && repeatWeekdays.isEmpty()) errors += CalendarEditorError.RepeatDayRequired
        if (reminders.any { it > startDateTime }) errors += CalendarEditorError.ReminderAfterStart
        if (subtasks.any { it.title.isBlank() }) errors += CalendarEditorError.EmptySubtask
        return errors
    }

    fun toTask(): CalendarTaskItem {
        val safe = normalized()
        require(safe.validate().isEmpty()) { "Calendar editor contains invalid values." }
        return CalendarTaskItem(
            id = safe.id,
            uuid = safe.uuid,
            title = safe.title,
            description = safe.description,
            start = safe.startDateTime,
            end = safe.endDateTime,
            allDay = safe.allDay,
            isCompleted = safe.isCompleted,
            completedDates = safe.completedDates,
            isPinned = safe.isPinned,
            quadrant = safe.quadrant,
            colorHex = safe.colorHex,
            reminders = safe.reminders,
            isRepeated = safe.isRepeated,
            repeatWeekdays = safe.repeatWeekdays,
            subtasks = safe.subtasks,
        )
    }

    companion object {
        fun fromTask(task: CalendarTaskItem): CalendarEditorState = CalendarEditorState(
            id = task.id,
            uuid = task.uuid,
            title = task.title,
            description = task.description,
            startDate = task.start.toLocalDate(),
            startTime = task.start.toLocalTime(),
            endDate = task.end.toLocalDate(),
            endTime = task.end.toLocalTime(),
            allDay = task.allDay,
            isCompleted = task.isCompleted,
            completedDates = task.completedDates,
            isPinned = task.isPinned,
            quadrant = task.quadrant,
            colorHex = task.colorHex,
            reminders = task.reminders,
            isRepeated = task.isRepeated,
            repeatWeekdays = task.repeatWeekdays,
            subtasks = task.subtasks,
        )
    }
}

sealed class CalendarEditorError(val message: String) {
    data object TitleRequired : CalendarEditorError("请输入日程标题")
    data object TitleTooLong : CalendarEditorError("标题最多 120 个字符")
    data object DescriptionTooLong : CalendarEditorError("描述最多 1000 个字符")
    data object EndBeforeStart : CalendarEditorError("结束时间不能早于开始时间")
    data object RepeatDayRequired : CalendarEditorError("重复日程至少选择一个星期")
    data object ReminderAfterStart : CalendarEditorError("提醒时间不能晚于日程开始时间")
    data object EmptySubtask : CalendarEditorError("子任务内容不能为空")
}

val LuluTaskColors = listOf(
    "#CF3E70",
    "#D95F61",
    "#D49A3B",
    "#93A85D",
    "#6E9F82",
    "#4E9296",
    "#6287C5",
    "#8B71BA",
)

fun normalizeHexColor(value: String): String {
    val cleaned = value.trim().uppercase()
    return if (Regex("^#[0-9A-F]{6}$").matches(cleaned)) cleaned else LuluTaskColors.first()
}
