package com.orion.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.io.File

object ModelManager {

    private const val PREFS = "orion_prefs"

    // Live observable list of model filenames (e.g. "MiniCPM5-2B-Q4_K_M.gguf")
    val availableModels = mutableStateListOf<String>()

    // Live observable mapping of tier -> assigned model filename
    val tierAssignments = mutableStateOf<Map<Tier, String>>(emptyMap())

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        refreshAvailableModels(context)
        loadAssignments()
    }

    /** Rescan the models folder. Call after import or delete. */
    fun refreshAvailableModels(context: Context) {
        val dir = LlamaEngine.getModelsDir(context)
        val files = dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".gguf", ignoreCase = true) }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        availableModels.clear()
        availableModels.addAll(files)
    }

    /** Persist the user's tier -> model choice. */
    fun assignModel(tier: Tier, modelFileName: String) {
        val updated = tierAssignments.value.toMutableMap()
        updated[tier] = modelFileName
        tierAssignments.value = updated
        prefs.edit().putString("tier_${tier.name}", modelFileName).apply()
    }

    /** Remove a tier assignment. */
    fun clearAssignment(tier: Tier) {
        val updated = tierAssignments.value.toMutableMap()
        updated.remove(tier)
        tierAssignments.value = updated
        prefs.edit().remove("tier_${tier.name}").apply()
    }

    private fun loadAssignments() {
        val map = mutableMapOf<Tier, String>()
        for (tier in Tier.entries) {
            val saved = prefs.getString("tier_${tier.name}", null)
            if (saved != null) map[tier] = saved
        }
        tierAssignments.value = map
    }

    /** Absolute path of the .gguf assigned to a tier, or null. */
    fun getAssignedModelPath(context: Context, tier: Tier): String? {
        val name = tierAssignments.value[tier] ?: return null
        val file = File(LlamaEngine.getModelsDir(context), name)
        return if (file.exists()) file.absolutePath else null
    }
}
