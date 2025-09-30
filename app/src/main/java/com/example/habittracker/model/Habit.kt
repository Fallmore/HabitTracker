package com.example.habittracker.model

data class Habit(
    val id: Int,
    val name: String,
    val description: String,
    val streak: Int = 0,
    val isCompleted: Boolean = false,
    val imageUri: String? = null, // URI изображения как строка
    val buddyName: String? = null, // Имя друга
    val buddyPhone: String? = null // Телефон друга
)
    : java.io.Serializable // Добавляем для работы с файловой системы