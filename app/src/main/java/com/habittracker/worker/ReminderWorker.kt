package com.habittracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.HabitApplication
import com.habittracker.data.HabitRepository
import com.habittracker.data.local.entity.HabitLog
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository: HabitRepository by lazy {
        val database = (applicationContext as HabitApplication).database
        HabitRepository(database.habitDao())
    }

    private val habitDao by lazy {
        (applicationContext as HabitApplication).database.habitDao()
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

            // For weekly habits, check if today is a selected reminder day
            if (habit.frequency == "WEEKLY") {
                val selectedDays = habit.reminderDays?.split(",")?.mapNotNull { dayStr ->
                    try { DayOfWeek.valueOf(dayStr) } catch (e: Exception) { null }
                } ?: emptyList()

                // If no days selected or today is not in selected days, skip
                if (selectedDays.isEmpty() || !selectedDays.contains(today.dayOfWeek)) {
                    return Result.success()
                }
            }

            // Check if already completed today (or this week for weekly habits)
            if (habit.frequency == "WEEKLY") {
                // For weekly habits, check if already completed this week
                val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
                val currentWeek = today.get(weekFields.weekOfWeekBasedYear())
                val currentYear = today.year

                val logs: List<HabitLog> = habitDao.getHabitLogs(habitId)
                val alreadyCompletedThisWeek = logs.any { log ->
                    val logDate = LocalDate.parse(log.date)
                    val logWeek = logDate.get(weekFields.weekOfWeekBasedYear())
                    val logYear = logDate.year
                    logYear == currentYear && logWeek == currentWeek && log.completed
                }

                if (alreadyCompletedThisWeek) {
                    return Result.success()
                }
            } else {
                // For daily habits, check if already completed today
                val log = repository.getHabitLog(habitId, today)
                if (log?.completed == true) {
                    return Result.success()
                }
            }

            // Show notification
            com.habittracker.util.NotificationHelper.showNotification(
                applicationContext,
                habit.name,
                habit.frequency,
                habitId.toInt()
            )

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
