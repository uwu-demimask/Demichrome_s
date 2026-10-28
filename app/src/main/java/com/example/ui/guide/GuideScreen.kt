package com.example.ui.guide

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accessibility.SystemDisplayHelper
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import kotlinx.coroutines.launch

/**
 * Interactive User Handbook & Feature Guide for Demichrome.
 */
@Composable
fun GuideScreen(
    viewModel: FocusViewModel,
    onNavigateBack: () -> Unit,
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = RetroTheme.palette

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        RetroHaptics.performClick(context)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("guide_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "DEMICHROME HANDBOOK",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "> USER MANUAL & OPERATING GUIDE",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            IconButton(
                onClick = {
                    RetroHaptics.performClick(context)
                    onNavigateBack()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // CONTENT SECTIONS
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Philosophy
            item {
                GuideCard(
                    icon = Icons.Default.HelpOutline,
                    badge = "01 // PHILOSOPHY",
                    title = "The Demichrome Principle",
                    content = "Modern smartphones exploit hyper-saturated colors and variable reward loops to monopolize attention. Demichrome reclaims your focus through high-contrast retro computing, CRT/pixel aesthetics, and distraction-free workflows."
                )
            }

            // 2. Picture-in-Picture
            item {
                GuideCard(
                    icon = Icons.Default.PictureInPicture,
                    badge = "02 // MULTITASKING",
                    title = "Floating Picture-in-Picture (PiP)",
                    content = "Keep an eye on your focus countdown while studying, reading documents, or drafting notes.\n\n• Automatic PiP: When a timer is running, leaving the app automatically transitions into a floating retro PiP window.\n• Manual Float: Tap the PiP button in the timer header or Deep Focus screen.\n• Tap window to toggle Play/Pause directly."
                ) {
                    RetroOutlineButton(
                        text = "TEST PIP FLOATING WINDOW",
                        onClick = {
                            viewModel.startTimer()
                            onEnterPip()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.PictureInPicture,
                        testTag = "guide_test_pip_btn"
                    )
                }
            }

            // 3. Notification Shade Live Control
            item {
                GuideCard(
                    icon = Icons.Default.Notifications,
                    badge = "03 // PERSISTENCE",
                    title = "Notification Panel Live Controls",
                    content = "When your session starts, a battery-efficient foreground service updates your Android notification bar in real time.\n\n• Live countdown progress meter\n• Quick action buttons: Pause, Resume, Reset, and Toggle Deep Focus\n• Control sessions from lockscreen without unlocking your device"
                ) {
                    RetroOutlineButton(
                        text = "START SESSION & TEST NOTIFICATION",
                        onClick = {
                            viewModel.startTimer()
                            RetroHaptics.performClick(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Notifications,
                        testTag = "guide_test_notif_btn"
                    )
                }
            }

            // 4. Deep Focus Mode & Strict Lock
            item {
                GuideCard(
                    icon = Icons.Default.Lock,
                    badge = "04 // HIGH IMMERSION",
                    title = "Deep Focus Mode & Strict Lock",
                    content = "For intense study and deep work sprints:\n\n• Fullscreen Distraction Shield: Eliminates status bars and chrome.\n• 3-Second Hold Strict Lock: Requires holding the unlock button for 3 seconds with tactile feedback to break out, curbing impulse switches.\n• Ambient Audio Generator: Built-in 432Hz Binaural Theta waves, Soothing Brown Noise, and Mechanical Clockwork."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "ENGAGE DEEP FOCUS",
                            onClick = {
                                viewModel.startTimer()
                                viewModel.setDeepFocusMode(true)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Lock,
                            testTag = "guide_launch_deep_focus_btn"
                        )
                    }
                }
            }

            // 5. Ambient Focus Synthesizer
            item {
                GuideCard(
                    icon = Icons.Default.GraphicEq,
                    badge = "05 // RETRO AUDIO",
                    title = "Synthesizer Audio Engine",
                    content = "Monochrome generates 100% offline, zero-latency PCM audio waves directly using Android AudioTrack without loading heavy sound files.\n\n• 432Hz Binaural Carrier: Promotes alpha/theta brainwave focus\n• Warm Brown Noise: Masks background chatter and room noise\n• Vintage Mechanical Tick: Subtle temporal rhythm"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroOutlineButton(
                            text = "PLAY CHIME",
                            onClick = {
                                scope.launch { RetroAudioSynthesizer.playStartChirp() }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "guide_test_chime_btn"
                        )
                        RetroOutlineButton(
                            text = "TEST 432Hz",
                            onClick = {
                                RetroAudioSynthesizer.startAmbient(
                                    RetroAudioSynthesizer.AmbientSoundType.BINAURAL_432HZ,
                                    volume = 0.25f,
                                    coroutineScope = scope
                                )
                                scope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    RetroAudioSynthesizer.stopAmbient()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "guide_test_432hz_btn"
                        )
                    }
                }
            }

            // 6. System Grayscale Integration
            item {
                GuideCard(
                    icon = Icons.Default.Visibility,
                    badge = "06 // SYSTEM INTEGRATION",
                    title = "System-Wide Grayscale",
                    content = "Take distraction-free productivity beyond this app. Use Android's built-in Color Correction to render your entire phone in monochrome E-ink style."
                ) {
                    RetroOutlineButton(
                        text = "OPEN ANDROID GRAYSCALE SETTINGS",
                        onClick = {
                            SystemDisplayHelper.openAccessibilitySettings(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.SettingsSuggest,
                        testTag = "guide_open_grayscale_btn"
                    )
                }
            }

            // 7. Secret Vault & Easter Eggs
            item {
                GuideCard(
                    icon = Icons.Default.Terminal,
                    badge = "07 // EASTER EGGS",
                    title = "Secret Terminal & The Vault",
                    content = "• Triple Tap Header: Tap the 'MONOCHROME' header title 3 times rapidly to unlock the hidden Vault screen.\n• Terminal Commands: Type commands into the terminal in the Vault (e.g. 'matrix', 'crt', 'mono', 'zen', 'beep', 'stats', 'vault', 'help').\n• Icon Vault: Browse and export clean monochrome icons."
                )
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GuideCard(
    icon: ImageVector,
    badge: String,
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val palette = RetroTheme.palette

    RetroCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badge,
                        color = palette.accent,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                action()
            }
        }
    }
}
