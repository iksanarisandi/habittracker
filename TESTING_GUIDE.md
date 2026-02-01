# 🧪 MANUAL TESTING GUIDE

This guide will help you verify all features of the Habit Tracker app, including recent critical fixes and new additions (Statistics, Navigation).

## 🚀 Prerequisites
1.  **Clean Install**: Uninstall any previous version of the app to ensure a clean database state (optional but recommended).
2.  **Date/Time**: Ensure your device date and time are set to automatic.

---

## 🧪 Test Scenarios

### 1. Core Habits (CRUD)
| ID | Action | Expected Result | Status |
|----|--------|-----------------|--------|
| 1.1 | **Add Daily Habit** | Tap `+`, enter "Drink Water", select "Daily", set Reminder 09:00. Click Save. Habit appears in list. | ⬜ |
| 1.2 | **Add Weekly Habit** | Tap `+`, enter "Gym", select "Weekly". Click Save. Habit appears with "Weekly" badge. | ⬜ |
| 1.3 | **Edit Habit** | Tap on "Drink Water" text. Change name to "Drink 2L Water". Save. List updates immediately. | ⬜ |
| 1.4 | **Delete Habit** | Long press (or tap delete icon if available) on a habit. Confirm dialog appears. Click Delete. Habit removed. | ⬜ |
| 1.5 | **Validation** | Try adding habit with empty name. -> Error "Name required". | ⬜ |
| 1.6 | **Max Length** | Try adding habit with >50 chars name. -> Error "Max 50 characters". | ⬜ |
| 1.7 | **Max Limit** | Add 20 habits. Try adding 21st. -> Error/Toast "Max 20 habits reached". | ⬜ |

### 2. Tracking & Streaks
| ID | Action | Expected Result | Status |
|----|--------|-----------------|--------|
| 2.1 | **Complete Habit** | Tap checkbox on a habit. Strikethrough appears. "Current Streak" increases by 1. | ⬜ |
| 2.2 | **Uncheck Habit** | Tap checkbox again. Strikethrough removed. Streak decreases. | ⬜ |
| 2.3 | **Best Streak** | Complete a habit for 2+ days (simulate by changing device date or DB). Verify "Best: X" updates. | ⬜ |
| 2.4 | **Progress Bar** | Complete 1 of 2 habits. Top progress bar shows "50%" and "1/2 completed". | ⬜ |

### 3. Statistics (New Feature)
| ID | Action | Expected Result | Status |
|----|--------|-----------------|--------|
| 3.1 | **View Stats** | Tap "Statistics" icon in bottom nav. Screen loads. | ⬜ |
| 3.2 | **Weekly Chart** | Verify chart bars correspond to your activity over the last 7 days. | ⬜ |
| 3.3 | **Overview** | Check "Total Habits" matches your list count. "Completion Rate" seems reasonable. | ⬜ |

### 4. Navigation & Settings
| ID | Action | Expected Result | Status |
|----|--------|-----------------|--------|
| 4.1 | **Tab Switching** | Switch between Home, Statistics, Settings. App retains state (doesn't crash). | ⬜ |
| 4.2 | **Settings** | Tap "Settings". Should show "Coming Soon" placeholder. | ⬜ |

### 5. Notifications
| ID | Action | Expected Result | Status |
|----|--------|-----------------|--------|
| 5.1 | **Receive Reminder** | Set habit reminder to 2 mins from now. Close app. Wait. Notification should appear. | ⬜ |
| 5.2 | **Tap Notification** | Tap the notification. App should open. | ⬜ |

---

## 🐞 Edge Case Verification
-   **Rotation**: Rotate device to Landscape. Ensure no crash and data persists.
-   **Background**: Press Home, then open app again. State should be preserved.
-   **Date Change**: Change device date to tomorrow. Open app. Daily habits should reset (unchecked).

## 📝 Notes
-   If you encounter a crash, please note the action performed.
-   Statistics are calculated based on "Completed" status logs in the database.
