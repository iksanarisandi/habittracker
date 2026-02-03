package com.habittracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.HabitApplication
import com.habittracker.data.HabitRepository
import com.habittracker.ui.home.MainActivity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository: HabitRepository by lazy {
        val database = (applicationContext as HabitApplication).database
        HabitRepository(database.habitDao())
    }

    // Get habit ID from input data
    private val habitId: Long = workerParams.inputData.getLong("habitId", -1L)

    override suspend fun doWork(): Result {
        if (habitId == -1L) {
            return Result.failure()
        }

        val today = LocalDate.now()

        try {
            // Get specific habit
            val habit = repository.getHabitById(habitId).first()
                ?: return Result.failure()

            // Check if reminder is enabled
            if (!habit.isReminderEnabled || habit.reminderTime == null) {
                return Result.success()
            }

            // Check if already completed today
            val log = repository.getHabitLog(habitId, today)
            if (log?.completed == true) {
                return Result.success()
            }

            // Show notification
            showNotification(habit.name, habitId.toInt())

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun showNotification(habitName: String, notificationId: Int) {
        val context = applicationContext
        val channelId = "habit_reminders"

        createNotificationChannel(context, channelId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Habit Reminder")
            .setContentText("Time to $habitName!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)

        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Reminders for your daily habits"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
