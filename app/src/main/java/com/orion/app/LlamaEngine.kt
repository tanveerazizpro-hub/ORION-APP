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
    init { System.loadLibrary("orion") }

    external fun loadModel(modelPath: String, nCtx: Int): Boolean
    external fun isModelLoaded(modelPath: String, nCtx: Int): Boolean
    external fun generateStreaming(
        prompt: String, maxTokens: Int, temp: Float, threads: Int,
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

    fun getModelPath(context: Context, modelName: String): String =
        File(getModelsDir(context), modelName).absolutePath

    fun configureLogging(context: Context) {
        val logFile = File(context.filesDir, "orion_log.txt")
        try { setLogFile(logFile.absolutePath) } catch (_: Exception) {}
    }

    fun readLogTail(maxLines: Int = 300): String =
        try { getLogTail(maxLines) } catch (e: Exception) { "Log error: ${e.message}" }

    fun clearLog(context: Context) {
        val logFile = File(context.filesDir, "orion_log.txt")
        if (logFile.exists()) logFile.delete()
        configureLogging(context)
    }

    suspend fun loadModelAsync(context: Context, path: String, nCtx: Int): String? =
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) return@withContext "Model file not found"
            if (file.length() == 0L) return@withContext "Model file is empty"
            val ok = loadModel(path, nCtx)
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
        generateStreaming(prompt, maxTokens, temp, 3, callback)
    }

    /**
     * Build a prompt using the model's native chat template.
     * No system prompt — user messages only.
     * No explicit BOS — llama.cpp adds it automatically.
     */
    private fun buildPrompt(messages: List<ChatMessage>, modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("zaya") -> buildZaya(messages)
            else -> buildChatML(messages)   // lfm2, qwen, minicpm
        }
    }

    private fun buildChatML(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        for (msg in messages) {
            sb.append("<|im_start|>").append(msg.role).append("\n")
            sb.append(msg.content).append("<|im_end|>\n")
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildZaya(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        for (msg in messages) {
            val tag = if (msg.role == "user") "User" else "Assistant"
            sb.append(tag).append(": ").append(msg.content).append("\n")
        }
        sb.append("Assistant:")
        return sb.toString()
    }

    fun unloadModel() { try { freeModel() } catch (_: Exception) {} }
}
