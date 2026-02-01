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
import com.habittracker.R
import com.habittracker.databinding.BottomSheetAddHabitBinding
import java.util.Calendar

class AddHabitBottomSheet(
    private val existingHabit: com.habittracker.data.local.entity.Habit? = null,
    private val onSave: (name: String, frequency: String, reminderTime: String?, isReminderEnabled: Boolean) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddHabitBinding? = null
    private val binding get() = _binding!!
    
    private var selectedTimeStr: String? = null

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

        setupReminderSwitch()
        setupTimePicker()
        setupSaveButton()
        
        // Pre-fill if editing
        existingHabit?.let { habit ->
            binding.tvTitle.text = getString(R.string.add_habit).replace("Add", "Edit") // Simple hack, better use resource
            binding.etName.setText(habit.name)
            if (habit.frequency == "WEEKLY") binding.rbWeekly.isChecked = true else binding.rbDaily.isChecked = true
            binding.switchReminder.isChecked = habit.isReminderEnabled
            if (habit.isReminderEnabled && habit.reminderTime != null) {
                selectedTimeStr = habit.reminderTime
                binding.tvReminderTime.text = habit.reminderTime
                binding.tvReminderTime.visibility = View.VISIBLE
            }
        }
    }

    private fun setupReminderSwitch() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.tvReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked && selectedTimeStr == null) {
                // Default to 09:00
                selectedTimeStr = "09:00"
                binding.tvReminderTime.text = selectedTimeStr
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

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilName.error = getString(R.string.habit_name_hint) // Should define proper error string
                return@setOnClickListener
            }

            val frequency = if (binding.rbDaily.isChecked) "DAILY" else "WEEKLY"
            val isReminderEnabled = binding.switchReminder.isChecked
            
            onSave(name, frequency, selectedTimeStr, isReminderEnabled)
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
