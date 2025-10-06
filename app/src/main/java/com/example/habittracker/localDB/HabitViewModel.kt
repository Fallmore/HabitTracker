package com.example.habittracker.localDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.model.Habit
import com.example.habittracker.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    val habits = repository.getAllHabits()

    private val _uiState = MutableStateFlow<HabitUiState>(HabitUiState.Loading)
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.insertHabit(habit)
                _uiState.value = HabitUiState.Success("Привычка добавлена")
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error("Ошибка добавления: ${e.message}")
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.updateHabit(habit)
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error("Ошибка обновления: ${e.message}")
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habit)
                _uiState.value = HabitUiState.Success("Привычка удалена")
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error("Ошибка удаления: ${e.message}")
            }
        }
    }

    //region Удаленная БД

    fun syncWithServer() {
        viewModelScope.launch {
            try {
                _uiState.value = HabitUiState.Loading
                repository.syncWithServer()
                _uiState.value = HabitUiState.Success("Синхронизация завершена")
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error("Ошибка синхронизации: ${e.message}")
            }
        }
    }

    fun addHabitWithSync(habit: Habit) {
        viewModelScope.launch {
            try {
                // Сначала сохраняем локально
                val localId = repository.insertHabit(habit)

                // Пытаемся синхронизировать с сервером
                val synced = repository.syncHabitToServer(habit)

                if (synced) {
                    _uiState.value = HabitUiState.Success("Привычка добавлена и синхронизирована")
                } else {
                    _uiState.value = HabitUiState.Success("Привычка добавлена локально (оффлайн)")
                }
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    //endregion
}

sealed class HabitUiState {
    object Loading : HabitUiState()
    data class Success(val message: String) : HabitUiState()
    data class Error(val message: String) : HabitUiState()
}