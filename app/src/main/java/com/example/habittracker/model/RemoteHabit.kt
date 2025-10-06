package com.example.habittracker.model

// Модель для отправки на сервер
data class RemoteHabit(
    val id: Int,
    val name: String,
    val description: String,
    val streak: Int = 0,
    val isCompleted: Boolean = false,
    val imageUri: String? = null,
    val cameraPhotoUri: String? = null,
    val buddyName: String? = null,
    val buddyPhone: String? = null,
    val synced: Boolean = false // Флаг синхронизации
)