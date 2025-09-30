package com.example.habittracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.habittracker.model.Habit
import java.io.*

class FileUtils {
    companion object {
        private const val BACKUP_FILENAME = "habits_backup.json"

        // Сохраняем привычки в JSON-файл
        fun saveHabitsToFile(context: Context, habits: List<Habit>): Boolean {
            return try {
                val gson = Gson()
                val jsonString = gson.toJson(habits)

                val file = File(context.filesDir, BACKUP_FILENAME)
                file.writeText(jsonString)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        // Загружаем привычки из JSON-файла
        fun loadHabitsFromFile(context: Context): List<Habit>? {
            return try {
                val file = File(context.filesDir, BACKUP_FILENAME)
                if (!file.exists()) return null

                val jsonString = file.readText()
                val gson = Gson()
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson<List<Habit>>(jsonString, type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // Проверяем существует ли бэкап
        fun doesBackupExist(context: Context): Boolean {
            val file = File(context.filesDir, BACKUP_FILENAME)
            return file.exists()
        }
    }
}