package com.shubham.hard75kmm.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream



actual interface CameraManager {
    actual fun launch()
}

@Composable
actual fun rememberCameraManager(onImageTaken: (ByteArray) -> Unit): CameraManager {
    val context = LocalContext.current

    // Launcher for capturing the image preview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            onImageTaken(stream.toByteArray())
        }
    }

    // Launcher for requesting camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // If permission is granted, launch the camera
            cameraLauncher.launch()
        }
        // Optionally, handle the case where the user denies the permission
    }

    // Return the actual CameraManager implementation
    return object : CameraManager {
        override fun launch() {
            // Check for permission before launching camera
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                    // Permission is already granted
                    cameraLauncher.launch()
                }
                else -> {
                    // Permission has not been granted, request it
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}