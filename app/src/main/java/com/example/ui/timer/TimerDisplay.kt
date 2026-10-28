package com.example.ui.timer

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TimerStyleId
import com.example.ui.theme.RetroTheme
import java.util.Locale

enum class FocusState {
    READY,
    RUNNING,
    PAUSED,
    COMPLETED
}

@Composable
fun TimerDisplay(
    remainingSeconds: Int,
    totalSeconds: Int,
    state: FocusState,
    timerStyle: TimerStyleId,
    onLongPressTimer: () -> Unit = {},
    onClickTimer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val progress = if (totalSeconds > 0) {
        1.0f - (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .testTag("timer_display_container")
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClickTimer
            ),
        contentAlignment = Alignment.Center
    ) {
        when (timerStyle) {
            TimerStyleId.CLASSIC -> {
                ClassicTimerView(
                    formattedTime = formattedTime,
                    state = state,
                    progress = progress,
                    palette = palette
                )
            }
            TimerStyleId.CRT -> {
                CrtTimerView(
                    formattedTime = formattedTime,
                    state = state,
                    progress = progress,
                    palette = palette
                )
            }
            TimerStyleId.TERMINAL -> {
                TerminalTimerView(
                    formattedTime = formattedTime,
                    state = state,
                    progress = progress,
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalSeconds,
                    palette = palette
                )
            }
            TimerStyleId.MINIMAL -> {
                MinimalTimerView(
                    formattedTime = formattedTime,
                    state = state,
                    progress = progress,
                    palette = palette
                )
            }
        }
    }
}

@Composable
private fun ClassicTimerView(
    formattedTime: String,
    state: FocusState,
    progress: Float,
    palette: com.example.ui.theme.RetroPalette
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(palette.surfaceBorder.copy(alpha = 0.3f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (state) {
                    FocusState.READY -> "FOCUS READY"
                    FocusState.RUNNING -> "SESSION ACTIVE"
                    FocusState.PAUSED -> "PAUSED"
                    FocusState.COMPLETED -> "COMPLETED"
                },
                color = if (state == FocusState.RUNNING) palette.textAccent else palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-contrast clean digital timer digits
        Text(
            text = formattedTime,
            color = palette.textPrimary,
            fontFamily = palette.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Smooth segmented progress bar
        SmoothProgressBar(progress = progress, palette = palette)
    }
}

@Composable
private fun CrtTimerView(
    formattedTime: String,
    state: FocusState,
    progress: Float,
    palette: com.example.ui.theme.RetroPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(8.dp))
            .background(palette.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CH: 01 // FOCUS",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = when (state) {
                        FocusState.READY -> "[READY]"
                        FocusState.RUNNING -> "[ACTIVE ●]"
                        FocusState.PAUSED -> "[PAUSED]"
                        FocusState.COMPLETED -> "[DONE]"
                    },
                    color = if (state == FocusState.RUNNING) palette.textAccent else palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formattedTime,
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 58.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            SmoothProgressBar(progress = progress, palette = palette)
        }
    }
}

@Composable
private fun TerminalTimerView(
    formattedTime: String,
    state: FocusState,
    progress: Float,
    remainingSeconds: Int,
    totalSeconds: Int,
    palette: com.example.ui.theme.RetroPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
            .background(palette.surface.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "$ demichrome --session active",
                color = palette.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Text(
                text = "> status: ${state.name.lowercase()} | elapsed: ${(totalSeconds - remainingSeconds) / 60}m",
                color = palette.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIME_REMAINING: ",
                    color = palette.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = formattedTime,
                    color = palette.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val percent = (progress * 100).toInt()
            Text(
                text = "PROGRESS: $percent%",
                color = palette.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            SmoothProgressBar(progress = progress, palette = palette)
        }
    }
}

@Composable
private fun MinimalTimerView(
    formattedTime: String,
    state: FocusState,
    progress: Float,
    palette: com.example.ui.theme.RetroPalette
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = formattedTime,
            color = palette.textPrimary,
            fontFamily = palette.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (state) {
                FocusState.READY -> "READY"
                FocusState.RUNNING -> "FOCUS"
                FocusState.PAUSED -> "PAUSED"
                FocusState.COMPLETED -> "DONE"
            },
            color = palette.textSecondary,
            fontFamily = palette.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        SmoothProgressBar(
            progress = progress,
            palette = palette,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

/**
 * High-performance, pixel-crisp segmented progress bar.
 * Avoids font glyph jitter or layout wrapping across all Android screen densities.
 */
@Composable
fun SmoothProgressBar(
    progress: Float,
    palette: com.example.ui.theme.RetroPalette,
    modifier: Modifier = Modifier,
    totalSegments: Int = 16
) {
    val activeSegments = (progress * totalSegments).toInt().coerceIn(0, totalSegments)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSegments) {
            val isActive = i < activeSegments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isActive) 6.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) palette.textPrimary
                        else palette.surfaceBorder.copy(alpha = 0.5f)
                    )
            )
        }
    }
}
