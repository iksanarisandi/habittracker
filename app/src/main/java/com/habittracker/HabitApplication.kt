package com.habittracker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.local.HabitDatabase
import com.habittracker.worker.DailyResetWorker
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.Duration

class HabitApplication : Application() {
    val database by lazy { HabitDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        setupDailyResetWorker()
        // Note: Reminder scheduling is now handled when habits are added/updated
        // See AddHabitBottomSheet and related presenters for reminder scheduling
    }

    private fun setupDailyResetWorker() {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val initialDelay = Duration.between(now, midnight).toMillis()

        val resetWork = PeriodicWorkRequestBuilder<DailyResetWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyResetWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            resetWork
        )
    }
}
