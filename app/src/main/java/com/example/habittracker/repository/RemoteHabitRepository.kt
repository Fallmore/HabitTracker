package com.example.habittracker.repository

import com.example.habittracker.api.HabitApiService
import com.example.habittracker.api.RetrofitClient
import com.example.habittracker.model.RemoteHabit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RemoteHabitRepository {

    private val apiService: HabitApiService = RetrofitClient.habitApiService

    // Реальное получение данных с сервера
    suspend fun getHabitsFromServer(): List<RemoteHabit> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<RemoteHabit>> = apiService.getHabits()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                // Если запрос неуспешен, возвращаем mock данные
                getMockHabits()
            }
        } catch (e: Exception) {
            // При ошибке сети возвращаем mock данные
            e.printStackTrace()
            getMockHabits()
        }
    }

    // Реальная отправка данных на сервер
    suspend fun syncHabitToServer(habit: RemoteHabit): Boolean = withContext(Dispatchers.IO) {
        try {
            val response: Response<RemoteHabit> = apiService.createHabit(habit)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Mock данные для fallback
    private fun getMockHabits(): List<RemoteHabit> {
        return listOf(
            RemoteHabit(
                id = 1001,
                name = "Серверная привычка 1",
                description = "Эта привычка пришла с сервера",
                streak = 7,
                isCompleted = true
            ),
            RemoteHabit(
                id = 1002,
                name = "Серверная привычка 2",
                description = "Еще одна привычка с сервера",
                streak = 3,
                isCompleted = false
            )
        )
    }

    // Обновление привычки на сервере
    suspend fun updateHabitOnServer(habit: RemoteHabit): Boolean = withContext(Dispatchers.IO) {
        try {
            val response: Response<RemoteHabit> = apiService.updateHabit(habit.id, habit)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Удаление привычки с сервера
    suspend fun deleteHabitOnServer(habitId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response: Response<Unit> = apiService.deleteHabit(habitId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}