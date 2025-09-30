package com.example.habittracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class CameraActivity : AppCompatActivity() {
    private lateinit var viewFinder: androidx.camera.view.PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnCancel: Button

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // Запрашиваем разрешения при создании активити
        if (PermissionUtils.hasCameraPermission(this)) {
            startCamera()
        } else {
            PermissionUtils.requestCameraPermission(this)
        }

        setupClickListeners()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initViews() {
        viewFinder = findViewById(R.id.viewFinder)
        btnCapture = findViewById(R.id.btnCapture)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupClickListeners() {
        btnCapture.setOnClickListener {
            takePhoto()
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Ошибка запуска камеры: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Создаем файл для сохранения фото
        val photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Ошибка съемки: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val message = "Фото сохранено!"
                    Toast.makeText(this@CameraActivity, message, Toast.LENGTH_SHORT).show()

                    // Возвращаем URI фото в MainActivity
                    val resultIntent = Intent().apply {
                        putExtra("photo_uri", savedUri.toString())
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        )
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null) // Сохраняем в папку приложения

        return File.createTempFile(
            "HABIT_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionUtils.isPermissionGranted(requestCode, grantResults)) {
            startCamera()
        } else {
            Toast.makeText(this, "Разрешение на камеру необходимо", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
