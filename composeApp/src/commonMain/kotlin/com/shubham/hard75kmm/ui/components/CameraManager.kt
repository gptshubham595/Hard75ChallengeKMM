package com.shubham.hard75kmm.ui.components

import androidx.compose.runtime.Composable

/**
 * An expected composable that provides a way to launch the camera
 * and receive the captured image as a byte array.
 * Each platform (android, ios) will provide its own actual implementation.
 */
@Composable
expect fun rememberCameraManager(onImageTaken: (ByteArray) -> Unit): CameraManager

expect interface CameraManager {
    fun launch()
}
