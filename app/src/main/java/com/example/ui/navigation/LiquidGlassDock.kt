package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.ui.theme.RetroTheme

/**
 * Modern Liquid Glass Dock navigation capsule inspired by fluid glassmorphism.
 * Features:
 * - Floating translucent glass pill with curved edges & specular rim reflection
 * - Dynamic active indicator capsule with soft glow
 * - Real-time notification badges for Focus timer, pending Tasks, and Shield status
 * - Quick "More Apps" launcher sheet for secondary utilities (Cards, Mono, Vault, Guide)
 * - Minimum 48dp touch targets and tactile haptic clicks
 */
@Composable
fun LiquidGlassDock(
    currentScreen: RetroScreen,
    onNavigate: (RetroScreen) -> Unit,
    modifier: Modifier = Modifier,
    activeTasksCount: Int = 0,
    isFocusTimerRunning: Boolean = false,
    isShieldActive: Boolean = false,
    activeHabitsPending: Int = 0,
    glassIntensity: Float = 0.85f
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    var showMoreMenu by remember { mutableStateOf(false) }

    // Primary items displayed directly in the pill dock
    val primaryDockTabs = listOf(
        DockItem(RetroScreen.FOCUS, "Focus", Icons.Default.Timer, badge = if (isFocusTimerRunning) "●" else null),
        DockItem(RetroScreen.TASKS, "Tasks", Icons.Default.CheckBox, badge = if (activeTasksCount > 0) activeTasksCount.toString() else null),
        DockItem(RetroScreen.HABITS, "Habits", Icons.Default.CalendarMonth, badge = if (activeHabitsPending > 0) activeHabitsPending.toString() else null),
        DockItem(RetroScreen.MONEY, "Money", Icons.Default.Paid),
        DockItem(RetroScreen.SHIELD, "Shield", Icons.Default.Shield, badge = if (isShieldActive) "ON" else null),
        DockItem(RetroScreen.STATS, "Stats", Icons.Default.BarChart),
        DockItem(RetroScreen.SETTINGS, "Config", Icons.Default.Settings)
    )

    // Secondary items accessible via the "+ More" glass popover
    val secondaryTabs = listOf(
        DockItem(RetroScreen.FLASHCARDS, "Cards", Icons.Default.Style),
        DockItem(RetroScreen.MONO, "Mono", Icons.Default.Terminal),
        DockItem(RetroScreen.VAULT, "Vault", Icons.Default.LockOpen),
        DockItem(RetroScreen.GUIDE, "Guide", Icons.Default.MenuBook)
    )

    val isCurrentScreenInSecondary = secondaryTabs.any { it.screen == currentScreen }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Floating "More Apps" secondary glass tray
        AnimatedVisibility(
            visible = showMoreMenu,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.92f),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .widthIn(max = 420.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.5f),
                        ambientColor = Color.Black.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                palette.surface.copy(alpha = 0.88f * glassIntensity),
                                palette.background.copy(alpha = 0.94f * glassIntensity)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    secondaryTabs.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .testTag("liquid_dock_secondary_${item.screen.name.lowercase()}")
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) palette.textAccent.copy(alpha = 0.22f) else Color.Transparent
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    RetroHaptics.performClick(context)
                                    onNavigate(item.screen)
                                    showMoreMenu = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) palette.textAccent else palette.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                color = if (isSelected) palette.textAccent else palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Main Liquid Glass Capsule
        Box(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.55f),
                    ambientColor = Color.Black.copy(alpha = 0.40f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.surface.copy(alpha = 0.82f * glassIntensity),
                            palette.background.copy(alpha = 0.90f * glassIntensity)
                        )
                    )
                )
                .border(
                    width = 1.3.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            // Internal specular sheen line at the upper curvature
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            ) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.70f),
                            Color.White.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(40f, 1f),
                    end = Offset(size.width - 40f, 1f),
                    strokeWidth = 2f
                )
            }

            // Dock Navigation Items Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                primaryDockTabs.forEach { tab ->
                    val isSelected = currentScreen == tab.screen
                    LiquidDockTabItem(
                        item = tab,
                        isSelected = isSelected,
                        onClick = {
                            showMoreMenu = false
                            RetroHaptics.performClick(context)
                            onNavigate(tab.screen)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick More Apps toggle
                Box(
                    modifier = Modifier
                        .testTag("liquid_dock_more_button")
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (showMoreMenu || isCurrentScreenInSecondary) {
                                palette.textAccent.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            RetroHaptics.performClick(context)
                            showMoreMenu = !showMoreMenu
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More Utilities",
                        tint = if (showMoreMenu || isCurrentScreenInSecondary) palette.textAccent else palette.textSecondary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class DockItem(
    val screen: RetroScreen,
    val label: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
private fun LiquidDockTabItem(
    item: DockItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette

    // Animated indicator scaling & color
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "dock_scale"
    )

    val itemBgColor by animateColorAsState(
        targetValue = if (isSelected) palette.textAccent.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "dock_bg_color"
    )

    Box(
        modifier = modifier
            .testTag("liquid_dock_tab_${item.screen.name.lowercase()}")
            .height(52.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(itemBgColor)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) palette.textAccent.copy(alpha = 0.35f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.70f),
                    modifier = Modifier
                        .size(19.dp)
                        .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                )

                // Fluid Notification Badge (matches WhatsApp / Liquid Glass reference)
                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-7).dp)
                            .shadow(2.dp, CircleShape)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                // Vibrant green/accent badge matching reference image
                                Color(0xFF22C55E)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.badge,
                            color = Color.Black,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.label,
                color = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.65f),
                fontFamily = palette.fontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
