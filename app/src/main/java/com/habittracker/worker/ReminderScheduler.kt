package com.habittracker.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.local.entity.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
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
            val now = LocalDateTime.now()
            val today = LocalDate.now()

            // Calculate initial delay to first reminder
            val initialDelayMillis = if (habit.frequency == "WEEKLY") {
                // For weekly habits, find the next selected day
                calculateNextWeeklyReminderDelay(today, reminderTime, habit.reminderDays)
            } else {
                // For daily habits, calculate next occurrence of reminder time today or tomorrow
                val todayReminderTime = today.atTime(reminderTime)
                if (now.isBefore(todayReminderTime)) {
                    // Reminder is later today
                    java.time.Duration.between(now, todayReminderTime).toMillis()
                } else {
                    // Reminder is tomorrow
                    java.time.Duration.between(now, todayReminderTime.plusDays(1)).toMillis()
                }
            }

            // Create work request with habit ID
            val inputData = Data.Builder()
                .putLong("habitId", habit.id)
                .build()

            // Use 24-hour periodic work for all habits - the worker will check day of week for weekly
            val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("habit_reminder")
                .build()

            // Enqueue unique work for this habit
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "habit_reminder_${habit.id}",
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderWork
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateNextWeeklyReminderDelay(
        today: LocalDate,
        reminderTime: LocalTime,
        reminderDays: String?
    ): Long {
        val now = LocalDateTime.now()

        // Parse the selected days
        val selectedDays = reminderDays?.split(",")?.mapNotNull { dayStr ->
            try { DayOfWeek.valueOf(dayStr) } catch (e: Exception) { null }
        } ?: emptyList()

        if (selectedDays.isEmpty()) {
            // If no days selected, default to all days
            return 0
        }

        // Sort days by their order in the week (starting from today)
        val todayDayOfWeek = today.dayOfWeek

        // Find the next selected day (including today)
        for (i in 0..7) {
            val checkDate = today.plusDays(i.toLong())
            val checkDayOfWeek = checkDate.dayOfWeek

            if (selectedDays.contains(checkDayOfWeek)) {
                val targetDateTime = checkDate.atTime(reminderTime)

                // If it's today and the reminder time hasn't passed yet, use today
                if (i == 0 && now.isBefore(targetDateTime)) {
                    return java.time.Duration.between(now, targetDateTime).toMillis()
                }
                // If it's a future day, use that day
                if (i > 0) {
                    return java.time.Duration.between(now, targetDateTime).toMillis()
                }
            }
        }

        // Fallback: next occurrence of the first selected day
        val firstSelectedDay = selectedDays.first()
        val nextDate = today.with(TemporalAdjusters.nextOrSame(firstSelectedDay))
        val targetDateTime = nextDate.atTime(reminderTime)
        return java.time.Duration.between(now, targetDateTime).toMillis()
    }

    fun cancelReminderForHabit(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
    }

    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_reminder")
    }
}
