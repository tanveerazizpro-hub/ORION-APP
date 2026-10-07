package com.orion.app

import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ChatMessage(
    val role: String,
    val content: String,
    val tokenCount: Int = 0,
    val tokPerSec: Double = 0.0,
    val elapsedMs: Long = 0L
)

@Keep
class TokenCallback(val block: (String) -> Unit) {
    @Keep
    fun onToken(token: String) {
        block(token)
    }
}

object LlamaEngine {
    init {
        System.loadLibrary("orion")
    }

    external fun loadModel(modelPath: String): Boolean
    external fun generateStreaming(
        prompt: String,
        maxTokens: Int,
        temp: Float,
        threads: Int,
        callback: TokenCallback
    ): String
    external fun freeModel()
    external fun setLogFile(path: String)
    external fun getLogTail(maxLines: Int): String
    external fun getLoadProgress(): Int

    fun getModelsDir(context: Context): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(base, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listModels(context: Context): List<String> {
        return getModelsDir(context).listFiles()
            ?.filter { it.extension == "gguf" }
            ?.map { it.name }
            ?: emptyList()
    }

    fun getModelPath(context: Context, modelName: String): String {
        return File(getModelsDir(context), modelName).absolutePath
    }

    fun configureLogging(context: Context) {
        val logFile = File(context.filesDir, "orion_log.txt")
        try { setLogFile(logFile.absolutePath) } catch (_: Exception) {}
    }

    fun readLogTail(context: Context, maxLines: Int = 300): String {
        return try { getLogTail(maxLines) } catch (e: Exception) {
            "Could not read log: ${e.message}"
        }
    }

    fun clearLog(context: Context) {
        val logFile = File(context.filesDir, "orion_log.txt")
        if (logFile.exists()) logFile.delete()
        configureLogging(context)
    }

    suspend fun loadModelAsync(context: Context, path: String): String? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext "Model file not found"
        if (file.length() == 0L) return@withContext "Model file is empty or corrupted"

        try { freeModel() } catch (_: Exception) {}

        val ok = loadModel(path)
        if (!ok) "Failed to load model" else null
    }

    suspend fun generateStreamingAsync(
        messages: List<ChatMessage>,
        modelName: String,
        maxTokens: Int,
        temp: Float,
        onToken: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(messages, modelName)
        val callback = TokenCallback { piece -> onToken(piece) }
        generateStreaming(prompt, maxTokens, temp, 4, callback)
    }

    private fun buildPrompt(messages: List<ChatMessage>, modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("lfm2") -> {
                val sb = StringBuilder("<|startoftext|>")
                for (msg in messages) {
                    sb.append("<|im_start|>").append(msg.role).append("\n")
                    sb.append(msg.content).append("<|im_end|>\n")
                }
                sb.append("<|im_start|>assistant\n")
                sb.toString()
            }
            lower.contains("qwen") -> {
                val sb = StringBuilder()
                for (msg in messages) {
                    sb.append("<|im_start|>").append(msg.role).append("\n")
                    sb.append(msg.content).append("<|im_end|>\n")
                }
                sb.append("<|im_start|>assistant\n")
                sb.toString()
            }
            lower.contains("minicpm") -> {
                val sb = StringBuilder()
                for (msg in messages) {
                    sb.append("<|im_start|>").append(msg.role).append("\n")
                    sb.append(msg.content).append("<|im_end|>\n")
                }
                sb.append("<|im_start|>assistant\n")
                sb.toString()
            }
            lower.contains("zaya") -> {
                val sb = StringBuilder()
                for (msg in messages) {
                    val tag = if (msg.role == "user") "User" else "Assistant"
                    sb.append(tag).append(": ").append(msg.content).append("\n")
                }
                sb.append("Assistant:")
                sb.toString()
            }
            else -> {
                messages.joinToString("\n") { "${it.role}: ${it.content}" } + "\nassistant:"
            }
        }
    }

    fun unloadModel() {
        try { freeModel() } catch (_: Exception) {}
    }
}
