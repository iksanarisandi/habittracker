package com.habittracker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.local.HabitDatabase
import com.habittracker.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class HabitApplication : Application() {
    val database by lazy { HabitDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "HabitReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }
}
