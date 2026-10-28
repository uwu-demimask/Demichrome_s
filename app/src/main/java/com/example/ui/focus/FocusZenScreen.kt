package com.example.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroIconButton
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import com.example.ui.timer.TimerDisplay

@Composable
fun FocusZenScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val state by viewModel.state.collectAsState()
    val timerStyle by viewModel.userPreferences.timerStyle.collectAsState()

    Box(
        modifier = modifier
            .testTag("focus_zen_screen")
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top exit bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ ZEN FOCUS ENGAGED ]",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
                RetroIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Exit Focus Mode",
                    onClick = { viewModel.exitFocusMode() },
                    testTag = "btn_exit_zen"
                )
            }

            // Central large timer
            TimerDisplay(
                remainingSeconds = remainingSeconds,
                totalSeconds = targetMinutes * 60,
                state = state,
                timerStyle = timerStyle,
                onClickTimer = {
                    if (state == FocusState.RUNNING) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                }
            )

            // Minimal lower controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state == FocusState.RUNNING) {
                    RetroButton(
                        text = "PAUSE",
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier.weight(1f),
                        testTag = "zen_btn_pause"
                    )
                } else {
                    RetroButton(
                        text = "START",
                        onClick = { viewModel.startTimer() },
                        isPrimary = true,
                        modifier = Modifier.weight(1f),
                        testTag = "zen_btn_start"
                    )
                }

                RetroButton(
                    text = "RESET",
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier.weight(1f),
                    testTag = "zen_btn_reset"
                )
            }
        }
    }
}
