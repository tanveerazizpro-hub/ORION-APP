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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================
// COLORS
// ============================================
val DeepSpace = Color(0xFF0A0E1A)
val Surface1 = Color(0xFF0F1422)
val SurfaceContainer = Color(0xFF161C2C)
val SurfaceContainerHigh = Color(0xFF1B2235)
val TextPrimary = Color(0xFFE6EAF5)
val TextMuted = Color(0xFF8891B0)
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
    var isSidebarOpen by remember { mutableStateOf(false) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarOpen) 300.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sidebarWidth"
    )

    var currentTier by remember { mutableStateOf(Tier.PRIME) }
    var extendedMode by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var greeting by remember { mutableStateOf("What should we focus on?") }

    val sessions = listOf(
        ChatSession("Building Guardian App"),
        ChatSession("O.R.I.O.N. UI Mockup"),
        ChatSession("Android Local LLM"),
        ChatSession("Physics — Forces"),
        ChatSession("Chemistry — Matter"),
        ChatSession("Definition of a Queen")
    )

    Row(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        // Adaptive Sidebar (Pushes content, does not overlay)
        Box(modifier = Modifier.width(sidebarWidth).fillMaxHeight()) {
            if (sidebarWidth > 0.dp) {
                SidebarContent(sessions, onClose = { isSidebarOpen = false })
            }
        }

        // Main Content Area
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
                        onNewChatClick = { greeting = "What should we focus on?" },
                        onTierSelected = { currentTier = it },
                        onExtendedToggle = { extendedMode = !extendedMode }
                    )
                },
                bottomBar = {
                    InputBar(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                val label = if (extendedMode) "${currentTier.display} Extended" else "Orion ${currentTier.display}"
                                greeting = "[$label] Mock response"
                                inputText = ""
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
                    EmptyState(greeting)
                }
            }
        }
    }
}

// ============================================
// TOP BAR (Main)
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

    // Smooth animation states for the hamburger menu
    val hamburgerAlpha by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "hamburgerAlpha"
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
        // 2-line hamburger menu to OPEN sidebar (Animated)
        Box(
            modifier = Modifier
                .width(48.dp)
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

        // Tier pill
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
                                onCheckedChange = { onExtendedToggle() }
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
fun SidebarContent(sessions: List<ChatSession>, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Sidebar Header
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

            // Gemini-style collapse icon to CLOSE sidebar (Top Right)
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
            Icon(
                Icons.Default.Settings,
                null,
                tint = TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ============================================
// EMPTY STATE (Pulsing Animation)
// ============================================
@Composable
fun EmptyState(greeting: String) {
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
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
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
            greeting,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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
