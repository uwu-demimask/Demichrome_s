package com.example.ui.mono

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.RetroThemeId
import com.example.data.TimerStyleId
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroHeader
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.components.RetroToggle
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme

@Composable
fun MonoWorkspaceScreen(
    viewModel: FocusViewModel,
    onNavigateToIconVault: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val quickNotes by viewModel.quickNotes.collectAsState()
    val themeId by viewModel.userPreferences.theme.collectAsState()
    val ambientSound by viewModel.userPreferences.ambientSound.collectAsState()
    val crtEffect by viewModel.userPreferences.crtEffect.collectAsState()
    val monochromeMode by viewModel.userPreferences.monochromeMode.collectAsState()
    val floatingNotchTimerEnabled by viewModel.floatingNotchTimerEnabled.collectAsState()

    var terminalInput by remember { mutableStateOf("") }
    var terminalOutput by remember { mutableStateOf("DEMICHROME MONO WORKSPACE v1.0\nType 'help' for terminal commands.") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                RetroHeader(
                    title = "MONO WORKSPACE",
                    subtitle = "SCRATCHPAD, TERMINAL & CRT CONTROLS"
                )
            }

            // 1. Quick Monospace Scratchpad
            item {
                RetroCard(
                    title = "QUICK SCRATCHPAD",
                    badge = "OFFLINE LOCAL"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "A distraction-free typewriter scratchpad for fleeting thoughts during focus:",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = quickNotes,
                            onValueChange = { viewModel.setQuickNotes(it) },
                            placeholder = {
                                Text(
                                    text = "Type ideas, thoughts, equations, or temporary notes...",
                                    color = palette.textSecondary.copy(alpha = 0.5f),
                                    fontFamily = palette.fontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("input_quick_scratchpad"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.buttonActiveBackground,
                                unfocusedBorderColor = palette.surfaceBorder,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary,
                                cursorColor = palette.textAccent
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${quickNotes.length} chars • Saved automatically",
                                color = palette.textSecondary.copy(alpha = 0.6f),
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 2. Floating Notch Timer Quick Toggle
            item {
                RetroCard(
                    title = "FLOATING / NOTCH TIMER",
                    badge = if (floatingNotchTimerEnabled) "ACTIVE" else "OFF"
                ) {
                    RetroToggle(
                        label = "Show Floating Notch Status Timer",
                        checked = floatingNotchTimerEnabled,
                        onCheckedChange = { viewModel.setFloatingNotchTimerEnabled(it) },
                        description = "Displays a minimal monochrome timer at the top of the screen during active sessions."
                    )
                }
            }

            // 3. Interactive Terminal Command Console
            item {
                RetroCard(
                    title = "INTERACTIVE TERMINAL",
                    badge = "CLI"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                                .background(palette.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = terminalOutput,
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = terminalInput,
                                onValueChange = { terminalInput = it },
                                placeholder = {
                                    Text(
                                        text = "Enter command (e.g. help, tasks, matrix, zen)...",
                                        color = palette.textSecondary.copy(alpha = 0.5f),
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_terminal_command"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = palette.buttonActiveBackground,
                                    unfocusedBorderColor = palette.surfaceBorder,
                                    focusedTextColor = palette.textPrimary,
                                    unfocusedTextColor = palette.textPrimary,
                                    cursorColor = palette.textAccent
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )

                            RetroButton(
                                text = "RUN",
                                onClick = {
                                    if (terminalInput.isNotBlank()) {
                                        val res = viewModel.executeCommand(terminalInput)
                                        terminalOutput = "> $terminalInput\n$res"
                                        terminalInput = ""
                                        RetroHaptics.performClick(context)
                                    }
                                },
                                isPrimary = true,
                                testTag = "btn_run_command"
                            )
                        }
                    }
                }
            }

            // 4. Retro Phosphor Themes & Ambient Noise
            item {
                RetroCard(
                    title = "DISPLAY & RETRO PHOSPHOR",
                    badge = "THEMES"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PHOSPHOR PALETTE:",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val themes = listOf(
                                Pair(RetroThemeId.PURE_MONOCHROME, "MONO"),
                                Pair(RetroThemeId.CRT_GREEN, "GREEN"),
                                Pair(RetroThemeId.CRT_AMBER, "AMBER"),
                                Pair(RetroThemeId.TERMINAL_MATRIX, "MATRIX")
                            )
                            themes.forEach { (theme, label) ->
                                RetroChip(
                                    text = label,
                                    isSelected = themeId == theme,
                                    onClick = {
                                        viewModel.userPreferences.setTheme(theme)
                                        RetroHaptics.performClick(context)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        RetroToggle(
                            label = "CRT Scanlines & Phosphor Glow",
                            checked = crtEffect,
                            onCheckedChange = { viewModel.toggleCrt() },
                            description = "Authentic retro Cathode Ray Tube screen simulation."
                        )
                    }
                }
            }

            // 5. Icon Vault Quick Link
            item {
                RetroCard(
                    title = "ICON VAULT & EXTRAS",
                    badge = "ASSETS"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Custom retro monochrome app launcher icons and wallpapers.",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroOutlineButton(
                                text = "OPEN ICON VAULT",
                                onClick = onNavigateToIconVault,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_open_icon_vault"
                            )
                            RetroOutlineButton(
                                text = "HOW TO USE GUIDE",
                                onClick = onNavigateToGuide,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 6. PMAK Maker Credits Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(8.dp))
                        .background(palette.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "DEMICHROME",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Demichrome is made by PMAK.",
                            color = palette.textAccent,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "A quiet retro digital workspace that makes your phone less distracting.",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
