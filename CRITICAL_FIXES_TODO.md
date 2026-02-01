# 🔧 CRITICAL FIXES - IMMEDIATE ACTION REQUIRED

**Priority:** 🔴 CRITICAL - Must fix before any release  
**Estimated Time:** 6 hours (1 day)  
**Target:** Fix all crash bugs and blocking issues

---

## 🚨 URGENT: FIX THESE FIRST (In Order)

### 1️⃣ Fix Edit Habit Crash - 1 HOUR
**Status:** 🔴 CRASH - App crashes when user taps habit to edit

**File:** `app/src/main/java/com/habittracker/ui/home/HabitAdapter.kt`

**Current Code (Line 49):**
```kotlin
binding.root.setOnClickListener {
    onEdit(model.habit)  // ❌ Method does not exist!
}
```

**Solution:**

**Step 1:** Update `HabitAdapter` constructor:
```kotlin
class HabitAdapter(
    private val onToggle: (Habit, Boolean) -> Unit,
    private val onDelete: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit  // ✅ ADD THIS LINE
) : ListAdapter<HabitUiModel, HabitAdapter.HabitViewHolder>(DiffCallback())
```

**Step 2:** Update `MainActivity.kt` adapter creation (Line ~35):
```kotlin
private fun setupRecyclerView() {
    adapter = HabitAdapter(
        onToggle = { habit, isCompleted ->
            presenter.toggleHabit(habit, isCompleted)
        },
        onDelete = { habit ->
            showDeleteConfirmation(habit)
        },
        onEdit = { habit ->  // ✅ ADD THIS
            showEditHabitDialog(habit)
        }
    )
    binding.recyclerView.layoutManager = LinearLayoutManager(this)
    binding.recyclerView.adapter = adapter
}
```

**Test:** Tap any habit item → Should open edit dialog ✅

---

### 2️⃣ Fix Max 20 Habits Crash - 30 MIN
**Status:** 🔴 CRASH - App crashes when adding 21st habit

**File:** `app/src/main/java/com/habittracker/data/HabitRepository.kt`

**Current Code (Line 44 in HomePresenter):**
```kotlin
val count = repository.getHabitCount()  // ❌ Method does not exist!
```

**Solution:**

**Option A: Quick Fix (In Repository)**
```kotlin
// Add to HabitRepository.kt:
suspend fun getHabitCount(): Int {
    return allHabits.first().size
}
```

**Option B: Better Performance (In DAO)**
```kotlin
// Add to HabitDao.kt:
@Query("SELECT COUNT(*) FROM habits")
suspend fun getHabitCount(): Int

// Then in HabitRepository.kt:
suspend fun getHabitCount(): Int {
    return habitDao.getHabitCount()
}
```

**Test:** Try to add 21st habit → Should show error "Max 20 habits reached" ✅

---

### 3️⃣ Fix Weekly Streak Calculation - 2 HOURS
**Status:** 🟠 HIGH - Wrong data displayed for weekly habits

**File:** `app/src/main/java/com/habittracker/data/HabitRepository.kt`

**Current Code (Lines 48-58):**
```kotlin
suspend fun calculateStreak(habitId: Long): Int {
    val logs = habitDao.getHabitLogs(habitId)
    var streak = 0
    var checkDate = LocalDate.now()
    
    // ... logic ...
    
    checkDate = checkDate.minusDays(1)  // ❌ Always subtracts 1 day!
}
```

**Solution:**
```kotlin
suspend fun calculateStreak(habitId: Long): Int {
    val habit = habitDao.getHabitById(habitId) ?: return 0
    val logs = habitDao.getHabitLogs(habitId)
    val isWeekly = habit.frequency == "WEEKLY"
    
    var streak = 0
    var checkDate = LocalDate.now()
    
    // Check today/this week first
    val todayLog = logs.find { it.date == checkDate.toString() }
    if (todayLog?.completed == true) {
        streak++
        checkDate = if (isWeekly) checkDate.minusWeeks(1) else checkDate.minusDays(1)
    } else {
        // Start checking from previous day/week
        checkDate = if (isWeekly) checkDate.minusWeeks(1) else checkDate.minusDays(1)
    }

    // Count backwards
    while (true) {
        val dateStr = checkDate.toString()
        val log = logs.find { it.date == dateStr }
        if (log?.completed == true) {
            streak++
            checkDate = if (isWeekly) checkDate.minusWeeks(1) else checkDate.minusDays(1)
        } else {
            break
        }
    }
    return streak
}
```

**Test:** 
- Create weekly habit
- Complete for 3 weeks
- Check streak shows "3 week streak" ✅

---

### 4️⃣ Add Name Validation (50 chars) - 30 MIN
**Status:** 🟠 MEDIUM - PRD requirement not met

**File:** `app/src/main/java/com/habittracker/ui/add/AddHabitBottomSheet.kt`

**Current Code (Lines 73-76):**
```kotlin
if (name.isEmpty()) {
    binding.tilName.error = getString(R.string.habit_name_hint)
    return@setOnClickListener
}
// ❌ No check for length > 50!
```

**Solution:**
```kotlin
if (name.isEmpty()) {
    binding.tilName.error = "Nama habit tidak boleh kosong"
    return@setOnClickListener
}
if (name.length > 50) {
    binding.tilName.error = "Maksimal 50 karakter"
    return@setOnClickListener
}

// Continue with save logic...
val frequency = if (binding.rbDaily.isChecked) "DAILY" else "WEEKLY"
val isReminderEnabled = binding.switchReminder.isChecked

onSave(name, frequency, selectedTimeStr, isReminderEnabled)
dismiss()
```

**Test:**
- Try empty name → Show error ✅
- Try 51+ characters → Show error ✅
- Try valid name → Save successfully ✅

---

### 5️⃣ Fix Memory Leak in Presenter - 1 HOUR
**Status:** 🟠 MEDIUM - Memory leak on rotation

**File:** `app/src/main/java/com/habittracker/ui/home/HomePresenter.kt`

**Current Code (Lines 12-14, 105-108):**
```kotlin
private val scope = CoroutineScope(Dispatchers.Main + Job())

// ...

override fun detach() {
    view = null
    // ❌ Scope not cancelled!
}
```

**Solution:**
```kotlin
// Change Job to SupervisorJob for better error handling
private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

override fun detach() {
    view = null
    scope.cancel()  // ✅ ADD THIS LINE
}
```

**Better Solution:** Use `ViewModel` instead:
```kotlin
// Create HomeViewModel.kt:
class HomeViewModel(
    private val repository: HabitRepository
) : ViewModel() {
    // ViewModel automatically manages scope
    // Use viewModelScope which is cancelled automatically
}

// Update MainActivity to use ViewModel:
private val viewModel: HomeViewModel by viewModels()
```

**Test:** Rotate device → Check memory profiler → No leak ✅

---

### 6️⃣ Fix Reminder Time Precision - 1 HOUR
**Status:** 🟠 MEDIUM - Notifications trigger for entire hour

**File:** `app/src/main/java/com/habittracker/worker/ReminderWorker.kt`

**Current Code (Lines 43-46):**
```kotlin
val reminderTime = LocalTime.parse(habit.reminderTime)
if (now.hour == reminderTime.hour) {  // ❌ Only checks hour!
    showNotification(habit.name, habit.id.toInt())
}
```

**Solution:**
```kotlin
val reminderTime = LocalTime.parse(habit.reminderTime)

// Convert to minutes since midnight for precise comparison
val nowMinutes = now.hour * 60 + now.minute
val reminderMinutes = reminderTime.hour * 60 + reminderTime.minute

// Check if within 5 minutes (to handle worker running hourly)
if (kotlin.math.abs(nowMinutes - reminderMinutes) <= 5) {
    showNotification(habit.name, habit.id.toInt())
}
```

**Better Solution - Track last notification:**
```kotlin
// Add to habit_logs or create notification_tracker table
// to ensure we only notify once per day per habit

// In ReminderWorker:
val lastNotified = getLastNotificationDate(habitId, today)
if (lastNotified != today && isTimeMatch(now, reminderTime)) {
    showNotification(habit.name, habit.id.toInt())
    markAsNotified(habitId, today)
}
```

**Test:** Set reminder for 9:00 → Only get notification around 9:00 ✅

---

## ✅ VERIFICATION CHECKLIST

After implementing fixes, verify:

- [ ] **Test 1:** Tap habit item → Edit dialog opens (no crash)
- [ ] **Test 2:** Add 21st habit → Error message shown (no crash)
- [ ] **Test 3:** Create weekly habit → Streak counts weeks not days
- [ ] **Test 4:** Try 51-char name → Error "Maksimal 50 karakter"
- [ ] **Test 5:** Rotate device → No memory leak in profiler
- [ ] **Test 6:** Set reminder 9:00 → Notification only at ~9:00

---

## 📝 ADDITIONAL IMPROVEMENTS (Not Critical)

These are nice-to-have but not blocking:

### 7. Add Best Streak Calculation - 3 HOURS
**File:** `HabitRepository.kt`

```kotlin
suspend fun calculateBestStreak(habitId: Long): Int {
    val logs = habitDao.getHabitLogs(habitId)
    var bestStreak = 0
    var currentStreak = 0
    var prevDate: LocalDate? = null
    
    val sortedLogs = logs.sortedBy { it.date }
    
    for (log in sortedLogs) {
        if (log.completed) {
            val logDate = LocalDate.parse(log.date)
            if (prevDate == null || 
                ChronoUnit.DAYS.between(prevDate, logDate) == 1L) {
                currentStreak++
            } else {
                currentStreak = 1
            }
            bestStreak = maxOf(bestStreak, currentStreak)
            prevDate = logDate
        }
    }
    return bestStreak
}

// Update HabitUiModel:
data class HabitUiModel(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int
)

// Update UI to show both streaks
```

---

## 🚀 NEXT STEPS

### After Critical Fixes (Day 2-5):
1. Implement daily reset logic
2. Add Statistics screen
3. Add Settings screen
4. Implement bottom navigation
5. Add progress bar to home screen

### After MVP Features (Day 6-10):
1. Add UI animations
2. Add haptic feedback
3. Implement onboarding
4. Add icon picker
5. Write unit tests

---

## 📊 EFFORT SUMMARY

| Fix | Priority | Time | Risk |
|-----|----------|------|------|
| Edit habit crash | 🔴 P0 | 1 hr | High impact |
| Max 20 habits crash | 🔴 P0 | 0.5 hr | Medium impact |
| Weekly streak logic | 🟠 P1 | 2 hrs | Medium impact |
| Name validation | 🟠 P1 | 0.5 hr | Low impact |
| Memory leak | 🟠 P1 | 1 hr | Medium impact |
| Reminder precision | 🟠 P1 | 1 hr | Low impact |
| **TOTAL** | - | **6 hrs** | - |

**Recommendation:** Complete all 6 fixes before adding new features.

---

## 🧪 TESTING STRATEGY

### Unit Tests (Add these):
```kotlin
@Test
fun `calculateStreak returns correct streak for weekly habit`() = runTest {
    val habit = Habit(id = 1, frequency = "WEEKLY")
    // Insert logs for 3 consecutive weeks
    // Assert streak == 3
}

@Test
fun `getHabitCount returns correct count`() = runTest {
    // Insert 5 habits
    // Assert count == 5
}

@Test
fun `validation rejects names longer than 50 chars`() {
    val name = "a".repeat(51)
    // Assert validation error
}
```

### Manual Testing:
1. Install debug APK
2. Go through all user flows
3. Force crash scenarios
4. Check logcat for errors

---

## 📞 NEED HELP?

**Check PRD:** `PRD.txt`  
**Check Architecture:** `ANDROID_BUILD_GUIDE.md`  
**Full Analysis:** `MVP_ANALYSIS_REPORT.md`

---

**Priority:** 🔴 FIX THESE TODAY  
**Timeline:** 6 hours (1 day)  
**Status:** Not started

---

**Last Updated:** February 1, 2026  
**Version:** 1.0
