package com.example.habittracker.repository

import com.example.habittracker.localDB.HabitDao
import com.example.habittracker.model.Habit
import com.example.habittracker.model.RemoteHabit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun deleteAllHabits() = habitDao.deleteAllHabits()

    //region Удаленная БД
    private val remoteRepository = RemoteHabitRepository()

    suspend fun syncWithServer() {
        try {
            // Получаем привычки с сервера
            val remoteHabits = remoteRepository.getHabitsFromServer()

            // Сохраняем их в локальную БД
            remoteHabits.forEach { remoteHabit ->
                val localHabit = remoteHabit.toLocalHabit()
                habitDao.insertHabit(localHabit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncHabitToServer(habit: Habit): Boolean {
        return try {
            val remoteHabit = habit.toRemoteHabit()
            remoteRepository.syncHabitToServer(remoteHabit)
        } catch (e: Exception) {
            false
        }
    }
    //endregion
}

// Extension functions для конвертации между моделями
fun RemoteHabit.toLocalHabit(): Habit {
    return Habit(
        id = this.id,
        name = this.name,
        description = this.description,
        streak = this.streak,
        isCompleted = this.isCompleted,
        imageUri = this.imageUri,
        buddyName = this.buddyName,
        buddyPhone = this.buddyPhone,
    )
}

fun Habit.toRemoteHabit(): RemoteHabit {
    return RemoteHabit(
        id = this.id,
        name = this.name,
        description = this.description,
        streak = this.streak,
        isCompleted = this.isCompleted,
        imageUri = this.imageUri,
        buddyName = this.buddyName,
        buddyPhone = this.buddyPhone,
    )
}