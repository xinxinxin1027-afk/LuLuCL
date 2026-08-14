package com.vishal2376.snaptick.data.repositories

import android.content.Context
import androidx.room.withTransaction
import com.vishal2376.snaptick.data.calendar.CalendarPusher
import com.vishal2376.snaptick.data.local.TaskCompletion
import com.vishal2376.snaptick.data.local.TaskCompletionDao
import com.vishal2376.snaptick.data.local.TaskDao
import com.vishal2376.snaptick.data.local.TaskDatabase
import com.vishal2376.snaptick.data.local.TaskReminder
import com.vishal2376.snaptick.data.local.TaskReminderDao
import com.vishal2376.snaptick.domain.model.BACKUP_VERSION
import com.vishal2376.snaptick.domain.model.BackupCompletion
import com.vishal2376.snaptick.domain.model.BackupData
import com.vishal2376.snaptick.domain.model.BackupReminder
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.service.PomodoroService
import com.vishal2376.snaptick.util.ReminderScheduler
import com.vishal2376.snaptick.widget.worker.WidgetUpdateWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

class TaskRepository(
    private val dao: TaskDao,
    private val completionDao: TaskCompletionDao,
    private val reminderDao: TaskReminderDao,
    private val database: TaskDatabase,
    private val context: Context,
    private val calendarPusher: CalendarPusher,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend fun insertTask(task: Task, reminderOffsets: List<Int> = defaultOffsets(task)) {
        val normalized = normalizeReminderMetadata(task, reminderOffsets)
        dao.insertTask(normalized)
        val saved = dao.getTaskByUuid(normalized.uuid) ?: normalized
        writeReminderOffsets(saved.uuid, reminderOffsets)
        reminderScheduler.schedule(saved, offsets = reminderOffsets)
        calendarPusher.pushInsert(saved)
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun deleteTask(task: Task) {
        reminderScheduler.cancel(task.id)
        PomodoroService.stopIfRunningFor(context, task.id)
        completionDao.deleteAllForTask(task.uuid)
        reminderDao.deleteAllForTask(task.uuid)
        dao.deleteTask(task)
        calendarPusher.pushDelete(task)
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun updateTask(task: Task, reminderOffsets: List<Int>? = null) {
        reminderScheduler.cancel(task.id)
        if (task.isCompleted) PomodoroService.stopIfRunningFor(context, task.id)
        val effective = reminderOffsets
            ?: task.reminderOffsets().ifEmpty { reminderDao.offsetsForTask(task.uuid) }
                .ifEmpty { defaultOffsets(task) }
        val normalized = normalizeReminderMetadata(task, effective)
        dao.updateTask(normalized)
        if (reminderOffsets != null) writeReminderOffsets(task.uuid, effective)
        reminderScheduler.schedule(normalized, offsets = effective)
        calendarPusher.pushUpdate(normalized)
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun getReminderOffsets(uuid: String): List<Int> = reminderDao.offsetsForTask(uuid)

    private fun normalizeReminderMetadata(task: Task, offsets: List<Int>): Task = task.withReminderOffsets(offsets)

    private suspend fun writeReminderOffsets(uuid: String, offsets: List<Int>) {
        reminderDao.deleteAllForTask(uuid)
        if (offsets.isNotEmpty()) {
            reminderDao.insertAll(offsets.distinct().map { TaskReminder(uuid = uuid, offsetMinutes = it) })
        }
    }

    private fun defaultOffsets(task: Task): List<Int> = when {
        task.reminderOffsets().isNotEmpty() -> task.reminderOffsets()
        task.reminder -> listOf(0)
        else -> emptyList()
    }

    suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)

    suspend fun deleteAllTasks() {
        val active = PomodoroService.runningTaskId
        if (active > 0) PomodoroService.stopIfRunningFor(context, active)
        dao.deleteAllTasks()
        WidgetUpdateWorker.enqueueWorker(context)
    }

    fun getTasksByDate(selectedDate: LocalDate): Flow<List<Task>> =
        combine(dao.getAllTasks(), completionDao.completedUuidsOn(selectedDate.toString())) { tasks, completedUuids ->
            val completedSet = completedUuids.toHashSet()
            tasks.asSequence()
                .filter { it.shouldOccurOn(selectedDate) }
                .map { task ->
                    if (task.isRepeated) task.copy(isCompleted = task.uuid in completedSet || task.isCompletedOn(selectedDate))
                    else task
                }
                .sortedWith(taskDisplayComparator())
                .toList()
        }

    fun getTodayTasks(): Flow<List<Task>> = getTasksByDate(LocalDate.now())
    fun getTodayTasksWithCompletions(): Flow<List<Task>> = getTasksByDate(LocalDate.now())

    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks().onEach {
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun getAllTasksSnapshot(): List<Task> = dao.getAllTasksSnapshot()

    suspend fun snapshotBackup(): BackupData {
        val tasks = dao.getAllTasksSnapshot()
        val completions = completionDao.getAllSnapshot().map { BackupCompletion(uuid = it.uuid, date = it.date) }
        val reminders = reminderDao.getAllSnapshot().map { BackupReminder(uuid = it.uuid, offsetMinutes = it.offsetMinutes) }
        return BackupData(tasks = tasks, completions = completions, reminders = reminders)
    }

    suspend fun restoreFromBackup(data: BackupData) {
        require(data.version in 1..BACKUP_VERSION)
        database.withTransaction {
            dao.deleteAllTasks()
            completionDao.deleteAll()
            reminderDao.deleteAll()
            for (task in data.tasks) dao.insertTask(task)
            if (data.completions.isNotEmpty()) {
                completionDao.insertAll(data.completions.map { TaskCompletion(uuid = it.uuid, date = it.date) })
            }
            val explicitReminders = data.reminders.map { TaskReminder(uuid = it.uuid, offsetMinutes = it.offsetMinutes) }
            val backfilled = if (explicitReminders.isEmpty()) {
                data.tasks.filter { it.reminder }.flatMap { task ->
                    task.reminderOffsets().ifEmpty { listOf(0) }.map { TaskReminder(uuid = task.uuid, offsetMinutes = it) }
                }
            } else explicitReminders
            if (backfilled.isNotEmpty()) reminderDao.insertAll(backfilled)
        }
        val saved = dao.getAllTasksSnapshot()
        reminderScheduler.rescheduleAll(saved.map { task -> task to reminderDao.offsetsForTask(task.uuid) })
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun markCompletedForDate(uuid: String, date: LocalDate) {
        completionDao.insert(TaskCompletion(uuid = uuid, date = date.toString()))
        dao.getTaskByUuid(uuid)?.let { task ->
            val updated = task.withCompletionFor(date, true)
            dao.updateTask(updated)
            if (date == LocalDate.now()) PomodoroService.stopIfRunningFor(context, task.id)
            val offsets = reminderDao.offsetsForTask(uuid)
            reminderScheduler.cancel(task.id)
            reminderScheduler.schedule(updated, offsets = offsets, skipToday = date == LocalDate.now())
        }
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun unmarkCompletedForDate(uuid: String, date: LocalDate) {
        completionDao.delete(uuid, date.toString())
        dao.getTaskByUuid(uuid)?.let { task ->
            val updated = task.withCompletionFor(date, false)
            dao.updateTask(updated)
            val offsets = reminderDao.offsetsForTask(uuid)
            reminderScheduler.cancel(task.id)
            reminderScheduler.schedule(updated, offsets = offsets)
        }
        WidgetUpdateWorker.enqueueWorker(context)
    }

    suspend fun isCompletedOn(uuid: String, date: LocalDate): Boolean = completionDao.isCompleted(uuid, date.toString())

    suspend fun syncAllTasksNow() { calendarPusher.pushAllUnmirrored(dao.getAllTasksSnapshot()) }
    suspend fun deletePushedCalendarEvents(): Int = calendarPusher.deleteAllPushedEvents(dao.getAllTasksSnapshot())

    suspend fun rescheduleAllReminders() {
        val all = dao.getAllTasksSnapshot().filter { it.reminder && !it.isCompleted }
        reminderScheduler.rescheduleAll(all.map { task ->
            val stored = reminderDao.offsetsForTask(task.uuid)
            task to stored.ifEmpty { defaultOffsets(task) }
        })
    }

    private fun taskDisplayComparator(): Comparator<Task> = Comparator { a, b ->
        when {
            a.isCompleted != b.isCompleted -> if (a.isCompleted) 1 else -1
            a.isPinned != b.isPinned -> if (a.isPinned) -1 else 1
            a.startTime != b.startTime -> a.startTime.compareTo(b.startTime)
            else -> a.id.compareTo(b.id)
        }
    }
}
