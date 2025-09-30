package com.example.habittracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class AddHabbit : AppCompatActivity() {
    // Константы для запросов разрешений и активностей
    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_CONTACT_PICK = 2
        private const val REQUEST_READ_CONTACTS_PERMISSION = 101
        private const val REQUEST_READ_STORAGE_PERMISSION = 102
        private const val REQUEST_CAMERA_PERMISSION = 103
        private const val REQUEST_CAMERA_CAPTURE = 3
    }

    // Views
    private lateinit var etHabitName: EditText
    private lateinit var etHabitDescription: EditText
    private lateinit var ivHabitImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var tvSelectedContact: TextView
    private lateinit var btnSelectContact: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var ivCameraPhoto: ImageView
    private lateinit var btnTakePhoto: Button

    // Переменные для хранения выбранных данных
    private var selectedImageUri: Uri? = null
    private var selectedContactName: String? = null
    private var selectedContactPhone: String? = null
    private var cameraPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_habbit)
        initViews()
        setupClickListeners()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        etHabitName = findViewById(R.id.etHabitName)
        etHabitDescription = findViewById(R.id.etHabitDescription)
        ivHabitImage = findViewById(R.id.ivHabitImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        tvSelectedContact = findViewById(R.id.tvSelectedContact)
        btnSelectContact = findViewById(R.id.btnSelectContact)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        ivCameraPhoto = findViewById(R.id.ivCameraPhoto)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
    }

    private fun setupClickListeners() {
        // Кнопка выбора изображения
        btnSelectImage.setOnClickListener {
            checkStoragePermissionAndPickImage()
        }

        // Кнопка выбора контакта
        btnSelectContact.setOnClickListener {
            checkContactsPermissionAndPickContact()
        }

        // Кнопка сохранения
        btnSave.setOnClickListener {
            saveHabit()
        }

        // Кнопка отмены
        btnCancel.setOnClickListener {
            finish()
        }

        // Кнопка съемки фото
        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }
    }

    //region Работа с изображениями
    private fun checkStoragePermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже есть - выбираем изображение
                pickImageFromGallery()
            }
            else -> {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_READ_STORAGE_PERMISSION
                )
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    //endregion

    //region Работа с камерой
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже есть - открываем камеру
                openCamera()
            }
            else -> {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_CAMERA_CAPTURE)
    }
    //endregion

    //region Работа с контактами
    private fun checkContactsPermissionAndPickContact() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже есть - выбираем контакт
                pickContactFromPhonebook()
            }
            else -> {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    REQUEST_READ_CONTACTS_PERMISSION
                )
            }
        }
    }

    private fun pickContactFromPhonebook() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        startActivityForResult(intent, REQUEST_CONTACT_PICK)
    }
    //endregion

    //region Обработка результатов
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_READ_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Разрешение на доступ к галерее необходимо для выбора изображения", Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_READ_CONTACTS_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickContactFromPhonebook()
                } else {
                    Toast.makeText(this, "Разрешение на доступ к контактам необходимо для выбора друга", Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Разрешение на камеру необходимо для съемки фото", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        ivHabitImage.setImageURI(uri)
                        Toast.makeText(this, "Изображение выбрано", Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_CONTACT_PICK -> {
                    data?.data?.let { contactUri ->
                        getContactInfo(contactUri)
                    }
                }
                REQUEST_CAMERA_CAPTURE -> {
                    data?.getStringExtra("photo_uri")?.let { uriString ->
                        cameraPhotoUri = uriString.toUri()
                        ivCameraPhoto.setImageURI(cameraPhotoUri)
                        Toast.makeText(this, "Фото сделано!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getContactInfo(contactUri: Uri) {
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    selectedContactName = cursor.getString(nameIndex)
                    selectedContactPhone = cursor.getString(phoneIndex)

                    tvSelectedContact.text = "$selectedContactName\n$selectedContactPhone"
                    Toast.makeText(this, "Контакт выбран: $selectedContactName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при чтении контакта", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    //endregion

    private fun saveHabit() {
        val name = etHabitName.text.toString().trim()
        val description = etHabitDescription.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название привычки", Toast.LENGTH_SHORT).show()
            return
        }

        // Создаем результат для возврата в MainActivity
        val resultIntent = Intent().apply {
            putExtra("habit_name", name)
            putExtra("habit_description", description)
            selectedImageUri?.let { putExtra("image_uri", it.toString()) }
            selectedContactName?.let { putExtra("contact_name", it) }
            selectedContactPhone?.let { putExtra("contact_phone", it) }
            cameraPhotoUri?.let { putExtra("camera_photo_uri", it.toString()) }
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()

        Toast.makeText(this, "Привычка \"$name\" сохранена!", Toast.LENGTH_SHORT).show()
    }
}