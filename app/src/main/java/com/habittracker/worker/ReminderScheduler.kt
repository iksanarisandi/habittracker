package com.habittracker.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.local.entity.Habit
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminderForHabit(context: Context, habit: Habit) {
        // Cancel existing work for this habit
        cancelReminderForHabit(context, habit.id)

        // Only schedule if reminder is enabled and time is set
        if (!habit.isReminderEnabled || habit.reminderTime == null) {
            return
        }

        try {
            val reminderTime = LocalTime.parse(habit.reminderTime)
            val now = LocalTime.now()
            val today = java.time.LocalDate.now()

            // Calculate initial delay to first reminder
            val todayDateTime = today.atTime(reminderTime)
            val nowDateTime = today.atTime(now)

            val initialDelayMillis = if (nowDateTime.isBefore(todayDateTime)) {
                // Reminder is later today
                java.time.Duration.between(nowDateTime, todayDateTime).toMillis()
            } else {
                // Reminder is tomorrow
                java.time.Duration.between(nowDateTime, todayDateTime.plusDays(1)).toMillis()
            }

            // Create work request with habit ID
            val inputData = Data.Builder()
                .putLong("habitId", habit.id)
                .build()

            val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
                24, TimeUnit.HOURS // Repeat daily
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            // Enqueue unique work for this habit
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "habit_reminder_${habit.id}",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWork
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminderForHabit(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
    }

    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_reminder")
    }
}
