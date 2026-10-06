package com.orion.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ModelImporter {

    // Live state the UI can watch
    val isImporting = mutableStateOf(false)
    val progress = mutableStateOf(0f)
    val importingFileName = mutableStateOf("")
    val statusMessage = mutableStateOf("")

    /**
     * Copies a .gguf file from a SAF URI into the app's models folder.
     * Runs on a background thread. Updates live state for the UI.
     * Returns Result.success(fileName) or Result.failure(exception).
     */
    suspend fun importModel(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        var targetFile: File? = null
        try {
            val fileName = getFileName(context, uri)
                ?: return@withContext fail("Could not read file name")

            if (!fileName.endsWith(".gguf", ignoreCase = true)) {
                return@withContext fail("Only .gguf files are supported")
            }

            targetFile = File(LlamaEngine.getModelsDir(context), fileName)
            if (targetFile.exists()) {
                return@withContext fail("A model named '$fileName' already exists")
            }

            val totalBytes = getFileSize(context, uri)

            withContext(Dispatchers.Main) {
                isImporting.value = true
                progress.value = 0f
                importingFileName.value = fileName
                statusMessage.value = "Importing $fileName..."
            }

            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext fail("Could not open source file")

            input.use { src ->
                targetFile.outputStream().use { dst ->
                    val buffer = ByteArray(1024 * 1024) // 1 MB buffer
                    var copied = 0L
                    var read: Int
                    while (src.read(buffer).also { read = it } > 0) {
                        dst.write(buffer, 0, read)
                        copied += read
                        if (totalBytes > 0) {
                            val p = (copied.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) { progress.value = p }
                        }
                    }
                    dst.flush()
                }
            }

            // Refresh model list so it shows up immediately in the picker
            ModelManager.refreshAvailableModels(context)

            withContext(Dispatchers.Main) {
                isImporting.value = false
                progress.value = 1f
                statusMessage.value = "Imported: $fileName"
            }
            Result.success(fileName)

        } catch (e: Exception) {
            // Clean up partial file on failure
            targetFile?.takeIf { it.exists() }?.delete()
            withContext(Dispatchers.Main) {
                isImporting.value = false
                statusMessage.value = "Import failed: ${e.message ?: "unknown error"}"
            }
            Result.failure(e)
        }
    }

    /** Reset status so we can import again. */
    fun clearStatus() {
        statusMessage.value = ""
        importingFileName.value = ""
        progress.value = 0f
    }

    private suspend fun fail(message: String): Result<String> {
        withContext(Dispatchers.Main) {
            isImporting.value = false
            statusMessage.value = message
        }
        return Result.failure(Exception(message))
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
        }
        return uri.lastPathSegment
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getLong(idx)
        }
        return 0L
    }
}
