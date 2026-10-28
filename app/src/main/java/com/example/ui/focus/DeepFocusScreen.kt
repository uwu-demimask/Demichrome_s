package com.example.ui.focus

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroOutlineButton
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import kotlinx.coroutines.delay

/**
 * Deep Focus Mode — High-immersion distraction-free digital sanctuary.
 * Features: Fullscreen distraction shield, 3-second hold exit lock, ambient binaural synth, PiP launcher.
 */
@Composable
fun DeepFocusScreen(
    viewModel: FocusViewModel,
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val palette = RetroTheme.palette

    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val timerState by viewModel.state.collectAsState()
    val ambientSound by viewModel.userPreferences.ambientSound.collectAsState()
    val strictLock by viewModel.userPreferences.strictExitLock.collectAsState()

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)

    val totalSecs = (targetMinutes * 60).coerceAtLeast(1)
    val elapsed = (totalSecs - remainingSeconds).coerceIn(0, totalSecs)
    val progress = (elapsed.toFloat() / totalSecs).coerceIn(0f, 1f)

    // Unlock hold tracking
    var isHoldingUnlock by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var showJotNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isHoldingUnlock) {
        if (isHoldingUnlock) {
            val totalSteps = 30 // 3 seconds at 100ms
            for (step in 1..totalSteps) {
                delay(100)
                if (!isHoldingUnlock) break
                holdProgress = step.toFloat() / totalSteps
                if (step % 5 == 0) {
                    RetroHaptics.performClick(context)
                }
            }
            if (isHoldingUnlock && holdProgress >= 1f) {
                RetroHaptics.performHeavyClick(context)
                viewModel.setDeepFocusMode(false)
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Status & Ambient Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (timerState == FocusState.RUNNING) palette.textAccent else palette.textSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEEP FOCUS LOCK",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Float to PiP Icon
                    IconButton(
                        onClick = onEnterPip,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("deep_focus_pip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Picture in Picture",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "> DISTRACTION SHIELD ENGAGED",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            // CENTER: Big Monolithic Timer & Intention
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "[ SESSION IN PROGRESS ]",
                    color = palette.textSecondary.copy(alpha = 0.7f),
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formattedTime,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 68.sp,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.textAccent,
                    trackColor = palette.surfaceBorder.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(progress * 100).toInt()}% COMPLETED  |  ${elapsed / 60}m ELAPSED",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            // BOTTOM CONTROLS & AMBIENT AUDIO SELECTOR
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ambient Audio Switcher Chips
                RetroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AMBIENT SOUND GENERATOR",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = palette.textAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val options = listOf(
                                RetroAudioSynthesizer.AmbientSoundType.NONE to "MUTE",
                                RetroAudioSynthesizer.AmbientSoundType.BINAURAL_432HZ to "432Hz BINAURAL",
                                RetroAudioSynthesizer.AmbientSoundType.WARM_BROWN_NOISE to "BROWN NOISE",
                                RetroAudioSynthesizer.AmbientSoundType.MECHANICAL_TICK to "TICK"
                            )

                            options.forEach { (type, label) ->
                                val isSelected = ambientSound == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) palette.buttonActiveBackground else palette.surfaceBorder.copy(alpha = 0.3f))
                                        .clickable {
                                            viewModel.userPreferences.setAmbientSound(type)
                                            if (timerState == FocusState.RUNNING) {
                                                RetroAudioSynthesizer.startAmbient(
                                                    type,
                                                    volume = 0.25f,
                                                    coroutineScope = coroutineScope
                                                )
                                            }
                                            RetroHaptics.performClick(context)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) palette.buttonActiveText else palette.textPrimary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Play / Pause / Reset Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timerState == FocusState.RUNNING) {
                        RetroOutlineButton(
                            text = "PAUSE",
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Pause,
                            testTag = "deep_focus_pause_btn"
                        )
                    } else {
                        RetroButton(
                            text = "RESUME",
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.PlayArrow,
                            isPrimary = true,
                            testTag = "deep_focus_resume_btn"
                        )
                    }

                    RetroOutlineButton(
                        text = "RESET",
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.weight(0.7f),
                        icon = Icons.Default.Refresh,
                        testTag = "deep_focus_reset_btn"
                    )

                    RetroOutlineButton(
                        text = "JOT",
                        onClick = { showJotNoteDialog = true },
                        modifier = Modifier.weight(0.6f),
                        icon = Icons.Default.EditNote,
                        testTag = "deep_focus_jot_note_btn"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // STRICT EXIT LOCK BUTTON (Hold for 3s to break deep focus)
                if (strictLock) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                            .background(palette.surface)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHoldingUnlock = true
                                        tryAwaitRelease()
                                        isHoldingUnlock = false
                                    }
                                )
                            }
                            .testTag("strict_exit_hold_button"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Filling progress gauge on hold
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(holdProgress)
                                    .fillMaxSize()
                                    .background(palette.textAccent.copy(alpha = 0.35f))
                            )
                        }

                        // Centered text label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (holdProgress >= 0.8f) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHoldingUnlock) "HOLDING... ${(holdProgress * 100).toInt()}%" else "HOLD 3s TO EXIT DEEP FOCUS",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    RetroOutlineButton(
                        text = "EXIT DEEP FOCUS",
                        onClick = { viewModel.setDeepFocusMode(false) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "direct_exit_deep_btn"
                    )
                }
            }
        }

        if (showJotNoteDialog) {
            var quickText by remember { mutableStateOf("") }
            var quickTag by remember { mutableStateOf("IDEA") }
            val tags = listOf("IDEA", "TASK", "INSIGHT", "THOUGHT")

            Dialog(onDismissRequest = { showJotNoteDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, palette.border, RoundedCornerShape(8.dp))
                        .background(palette.surface)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "MINIMALIST NOTE // QUICK JOT",
                            color = palette.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tags.forEach { tag ->
                                RetroChip(
                                    text = "#$tag",
                                    isSelected = quickTag == tag,
                                    onClick = { quickTag = tag },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = quickText,
                            onValueChange = { quickText = it },
                            placeholder = { Text("Fleeting thought...", fontSize = 12.sp, color = palette.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accent,
                                unfocusedBorderColor = palette.border,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroOutlineButton(
                                text = "CANCEL",
                                onClick = { showJotNoteDialog = false },
                                modifier = Modifier.weight(1f)
                            )
                            RetroButton(
                                text = "SAVE NOTE",
                                onClick = {
                                    if (quickText.isNotBlank()) {
                                        viewModel.saveMinimalistNote(quickText, quickTag)
                                        showJotNoteDialog = false
                                    }
                                },
                                isPrimary = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
