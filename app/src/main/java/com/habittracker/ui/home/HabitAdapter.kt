package com.habittracker.ui.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.habittracker.data.local.entity.Habit
import com.habittracker.databinding.ItemHabitBinding

class HabitAdapter(
    private val onToggle: (Habit, Boolean) -> Unit,
    private val onDelete: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit
) : ListAdapter<HabitUiModel, HabitAdapter.HabitViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: HabitUiModel) {
            binding.tvName.text = model.habit.name
            binding.tvStreak.text = "${model.currentStreak} streak • Best: ${model.bestStreak}"

            // Remove listener to avoid triggering it while setting state
            binding.cbComplete.setOnCheckedChangeListener(null)
            binding.cbComplete.isChecked = model.isCompletedToday

            // Strikethrough if completed
            if (model.isCompletedToday) {
                binding.tvName.paintFlags = binding.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvName.paintFlags = binding.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.cbComplete.setOnCheckedChangeListener { _, isChecked ->
                onToggle(model.habit, isChecked)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(model.habit)
            }
            
            binding.root.setOnClickListener {
                onEdit(model.habit)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitUiModel>() {
        override fun areItemsTheSame(oldItem: HabitUiModel, newItem: HabitUiModel): Boolean {
            return oldItem.habit.id == newItem.habit.id
        }

        override fun areContentsTheSame(oldItem: HabitUiModel, newItem: HabitUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
