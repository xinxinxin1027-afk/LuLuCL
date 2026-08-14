package com.vishal2376.snaptick.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `task_table_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `startTime` TEXT NOT NULL,
                `endTime` TEXT NOT NULL,
                `reminder` INTEGER NOT NULL,
                `isRepeated` INTEGER NOT NULL,
                `repeatWeekdays` TEXT NOT NULL,
                `pomodoroTimer` INTEGER NOT NULL,
                `date` TEXT NOT NULL,
                `priority` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `task_table_new`
                (id, uuid, title, isCompleted, startTime, endTime, reminder, isRepeated, repeatWeekdays, pomodoroTimer, date, priority)
            SELECT
                id, uuid, title, isCompleted, startTime, endTime, reminder, isRepeat, repeatWeekdays, pomodoroTimer, date, priority
            FROM `task_table`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `task_table`")
        db.execSQL("ALTER TABLE `task_table_new` RENAME TO `task_table`")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `task_table` ADD COLUMN `calendarEventId` INTEGER DEFAULT NULL")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `task_completions` (
                `uuid` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                PRIMARY KEY(`uuid`, `date`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `task_reminders` (
                `uuid` TEXT NOT NULL,
                `offsetMinutes` INTEGER NOT NULL,
                PRIMARY KEY(`uuid`, `offsetMinutes`)
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT OR IGNORE INTO `task_reminders` (uuid, offsetMinutes) " +
                "SELECT uuid, 0 FROM `task_table` WHERE reminder = 1"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `task_table_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `startTime` TEXT NOT NULL,
                `endTime` TEXT NOT NULL,
                `reminder` INTEGER NOT NULL,
                `isRepeated` INTEGER NOT NULL,
                `repeatWeekdays` TEXT NOT NULL,
                `pomodoroTimer` INTEGER NOT NULL,
                `date` TEXT NOT NULL,
                `priority` INTEGER NOT NULL,
                `calendarEventId` INTEGER,
                `description` TEXT NOT NULL,
                `endDate` TEXT NOT NULL,
                `colorHex` TEXT NOT NULL,
                `quadrant` INTEGER NOT NULL,
                `isPinned` INTEGER NOT NULL,
                `subtasksJson` TEXT NOT NULL,
                `reminderOffsetsCsv` TEXT NOT NULL,
                `completedDatesCsv` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `task_table_new` (
                id, uuid, title, isCompleted, startTime, endTime, reminder,
                isRepeated, repeatWeekdays, pomodoroTimer, date, priority,
                calendarEventId, description, endDate, colorHex, quadrant,
                isPinned, subtasksJson, reminderOffsetsCsv, completedDatesCsv
            )
            SELECT
                id, uuid, title, isCompleted, startTime, endTime, reminder,
                isRepeated, repeatWeekdays, pomodoroTimer, date, priority,
                calendarEventId, '', date, '', priority + 1, 0, '[]',
                CASE WHEN reminder = 1 THEN '0' ELSE '' END, ''
            FROM `task_table`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `task_table`")
        db.execSQL("ALTER TABLE `task_table_new` RENAME TO `task_table`")
    }
}
