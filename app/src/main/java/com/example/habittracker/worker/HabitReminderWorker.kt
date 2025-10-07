package com.example.habittracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habittracker.NotificationService
import com.example.habittracker.localDB.AppDatabase
import com.example.habittracker.repository.HabitRepository

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Получаем привычки из БД
            val database = AppDatabase.getInstance(applicationContext)
            val repository = HabitRepository(database.habitDao())
            val habits = repository.getAllHabits()

            // Используем Flow для получения списка
            var habitsList = emptyList<com.example.habittracker.model.Habit>()
            habits.collect { habitsList = it }

            // Показываем уведомление
            val notificationService = NotificationService(applicationContext)
            notificationService.showRandomHabitReminder(habitsList)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}