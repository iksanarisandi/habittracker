package com.habittracker.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.habittracker.HabitApplication
import com.habittracker.data.HabitRepository
import com.habittracker.databinding.FragmentStatisticsBinding
import com.habittracker.databinding.ItemChartBarBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class StatisticsFragment : Fragment(), StatisticsContract.View {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: StatisticsContract.Presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDependencies()
        presenter.loadStatistics()
    }

    override fun onDestroyView() {
        presenter.detach()
        _binding = null
        super.onDestroyView()
    }

    private fun setupDependencies() {
        val database = (requireActivity().application as HabitApplication).database
        val repository = HabitRepository(database.habitDao())
        presenter = StatisticsPresenter(this, repository)
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun showWeeklyProgress(data: Map<LocalDate, Float>) {
        if (_binding == null) return

        // Bind data to the 7 bars
        // Assuming data contains last 7 days. If not, we handle it.
        // We need to map the bars (bar1..bar7) to the dates.
        
        val bars = listOf(
            binding.bar1,
            binding.bar2,
            binding.bar3,
            binding.bar4,
            binding.bar5,
            binding.bar6,
            binding.bar7
        )

        val sortedDates = data.keys.sorted()
        if (sortedDates.isEmpty()) return

        // Fill bars
        bars.forEachIndexed { index, includeBar ->
            if (index < sortedDates.size) {
                val date = sortedDates[index]
                val percentage = data[date] ?: 0f

                // Access views using findViewById on the included layout
                val tvDay = includeBar.findViewById<android.widget.TextView>(com.habittracker.R.id.tvDay)
                val barView = includeBar.findViewById<android.view.View>(com.habittracker.R.id.barView)

                // Set Day Label (e.g., "Mon")
                tvDay.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1)
                
                // Set Bar Height
                // Max height is 100dp (defined in parent container as 150dp, let's say max bar is 100dp)
                // Actually we should use layout_weight or explicit height calculation.
                // Since it's a LinearLayout with weightSum, vertical orientation... wait.
                // The parent chartContainer is horizontal.
                // Each bar item is vertical.
                // To animate height, we can set layout_height or layout_weight if inside vertical linear layout.
                // Here barView is inside vertical LinearLayout.
                
                val params = barView.layoutParams
                // Set height based on percentage (max 100dp approx or proportional)
                // Let's use a fixed max height of 120dp for the bar part
                val density = resources.displayMetrics.density
                val maxHeight = 120 * density
                params.height = (maxHeight * percentage).toInt().coerceAtLeast((1 * density).toInt()) // Min 1dp to show baseline
                barView.layoutParams = params

                // Highlight today
                if (date == LocalDate.now()) {
                    tvDay.setTypeface(null, android.graphics.Typeface.BOLD)
                    barView.alpha = 1.0f
                } else {
                    tvDay.setTypeface(null, android.graphics.Typeface.NORMAL)
                    barView.alpha = 0.6f
                }
            }
        }
    }

    override fun showOverallStats(totalHabits: Int, completionRate: Int, bestStreak: Int) {
        if (_binding == null) return
        binding.tvTotalHabits.text = totalHabits.toString()
        binding.tvCompletionRate.text = "$completionRate%"
        binding.tvBestStreak.text = bestStreak.toString()
    }

    override fun showError(message: String) {
        if (context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
