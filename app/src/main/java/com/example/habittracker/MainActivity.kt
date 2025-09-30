package com.example.habittracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.habittracker.adapter.HabitAdapter
import com.example.habittracker.model.Habit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.example.habittracker.utils.FileUtils
import com.yourname.habittracker.network.SimpleNetworkMonitor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupNetworkMonitoring()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //region ЛР 1 работа с интерфейсом и ЛР 2 работа с разрешениями
    // Ссылка на витрину, которая показывает список
    private lateinit var habitsRecyclerView: RecyclerView
    // Кнопка добавления новой привычки
    private lateinit var fabAddHabit: FloatingActionButton
    // Адаптер, главный посредник между данными и RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private var habitIdCounter = 0

    // Временный список привычек для демонстрации
    private val sampleHabits = mutableListOf(
        Habit(++habitIdCounter, "Утренняя зарядка", "15 минут упражнений", 5, false),
        Habit(++habitIdCounter, "Чтение книги", "30 минут чтения", 12, true),
        Habit(++habitIdCounter, "Пить воду", "2 литра в день", 3, false),
        Habit(++habitIdCounter, "Изучение английского", "Новые слова и грамматика", 7, true),
        Habit(++habitIdCounter, "Прогулка", "Прогулка на свежем воздухе 30 мин", 0, false)
    )

    // Launcher для получения результата из AddHabitActivity
    private val addHabitLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                handleNewHabitData(data)
            }
        }
    }

    private fun initViews() {
        habitsRecyclerView = findViewById(R.id.habitsRecyclerView)
        fabAddHabit = findViewById(R.id.fabAddHabit)
    }

    private fun setupRecyclerView() {
        // Создаём адаптер и передаем ему функцию-обработчик
        habitAdapter = HabitAdapter { habit, isChecked ->
            // lambda-функция будет вызвана адаптером
            // когда пользователь нажмет на чекбокс в списке

            // Создаем обновленную привычку
            val updatedHabit = habit.copy(
                isCompleted = isChecked,
                streak = if (isChecked) habit.streak + 1 else habit.streak
            )

            // Обновляем в нашем списке
            updateHabitInList(updatedHabit)

            // Обновляем в адаптере
            habitAdapter.updateHabit(updatedHabit)

            val message = if (isChecked) {
                "${habit.name} выполнена! Серия: ${habit.streak + 1} дней"
            } else {
                "${habit.name} не выполнена"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        habitsRecyclerView.apply {
            // Размещаем элементы вертикальным список
            layoutManager = LinearLayoutManager(this@MainActivity)
            // Связываем адаптер с RecyclerView
            adapter = habitAdapter
        }

        // Устанавливаем начальный список
        habitAdapter.setHabits(sampleHabits)
    }

    private fun setupClickListeners() {
        fabAddHabit.setOnClickListener {
            val intent = Intent(this, AddHabbit::class.java)
            addHabitLauncher.launch(intent)
        }
    }

    // Добавление новой привычки
    private fun handleNewHabitData(data: Intent) {
        val name = data.getStringExtra("habit_name") ?: ""
        val description = data.getStringExtra("habit_description") ?: ""
        val imageUriString = data.getStringExtra("image_uri")
        val contactName = data.getStringExtra("contact_name")
        val contactPhone = data.getStringExtra("contact_phone")

        if (name.isEmpty()) {
            Toast.makeText(this, "Ошибка: название привычки пустое", Toast.LENGTH_SHORT).show()
            return
        }

        // Формируем описание с учетом выбранного контакта
        var fullDescription = description
        if (!contactName.isNullOrEmpty()) {
            fullDescription += if (fullDescription.isNotEmpty()) {
                "\n\nНапарник: $contactName"
            } else {
                "Напарник: $contactName"
            }
        }

        // Создаем новую привычку
        val newHabit = Habit(
            id = habitIdCounter++,
            name = name,
            description = fullDescription,
            streak = 0,
            isCompleted = false,
            imageUri = imageUriString,
            buddyName = contactName,
            buddyPhone = contactPhone
        )

        // Добавляем в наш список
        sampleHabits.add(0, newHabit)

        // Добавляем в адаптер (безопасно)
        habitAdapter.addHabit(newHabit)

        // Показываем информацию о выбранных данных
        var message = "Привычка \"$name\" добавлена!"
        if (!contactName.isNullOrEmpty()) {
            message += "\nНапарник: $contactName"
        }
        if (!imageUriString.isNullOrEmpty()) {
            message += "\nИзображение добавлено"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateHabitInList(updatedHabit: Habit) {
        val index = sampleHabits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            sampleHabits[index] = updatedHabit
        }
    }
    //endregion

    //region ЛР 3 работа с сетью
    private lateinit var networkMonitor: SimpleNetworkMonitor

    private fun setupNetworkMonitoring() {
        // Создаем и запускаем мониторинг сети
        networkMonitor = SimpleNetworkMonitor(this)
        networkMonitor.startMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Останавливаем мониторинг сети при закрытии приложения
        networkMonitor.stopMonitoring()
    }
    //endregion

    //region ЛР 4 файловая система

    // Создаем меню
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                createBackup()
                true
            }
            R.id.action_restore -> {
                restoreFromBackup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createBackup() {
        val success = FileUtils.saveHabitsToFile(this, sampleHabits)
        val message = if (success) {
            "Бэкап создан! Сохранено ${sampleHabits.size} привычек"
        } else {
            "Ошибка при создании бэкапа"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun restoreFromBackup() {
        if (!FileUtils.doesBackupExist(this)) {
            Toast.makeText(this, "Бэкап не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val restoredHabits = FileUtils.loadHabitsFromFile(this)
        if (restoredHabits != null) {
            sampleHabits.clear()
            sampleHabits.addAll(restoredHabits)
            habitAdapter.setHabits(sampleHabits)

            Toast.makeText(this, "Восстановлено ${restoredHabits.size} привычек", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ошибка при восстановлении", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion


}