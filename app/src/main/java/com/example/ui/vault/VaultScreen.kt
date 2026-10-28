package com.example.ui.vault

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RetroThemeId
import com.example.data.TimerStyleId
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroPalettes
import com.example.ui.theme.RetroTheme

@Composable
fun VaultScreen(
    viewModel: FocusViewModel,
    onNavigateToIconVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val currentTheme by viewModel.userPreferences.theme.collectAsState()
    val timerStyle by viewModel.userPreferences.timerStyle.collectAsState()
    val crtEffect by viewModel.userPreferences.crtEffect.collectAsState()
    val scanlineIntensity by viewModel.userPreferences.scanlineIntensity.collectAsState()

    var terminalInput by remember { mutableStateOf("") }
    val terminalLogs = remember {
        mutableStateListOf(
            "MONOCHROME VAULT v2.0 - SECURE TERMINAL",
            "Type 'help' for available commands (matrix, crt, vault, zen, beep, stats)."
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val allThemes = listOf(
        RetroPalettes.PureMonochrome,
        RetroPalettes.CrtGreen,
        RetroPalettes.CrtAmber,
        RetroPalettes.TerminalMatrix,
        RetroPalettes.RetroWindows,
        RetroPalettes.PixelArcade,
        RetroPalettes.MinimalEInk,
        RetroPalettes.DarkPaper
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "THE VAULT",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "> RETRO THEMES & EXPERIMENTS",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    RetroButton(
                        text = "ICON VAULT",
                        onClick = onNavigateToIconVault,
                        testTag = "btn_open_icon_vault"
                    )
                }
            }

            // Interactive Terminal Console
            item {
                RetroCard(title = "INTERACTIVE TERMINAL CONSOLE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .border(1.dp, palette.surfaceBorder, RectangleShape)
                            .background(Color.Black, RectangleShape)
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            items(terminalLogs) { log ->
                                Text(
                                    text = "> $log",
                                    color = if (log.startsWith("Type") || log.startsWith("MONOCHROME")) palette.textSecondary else palette.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = terminalInput,
                            onValueChange = { terminalInput = it },
                            placeholder = {
                                Text(
                                    text = "type 'matrix', 'crt', 'beep'...",
                                    color = palette.textSecondary.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.buttonBorder,
                                unfocusedBorderColor = palette.surfaceBorder,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary,
                                cursorColor = palette.textPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (terminalInput.isNotBlank()) {
                                    val cmd = terminalInput
                                    terminalLogs.add(cmd)
                                    val result = viewModel.executeCommand(cmd)
                                    terminalLogs.add(result)
                                    terminalInput = ""
                                    keyboardController?.hide()
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("terminal_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        RetroIconButton(
                            icon = Icons.Default.Send,
                            contentDescription = "Execute command",
                            onClick = {
                                if (terminalInput.isNotBlank()) {
                                    val cmd = terminalInput
                                    terminalLogs.add(cmd)
                                    val result = viewModel.executeCommand(cmd)
                                    terminalLogs.add(result)
                                    terminalInput = ""
                                    keyboardController?.hide()
                                }
                            },
                            testTag = "btn_terminal_send"
                        )
                    }
                }
            }

            // Theme Selection
            item {
                Text(
                    text = "AESTHETIC THEMES",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            items(allThemes) { themeItem ->
                val isSelected = currentTheme == themeItem.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) palette.buttonBorder else palette.surfaceBorder,
                            shape = RectangleShape
                        )
                        .background(palette.surface, RectangleShape)
                        .clickable {
                            viewModel.userPreferences.setTheme(themeItem.id)
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Theme color preview box
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, themeItem.buttonBorder, RectangleShape)
                                    .background(themeItem.background, RectangleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "42",
                                    color = themeItem.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = themeItem.name.uppercase(),
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = themeItem.description,
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active theme",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Timer Style Selection
            item {
                RetroCard(title = "TIMER STYLES") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimerStyleId.values().forEach { style ->
                            RetroChip(
                                text = style.name,
                                isSelected = timerStyle == style,
                                onClick = { viewModel.userPreferences.setTimerStyle(style) },
                                modifier = Modifier.weight(1f),
                                testTag = "chip_timer_style_${style.name}"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
