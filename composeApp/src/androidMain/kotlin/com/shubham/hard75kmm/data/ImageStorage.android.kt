package com.shubham.hard75kmm.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class ImageStorage(
    private val context: Context
) {
    actual suspend fun saveImage(bytes: ByteArray): String {
        return withContext(Dispatchers.IO) {
            val fileName = generateUniqueFileName()
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use {
                it.write(bytes)
            }
            // Return the correct URI format for local files
            "file://${file.absolutePath}"
        }
    }
}