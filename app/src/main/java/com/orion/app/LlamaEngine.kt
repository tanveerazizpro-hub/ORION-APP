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

    // ============================================================
    // System prompt — shapes model behavior on every conversation.
    // Kept short so it doesn't eat into the context window.
    // ============================================================
    private const val SYSTEM_PROMPT =
        "You are O.R.I.O.N., a precise and helpful AI assistant. " +
        "Answer clearly and directly. Use plain language. " +
        "If you are unsure, say so instead of guessing. " +
        "If the question is simple, keep the answer short. " +
        "If it needs reasoning, think step by step."

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
        generateStreaming(prompt, maxTokens, temp, 4, callback)
    }

    /**
     * Build a prompt for the model using its native chat template.
     * Prepends a system message so the model knows how to behave.
     */
    private fun buildPrompt(messages: List<ChatMessage>, modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("lfm2") -> buildChatML(messages, includeStartOfText = true)
            lower.contains("qwen") -> buildChatML(messages, includeStartOfText = false)
            lower.contains("minicpm") -> buildChatML(messages, includeStartOfText = false)
            lower.contains("zaya") -> buildZaya(messages)
            else -> buildChatML(messages, includeStartOfText = false)
        }
    }

    // ChatML format used by LFM2, Qwen, MiniCPM
    private fun buildChatML(messages: List<ChatMessage>, includeStartOfText: Boolean): String {
        val sb = StringBuilder()
        if (includeStartOfText) sb.append("<|startoftext|>")

        // System prompt first
        sb.append("<|im_start|>system\n")
        sb.append(SYSTEM_PROMPT)
        sb.append("<|im_end|>\n")

        // Then conversation history
        for (msg in messages) {
            sb.append("<|im_start|>").append(msg.role).append("\n")
            sb.append(msg.content).append("<|im_end|>\n")
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildZaya(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("System: ").append(SYSTEM_PROMPT).append("\n\n")
        for (msg in messages) {
            val tag = if (msg.role == "user") "User" else "Assistant"
            sb.append(tag).append(": ").append(msg.content).append("\n")
        }
        sb.append("Assistant:")
        return sb.toString()
    }

    fun unloadModel() { try { freeModel() } catch (_: Exception) {} }
}
