package com.shubham.hard75kmm.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject


actual interface CameraManager {
    actual fun launch()
}

@Composable
actual fun rememberCameraManager(onImageTaken: (ByteArray) -> Unit): CameraManager {
    val imagePicker = remember { UIImagePickerController() }

    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image =
                    didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage
                if (image != null) {
                    val imageData = UIImageJPEGRepresentation(image, 1.0)
                    if (imageData != null) {
                        onImageTaken(imageData.toByteArray())
                    }
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    imagePicker.delegate = delegate
    imagePicker.sourceType =
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

    return remember {
        IOSCameraManager(
            imagePicker = imagePicker,
        )
    }
}

private class IOSCameraManager(
    private val imagePicker: UIImagePickerController
) : CameraManager {

    // Function to find the top-most view controller to present the camera UI
    private fun findUIViewController(): UIViewController? {
        var viewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (viewController?.presentedViewController != null) {
            viewController = viewController.presentedViewController
        }
        return viewController
    }

    override fun launch() {
        // Check for camera permission status
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                // Permission granted, present the camera
                findUIViewController()?.presentViewController(imagePicker, true, null)
            }

            AVAuthorizationStatusNotDetermined -> {
                // Permission not yet requested, ask for it
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                    if (isGranted) {
                        findUIViewController()?.presentViewController(imagePicker, true, null)
                    }
                }
            }

            AVAuthorizationStatusDenied -> {
                // Permission denied. Optionally, show an alert to the user.
            }

            else -> {
                // Handle other statuses if necessary
            }
        }
    }
}

// Helper function to convert NSData to ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(this.length.toInt()).apply {
    this.usePinned {
        platform.posix.memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}