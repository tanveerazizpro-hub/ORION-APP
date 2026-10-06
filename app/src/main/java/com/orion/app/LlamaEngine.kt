package com.orion.app

import android.content.Context
import java.io.File

object LlamaEngine {
    init {
        System.loadLibrary("orion")
    }

    // Native functions (implemented in C++)
    external fun loadModel(modelPath: String): Boolean
    external fun generate(prompt: String, maxTokens: Int, temp: Float, threads: Int): String
    external fun freeModel()

    // Helper to get the folder where models are stored
    fun getModelsDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // List all .gguf models in the folder
    fun listModels(context: Context): List<String> {
        return getModelsDir(context).listFiles()
            ?.filter { it.extension == "gguf" }
            ?.map { it.name }
            ?: emptyList()
    }

    // Get the full path of a specific model
    fun getModelPath(context: Context, modelName: String): String {
        return File(getModelsDir(context), modelName).absolutePath
    }
}
