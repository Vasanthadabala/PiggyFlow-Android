package com.piggylabs.piggyflow.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Keeps receipt images alive past the picker's temporary read grant by copying the
 * picked file into app-private storage. Only the resulting path is persisted.
 */
object ReceiptStorage {

    private const val TAG = "ReceiptStorage"
    private const val DIR_NAME = "receipts"

    /** Returns the absolute path of the stored copy, or null when the copy failed. */
    fun copyToInternal(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val target = File(dir, "receipt_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            target.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Could not copy receipt", e)
            null
        }
    }

    /** Deletes a copy made by [copyToInternal]. No-op for a blank path. */
    fun delete(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).delete() }
    }
}
