package com.example.habittracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.model.Habit
import androidx.core.net.toUri

// ListAdapter автоматически дает:
// - submitList() - безопасное обновление данных
// - DiffUtil - умное сравнение списков
// - Автоматические анимации изменений
// - Потокобезопасность
class HabitAdapter(private val onHabitChecked: (Habit, Boolean) -> Unit)
    : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    // ViewHolder класс
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivHabitIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvHabitDescription)
        val tvStreak: TextView = itemView.findViewById(R.id.tvHabitStreak)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbHabitCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = getItem(position)

        // Заполняем данные
        holder.tvName.text = habit.name
        holder.tvDescription.text = habit.description
        holder.tvStreak.text = "Серия: ${habit.streak} дней"

        // Отображаем изображение если есть
        habit.imageUri?.let { uriString ->
            try {
                val uri = uriString.toUri()
                holder.ivIcon.setImageURI(uri)
            } catch (e: Exception) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit)
                e.printStackTrace()
            }
        } ?: run {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit)
        }

        // Убираем предыдущий слушатель чтобы избежать бесконечного цикла
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = habit.isCompleted

        // Обработка клика на чекбокс
        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onHabitChecked(habit, isChecked)
        }
    }

    // Метод для обновления отдельной привычки
    fun updateHabit(updatedHabit: Habit) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            currentList[index] = updatedHabit
            submitList(currentList)
        }
    }

    // Метод для добавления новой привычки
    fun addHabit(newHabit: Habit) {
        val currentList = currentList.toMutableList()
        currentList.add(0, newHabit) // Добавляем в начало
        submitList(currentList)
    }

    // Метод для полной замены списка
    fun setHabits(habits: List<Habit>) {
        submitList(habits.toList())
    }
}

// DiffUtil для эффективного обновления списка
class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
    override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
        return oldItem == newItem
    }
}