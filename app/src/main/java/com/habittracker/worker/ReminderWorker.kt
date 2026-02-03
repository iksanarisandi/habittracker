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
import com.habittracker.R
import com.habittracker.data.HabitRepository
import com.habittracker.ui.home.MainActivity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository: HabitRepository by lazy {
        val database = (applicationContext as HabitApplication).database
        HabitRepository(database.habitDao())
    }

    override suspend fun doWork(): Result {
        val now = LocalTime.now()
        val today = LocalDate.now()

        try {
            val habits = repository.allHabits.first()
            
            for (habit in habits) {
                if (habit.isReminderEnabled && habit.reminderTime != null) {
                    // Check if completed today
                    val log = repository.getHabitLog(habit.id, today)
                    if (log?.completed == true) continue

                    // Check time (Simple logic: if current hour matches reminder hour)
                    // Format reminderTime "HH:mm"
                    try {
                        val reminderTime = LocalTime.parse(habit.reminderTime)
                        
                        val nowMinutes = now.hour * 60 + now.minute
                        val reminderMinutes = reminderTime.hour * 60 + reminderTime.minute
                        
                        // Check if within 15 minutes (since worker interval might be 15 min minimum)
                        if (kotlin.math.abs(nowMinutes - reminderMinutes) <= 15) {
                            showNotification(habit.name, habit.id.toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
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
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // Use Android system icon
            .setContentTitle("Habit Reminder")
            .setContentText("Time to $habitName!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Reminders for your daily habits"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
