package com.vishal2376.snaptick.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vishal2376.snaptick.domain.converters.LocalDateConverter
import com.vishal2376.snaptick.domain.converters.LocalTimeConverter
import com.vishal2376.snaptick.util.Constants
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "task_table")
@TypeConverters(
    LocalTimeConverter::class,
    LocalDateConverter::class,
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val title: String = "",
    val isCompleted: Boolean = false,
    val startTime: LocalTime = LocalTime.now(),
    val endTime: LocalTime = LocalTime.now(),
    val reminder: Boolean = false,
    val isRepeated: Boolean = false,
    val repeatWeekdays: String = "",
    val pomodoroTimer: Int = -1,
    val date: LocalDate = LocalDate.now(),
    val priority: Int = 0,
    val calendarEventId: Long? = null,

    // LuluCalendar calendar-first fields, modelled after the SudaTools mini-program.
    val description: String = "",
    val endDate: LocalDate = date,
    val colorHex: String = "",
    val quadrant: Int = 4,
    val isPinned: Boolean = false,
    val subtasksJson: String = "[]",
    val reminderOffsetsCsv: String = "",
    val completedDatesCsv: String = "",
) {
    fun isAllDayTaskEnabled(): Boolean =
        startTime == endTime || (startTime == LocalTime.MIDNIGHT && endTime == LocalTime.of(23, 59))

    fun getRepeatWeekList(): List<Int> =
        if (repeatWeekdays.isBlank()) emptyList()
        else repeatWeekdays.split(",").mapNotNull { it.toIntOrNull() }

    /**
     * A one-off task occupies every day from [date] through [endDate], matching
     * SudaTools' start/end date range behavior. Repeating tasks keep the
     * original weekday recurrence model and ignore endDate as a recurrence cap.
     */
    fun shouldOccurOn(target: LocalDate): Boolean {
        if (!isRepeated) return !target.isBefore(date) && !target.isAfter(safeEndDate())
        if (target < date) return false
        val weekdayIndex = target.dayOfWeek.value - 1
        return getRepeatWeekList().contains(weekdayIndex)
    }

    fun safeEndDate(): LocalDate = if (endDate < date) date else endDate

    fun subtasks(): List<TaskSubtask> = TaskSubtaskCodec.decode(subtasksJson)

    fun withSubtasks(items: List<TaskSubtask>): Task = copy(subtasksJson = TaskSubtaskCodec.encode(items))

    fun reminderOffsets(): List<Int> = reminderOffsetsCsv
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .distinct()
        .sorted()

    fun withReminderOffsets(offsets: List<Int>): Task {
        val normalized = offsets.distinct().sorted()
        return copy(
            reminder = normalized.isNotEmpty(),
            reminderOffsetsCsv = normalized.joinToString(","),
        )
    }

    fun completedDates(): Set<String> = completedDatesCsv
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    fun isCompletedOn(target: LocalDate): Boolean =
        if (isRepeated) target.toString() in completedDates() else isCompleted

    fun withCompletionFor(target: LocalDate, completed: Boolean): Task {
        if (!isRepeated) return copy(isCompleted = completed)
        val values = completedDates().toMutableSet()
        if (completed) values += target.toString() else values -= target.toString()
        return copy(completedDatesCsv = values.sorted().joinToString(","))
    }

    fun isValidPomodoroSession(timeLeft: Long): Boolean =
        (getDuration() - timeLeft) >= Constants.MIN_VALID_POMODORO_SESSION * 60

    fun getDuration(checkPastTask: Boolean = false): Long {
        val startSec = startTime.toSecondOfDay()
        val endSec = endTime.toSecondOfDay()
        val crossesMidnight = endSec < startSec
        val fullDuration = if (crossesMidnight) {
            endSec + Constants.SECONDS_IN_DAY - startSec
        } else {
            endSec - startSec
        }

        if (!checkPastTask) return fullDuration.coerceAtLeast(0).toLong()

        val nowSec = LocalTime.now().toSecondOfDay()
        return if (crossesMidnight) {
            when {
                nowSec >= startSec -> (endSec + Constants.SECONDS_IN_DAY - nowSec).toLong()
                nowSec <= endSec -> (endSec - nowSec).toLong()
                else -> fullDuration.toLong()
            }
        } else {
            when {
                nowSec > endSec -> 0L
                nowSec in (startSec + 1)..<endSec -> (endSec - nowSec).toLong()
                else -> fullDuration.coerceAtLeast(0).toLong()
            }
        }
    }
}
