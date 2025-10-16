package com.shubham.hard75kmm.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual class ImageStorage {
    private val fileManager = NSFileManager.defaultManager
    private val documentDirectory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String

    actual suspend fun saveImage(bytes: ByteArray): String {
        val fileName = generateUniqueFileName()
        val fullPath = "$documentDirectory/$fileName"

        // This now uses the corrected helper function
        val data = bytes.toNSData()
        data.writeToFile(fullPath, atomically = true)

        return fullPath
    }
}

/**
 * Correctly converts a Kotlin ByteArray to a native NSData object.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    // Pin the array in memory so the garbage collector doesn't move it.
    // This provides a stable pointer to its contents.
    return this.usePinned {
        // NSData.create expects a CPointer and a length (as ULong).
        // it.addressOf(0) gets the memory address of the first element.
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
}