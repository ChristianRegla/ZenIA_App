package com.zenia.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zenia.app.MainActivity
import com.zenia.app.R
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.data.session.UserSessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryRepository: DiaryRepository,
    private val sessionManager: UserSessionManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayStr = LocalDate.now().toString()

            val entryToday = diaryRepository.getDiaryEntryByDate(todayStr)
            val hasEntryToday = entryToday != null

            if (!hasEntryToday) {
                val user = sessionManager.user.firstOrNull()
                val apodo = if (!user?.apodo.isNullOrBlank()) "${user?.apodo}, n" else "N"
                showNotification(
                    title = "¡No rompas tu racha! 🔥",
                    message = "${apodo}o olvides tomarte un minuto para registrar cómo te has sentido hoy."
                )
            }

            val isEnabled = userPreferencesRepository.streakReminderEnabled.first()
            if (isEnabled) {
                val hour = userPreferencesRepository.streakReminderHour.first()
                val minute = userPreferencesRepository.streakReminderMinute.first()
                NotificationScheduler.scheduleStreakReminder(appContext, hour, minute)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "zenia_streak_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Racha",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Te recuerda registrar tu estado de ánimo si no lo has hecho."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.logo_zenia)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}