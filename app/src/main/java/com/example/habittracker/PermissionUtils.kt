package com.example.habittracker

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    private const val CAMERA_PERMISSION_REQUEST_CODE = 1001

    fun hasCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    fun isPermissionGranted(
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {
        return requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}