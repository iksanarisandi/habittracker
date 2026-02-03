package com.habittracker.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.habittracker.data.local.entity.Habit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime
import java.util.concurrent.ExecutionException

@RunWith(AndroidJUnit4::class)
class ReminderSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun testScheduleReminder_schedulesWork() {
        // Create a habit with a reminder time
        val habit = Habit(
            id = 1,
            name = "Test Habit",
            reminderTime = "10:00",
            isReminderEnabled = true
        )

        ReminderScheduler.scheduleReminderForHabit(context, habit)

        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork("habit_reminder_1").get()

        assertEquals(1, workInfos.size)
        val workInfo = workInfos[0]
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun testCancelReminder_cancelsWork() {
        val habit = Habit(
            id = 2,
            name = "Test Habit 2",
            reminderTime = "10:00",
            isReminderEnabled = true
        )

        ReminderScheduler.scheduleReminderForHabit(context, habit)
        ReminderScheduler.cancelReminderForHabit(context, habit.id)

        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork("habit_reminder_2").get()
        
        // When cancelled, it might still be in the list but marked CANCELLED
        assertEquals(WorkInfo.State.CANCELLED, workInfos[0].state)
    }

    @Test
    fun testScheduleReminder_disabled_doesNotSchedule() {
        val habit = Habit(
            id = 3,
            name = "Disabled Reminder Habit",
            reminderTime = "10:00",
            isReminderEnabled = false
        )

        ReminderScheduler.scheduleReminderForHabit(context, habit)

        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork("habit_reminder_3").get()

        // Should be empty or cancelled if it existed (but here it didn't exist)
        // The logic cancels first, then checks enabled.
        // If it didn't exist, cancel does nothing.
        // Then it returns.
        assertEquals(0, workInfos.size)
    }
}
