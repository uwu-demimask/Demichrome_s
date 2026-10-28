package com.example.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState

/**
 * Dedicated Demichrome Blue & White layout for Picture-in-Picture floating mode.
 * Guaranteed visible contrast, never pitch-black, and allows 1-tap exit back to app.
 */
@Composable
fun FocusPipScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    onExitPip: () -> Unit = {}
) {
    val palette = RetroTheme.palette
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val state by viewModel.state.collectAsState()
    val isDeepFocus by viewModel.isDeepFocusActive.collectAsState()

    val totalSecs = (targetMinutes * 60).coerceAtLeast(1)
    val elapsed = (totalSecs - remainingSeconds).coerceIn(0, totalSecs)
    val progress = (elapsed.toFloat() / totalSecs).coerceIn(0f, 1f)

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    // Dynamically uses active Demichrome theme palette
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            palette.surfaceElevated,
            palette.surface
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .border(BorderStroke(1.5.dp, palette.border))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar with Return button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(palette.accent)
                    )
                    Text(
                        text = if (isDeepFocus) "DEEP LOCK" else "DEMICHROME",
                        color = palette.accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = when (state) {
                            FocusState.RUNNING -> "RUNNING"
                            FocusState.PAUSED -> "PAUSED"
                            FocusState.COMPLETED -> "DONE"
                            FocusState.READY -> "READY"
                        },
                        color = palette.textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Exit Miniplayer / return to main workspace button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(palette.accentSoft)
                            .border(BorderStroke(1.dp, palette.border), CircleShape)
                            .clickable { onExitPip() }
                            .testTag("pip_exit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Miniplayer",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Central Big Digit with applied theme textPrimary
            Text(
                text = timeFormatted,
                color = if (isDeepFocus) palette.accent else palette.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 32.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .clickable {
                        if (state == FocusState.RUNNING) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    }
                    .testTag("pip_timer_display")
            )

            // Progress Bar & Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.accent,
                    trackColor = palette.border
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.accent)
                            .clickable {
                                if (state == FocusState.RUNNING) {
                                    viewModel.pauseTimer()
                                } else {
                                    viewModel.startTimer()
                                }
                            }
                            .testTag("pip_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state == FocusState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Timer",
                            tint = palette.buttonActiveText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Reset button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceElevated)
                            .border(1.dp, palette.border, CircleShape)
                            .clickable { viewModel.resetTimer() }
                            .testTag("pip_reset_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
