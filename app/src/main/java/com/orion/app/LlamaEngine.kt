package com.orion.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LlamaEngine {
    init {
        System.loadLibrary("orion")
    }

    external fun loadModel(modelPath: String): Boolean
    external fun generate(prompt: String, maxTokens: Int, temp: Float, threads: Int): String
    external fun freeModel()

    fun getModelsDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "models")
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

    /** Free any previously loaded model, then load the new one. Runs off the UI thread. */
    suspend fun loadModelAsync(path: String): Boolean = withContext(Dispatchers.IO) {
        try { freeModel() } catch (_: Exception) {}
        loadModel(path)
    }

    /** Format the prompt using the model's chat template, then generate. Runs off the UI thread. */
    suspend fun generateAsync(
        userMessage: String,
        modelName: String,
        maxTokens: Int,
        temp: Float
    ): String = withContext(Dispatchers.IO) {
        val formatted = applyChatTemplate(userMessage, modelName)
        generate(formatted, maxTokens, temp, 4)
    }

    /** Apply the correct chat template based on the model family. */
    private fun applyChatTemplate(userMessage: String, modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("lfm2") ->
                "<|startoftext|><|im_start|>user\n$userMessage<|im_end|>\n<|im_start|>assistant\n"
            lower.contains("qwen") ->
                "<|im_start|>user\n$userMessage<|im_end|>\n<|im_start|>assistant\n"
            lower.contains("minicpm") ->
                "<|im_start|>user\n$userMessage<|im_end|>\n<|im_start|>assistant\n"
            lower.contains("zaya") ->
                "User: $userMessage\nAssistant:"
            else -> userMessage
        }
    }

    fun unloadModel() {
        try { freeModel() } catch (_: Exception) {}
    }
}
