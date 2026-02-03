package com.habittracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.HabitApplication
import com.habittracker.data.HabitRepository
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
            com.habittracker.util.NotificationHelper.showNotification(applicationContext, habit.name, habitId.toInt())

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
