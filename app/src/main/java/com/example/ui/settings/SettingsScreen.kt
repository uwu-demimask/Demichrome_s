package com.example.ui.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.accessibility.SystemDisplayHelper
import com.example.core.haptics.RetroHaptics
import com.example.data.NavigationStyle
import com.example.data.RetroThemeId
import com.example.data.sync.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.components.RetroToggle
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme

@Composable
fun SettingsScreen(
    viewModel: FocusViewModel,
    onNavigateToGuide: () -> Unit = {},
    onNavigateToShield: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val palette = RetroTheme.palette

    val currentThemeId by viewModel.userPreferences.theme.collectAsState()
    val customBg by viewModel.userPreferences.customThemeBg.collectAsState()
    val customSurface by viewModel.userPreferences.customThemeSurface.collectAsState()
    val customTextPrimary by viewModel.userPreferences.customThemeTextPrimary.collectAsState()
    val customTextSecondary by viewModel.userPreferences.customThemeTextSecondary.collectAsState()
    val customAccent by viewModel.userPreferences.customThemeAccent.collectAsState()
    val customBorder by viewModel.userPreferences.customThemeBorder.collectAsState()

    val soundEnabled by viewModel.userPreferences.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.userPreferences.vibrationEnabled.collectAsState()
    val edgeWheelHapticsEnabled by viewModel.edgeWheelHapticsEnabled.collectAsState()
    val keepScreenOn by viewModel.userPreferences.keepScreenOn.collectAsState()
    val defaultDuration by viewModel.userPreferences.defaultDuration.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val lastSyncResult by viewModel.lastSyncResult.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var authModeIsSignUp by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authDisplayName by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    val blockerEnabled by viewModel.blockerEnabled.collectAsState()
    val blockedPackages by viewModel.blockedPackages.collectAsState()
    val blockerStrictMode by viewModel.blockerStrictMode.collectAsState()

    val monochromeMode by viewModel.userPreferences.monochromeMode.collectAsState()
    val crtEffect by viewModel.userPreferences.crtEffect.collectAsState()
    val crtFlicker by viewModel.userPreferences.crtFlicker.collectAsState()
    val crtCurvature by viewModel.userPreferences.crtCurvature.collectAsState()
    val scanlineIntensity by viewModel.userPreferences.scanlineIntensity.collectAsState()
    val reducedMotion by viewModel.userPreferences.reducedMotion.collectAsState()
    val fontScale by viewModel.userPreferences.fontScale.collectAsState()
    val appScale by viewModel.userPreferences.appScale.collectAsState()
    val systemMonochromeOnFocus by viewModel.userPreferences.systemMonochromeOnFocus.collectAsState()

    val floatingNotchTimerEnabled by viewModel.floatingNotchTimerEnabled.collectAsState()
    val notchTimerPosition by viewModel.userPreferences.notchTimerPosition.collectAsState()
    val navigationStyle by viewModel.navigationStyle.collectAsState()
    val glassIntensity by viewModel.glassIntensity.collectAsState()
    val glassBlurEnabled by viewModel.glassBlurEnabled.collectAsState()
    val cornerRadiusDp by viewModel.cornerRadiusDp.collectAsState()
    val edgeWheelSide by viewModel.edgeWheelSide.collectAsState()
    val edgeWheelSensitivity by viewModel.edgeWheelSensitivity.collectAsState()
    val edgeWheelPosition by viewModel.edgeWheelPosition.collectAsState()
    val dynamicIslandOffsetX by viewModel.dynamicIslandOffsetX.collectAsState()
    val dynamicIslandOffsetY by viewModel.dynamicIslandOffsetY.collectAsState()
    val dynamicIslandWidthScale by viewModel.dynamicIslandWidthScale.collectAsState()
    val dynamicIslandHeightScale by viewModel.dynamicIslandHeightScale.collectAsState()

    val ambientSound by viewModel.userPreferences.ambientSound.collectAsState()
    val strictExitLock by viewModel.userPreferences.strictExitLock.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSystemGrayscaleDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    var hasOverlayPermission by remember { mutableStateOf(viewModel.hasOverlayPermission()) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = viewModel.hasOverlayPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isDeviceMonochromeActive by remember {
        mutableStateOf(SystemDisplayHelper.isDeviceMonochromeActive(context))
    }

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
                            text = "SETTINGS & PREFERENCES",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "> CONFIGURATION MATRIX",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 01 ACCOUNT & CLOUD ARCHITECTURE
            item {
                RetroCard(
                    title = "ACCOUNT & CLOUD VAULT",
                    badge = if (currentUser.isGuest) "LOCAL GUEST" else "CLOUD ACTIVE",
                    modifier = Modifier.testTag("settings_account_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.displayName.ifBlank { "Demichrome User" },
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = currentUser.email,
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }
                        if (currentUser.isGuest) {
                            RetroOutlineButton(
                                text = "SIGN IN",
                                onClick = {
                                    authError = null
                                    authModeIsSignUp = false
                                    showAuthDialog = true
                                },
                                modifier = Modifier.testTag("account_signin_btn")
                            )
                        } else {
                            RetroOutlineButton(
                                text = "SIGN OUT",
                                onClick = {
                                    viewModel.signOut()
                                    Toast.makeText(context, "Signed out to guest mode.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("account_signout_btn")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusLabel = when (syncStatus) {
                        SyncStatus.IDLE -> "IDLE // READY"
                        SyncStatus.SYNCING -> "SYNCING..."
                        SyncStatus.SYNCED -> "SYNCHRONIZED"
                        SyncStatus.OFFLINE -> "OFFLINE MODE"
                        SyncStatus.ERROR -> "SYNC ERROR"
                    }
                    val lastSyncedStr = if (lastSyncedAt > 0L) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastSyncedAt))
                    } else {
                        "Never"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.textAccent.copy(alpha = 0.06f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "SYNC STATUS: $statusLabel",
                                color = if (syncStatus == SyncStatus.ERROR) Color(0xFFEF5350) else palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "LAST SYNCED: $lastSyncedStr • ${if (isOnline) "ONLINE" else "OFFLINE"}",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                    }

                    lastSyncResult?.let { res ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "> ${res.message}",
                            color = if (res.success) palette.textSecondary else Color(0xFFEF5350),
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "SYNC NOW",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.performSync()
                                Toast.makeText(context, "Syncing data...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_sync_now_btn")
                        )
                        RetroOutlineButton(
                            text = "BACKUP",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.backupToCloud()
                                Toast.makeText(context, "Cloud backup created.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_backup_btn")
                        )
                        RetroOutlineButton(
                            text = "RESTORE",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.restoreFromCloud()
                                Toast.makeText(context, "Restoring data...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_restore_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.getBackendConfigurationStatus(),
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            // Quick Guide Banner
            item {
                RetroCard(
                    title = "OPERATING HANDBOOK",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "New to Demichrome? Read the full operating manual explaining PiP floating mode, notification tray controls, distraction shielding, and Deep Focus.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RetroButton(
                        text = "OPEN SYSTEM USER GUIDE",
                        onClick = onNavigateToGuide,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_open_guide_btn"
                    )
                }
            }

            // 02 APPEARANCE → THEMES (CRITICAL REQUIREMENT)
            item {
                RetroCard(
                    title = "APPEARANCE → THEMES",
                    badge = currentThemeId.name,
                    modifier = Modifier.testTag("settings_theme_matrix")
                ) {
                    Text(
                        text = "CENTRAL THEME ENGINE // SELECT PALETTE",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val themesList = listOf(
                        Triple(RetroThemeId.DEMICHROME, "DEMICHROME", "Original CRT monochrome theme with screen texture & scanlines."),
                        Triple(RetroThemeId.DARK_TERMINAL, "DARK TERMINAL", "Deep dark background, light pixel terminal text, command console."),
                        Triple(RetroThemeId.PAPER, "PAPER", "Warm off-white paper appearance, dark text, technical grid."),
                        Triple(RetroThemeId.NIGHT, "NIGHT", "Dark OLED-friendly true black interface, minimal glow & zero glare."),
                        Triple(RetroThemeId.CUSTOM, "CUSTOM", "User configurable background, surface, text, accent, and border colors.")
                    )

                    themesList.forEach { (themeEnum, title, desc) ->
                        val isSelected = currentThemeId == themeEnum
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) palette.textAccent else palette.surfaceBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) palette.buttonActiveBackground.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    viewModel.setTheme(themeEnum)
                                }
                                .padding(12.dp)
                                .testTag("theme_option_${title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = title,
                                            color = if (isSelected) palette.textAccent else palette.textPrimary,
                                            fontFamily = palette.fontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "[ACTIVE]",
                                                color = palette.textAccent,
                                                fontFamily = palette.fontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = desc,
                                        color = palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = palette.textAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // If CUSTOM theme is selected, show color presets and fine-tune controls
                    if (currentThemeId == RetroThemeId.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "CUSTOM THEME PRESETS & COLOR TOKENS",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val presets = listOf(
                            "PHOSPHOR" to Triple(0xFF051105L, 0xFF0D220DL, 0xFF33FF33L) to Triple(0xFF22AA22L, 0xFF66FF66L, 0xFF1E521EL),
                            "AMBER" to Triple(0xFF120700L, 0xFF241100L, 0xFFFFB000L) to Triple(0xFFB37B00L, 0xFFFFCC33L, 0xFF5A2E00L),
                            "CYBER" to Triple(0xFF0A0F14L, 0xFF121B24L, 0xFF00E5FFL) to Triple(0xFF00838FL, 0xFF18FFFFL, 0xFF004D40L),
                            "SLATE" to Triple(0xFF161618L, 0xFF222226L, 0xFFE6E6EBL) to Triple(0xFF8E8E98L, 0xFFFFFFFFL, 0xFF3A3A40L)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { (presetNameData, secondTriple) ->
                                val (name, firstTriple) = presetNameData
                                val (bg, surface, textPri) = firstTriple
                                val (textSec, accent, border) = secondTriple

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                                        .background(Color(bg))
                                        .clickable {
                                            viewModel.userPreferences.setCustomThemeColors(
                                                bg = bg,
                                                surface = surface,
                                                textPrimary = textPri,
                                                textSecondary = textSec,
                                                accent = accent,
                                                border = border
                                            )
                                            RetroHaptics.performClick(context)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        color = Color(textPri),
                                        fontFamily = palette.fontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ACTIVE CUSTOM TOKENS: BG: ${String.format("#%06X", customBg and 0xFFFFFFL)} | SURFACE: ${String.format("#%06X", customSurface and 0xFFFFFFL)} | TEXT: ${String.format("#%06X", customTextPrimary and 0xFFFFFFL)}",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Display & Aesthetic (CRITICAL)
            item {
                RetroCard(title = "DISPLAY & AESTHETIC EFFECTS") {
                    // App-Level Monochrome
                    RetroToggle(
                        label = "Monochrome Mode (App UI)",
                        description = "Forces pure OLED high-contrast black & white workspace",
                        checked = monochromeMode,
                        onCheckedChange = { viewModel.userPreferences.setMonochromeMode(it) },
                        testTag = "toggle_monochrome_mode"
                    )

                    // System-Wide Device Monochrome
                    RetroToggle(
                        label = "Device-Wide Grayscale (Whole Device)",
                        description = if (isDeviceMonochromeActive) "Active: entire Android system is in monochrome" else "Toggle system-wide monochrome mode across all apps",
                        checked = isDeviceMonochromeActive,
                        onCheckedChange = { targetState ->
                            val success = SystemDisplayHelper.setDeviceMonochrome(context, targetState)
                            if (success) {
                                isDeviceMonochromeActive = targetState
                                Toast.makeText(
                                    context,
                                    if (targetState) "System-wide Monochrome Enabled" else "System-wide Monochrome Disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Permission required: Show ADB / Settings guidance dialog
                                showSystemGrayscaleDialog = true
                            }
                        },
                        testTag = "toggle_system_wide_monochrome"
                    )

                    // Auto Monochrome on Focus
                    RetroToggle(
                        label = "Auto-Grayscale Device During Focus",
                        description = "Automatically switch device to monochrome during sprints, restored on completion",
                        checked = systemMonochromeOnFocus,
                        onCheckedChange = { viewModel.userPreferences.setSystemMonochromeOnFocus(it) },
                        testTag = "toggle_auto_system_monochrome"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // CRT Scanlines Overlay
                    RetroToggle(
                        label = "CRT Scanlines Overlay",
                        description = "Simulates authentic cathode ray tube scanlines & tube curvature",
                        checked = crtEffect,
                        onCheckedChange = { viewModel.userPreferences.setCrtEffect(it) },
                        testTag = "toggle_crt"
                    )

                    if (crtEffect) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "SCANLINE INTENSITY: ${(scanlineIntensity * 100).toInt()}%",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                            Slider(
                                value = scanlineIntensity,
                                onValueChange = { viewModel.userPreferences.setScanlineIntensity(it) },
                                valueRange = 0.05f..0.8f,
                                colors = SliderDefaults.colors(
                                    thumbColor = palette.buttonBorder,
                                    activeTrackColor = palette.textPrimary,
                                    inactiveTrackColor = palette.surfaceBorder
                                ),
                                modifier = Modifier.testTag("slider_scanlines")
                            )
                        }

                        // CRT Tube Curvature Vignette
                        RetroToggle(
                            label = "CRT Tube Curvature Vignette",
                            description = "Authentic convex glass barrel curvature shading and corner shadowing",
                            checked = crtCurvature,
                            onCheckedChange = { viewModel.userPreferences.setCrtCurvature(it) },
                            testTag = "toggle_crt_curvature"
                        )

                        // Phosphor Micro-Flicker
                        RetroToggle(
                            label = "Phosphor Micro-Flicker",
                            description = "Subtle 60Hz cathode ray tube micro-oscillation",
                            checked = crtFlicker,
                            onCheckedChange = { viewModel.userPreferences.setCrtFlicker(it) },
                            testTag = "toggle_crt_flicker"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Font Scaling Setting
                    Text(
                        text = "FONT SCALING / DENSITY",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val fontScales = listOf(
                            0.85f to "0.85x",
                            1.0f to "1.0x",
                            1.15f to "1.15x",
                            1.30f to "1.3x"
                        )
                        fontScales.forEach { (scale, label) ->
                            RetroChip(
                                text = label,
                                isSelected = kotlin.math.abs(fontScale - scale) < 0.05f,
                                onClick = {
                                    viewModel.userPreferences.setFontScale(scale)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "settings_font_scale_$label"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // App Scale / UI Zoom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APP SCALING / UI ZOOM",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(appScale * 100).toInt()}%",
                            color = palette.textAccent,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val appScales = listOf(
                            0.85f to "COMPACT (85%)",
                            1.0f to "100%",
                            1.15f to "115%",
                            1.30f to "LARGE (130%)"
                        )
                        appScales.forEach { (scale, label) ->
                            RetroChip(
                                text = label,
                                isSelected = kotlin.math.abs(appScale - scale) < 0.04f,
                                onClick = {
                                    viewModel.setAppScale(scale)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "settings_app_scale_$label"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = appScale,
                        onValueChange = { viewModel.setAppScale(it) },
                        valueRange = 0.80f..1.35f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.textPrimary,
                            activeTrackColor = palette.textAccent,
                            inactiveTrackColor = palette.surfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_app_scale")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reduced Motion
                    RetroToggle(
                        label = "Reduced Motion",
                        description = "Disables phosphor flicker, pulsation, and rapid transitions",
                        checked = reducedMotion,
                        onCheckedChange = { viewModel.userPreferences.setReducedMotion(it) },
                        testTag = "toggle_reduced_motion"
                    )
                }
            }

            // Android System Display Settings Quick Action
            item {
                RetroCard(title = "ANDROID SYSTEM DISPLAY INTEGRATION") {
                    Text(
                        text = "Android OS secures system-wide display filters behind Color Correction settings. You can launch Android's native grayscale settings directly with one tap below:",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RetroButton(
                        text = "LAUNCH ANDROID GRAYSCALE SETTINGS",
                        onClick = {
                            SystemDisplayHelper.openColorCorrectionSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_btn_system_grayscale"
                    )
                }
            }

            // Deep Focus & Immersion Configuration
            item {
                RetroCard(title = "DEEP FOCUS & SENSORY SHIELD") {
                    Text(
                        text = "AMBIENT FOCUS SYNTHESIZER",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val ambientOptions = listOf(
                            com.example.core.audio.RetroAudioSynthesizer.AmbientSoundType.NONE to "MUTE",
                            com.example.core.audio.RetroAudioSynthesizer.AmbientSoundType.BINAURAL_432HZ to "432Hz",
                            com.example.core.audio.RetroAudioSynthesizer.AmbientSoundType.WARM_BROWN_NOISE to "BROWN",
                            com.example.core.audio.RetroAudioSynthesizer.AmbientSoundType.MECHANICAL_TICK to "TICK"
                        )
                        ambientOptions.forEach { (type, name) ->
                            RetroChip(
                                text = name,
                                isSelected = ambientSound == type,
                                onClick = {
                                    viewModel.userPreferences.setAmbientSound(type)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "settings_ambient_${type.name.lowercase()}"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    RetroToggle(
                        label = "Strict 3-Second Exit Lock",
                        description = "Requires holding the exit button for 3 seconds with haptic ticks to break out of Deep Focus",
                        checked = strictExitLock,
                        onCheckedChange = { viewModel.userPreferences.setStrictExitLock(it) },
                        testTag = "toggle_strict_exit_lock"
                    )
                }
            }

            // Focus & Notch Configuration
            item {
                RetroCard(title = "FOCUS & TIMER") {
                    Text(
                        text = "DEFAULT DURATION",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(25, 45, 60, 90).forEach { mins ->
                            RetroChip(
                                text = "${mins}M",
                                isSelected = defaultDuration == mins,
                                onClick = {
                                    viewModel.userPreferences.setDefaultDuration(mins)
                                    viewModel.selectDuration(mins)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "settings_chip_duration_$mins"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Floating Notch Timer Configuration
                    RetroToggle(
                        label = "Notch-Side / Floating Timer",
                        description = "Shows minimal HUD timer element pinned at screen notch across all apps",
                        checked = floatingNotchTimerEnabled,
                        onCheckedChange = { viewModel.setFloatingNotchTimerEnabled(it) },
                        testTag = "toggle_floating_notch_timer"
                    )

                    if (floatingNotchTimerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!hasOverlayPermission) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, palette.accent, RectangleShape)
                                    .background(palette.surface)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠ OVERLAY PERMISSION REQUIRED",
                                    color = palette.accent,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Grant 'Display over other apps' so the floating notch timer stays pinned on top when opening external apps.",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                RetroButton(
                                    text = "GRANT OVERLAY PERMISSION",
                                    onClick = { viewModel.openOverlaySettings() },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_grant_overlay_settings"
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = "NOTCH TIMER POSITION",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val positions = listOf(
                                com.example.data.NotchPosition.TOP_CENTER to "TOP-MID",
                                com.example.data.NotchPosition.TOP_RIGHT to "TOP-R",
                                com.example.data.NotchPosition.TOP_LEFT to "TOP-L",
                                com.example.data.NotchPosition.BOTTOM_CENTER to "BOTTOM"
                            )
                            positions.forEach { (pos, label) ->
                                RetroChip(
                                    text = label,
                                    isSelected = notchTimerPosition == pos,
                                    onClick = {
                                        viewModel.userPreferences.setNotchTimerPosition(pos)
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_notch_pos_$label"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        RetroOutlineButton(
                            text = "CALIBRATE SIZE & POSITION",
                            onClick = { showCalibrationDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_calibrate_notch_timer")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // NAVIGATION & DOCK STYLE
                    Text(
                        text = "NAVIGATION & DOCK STYLE",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your preferred interface navigation: Tactile Edge Wheel, Industrial Normal Dock, or Modern Liquid Glass Dock.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Three-Way Style Switcher Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RetroChip(
                            text = "EDGE WHEEL",
                            isSelected = navigationStyle == NavigationStyle.EDGE_WHEEL,
                            onClick = { viewModel.setNavigationStyle(NavigationStyle.EDGE_WHEEL) },
                            modifier = Modifier.weight(1f),
                            testTag = "settings_nav_style_edge_wheel"
                        )
                        RetroChip(
                            text = "NORMAL DOCK",
                            isSelected = navigationStyle == NavigationStyle.NORMAL_DOCK,
                            onClick = { viewModel.setNavigationStyle(NavigationStyle.NORMAL_DOCK) },
                            modifier = Modifier.weight(1f),
                            testTag = "settings_nav_style_normal_dock"
                        )
                        RetroChip(
                            text = "LIQUID GLASS",
                            isSelected = navigationStyle == NavigationStyle.LIQUID_GLASS_DOCK,
                            onClick = { viewModel.setNavigationStyle(NavigationStyle.LIQUID_GLASS_DOCK) },
                            modifier = Modifier.weight(1f),
                            testTag = "settings_nav_style_liquid_glass"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contextual Controls for active navigation style
                    when (navigationStyle) {
                        NavigationStyle.EDGE_WHEEL -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, palette.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .background(palette.surface.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "EDGE WHEEL CONFIGURATION",
                                    color = palette.textAccent,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RetroChip(
                                        text = "RIGHT EDGE",
                                        isSelected = edgeWheelSide.equals("RIGHT", ignoreCase = true),
                                        onClick = { viewModel.setEdgeWheelSide("RIGHT") },
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_wheel_side_right"
                                    )
                                    RetroChip(
                                        text = "LEFT EDGE",
                                        isSelected = edgeWheelSide.equals("LEFT", ignoreCase = true),
                                        onClick = { viewModel.setEdgeWheelSide("LEFT") },
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_wheel_side_left"
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "VERTICAL PLACEMENT",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RetroChip(
                                        text = "THUMB (LOWER)",
                                        isSelected = edgeWheelPosition.equals("THUMB", ignoreCase = true),
                                        onClick = { viewModel.setEdgeWheelPosition("THUMB") },
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_wheel_pos_thumb"
                                    )
                                    RetroChip(
                                        text = "CENTER",
                                        isSelected = edgeWheelPosition.equals("CENTER", ignoreCase = true),
                                        onClick = { viewModel.setEdgeWheelPosition("CENTER") },
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_wheel_pos_center"
                                    )
                                    RetroChip(
                                        text = "UPPER",
                                        isSelected = edgeWheelPosition.equals("UPPER", ignoreCase = true),
                                        onClick = { viewModel.setEdgeWheelPosition("UPPER") },
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_wheel_pos_upper"
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ROTARY SENSITIVITY",
                                        color = palette.textPrimary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1fX", edgeWheelSensitivity),
                                        color = palette.accent,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Slider(
                                    value = edgeWheelSensitivity,
                                    onValueChange = { viewModel.setEdgeWheelSensitivity(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.accent,
                                        activeTrackColor = palette.accent,
                                        inactiveTrackColor = palette.surfaceBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_wheel_sensitivity_slider")
                                )

                                RetroToggle(
                                    label = "Edge Wheel Haptics",
                                    description = "Subtle tactile ticks as rotary dial crosses between utilities",
                                    checked = edgeWheelHapticsEnabled,
                                    onCheckedChange = { viewModel.setEdgeWheelHapticsEnabled(it) },
                                    testTag = "toggle_edge_wheel_haptics"
                                )
                            }
                        }
                        NavigationStyle.NORMAL_DOCK -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, palette.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .background(palette.surface.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "INDUSTRIAL NORMAL DOCK",
                                    color = palette.textAccent,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Standard Demichrome industrial bottom bar attached to the bottom edge. Includes real-time OS telemetry status strip, system version readout, and full-width grid buttons.",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        NavigationStyle.LIQUID_GLASS_DOCK -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, palette.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .background(palette.surface.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "LIQUID GLASS CAPSULE TUNING",
                                    color = palette.textAccent,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Floating translucent pill inspired by modern liquid glassmorphism. Highlights active tabs with a fluid capsule pill and real-time notification badges.",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "FROSTED GLASS OPACITY",
                                        color = palette.textPrimary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${(glassIntensity * 100).toInt()}%",
                                        color = palette.accent,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Slider(
                                    value = glassIntensity,
                                    onValueChange = { viewModel.setGlassIntensity(it) },
                                    valueRange = 0.40f..1.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.accent,
                                        activeTrackColor = palette.accent,
                                        inactiveTrackColor = palette.surfaceBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_glass_intensity_slider")
                                )

                                RetroToggle(
                                    label = "Frosted Backdrop Sheen",
                                    description = "Renders specular glass reflection along the top curvature rim",
                                    checked = glassBlurEnabled,
                                    onCheckedChange = { viewModel.setGlassBlurEnabled(it) },
                                    testTag = "toggle_glass_blur"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    RetroToggle(
                        label = "Retro Audio Synthesizer",
                        description = "Gentle square/sine wave tones on start, pause & completion",
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.userPreferences.setSoundEnabled(it) },
                        testTag = "toggle_sound"
                    )

                    RetroToggle(
                        label = "Tactile Haptics",
                        description = "Subtle vibration feedback on buttons and timer triggers",
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.userPreferences.setVibrationEnabled(it) },
                        testTag = "toggle_haptics"
                    )

                    RetroToggle(
                        label = "Keep Screen Active",
                        description = "Prevents device display timeout during active focus sessions",
                        checked = keepScreenOn,
                        onCheckedChange = { viewModel.userPreferences.setKeepScreenOn(it) },
                        testTag = "toggle_keep_screen_on"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RetroOutlineButton(
                        text = "RESET DAILY 24H ROUTINE NOW",
                        onClick = {
                            viewModel.resetDailyTasksManually()
                            Toast.makeText(context, "Daily 24-hour focus routine reset", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_settings_reset_routine"
                    )
                }
            }

            // Distraction Shield Settings
            item {
                RetroCard(
                    title = "DISTRACTION SHIELD & BLOCKER",
                    badge = "${blockedPackages.size} APPS"
                ) {
                    RetroToggle(
                        label = "Distraction Blocker",
                        description = "Intercepts distracting applications during active focus sessions",
                        checked = blockerEnabled,
                        onCheckedChange = { viewModel.toggleBlockerEnabled() },
                        testTag = "settings_toggle_blocker"
                    )

                    RetroToggle(
                        label = "Strict Zero-Tolerance Mode",
                        description = "Enforces mindful breathing delay before allowing emergency bypass",
                        checked = blockerStrictMode,
                        onCheckedChange = { viewModel.setBlockerStrictMode(it) },
                        testTag = "settings_toggle_strict_blocker"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RetroOutlineButton(
                        text = "MANAGE BLOCKED APPS LIST",
                        onClick = onNavigateToShield,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_settings_manage_blocked_apps"
                    )
                }
            }

            // Privacy, Local Storage & Session Export
            item {
                RetroCard(title = "SESSION EXPORT & DATA BACKUP") {
                    Text(
                        text = "Export your complete focus ledger to open standard formats (CSV, JSON, Markdown).",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "EXPORT CSV",
                            onClick = {
                                viewModel.shareExport(context, "CSV")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_export_csv"
                        )
                        RetroButton(
                            text = "EXPORT JSON",
                            onClick = {
                                viewModel.shareExport(context, "JSON")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_export_json"
                        )
                        RetroButton(
                            text = "REPORT",
                            onClick = {
                                viewModel.shareExport(context, "MARKDOWN")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_export_report"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "100% Offline-First. No internet connection is ever required. Your session history is strictly stored in your device's local Room database.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!showClearConfirm) {
                        RetroOutlineButton(
                            text = "CLEAR ALL LOCAL HISTORY",
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_clear_data"
                        )
                    } else {
                        Column {
                            Text(
                                text = "ARE YOU SURE? ALL SESSIONS WILL BE DELETED.",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RetroButton(
                                    text = "CANCEL",
                                    onClick = { showClearConfirm = false },
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_cancel_clear"
                                )
                                RetroButton(
                                    text = "YES, PURGE",
                                    onClick = {
                                        viewModel.clearAllData()
                                        showClearConfirm = false
                                    },
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_confirm_clear"
                                )
                            }
                        }
                    }
                }
            }

            // App Identity & Info
            item {
                RetroCard(title = "SYSTEM INFO") {
                    Text(
                        text = "DEMICHROME v2.1",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "A quiet retro digital workspace that makes your phone less distracting.\nBuilt with Kotlin & Jetpack Compose.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // System-wide Grayscale Permission / Setup Dialog
    if (showSystemGrayscaleDialog) {
        val adbCommand = SystemDisplayHelper.getAdbCommand(context)

        Dialog(onDismissRequest = { showSystemGrayscaleDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, palette.buttonBorder, RoundedCornerShape(8.dp))
                    .background(palette.surface)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM-WIDE MONOCHROME",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        RetroIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { showSystemGrayscaleDialog = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "To apply a true system-wide grayscale filter across your entire device and all other apps, Android provides two methods:",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Method 1: Direct Android Settings
                    Text(
                        text = "METHOD 1: NATIVE ANDROID SETTING (EASY)",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Open Accessibility Color Correction and toggle Grayscale.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RetroButton(
                        text = "⚡ OPEN COLOR CORRECTION",
                        onClick = {
                            SystemDisplayHelper.openColorCorrectionSettings(context)
                            showSystemGrayscaleDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "dialog_btn_open_color_correction"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Method 2: ADB 1-Time Permission
                    Text(
                        text = "METHOD 2: 1-CLICK AUTOMATION (ADB)",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Run this one-time command to let Demichrome toggle system grayscale directly:",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.background)
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(adbCommand))
                                RetroHaptics.performClick(context)
                                Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = adbCommand,
                                color = palette.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RetroOutlineButton(
                        text = "DISMISS",
                        onClick = { showSystemGrayscaleDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "dialog_btn_dismiss"
                    )
                }
            }
        }
    }

    if (showAuthDialog) {
        Dialog(onDismissRequest = { showAuthDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (authModeIsSignUp) "CREATE ACCOUNT" else "SIGN IN",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (authModeIsSignUp) "Create cloud identity to sync across devices" else "Sign in to access your persistent Demichrome cloud vault",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (authModeIsSignUp) {
                        OutlinedTextField(
                            value = authDisplayName,
                            onValueChange = { authDisplayName = it },
                            label = { Text("Display Name", fontFamily = palette.fontFamily, fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_display_name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary,
                                focusedBorderColor = palette.surfaceBorder,
                                unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                                focusedLabelColor = palette.textPrimary,
                                unfocusedLabelColor = palette.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = authEmail,
                        onValueChange = { authEmail = it },
                        label = { Text("Email Address", fontFamily = palette.fontFamily, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            focusedBorderColor = palette.surfaceBorder,
                            unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                            focusedLabelColor = palette.textPrimary,
                            unfocusedLabelColor = palette.textSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = authPassword,
                        onValueChange = { authPassword = it },
                        label = { Text("Password (min 6 chars)", fontFamily = palette.fontFamily, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            focusedBorderColor = palette.surfaceBorder,
                            unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                            focusedLabelColor = palette.textPrimary,
                            unfocusedLabelColor = palette.textSecondary
                        )
                    )

                    authError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = err,
                            color = Color(0xFFEF5350),
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroOutlineButton(
                            text = "CANCEL",
                            onClick = { showAuthDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        RetroButton(
                            text = if (authModeIsSignUp) "REGISTER" else "SIGN IN",
                            onClick = {
                                if (authModeIsSignUp) {
                                    viewModel.createAccount(authEmail, authPassword, authDisplayName) { res ->
                                        if (res.isSuccess) {
                                            showAuthDialog = false
                                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            authError = res.exceptionOrNull()?.message ?: "Account creation failed"
                                        }
                                    }
                                } else {
                                    viewModel.signIn(authEmail, authPassword) { res ->
                                        if (res.isSuccess) {
                                            showAuthDialog = false
                                            Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            authError = res.exceptionOrNull()?.message ?: "Sign in failed"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auth_submit_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (authModeIsSignUp) "Already have an account? Sign In" else "Don't have an account? Create one",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable {
                                authModeIsSignUp = !authModeIsSignUp
                                authError = null
                            }
                            .align(Alignment.CenterHorizontally)
                            .padding(4.dp)
                    )

                    if (!authModeIsSignUp) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Forgot password? Recover account",
                            color = palette.accent,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .clickable {
                                    showAuthDialog = false
                                    showRecoveryDialog = true
                                }
                                .align(Alignment.CenterHorizontally)
                                .padding(4.dp)
                                .testTag("auth_recover_link")
                        )
                    }
                }
            }
        }
    }

    if (showCalibrationDialog) {
        DynamicIslandCalibrationDialog(
            initialOffsetX = dynamicIslandOffsetX,
            initialOffsetY = dynamicIslandOffsetY,
            initialWidthScale = dynamicIslandWidthScale,
            initialHeightScale = dynamicIslandHeightScale,
            onSave = { x, y, w, h ->
                viewModel.setDynamicIslandCalibration(x, y, w, h)
                Toast.makeText(context, "Calibration applied!", Toast.LENGTH_SHORT).show()
            },
            onResetDefault = {
                viewModel.resetDynamicIslandCalibration()
            },
            onDismiss = { showCalibrationDialog = false }
        )
    }

    if (showRecoveryDialog) {
        AccountRecoveryDialog(
            viewModel = viewModel,
            onDismiss = { showRecoveryDialog = false }
        )
    }
}
