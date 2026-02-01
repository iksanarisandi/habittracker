package com.habittracker.ui.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.habittracker.HabitApplication
import com.habittracker.R
import com.habittracker.data.HabitRepository
import com.habittracker.databinding.ActivityMainBinding
import com.habittracker.ui.add.AddHabitBottomSheet

class MainActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var adapter: HabitAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupRecyclerView()
        setupListeners()
        checkNotificationPermission()
        
        presenter.loadHabits()
    }
    
    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun setupDependencies() {
        val database = (application as HabitApplication).database
        val repository = HabitRepository(database.habitDao())
        presenter = HomePresenter(this, repository)
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            onToggle = { habit, isCompleted ->
                presenter.toggleHabit(habit, isCompleted)
            },
            onDelete = { habit ->
                showDeleteConfirmation(habit)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val bottomSheet = AddHabitBottomSheet(null) { name, frequency, reminderTime, isReminderEnabled ->
            presenter.addHabit(name, frequency, reminderTime, isReminderEnabled)
        }
        bottomSheet.show(supportFragmentManager, AddHabitBottomSheet.TAG)
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
        bottomSheet.show(supportFragmentManager, AddHabitBottomSheet.TAG)
    }

    private fun showDeleteConfirmation(habit: com.habittracker.data.local.entity.Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showHabits(habits: List<HabitUiModel>) {
        binding.progressBar.visibility = View.GONE
        binding.emptyStateText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        adapter.submitList(habits)
    }

    override fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.emptyStateText.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        adapter.submitList(emptyList())
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        if (adapter.currentList.isEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}
