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

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabit(habitId: Long, date: LocalDate, completed: Boolean) {
        habitDao.toggleHabitCompletion(habitId, date.toString(), completed)
    }
    
    suspend fun getHabitLog(habitId: Long, date: LocalDate): HabitLog? {
        return habitDao.getHabitLog(habitId, date.toString())
    }
    
    // Simple streak calculation (can be moved to usecase)
    suspend fun calculateStreak(habitId: Long): Int {
        val logs = habitDao.getHabitLogs(habitId)
        var streak = 0
        var checkDate = LocalDate.now()
        
        // Check today first
        val todayLog = logs.find { it.date == checkDate.toString() }
        if (todayLog?.completed == true) {
            streak++
            checkDate = checkDate.minusDays(1)
        } else {
            // If not completed today, check yesterday. If yesterday also not completed, streak is 0.
             // However, strictly speaking, if I missed today, streak might not be broken yet until the day ends.
             // But for simplicity: if yesterday is missed, streak is broken.
             // Let's check yesterday directly if today is missing.
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
        return streak
    }
}
