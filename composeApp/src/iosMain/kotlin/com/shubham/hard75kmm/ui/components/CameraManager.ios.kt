package com.shubham.hard75kmm.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraManager(onImageTaken: (ByteArray) -> Unit): CameraManager {
    TODO("Not yet implemented")
}

actual interface CameraManager {
    actual fun launch()
}