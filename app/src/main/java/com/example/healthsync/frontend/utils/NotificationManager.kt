package com.example.healthsync.frontend.utils;

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class HealthNotificationManager(private val context: Context) {

    fun scheduleReminders() {
        scheduleWork("Water", 2, TimeUnit.HOURS)
        scheduleWork("Meal", 4, TimeUnit.HOURS)
        scheduleWork("Sleep", 24, TimeUnit.HOURS)
    }

    private fun scheduleWork(type: String, interval: Long, unit: TimeUnit) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(interval, unit)
            .setInputData(workDataOf("type" to type))
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "Reminder_$type",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
        override fun doWork(): Result {
            val type = inputData.getString("type") ?: "Health"
            showNotification(applicationContext, "HealthSync Reminder", "Time to log your $type!")
            return Result.success()
        }

        private fun showNotification(context: Context, title: String, message: String) {
            val channelId = "health_sync_reminders"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
