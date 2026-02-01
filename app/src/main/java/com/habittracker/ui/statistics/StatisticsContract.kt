package com.habittracker.ui.statistics

import java.time.LocalDate

interface StatisticsContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showWeeklyProgress(data: Map<LocalDate, Float>) // Date -> Completion % (0.0 - 1.0)
        fun showOverallStats(totalHabits: Int, completionRate: Int, bestStreak: Int)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadStatistics()
        fun detach()
    }
}
