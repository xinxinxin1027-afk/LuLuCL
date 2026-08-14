package com.vishal2376.snaptick.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vishal2376.snaptick.domain.model.Task

@Database(
    entities = [Task::class, TaskCompletion::class, TaskReminder::class],
    version = 6,
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun taskReminderDao(): TaskReminderDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "local_db",
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
