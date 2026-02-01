package com.habittracker.ui.statistics

import com.habittracker.data.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StatisticsPresenter(
    private var view: StatisticsContract.View?,
    private val repository: HabitRepository
) : StatisticsContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadStatistics() {
        view?.showLoading()
        scope.launch {
            try {
                // 1. Get all habits
                val habits = repository.allHabits.firstOrNull() ?: emptyList()
                val totalHabits = habits.size

                if (totalHabits == 0) {
                    view?.hideLoading()
                    view?.showOverallStats(0, 0, 0)
                    view?.showWeeklyProgress(emptyMap())
                    return@launch
                }

                // 2. Calculate Weekly Progress (Last 7 days)
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(6)
                val weeklyLogs = withContext(Dispatchers.IO) {
                    repository.getLogsForRange(startDate, endDate)
                }

                val weeklyData = mutableMapOf<LocalDate, Float>()
                var currentDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    val dateStr = currentDate.toString()
                    // Count completed habits for this date
                    val completedCount = weeklyLogs.count { it.date == dateStr && it.completed }
                    // Simple calculation: completed / total active habits
                    // Note: This assumes total habits hasn't changed much, which is a fair MVP approximation
                    val percentage = if (totalHabits > 0) (completedCount.toFloat() / totalHabits).coerceAtMost(1.0f) else 0f
                    weeklyData[currentDate] = percentage
                    currentDate = currentDate.plusDays(1)
                }

                // 3. Calculate Overall Stats
                // Best Streak across all habits
                var maxBestStreak = 0
                habits.forEach { habit ->
                    val streak = withContext(Dispatchers.IO) {
                        repository.calculateBestStreak(habit)
                    }
                    if (streak > maxBestStreak) {
                        maxBestStreak = streak
                    }
                }

                // Average Completion Rate (All time or last 30 days? Let's do last 7 days for now for consistency)
                val avgCompletion = weeklyData.values.average().toFloat() * 100
                
                view?.hideLoading()
                view?.showOverallStats(totalHabits, avgCompletion.toInt(), maxBestStreak)
                view?.showWeeklyProgress(weeklyData)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Failed to load statistics")
            }
        }
    }

    override fun detach() {
        view = null
        scope.cancel()
    }
}
