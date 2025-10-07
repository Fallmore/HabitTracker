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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.habittracker.localDB.AppDatabase
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.localDB.HabitUiState
import com.example.habittracker.localDB.HabitViewModel
import com.example.habittracker.utils.FileUtils
import com.yourname.habittracker.network.SimpleNetworkMonitor
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupNetworkMonitoring()
        initRoomComponents()
        observeHabits()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
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

            habitViewModel.updateHabit(updatedHabit)

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
            name = name,
            description = fullDescription,
            streak = 0,
            isCompleted = false,
            imageUri = imageUriString,
            buddyName = contactName,
            buddyPhone = contactPhone
        )

        // Сохраняем в БД
        //habitViewModel.insertHabit(newHabit)
        habitViewModel.addHabitWithSync(newHabit)

        // Показываем информацию о выбранных данных
        var message = "Привычка \"$name\" добавлена!"
        if (!contactName.isNullOrEmpty()) {
            message += "\nНапарник: $contactName"
        }
        if (!imageUriString.isNullOrEmpty()) {
            message += "\nИзображение добавлено"
        }

        // Toast показывается автоматически через observeHabits()
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
            R.id.action_map -> {
                openMap()
                true
            }
            R.id.action_backup -> {
                createBackup()
                true
            }
            R.id.action_restore -> {
                restoreFromBackup()
                true
            }
            R.id.action_sync -> { // Новая кнопка синхронизации
                syncWithServer()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createBackup() {
        val currentHabits = habitAdapter.currentList
        val success = FileUtils.saveHabitsToFile(this, currentHabits)
        val message = if (success) {
            "Бэкап создан! Сохранено ${currentHabits.size} привычек"
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
            // Удаляем все текущие привычки и добавляем восстановленные
            lifecycleScope.launch {
                habitViewModel.habits.collect { currentHabits ->
                    currentHabits.forEach { habitViewModel.deleteHabit(it) }
                }

                restoredHabits.forEach { habitViewModel.insertHabit(it) }
            }

            Toast.makeText(this, "Восстановлено ${restoredHabits.size} привычек", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ошибка при восстановлении", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    //region ЛР 6 локальная БД
    // Room компоненты
    private lateinit var habitViewModel: HabitViewModel

    private fun initRoomComponents() {
        val database = AppDatabase.getInstance(this)
        val repository = HabitRepository(database.habitDao())
        habitViewModel = HabitViewModel(repository)
    }

    private fun observeHabits() {
        // Подписываемся на Flow из БД
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.habits.collect { habits ->
                    habitAdapter.setHabits(habits)

                    // Обновляем счетчик ID
                    if (habits.isNotEmpty()) {
                        habitIdCounter = habits.maxByOrNull { it.id }?.id?.plus(1) ?: 1
                    }
                }
            }
        }

        // Наблюдаем за состоянием UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.uiState.collect { state ->
                    when (state) {
                        is HabitUiState.Success -> {
                            Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is HabitUiState.Error -> {
                            Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        HabitUiState.Loading -> {
                            // Можно показать ProgressBar
                        }
                    }
                }
            }
        }
    }
    //endregion

    //region ЛР 7 удаленная БД

    // Метод синхронизации
    private fun syncWithServer() {
        habitViewModel.syncWithServer()
    }

    //endregion

    //region ЛР 8 карта

    private fun openMap() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    //endregion

    //region ЛР 9 датчики

    private lateinit var sensorManager: SensorManager
    private var lastUpdate = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) > 100) {
                    val diffTime = currentTime - lastUpdate
                    lastUpdate = currentTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                    if (speed > 800) {
                        // Встряхивание обнаружено!
                        onShakeDetected(speed)
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val sensorLightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val light = it.values[0]
                if (light < 50f) {
                    onLightDetected("в темноте")
                } else if (light > 2000f) {
                    onLightDetected("на свету")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun onShakeDetected(speedShake: Float) {
        runOnUiThread {
            Toast.makeText(this, "Телефон встряхнут со скоростью $speedShake", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onLightDetected(text: String) {
        runOnUiThread {
            Toast.makeText(this, "Телефон находится $text", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        sensorManager.registerListener(
            sensorLightListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        sensorManager.unregisterListener(sensorLightListener)
    }

    //endregion
}