package com.orion.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importProgress by ModelImporter.progress
    val isImporting by ModelImporter.isImporting
    val statusMessage by ModelImporter.statusMessage
    val importingFile by ModelImporter.importingFileName
    val models = ModelManager.availableModels
    val assignments = ModelManager.tierAssignments.value

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch { ModelImporter.importModel(context, uri) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                "Settings",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== TIER ASSIGNMENTS =====
            item {
                Text(
                    "TIER ASSIGNMENTS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(Tier.entries.toList(), key = { it.name }) { tier ->
                TierAssignmentRow(
                    tier = tier,
                    assignedModel = assignments[tier],
                    availableModels = models,
                    onAssign = { modelName ->
                        ModelManager.assignModel(tier, modelName)
                    },
                    onClear = {
                        ModelManager.clearAssignment(tier)
                    }
                )
            }

            // ===== IMPORT MODEL =====
            item {
                Text(
                    "IMPORT MODEL",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .clickable(enabled = !isImporting) {
                            picker.launch(arrayOf("*/*"))
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        null,
                        tint = OrionPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Choose .gguf file",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "File will be copied to app storage. Delete the original afterward to save space.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (isImporting || statusMessage.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .padding(20.dp)
                    ) {
                        Text(
                            importingFile.ifEmpty { statusMessage },
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        if (isImporting) {
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { importProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = OrionPurple,
                                trackColor = SurfaceContainerHigh
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${(importProgress * 100).toInt()}%",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ===== INSTALLED MODELS =====
            item {
                Text(
                    "INSTALLED MODELS (${models.size})",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            if (models.isEmpty()) {
                item {
                    Text(
                        "No models yet. Import a .gguf file above.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(models, key = { it }) { modelName ->
                    val file = File(LlamaEngine.getModelsDir(context), modelName)
                    val sizeMb = file.length() / (1024.0 * 1024.0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                modelName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "%.1f MB".format(sizeMb),
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = {
                            // Unassign from any tier that was using it
                            Tier.entries.forEach { t ->
                                if (ModelManager.tierAssignments.value[t] == modelName) {
                                    ModelManager.clearAssignment(t)
                                }
                            }
                            file.delete()
                            ModelManager.refreshAvailableModels(context)
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TierAssignmentRow(
    tier: Tier,
    assignedModel: String?,
    availableModels: List<String>,
    onAssign: (String) -> Unit,
    onClear: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainer)
                .clickable { menuOpen = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                tier.icon,
                null,
                tint = if (assignedModel != null) OrionPurple else TextMuted,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tier.display,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    assignedModel ?: "Not assigned — tap to choose",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            modifier = Modifier.background(SurfaceContainerHigh)
        ) {
            if (availableModels.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No models imported", color = TextMuted, fontSize = 13.sp) },
                    onClick = { menuOpen = false }
                )
            } else {
                availableModels.forEach { modelName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                modelName,
                                color = if (modelName == assignedModel) OrionPurple else TextPrimary,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onAssign(modelName)
                            menuOpen = false
                        }
                    )
                }
            }
            if (assignedModel != null) {
                DropdownMenuItem(
                    text = { Text("Remove assignment", color = OrionPink, fontSize = 13.sp) },
                    onClick = {
                        onClear()
                        menuOpen = false
                    }
                )
            }
        }
    }
}
