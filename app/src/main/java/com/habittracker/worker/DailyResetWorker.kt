package com.habittracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // The app calculates "completed today" dynamically based on LocalDate.now().
        // This worker serves as a system trigger to ensure any background cleanup happens if needed.
        // For now, simply successfully completing marks the "reset" event.
        return Result.success()
    }
}
