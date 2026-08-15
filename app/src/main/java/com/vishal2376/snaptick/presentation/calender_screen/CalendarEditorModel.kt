package com.vishal2376.snaptick.presentation.calender_screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.domain.model.TaskSubtask
import com.vishal2376.snaptick.domain.model.TaskSubtaskCodec
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

internal val SudaColors = listOf(
    "#CF3E70", "#D95F61", "#D49A3B", "#93A85D",
    "#6E9F82", "#4E9296", "#6287C5", "#8B71BA",
)

internal data class QuadrantOption(
    val id: Int,
    val title: String,
    val description: String,
    val color: String,
)

internal val Quadrants = listOf(
    QuadrantOption(1, "重要紧急", "高价值且时间紧迫", "#D95F61"),
    QuadrantOption(2, "重要不急", "重要但可提前安排", "#D49A3B"),
    QuadrantOption(3, "紧急不重", "时间紧但价值较低", "#6287C5"),
    QuadrantOption(4, "轻松安排", "低压力与日常事项", "#CF3E70"),
)

internal data class CalendarEditorState(
    val id: Int = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate,
    val startTime: LocalTime = LocalTime.MIDNIGHT,
    val allDay: Boolean = true,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val colorHex: String = "#CF3E70",
    val quadrant: Int = 4,
    val isPinned: Boolean = false,
    val reminders: List<LocalDateTime> = emptyList(),
    val subtasks: List<TaskSubtask> = emptyList(),
    val isCompleted: Boolean = false,
    val isRepeated: Boolean = false,
    val repeatWeekdays: String = "",
    val pomodoroTimer: Int = -1,
    val calendarEventId: Long? = null,
    val completedDatesCsv: String = "",
) {
    val isEdit: Boolean get() = id > 0
}

internal fun pickDate(
    context: android.content.Context,
    initial: LocalDate,
    onPicked: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

internal fun pickTime(
    context: android.content.Context,
    initial: LocalTime,
    onPicked: (LocalTime) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
        initial.hour,
        initial.minute,
        true,
    ).show()
}

internal fun CalendarEditorState.toTask(): Task {
    val effectiveEndDate = endDate ?: startDate
    val effectiveStartTime = if (allDay) LocalTime.MIDNIGHT else startTime
    val effectiveEndTime = when {
        allDay && endDate == null -> effectiveStartTime
        allDay -> LocalTime.of(23, 59)
        endTime != null -> endTime
        endDate != null -> LocalTime.of(23, 59)
        else -> effectiveStartTime.plusHours(1)
    }
    val offsets = reminderOffsets()

    return Task(
        id = id,
        uuid = uuid,
        title = title.trim(),
        isCompleted = isCompleted,
        startTime = effectiveStartTime,
        endTime = effectiveEndTime,
        reminder = offsets.isNotEmpty(),
        isRepeated = isRepeated,
        repeatWeekdays = repeatWeekdays,
        pomodoroTimer = pomodoroTimer,
        date = startDate,
        priority = (quadrant - 1).coerceIn(0, 3),
        calendarEventId = calendarEventId,
        description = description.trim(),
        endDate = if (effectiveEndDate < startDate) startDate else effectiveEndDate,
        colorHex = colorHex,
        quadrant = quadrant.coerceIn(1, 4),
        isPinned = isPinned,
        subtasksJson = TaskSubtaskCodec.encode(subtasks),
        reminderOffsetsCsv = offsets.joinToString(","),
        completedDatesCsv = completedDatesCsv,
    )
}

internal fun CalendarEditorState.reminderOffsets(): List<Int> {
    if (reminders.isEmpty()) return emptyList()
    val anchor = LocalDateTime.of(startDate, if (allDay) LocalTime.MIDNIGHT else startTime)
    return reminders
        .map { Duration.between(it, anchor).toMinutes().toInt() }
        .filter { it >= 0 }
        .distinct()
        .sorted()
}

internal fun String.toComposeColor(fallback: Color): Color = runCatching {
    Color(AndroidColor.parseColor(this))
}.getOrDefault(fallback)
