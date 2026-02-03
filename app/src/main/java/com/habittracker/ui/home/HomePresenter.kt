package com.habittracker.ui.home

import android.content.Context
import com.habittracker.data.HabitRepository
import com.habittracker.data.local.entity.Habit
import com.habittracker.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class HomePresenter(
    private var view: HomeContract.View?,
    private val repository: HabitRepository,
    private val context: Context? = null
) : HomeContract.Presenter {

    private val job = kotlinx.coroutines.SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var loadJob: Job? = null

    override fun loadHabits() {
        loadJob?.cancel()
        view?.showLoading()
        loadJob = scope.launch {
            repository.allHabits
                .catch { e ->
                    view?.hideLoading()
                    view?.showError(e.message ?: "Unknown error")
                }
                .collectLatest { habits ->
                    if (habits.isEmpty()) {
                        view?.hideLoading()
                        view?.showEmptyState()
                    } else {
                        val uiModels = habits.map { habit ->
                            val today = LocalDate.now()
                            val log = withContext(Dispatchers.IO) {
                                repository.getHabitLog(habit.id, today)
                            }
                            val streak = withContext(Dispatchers.IO) {
                                repository.calculateStreak(habit)
                            }
                            val bestStreak = withContext(Dispatchers.IO) {
                                repository.calculateBestStreak(habit)
                            }
                            HabitUiModel(habit, log?.completed == true, streak, bestStreak)
                        }
                        view?.hideLoading()
                        view?.showHabits(uiModels)
                    }
                }
        }
    }

    override fun addHabit(name: String, frequency: String, reminderTime: String?, isReminderEnabled: Boolean) {
        if (name.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                // Check limit
                val count = repository.getHabitCount()
                if (count >= 20) {
                    withContext(Dispatchers.Main) {
                        view?.showError("Max 20 habits reached")
                    }
                    return@launch
                }

                val habit = Habit(
                    name = name,
                    frequency = frequency,
                    reminderTime = reminderTime,
                    isReminderEnabled = isReminderEnabled
                )
                val habitId = repository.insertHabit(habit)

                // Schedule reminder if enabled
                if (isReminderEnabled && reminderTime != null && context != null) {
                    val scheduledHabit = habit.copy(id = habitId)
                    ReminderScheduler.scheduleReminderForHabit(context, scheduledHabit)
                }

                // Flow will auto-update the list
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Failed to add habit")
                }
            }
        }
    }

    override fun updateHabit(habit: Habit) {
        scope.launch(Dispatchers.IO) {
            try {
                repository.updateHabit(habit)

                // Update reminder schedule
                if (context != null) {
                    ReminderScheduler.scheduleReminderForHabit(context, habit)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Failed to update habit")
                }
            }
        }
    }

    override fun deleteHabit(habit: Habit) {
        scope.launch(Dispatchers.IO) {
            try {
                repository.deleteHabit(habit)

                // Cancel reminder
                if (context != null) {
                    ReminderScheduler.cancelReminderForHabit(context, habit.id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Failed to delete habit")
                }
            }
        }
    }

    override fun toggleHabit(habit: Habit, isCompleted: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                repository.toggleHabit(habit.id, LocalDate.now(), isCompleted)
                // Flow will trigger reload, recalculating streaks and status
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Failed to update habit")
                }
            }
        }
    }

    override fun detach() {
        view = null
        job.cancel()
    }
}
