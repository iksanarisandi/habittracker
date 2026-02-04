package com.habittracker.ui.home

import com.habittracker.data.local.entity.Habit

data class HabitUiModel(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int
)

interface HomeContract {
    interface View {
        fun showHabits(habits: List<HabitUiModel>)
        fun showEmptyState()
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
    }

    interface Presenter {
        fun loadHabits()
        fun addHabit(name: String, frequency: String, reminderTime: String?, isReminderEnabled: Boolean, reminderDays: String?)
        fun updateHabit(habit: Habit)
        fun deleteHabit(habit: Habit)
        fun toggleHabit(habit: Habit, isCompleted: Boolean)
        fun detach()
    }
}
