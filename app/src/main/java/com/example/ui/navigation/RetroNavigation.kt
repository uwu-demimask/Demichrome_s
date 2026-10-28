package com.example.ui.navigation

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.ui.theme.RetroTheme

enum class RetroScreen(val title: String, val icon: ImageVector) {
    HOME("HOME", Icons.Default.Home),
    FOCUS("FOCUS", Icons.Default.Timer),
    TASKS("TASKS", Icons.Default.CheckBox),
    HABITS("HABITS", Icons.Default.CalendarMonth),
    FLASHCARDS("CARDS", Icons.Default.Style),
    MONEY("MONEY", Icons.Default.Paid),
    SHIELD("SHIELD", Icons.Default.Shield),
    STATS("STATS", Icons.Default.BarChart),
    MONO("MONO", Icons.Default.Terminal),
    SETTINGS("CONFIG", Icons.Default.Settings),
    VAULT("VAULT", Icons.Default.Terminal),
    ICON_VAULT("ICONS", Icons.Default.LockOpen),
    GUIDE("GUIDE", Icons.Default.MenuBook)
}

/**
 * Dedicated Bottom Navigation for Demichrome OS.
 * Focuses purely on the productivity suite: Focus, Tasks, Habits, Cards, Money, Shield, Stats, Settings.
 */
@Composable
fun DemichromeOSBottomNavigation(
    currentScreen: RetroScreen,
    onNavigate: (RetroScreen) -> Unit,
    statusText: String = "DEMICHROME OS READY",
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .background(palette.background)
            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.8f))
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // Status strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(palette.textAccent)
                )
                Text(
                    text = statusText.take(32),
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "DEMICHROME OS • v1.0",
                color = palette.textSecondary.copy(alpha = 0.5f),
                fontFamily = palette.fontFamily,
                fontSize = 9.sp
            )
        }

        // Primary Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                RetroScreen.FOCUS,
                RetroScreen.TASKS,
                RetroScreen.HABITS,
                RetroScreen.FLASHCARDS,
                RetroScreen.MONEY,
                RetroScreen.SHIELD,
                RetroScreen.STATS,
                RetroScreen.SETTINGS
            )

            tabs.forEach { screen ->
                val isSelected = currentScreen == screen
                Box(
                    modifier = Modifier
                        .testTag("os_nav_tab_${screen.name.lowercase()}")
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) palette.surfaceBorder.copy(alpha = 0.35f) else palette.background
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) palette.textAccent.copy(alpha = 0.4f) else androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            RetroHaptics.performClick(context)
                            onNavigate(screen)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            color = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.6f),
                            fontFamily = palette.fontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 7.5.sp,
                            letterSpacing = 0.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RetroBottomNavigation(
    currentScreen: RetroScreen,
    onNavigate: (RetroScreen) -> Unit,
    statusText: String = "DEMICHROME READY",
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .background(palette.background)
            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.8f))
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // Status strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(palette.textAccent)
                )
                Text(
                    text = statusText.take(32),
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DEMICHROME OS • PMAK",
                    color = palette.textSecondary.copy(alpha = 0.5f),
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
            }
        }

        // Primary Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                RetroScreen.FOCUS,
                RetroScreen.TASKS,
                RetroScreen.HABITS,
                RetroScreen.FLASHCARDS,
                RetroScreen.MONEY,
                RetroScreen.SHIELD,
                RetroScreen.STATS,
                RetroScreen.SETTINGS
            )

            tabs.forEach { screen ->
                val isSelected = currentScreen == screen
                Box(
                    modifier = Modifier
                        .testTag("nav_tab_${screen.name.lowercase()}")
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) palette.surfaceBorder.copy(alpha = 0.35f) else palette.background
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) palette.textAccent.copy(alpha = 0.4f) else androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            RetroHaptics.performClick(context)
                            onNavigate(screen)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            color = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.6f),
                            fontFamily = palette.fontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 7.5.sp,
                            letterSpacing = 0.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}
