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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            onImageTaken(stream.toByteArray())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    return object : CameraManager {
        override fun launch() {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                    cameraLauncher.launch()
                }

                else -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}
