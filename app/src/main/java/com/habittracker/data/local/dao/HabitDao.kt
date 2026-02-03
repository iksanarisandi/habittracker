package com.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.habittracker.data.local.entity.Habit
import com.habittracker.data.local.entity.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    // Habit Operations
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Long): Flow<Habit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long  // Returns the row ID of the inserted habit

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Log Operations
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun getHabitLog(habitId: Long, date: String): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getHabitLogs(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getLogsBetweenDates(startDate: String, endDate: String): List<HabitLog>

    // Combined/Transaction
    @Transaction
    suspend fun toggleHabitCompletion(habitId: Long, date: String, completed: Boolean) {
        val existingLog = getHabitLog(habitId, date)
        if (existingLog != null) {
            insertHabitLog(existingLog.copy(completed = completed, updatedAt = System.currentTimeMillis()))
        } else {
            insertHabitLog(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    completed = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null
                )
            )
        }
    }
}
