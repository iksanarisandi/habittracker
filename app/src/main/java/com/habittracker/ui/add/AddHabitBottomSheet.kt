package com.habittracker.ui.add

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.habittracker.R
import com.habittracker.databinding.BottomSheetAddHabitBinding
import java.util.Calendar

class AddHabitBottomSheet(
    private val existingHabit: com.habittracker.data.local.entity.Habit? = null,
    private val onSave: (name: String, frequency: String, reminderTime: String?, isReminderEnabled: Boolean, reminderDays: String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddHabitBinding? = null
    private val binding get() = _binding!!

    private var selectedTimeStr: String? = null
    private val selectedDays = mutableSetOf<java.time.DayOfWeek>()

    // Day of week mapping
    private val dayChips = mapOf(
        java.time.DayOfWeek.MONDAY to R.id.chipMonday,
        java.time.DayOfWeek.TUESDAY to R.id.chipTuesday,
        java.time.DayOfWeek.WEDNESDAY to R.id.chipWednesday,
        java.time.DayOfWeek.THURSDAY to R.id.chipThursday,
        java.time.DayOfWeek.FRIDAY to R.id.chipFriday,
        java.time.DayOfWeek.SATURDAY to R.id.chipSaturday,
        java.time.DayOfWeek.SUNDAY to R.id.chipSunday
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFrequencySelection()
        setupReminderSwitch()
        setupTimePicker()
        setupDayChips()
        setupSaveButton()

        // Pre-fill if editing
        existingHabit?.let { habit ->
            binding.tvTitle.text = getString(R.string.edit_habit)
            binding.etName.setText(habit.name)
            if (habit.frequency == "WEEKLY") binding.rbWeekly.isChecked = true else binding.rbDaily.isChecked = true
            binding.switchReminder.isChecked = habit.isReminderEnabled
            if (habit.isReminderEnabled && habit.reminderTime != null) {
                selectedTimeStr = habit.reminderTime
                binding.tvReminderTime.text = habit.reminderTime
                binding.tvReminderTime.visibility = View.VISIBLE
            }

            // Load existing reminder days
            if (habit.reminderDays != null) {
                val days = habit.reminderDays.split(",").mapNotNull { dayStr ->
                    try { java.time.DayOfWeek.valueOf(dayStr) } catch (e: Exception) { null }
                }
                selectedDays.addAll(days)
                updateChipStates()
            }
        }
    }

    private fun setupFrequencySelection() {
        binding.rbDaily.setOnCheckedChangeListener { _, _ -> updateDaySelectorVisibility() }
        binding.rbWeekly.setOnCheckedChangeListener { _, _ -> updateDaySelectorVisibility() }
    }

    private fun updateDaySelectorVisibility() {
        val isWeekly = binding.rbWeekly.isChecked
        val isReminderEnabled = binding.switchReminder.isChecked
        binding.tvDaysLabel.visibility = if (isWeekly && isReminderEnabled) View.VISIBLE else View.GONE
        binding.chipGroupDays.visibility = if (isWeekly && isReminderEnabled) View.VISIBLE else View.GONE
    }

    private fun setupReminderSwitch() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.tvReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateDaySelectorVisibility()
            if (isChecked && selectedTimeStr == null) {
                // Default to 09:00
                selectedTimeStr = "09:00"
                binding.tvReminderTime.text = selectedTimeStr
            }

            // Auto-select all days for weekly if switching on reminder with no days selected
            if (isChecked && binding.rbWeekly.isChecked && selectedDays.isEmpty()) {
                selectedDays.addAll(java.time.DayOfWeek.entries)
                updateChipStates()
            }
        }
    }

    private fun setupTimePicker() {
        binding.tvReminderTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                    selectedTimeStr = time
                    binding.tvReminderTime.text = time
                },
                hour,
                minute,
                DateFormat.is24HourFormat(requireContext())
            ).show()
        }
    }

    private fun setupDayChips() {
        dayChips.forEach { (dayOfWeek, chipId) ->
            binding.chipGroupDays.findViewById<Chip>(chipId)?.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    selectedDays.add(dayOfWeek)
                } else {
                    selectedDays.remove(dayOfWeek)
                }
            }
        }
    }

    private fun updateChipStates() {
        dayChips.forEach { (dayOfWeek, chipId) ->
            binding.chipGroupDays.findViewById<Chip>(chipId)?.isChecked = selectedDays.contains(dayOfWeek)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilName.error = "Habit name cannot be empty"
                return@setOnClickListener
            }

            if (name.length > 50) {
                binding.tilName.error = "Max 50 characters"
                return@setOnClickListener
            }

            val frequency = if (binding.rbDaily.isChecked) "DAILY" else "WEEKLY"
            val isReminderEnabled = binding.switchReminder.isChecked

            // For weekly habits with reminder, validate that at least one day is selected
            val reminderDays: String? = if (frequency == "WEEKLY" && isReminderEnabled) {
                if (selectedDays.isEmpty()) {
                    // Auto-select all days if none selected
                    selectedDays.addAll(java.time.DayOfWeek.entries)
                    updateChipStates()
                }
                selectedDays.joinToString(",") { it.name }
            } else null

            onSave(name, frequency, selectedTimeStr, isReminderEnabled, reminderDays)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddHabitBottomSheet"
    }
}
