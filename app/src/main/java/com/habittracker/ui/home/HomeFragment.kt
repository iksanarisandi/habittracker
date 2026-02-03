package com.habittracker.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.habittracker.HabitApplication
import com.habittracker.R
import com.habittracker.data.HabitRepository
import com.habittracker.databinding.FragmentHomeBinding
import com.habittracker.ui.add.AddHabitBottomSheet

class HomeFragment : Fragment(), HomeContract.View {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var adapter: HabitAdapter

    // Handle date change
    private val dateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == android.content.Intent.ACTION_DATE_CHANGED) {
                presenter.loadHabits()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDependencies()
        setupRecyclerView()
        setupListeners()
        checkNotificationPermission()
        
        presenter.loadHabits()

        requireContext().registerReceiver(dateReceiver, android.content.IntentFilter(android.content.Intent.ACTION_DATE_CHANGED))
    }

    override fun onDestroyView() {
        requireContext().unregisterReceiver(dateReceiver)
        presenter.detach()
        _binding = null
        super.onDestroyView()
    }

    private fun setupDependencies() {
        val database = (requireActivity().application as HabitApplication).database
        val repository = HabitRepository(database.habitDao())
        presenter = HomePresenter(this, repository, requireContext())
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            onToggle = { habit, isCompleted ->
                presenter.toggleHabit(habit, isCompleted)
            },
            onDelete = { habit ->
                showDeleteConfirmation(habit)
            },
            onEdit = { habit ->
                showEditHabitDialog(habit)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun showAddHabitDialog() {
        val bottomSheet = AddHabitBottomSheet(null) { name, frequency, reminderTime, isReminderEnabled ->
            presenter.addHabit(name, frequency, reminderTime, isReminderEnabled)
        }
        bottomSheet.show(childFragmentManager, AddHabitBottomSheet.TAG)
    }

    private fun showEditHabitDialog(habit: com.habittracker.data.local.entity.Habit) {
        val bottomSheet = AddHabitBottomSheet(habit) { name, frequency, reminderTime, isReminderEnabled ->
            val updatedHabit = habit.copy(
                name = name,
                frequency = frequency,
                reminderTime = reminderTime,
                isReminderEnabled = isReminderEnabled,
                updatedAt = System.currentTimeMillis()
            )
            presenter.updateHabit(updatedHabit)
        }
        bottomSheet.show(childFragmentManager, AddHabitBottomSheet.TAG)
    }

    private fun showDeleteConfirmation(habit: com.habittracker.data.local.entity.Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showHabits(habits: List<HabitUiModel>) {
        if (_binding == null) return
        
        binding.progressBar.visibility = View.GONE
        binding.emptyStateText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        adapter.submitList(habits)

        // Update Progress
        val completed = habits.count { it.isCompletedToday }
        val total = habits.size
        if (total > 0) {
            binding.progressBarDaily.max = total
            binding.progressBarDaily.progress = completed
            binding.tvProgressText.text = "$completed/$total completed today"
            binding.progressBarDaily.visibility = View.VISIBLE
            binding.tvProgressText.visibility = View.VISIBLE
        } else {
            binding.progressBarDaily.visibility = View.GONE
            binding.tvProgressText.visibility = View.GONE
        }
    }

    override fun showEmptyState() {
        if (_binding == null) return
        
        binding.progressBar.visibility = View.GONE
        binding.emptyStateText.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        adapter.submitList(emptyList())
        binding.progressBarDaily.visibility = View.GONE
        binding.tvProgressText.visibility = View.GONE
    }

    override fun showError(message: String) {
        if (context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showLoading() {
        if (_binding == null) return
        if (adapter.currentList.isEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        if (_binding != null) {
            binding.progressBar.visibility = View.GONE
        }
    }
}
