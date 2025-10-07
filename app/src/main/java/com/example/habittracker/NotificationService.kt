package com.example.habittracker

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.habittracker.model.Habit

class NotificationService(private val context: Context) {

    fun showRandomHabitReminder(habits: List<Habit>) {
        // Фильтруем невыполненные привычки
        val incompleteHabits = habits.filter { !it.isCompleted }

        if (incompleteHabits.isNotEmpty()) {
            // Выбираем случайную привычку для напоминания
            val randomHabit = incompleteHabits.random()
            showNotification(randomHabit)
        } else {
            // Если все привычки выполнены - показываем ободряющее уведомление
            showMotivationalNotification()
        }
    }

    private fun showNotification(habit: Habit) {
        val notification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📋 Напоминание о привычке")
            .setContentText("Не забудьте: ${habit.name}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${habit.name}\n\n${habit.description}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MainActivity.NOTIFICATION_ID, notification)
    }

    private fun showMotivationalNotification() {
        val motivationalMessages = listOf(
            "🎉 Отличная работа! Все привычки выполнены!",
            "🌟 Вы молодец! Продолжайте в том же духе!",
            "💪 Идеальный день! Все задачи завершены!",
            "🔥 Потрясающе! Не останавливайтесь!"
        )

        val randomMessage = motivationalMessages.random()

        val notification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎯 Прогресс привычек")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MainActivity.NOTIFICATION_ID + 1, notification)
    }

    fun showHabitCompletedNotification(habit: Habit) {
        val notification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("✅ Привычка выполнена!")
            .setContentText("${habit.name} - серия: ${habit.streak} дней")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habit.id.toInt(), notification) // Уникальный ID для каждой привычки
    }
}