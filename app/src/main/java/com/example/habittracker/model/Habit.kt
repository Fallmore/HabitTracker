package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var description: String,
    var streak: Int = 0,
    var isCompleted: Boolean = false,
    var imageUri: String? = null, // URI изображения как строка
    var buddyName: String? = null, // Имя друга
    var buddyPhone: String? = null // Телефон друга
)
    : java.io.Serializable // Добавляем для работы с файловой системы