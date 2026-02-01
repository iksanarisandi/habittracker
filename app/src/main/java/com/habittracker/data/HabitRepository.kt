package com.habittracker.data

import com.habittracker.data.local.dao.HabitDao
import com.habittracker.data.local.entity.Habit
import com.habittracker.data.local.entity.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun calculateBestStreak(habit: Habit): Int {
        val logs = habitDao.getHabitLogs(habit.id)
        if (logs.isEmpty()) return 0
        
        var bestStreak = 0
        var currentStreak = 0
        
        val sortedLogs = logs.filter { it.completed }.sortedBy { it.date }
        if (sortedLogs.isEmpty()) return 0

        if (habit.frequency == "WEEKLY") {
            val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
            var prevYear = -1
            var prevWeek = -1
            
            for (log in sortedLogs) {
                val date = LocalDate.parse(log.date)
                val year = date.year
                val week = date.get(weekFields.weekOfWeekBasedYear())
                
                if (prevYear == -1) {
                    currentStreak = 1
                } else {
                    val isConsecutive = if (year == prevYear) {
                        week == prevWeek + 1
                    } else if (year == prevYear + 1) {
                        prevWeek >= 52 && week == 1
                    } else {
                        false
                    }
                    
                    if (isConsecutive) {
                        currentStreak++
                    } else if (year == prevYear && week == prevWeek) {
                        // Same week, ignore
                    } else {
                        currentStreak = 1
                    }
                }
                bestStreak = kotlin.math.max(bestStreak, currentStreak)
                prevYear = year
                prevWeek = week
            }
        } else {
            var prevDate: LocalDate? = null
            for (log in sortedLogs) {
                val date = LocalDate.parse(log.date)
                if (prevDate == null) {
                    currentStreak = 1
                } else {
                    val days = java.time.temporal.ChronoUnit.DAYS.between(prevDate, date)
                    if (days == 1L) {
                        currentStreak++
                    } else if (days == 0L) {
                        // Same day
                    } else {
                        currentStreak = 1
                    }
                }
                bestStreak = kotlin.math.max(bestStreak, currentStreak)
                prevDate = date
            }
        }
        return bestStreak
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabit(habitId: Long, date: LocalDate, completed: Boolean) {
        habitDao.toggleHabitCompletion(habitId, date.toString(), completed)
    }
    
    suspend fun getHabitLog(habitId: Long, date: LocalDate): HabitLog? {
        return habitDao.getHabitLog(habitId, date.toString())
    }
    
    suspend fun getHabitCount(): Int {
        return habitDao.getAllHabits().firstOrNull()?.size ?: 0
    }

    suspend fun calculateStreak(habit: Habit): Int {
        val logs = habitDao.getHabitLogs(habit.id)
        var streak = 0
        
        if (habit.frequency == "WEEKLY") {
            // Weekly Logic
            val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
            val currentWeek = LocalDate.now().get(weekFields.weekOfWeekBasedYear())
            val currentYear = LocalDate.now().year
            
            // Check this week
            val hasThisWeek = logs.any { 
                val d = LocalDate.parse(it.date)
                d.year == currentYear && d.get(weekFields.weekOfWeekBasedYear()) == currentWeek
            }
            
            var checkYear = currentYear
            var checkWeek = currentWeek
            
            if (hasThisWeek) {
                streak++
                if (checkWeek == 1) {
                    checkYear--
                    checkWeek = 52
                } else {
                    checkWeek--
                }
            } else {
                if (checkWeek == 1) {
                    checkYear--
                    checkWeek = 52
                } else {
                    checkWeek--
                }
            }
            
            while (true) {
                val y = checkYear
                val w = checkWeek
                val hasLog = logs.any { 
                    val d = LocalDate.parse(it.date)
                    d.year == y && d.get(weekFields.weekOfWeekBasedYear()) == w
                }
                
                if (hasLog) {
                    streak++
                    if (checkWeek == 1) {
                        checkYear--
                        checkWeek = 52
                    } else {
                        checkWeek--
                    }
                } else {
                    break
                }
            }
        } else {
            // DAILY Logic
            var checkDate = LocalDate.now()
            
            val todayLog = logs.find { it.date == checkDate.toString() }
            if (todayLog?.completed == true) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                checkDate = checkDate.minusDays(1)
            }
            
            while (true) {
                val dateStr = checkDate.toString()
                val log = logs.find { it.date == dateStr }
                if (log?.completed == true) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        }
        return streak
    }
}
