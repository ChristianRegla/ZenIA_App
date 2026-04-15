package com.zenia.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleStreakReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<StreakReminderWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("streak_reminder_work")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "streak_reminder_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelStreakReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("streak_reminder_work")
    }
}