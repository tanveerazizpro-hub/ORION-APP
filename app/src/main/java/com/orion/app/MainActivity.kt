package com.orion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================
// COLORS
// ============================================
val DeepSpace = Color(0xFF0A0E1A)
val Surface1 = Color(0xFF0F1422)
val SurfaceContainer = Color(0xFF161C2C)
val SurfaceContainerHigh = Color(0xFF1B2235)
val TextPrimary = Color(0xFFE6EAF5)
val TextMuted = Color(0xFF8891B0)
val UserBubbleText = Color(0xFF0A0E1A)
val OrionPurple = Color(0xFF9B6BFF)
val OrionPink = Color(0xFFFF5C9E)
val OrionOrange = Color(0xFFFF8C42)
val StarRed = Color(0xFFFF5C7A)

val OrionGradient = Brush.linearGradient(listOf(OrionPurple, OrionPink, OrionOrange))

// ============================================
// DATA
// ============================================
enum class Tier(val display: String, val subtitle: String, val icon: ImageVector) {
    SWIFT("Swift", "Instant answers", Icons.Default.Bolt),
    CORE("Core", "Balanced reasoning", Icons.Default.Memory),
    PRIME("Prime", "High intelligence", Icons.Default.LocalFireDepartment),
    ULTRA("Ultra", "Maximum knowledge", Icons.Default.Star)
}

data class ChatSession(val title: String)

// ============================================
// THEME
// ============================================
@Composable
fun OrionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = OrionPurple,
            background = DeepSpace,
            surface = Surface1,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        content = content
    )
}

// ============================================
// 4-POINT SPARKLE
// ============================================
fun DrawScope.drawFourPointSparkle(center: Offset, size: Float, brush: Brush) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        cubicTo(
            center.x + size * 0.15f, center.y - size * 0.15f,
            center.x + size * 0.15f, center.y - size * 0.15f,
            center.x + size, center.y
        )
        cubicTo(
            center.x + size * 0.15f, center.y + size * 0.15f,
            center.x + size * 0.15f, center.y + size * 0.15f,
            center.x, center.y + size
        )
        cubicTo(
            center.x - size * 0.15f, center.y + size * 0.15f,
            center.x - size * 0.15f, center.y + size * 0.15f,
            center.x - size, center.y
        )
        cubicTo(
            center.x - size * 0.15f, center.y - size * 0.15f,
            center.x - size * 0.15f, center.y - size * 0.15f,
            center.x, center.y - size
        )
        close()
    }
    drawPath(path, brush)
}

@Composable
fun SparkleStar(modifier: Modifier = Modifier, size: Int = 72) {
    Canvas(modifier = modifier.size(size.dp)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f
        drawFourPointSparkle(
            center = center,
            size = radius,
            brush = Brush.linearGradient(listOf(OrionPurple, OrionPink, OrionOrange))
        )
    }
}

// ============================================
// MAIN
// ============================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrionTheme { OrionApp() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrionApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        LlamaEngine.configureLogging(context)
        ModelManager.init(context)
    }

    var showSettings by remember { mutableStateOf(false) }
    var isSidebarOpen by remember { mutableStateOf(false) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarOpen) 300.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sidebarWidth"
    )

    var currentTier by remember { mutableStateOf(Tier.PRIME) }
    var extendedMode by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf("What should we focus on?") }
    var conversation by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var streamingText by remember { mutableStateOf("") }
    var isModelLoading by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(currentTier, ModelManager.tierAssignments.value) {
        val assigned = ModelManager.tierAssignments.value[currentTier]
        if (assigned == null) {
            isModelLoading = false
            return@LaunchedEffect
        }
        val path = ModelManager.getAssignedModelPath(context, currentTier)
        if (path == null) {
            statusMessage = "Assigned model '$assigned' not found"
            isModelLoading = false
            return@LaunchedEffect
        }
        isModelLoading = true
        statusMessage = "Loading ${currentTier.display}..."
        val error = LlamaEngine.loadModelAsync(context, path)
        isModelLoading = false
        statusMessage = error ?: "What should we focus on?"
    }

    val sessions = emptyList<ChatSession>()

    if (showSettings) {
        SettingsScreen(onClose = { showSettings = false })
        return
    }

    Row(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        Box(modifier = Modifier.width(sidebarWidth).fillMaxHeight()) {
            if (sidebarWidth > 0.dp) {
                SidebarContent(
                    sessions = sessions,
                    onClose = { isSidebarOpen = false },
                    onOpenSettings = {
                        isSidebarOpen = false
                        showSettings = true
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Scaffold(
                containerColor = DeepSpace,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopBar(
                        currentTier = currentTier,
                        extendedMode = extendedMode,
                        isSidebarOpen = isSidebarOpen,
                        onMenuClick = { isSidebarOpen = true },
                        onNewChatClick = {
                            conversation = emptyList()
                            streamingText = ""
                            statusMessage = "What should we focus on?"
                        },
                        onTierSelected = { newTier ->
                            if (newTier != currentTier) {
                                currentTier = newTier
                                conversation = emptyList()
                                streamingText = ""
                            }
                        },
                        onExtendedToggle = { extendedMode = !extendedMode }
                    )
                },
                bottomBar = {
                    InputBar(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onSend = {
                            val prompt = inputText.trim()
                            if (prompt.isEmpty() || isGenerating || isModelLoading) return@InputBar

                            val assigned = ModelManager.tierAssignments.value[currentTier]
                            if (assigned == null) {
                                statusMessage = "No model assigned to ${currentTier.display}. Open Settings → assign a model."
                                return@InputBar
                            }

                            keyboardController?.hide()
                            focusManager.clearFocus()

                            inputText = ""
                            val updatedConversation = conversation + ChatMessage("user", prompt)
                            conversation = updatedConversation
                            streamingText = ""
                            isGenerating = true
                            statusMessage = ""

                            val maxTokens = if (extendedMode) 4096 else 2048
                            scope.launch {
                                // Buffer tokens off the UI thread, flush to Compose state every ~80ms.
                                // This avoids re-composing the whole tree per token, which was
                                // fighting the inference engine for CPU cycles.
                                val buffer = StringBuilder()
                                val lock = Any()
                                var finished = false

                                val flusher = launch(Dispatchers.Main) {
                                    while (!finished) {
                                        delay(80)
                                        val snapshot = synchronized(lock) { buffer.toString() }
                                        if (snapshot.isNotEmpty()) {
                                            streamingText = snapshot
                                        }
                                    }
                                }

                                try {
                                    val fullResult = LlamaEngine.generateStreamingAsync(
                                        messages = updatedConversation,
                                        modelName = assigned,
                                        maxTokens = maxTokens,
                                        temp = 0.8f,
                                        onToken = { piece ->
                                            synchronized(lock) { buffer.append(piece) }
                                        }
                                    )
                                    finished = true
                                    flusher.cancel()

                                    val cleaned = cleanMarkdown(fullResult.trim())
                                        .ifEmpty { "(empty response)" }
                                    conversation = conversation + ChatMessage("assistant", cleaned)
                                    streamingText = ""
                                } catch (e: Exception) {
                                    finished = true
                                    flusher.cancel()
                                    conversation = conversation + ChatMessage(
                                        "assistant",
                                        "Error: ${e.message ?: "unknown"}"
                                    )
                                    streamingText = ""
                                } finally {
                                    isGenerating = false
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(DeepSpace)
                ) {
                    ChatArea(
                        conversation = conversation,
                        streamingText = streamingText,
                        isGenerating = isGenerating || isModelLoading,
                        statusMessage = statusMessage
                    )
                }
            }
        }
    }
}

fun cleanMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("^\\s*-\\s+", RegexOption.MULTILINE), "• ")
}

// ============================================
// TOP BAR
// ============================================
@Composable
fun TopBar(
    currentTier: Tier,
    extendedMode: Boolean,
    isSidebarOpen: Boolean,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onTierSelected: (Tier) -> Unit,
    onExtendedToggle: () -> Unit
) {
    var dropdownOpen by remember { mutableStateOf(false) }

    val hamburgerAlpha by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hamburgerAlpha"
    )
    val hamburgerWidth by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else 48.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hamburgerWidth"
    )
    val hamburgerScale by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0.8f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hamburgerScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(hamburgerWidth)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .alpha(hamburgerAlpha)
                    .scale(hamburgerScale)
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    val lineGap = 8.dp.toPx()
                    val centerY = size.height / 2f
                    val startX = 3.dp.toPx()
                    val endX = size.width - 3.dp.toPx()
                    drawLine(
                        color = TextPrimary,
                        start = Offset(startX, centerY - lineGap / 2),
                        end = Offset(endX, centerY - lineGap / 2),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = TextPrimary,
                        start = Offset(startX, centerY + lineGap / 2),
                        end = Offset(endX, centerY + lineGap / 2),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(SurfaceContainerHigh)
                    .clickable { dropdownOpen = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OrionGradient)
                )
                Text(
                    text = if (extendedMode) "${currentTier.display} Extended" else "Orion ${currentTier.display}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = dropdownOpen,
                onDismissRequest = { dropdownOpen = false },
                modifier = Modifier.background(SurfaceContainer)
            ) {
                Tier.entries.forEach { tier ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    tier.icon,
                                    null,
                                    tint = if (tier == currentTier) OrionPurple else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(tier.display, color = TextPrimary, fontSize = 14.sp)
                                    Text(tier.subtitle, color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        },
                        onClick = {
                            onTierSelected(tier)
                            dropdownOpen = false
                        }
                    )
                }
                HorizontalDivider(color = Color(0x1FFFFFFF))
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInFull,
                                null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Extended Mode", color = TextPrimary, fontSize = 14.sp)
                                Text("Longer context window", color = TextMuted, fontSize = 12.sp)
                            }
                            Switch(
                                checked = extendedMode,
                                onCheckedChange = null
                            )
                        }
                    },
                    onClick = { onExtendedToggle() }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onNewChatClick) {
            Icon(
                Icons.Outlined.Edit,
                "New chat",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(OrionPurple, OrionPink))),
            contentAlignment = Alignment.Center
        ) {
            Text("T", color = DeepSpace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ============================================
// SIDEBAR
// ============================================
@Composable
fun SidebarContent(
    sessions: List<ChatSession>,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp, 20.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "O.R.I.O.N.",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val stroke = 2.dp.toPx()
                    val rectSize = 20.dp.toPx()
                    val left = (size.width - rectSize) / 2f
                    val top = (size.height - rectSize) / 2f
                    val corner = 5.dp.toPx()
                    val path = Path().apply {
                        moveTo(left + corner, top)
                        lineTo(left + rectSize - corner, top)
                        quadraticBezierTo(left + rectSize, top, left + rectSize, top + corner)
                        lineTo(left + rectSize, top + rectSize - corner)
                        quadraticBezierTo(left + rectSize, top + rectSize, left + rectSize - corner, top + rectSize)
                        lineTo(left + corner, top + rectSize)
                        quadraticBezierTo(left, top + rectSize, left, top + rectSize - corner)
                        lineTo(left, top + corner)
                        quadraticBezierTo(left, top, left + corner, top)
                        close()
                    }
                    drawPath(path, color = TextPrimary, style = Stroke(width = stroke))
                    drawLine(
                        color = TextPrimary,
                        start = Offset(left + rectSize * 0.35f, top + 2.dp.toPx()),
                        end = Offset(left + rectSize * 0.35f, top + rectSize - 2.dp.toPx()),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(SurfaceContainerHigh)
                .clickable { onClose() }
                .padding(20.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Outlined.Edit,
                null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "New chat",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            items(sessions) { session ->
                Text(
                    session.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onClose() }
                        .padding(20.dp, 11.dp)
                )
            }
        }

        HorizontalDivider(color = Color(0x14FFFFFF))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 14.dp, 20.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(OrionPurple, OrionPink))),
                contentAlignment = Alignment.Center
            ) {
                Text("T", color = DeepSpace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Tanveer Aziz",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text("PRO", color = TextMuted, fontSize = 12.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Default.Settings,
                    "Settings",
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ============================================
// CHAT AREA
// ============================================
@Composable
fun ChatArea(
    conversation: List<ChatMessage>,
    streamingText: String,
    isGenerating: Boolean,
    statusMessage: String
) {
    val isEmpty = conversation.isEmpty() && streamingText.isEmpty() && !isGenerating

    if (isEmpty) {
        WelcomeScreen(statusMessage)
    } else {
        val scrollState = rememberScrollState()

        // Instant scroll (not animated) — cheap on CPU. Animations here were
        // scheduling frame-clock work 12x/sec, competing with inference.
        LaunchedEffect(conversation.size, streamingText.length) {
            scrollState.scrollTo(scrollState.maxValue)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            conversation.forEach { msg ->
                MessageBubble(msg)
            }

            if (isGenerating) {
                val liveText = streamingText.ifEmpty { "…" }
                MessageBubble(ChatMessage("assistant", liveText))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun WelcomeScreen(statusMessage: String) {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                StarRed.copy(alpha = glowAlpha),
                                OrionPurple.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
            SparkleStar(modifier = Modifier, size = 90)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            statusMessage.ifEmpty { "What should we focus on?" },
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    val bubbleBrush = if (isUser)
        Brush.linearGradient(listOf(OrionPurple, OrionPink))
    else
        Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(bubbleBrush)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.content,
                color = if (isUser) UserBubbleText else TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// ============================================
// INPUT BAR
// ============================================
@Composable
fun InputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(SurfaceContainer)
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(999.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Add, "Add", tint = TextMuted)
            }

            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = null,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrionPurple,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            IconButton(onClick = { }) {
                Icon(Icons.Default.Mic, "Mic", tint = TextMuted)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OrionPurple.copy(alpha = 0.15f))
                    .border(1.dp, OrionPurple.copy(alpha = 0.4f), CircleShape)
                    .clickable { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowUpward, "Send", tint = OrionPurple)
            }
        }
    }
}

@Preview
@Composable
fun PreviewOrion() {
    OrionTheme { OrionApp() }
}
