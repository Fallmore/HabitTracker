package com.example.habittracker

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация MapKit ДО setContentView
        MapKitFactory.setApiKey("b9aa723f-f5ef-4f87-b4f8-75a96c51728e")
        MapKitFactory.initialize(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        initViews()
        setupMap()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private lateinit var mapView: MapView
    private lateinit var btnMyLocation: Button
    private lateinit var btnBack: Button
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null
    private var selectedPoint: Point? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }

    private fun initViews() {
        mapView = findViewById(R.id.mapview)
        btnMyLocation = findViewById(R.id.btnMyLocation)
        btnBack = findViewById(R.id.btnBack)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun setupMap() {
        mapView.map.move(
            CameraPosition(Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        // Обработка нажатий на карту
        mapView.map.addInputListener(object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                selectedPoint = point

                // Очищаем предыдущие маркеры
                mapView.map.mapObjects.clear()

                // Добавляем новый маркер
                val marker = mapView.map.mapObjects.addPlacemark(point)
                marker.setOpacity(0.7f)

                Toast.makeText(
                    this@MapActivity,
                    "Выбрана точка: ${point.latitude}, ${point.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onMapLongTap(map: Map, point: Point) {
                // Не используется, но обязателен для реализации
            }
        })

        // Запрашиваем разрешение на геолокацию
        requestLocationPermission()
    }

    private fun setupClickListeners() {

        btnMyLocation.setOnClickListener {
            requestLocationPermission()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            checkCurrentLocation()
        }
    }

    fun checkCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Удаляем предыдущий слушатель
            locationListener?.let {
                locationManager.removeUpdates(it)
            }

            // Создаем новый слушатель для однократного получения локации
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Toast.makeText(
                        this@MapActivity,
                        "Получены координаты:\nШ: $latitude\nД: $longitude",
                        Toast.LENGTH_LONG
                    ).show()

                    moveToLocation(Point(latitude, longitude), "Мое местоположение")

                    // Удаляем слушатель после получения локации
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                // Запрашиваем обновление локации
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    locationListener as LocationListener,
                    null
                )

                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Показываем координаты
                    Toast.makeText(
                        this,
                        "Текущие координаты:\nШ: $latitude\nД: $longitude",
                        Toast.LENGTH_LONG
                    ).show()

                    // Перемещаем карту к текущему местоположению
                    moveToLocation(Point(latitude, longitude), "Мое местоположение")

                } else {
                    Toast.makeText(
                        this,
                        "Координаты не получены.\nУстановите местоположение в эмуляторе.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: SecurityException) {
                Toast.makeText(this, "Ошибка доступа к геолокации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToLocation(point: Point, locationName: String) {
        mapView.map.move(
            CameraPosition(point, 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        // Очищаем предыдущие маркеры и добавляем новый
        mapView.map.mapObjects.clear()
        val marker = mapView.map.mapObjects.addPlacemark(point)
        marker.setOpacity(0.8f)

        Toast.makeText(this, "Перемещение в $locationName", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}