package com.vishal2376.snaptick

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vishal2376.snaptick.worker.RescheduleAllRemindersWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject

@HiltAndroidApp
class SnaptickApplication : Application(), Configuration.Provider {

	@Inject
	lateinit var workerFactory: HiltWorkerFactory

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.setMinimumLoggingLevel(Log.INFO)
			.setExecutor(Dispatchers.Default.asExecutor())
			.build()

	override fun onCreate() {
		super.onCreate()
		ensureRemindersArmed()
	}

	private fun ensureRemindersArmed() {
		val request = OneTimeWorkRequestBuilder<RescheduleAllRemindersWorker>().build()
		WorkManager.getInstance(applicationContext).enqueueUniqueWork(
			UNIQUE_BACKFILL_WORK_NAME,
			ExistingWorkPolicy.KEEP,
			request,
		)
	}

	companion object {
		private const val UNIQUE_BACKFILL_WORK_NAME = "lulucalendar.reminder-backfill"
	}
}
