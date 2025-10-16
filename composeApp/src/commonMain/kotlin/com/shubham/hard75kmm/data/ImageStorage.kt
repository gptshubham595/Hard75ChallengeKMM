package com.shubham.hard75kmm.data

import com.benasher44.uuid.uuid4

/**
 * A platform-agnostic interface for saving image data to the device's storage.
 */
expect class ImageStorage {
    /**
     * Saves image bytes to a file and returns its platform-specific path.
     * @param bytes The raw byte data of the image.
     * @return The accessible file path (e.g., "file:///...") for the saved image.
     */
    suspend fun saveImage(bytes: ByteArray): String
}

// Helper to generate unique filenames
fun generateUniqueFileName() = "${uuid4()}.jpg"