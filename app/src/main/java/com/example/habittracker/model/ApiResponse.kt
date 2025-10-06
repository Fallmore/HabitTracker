package com.example.habittracker.model

// Ответ от сервера
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)