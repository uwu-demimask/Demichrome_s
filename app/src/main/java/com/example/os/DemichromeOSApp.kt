package com.example.os

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.ui.blocker.DistractionBlockerScreen
import com.example.ui.blocker.DistractionInterceptionDialog
import com.example.ui.components.FloatingNotchTimer
import com.example.ui.components.RetroCrtContainer
import com.example.ui.components.SessionRatingDialog
import com.example.ui.components.demichromeMonochrome
import com.example.ui.flashcards.FlashcardScreen
import com.example.ui.focus.DeepFocusScreen
import com.example.ui.focus.FocusPipScreen
import com.example.ui.focus.FocusScreen
import com.example.ui.focus.FocusViewModel
import com.example.ui.focus.FocusZenScreen
import com.example.ui.guide.GuideScreen
import com.example.ui.habits.SanctuaryScreen
import com.example.ui.money.DemichromeMoneyScreen
import com.example.ui.mono.MonoWorkspaceScreen
import com.example.ui.home.HomeScreen
import com.example.data.NavigationStyle
import com.example.ui.navigation.DemichromeEdgeHapticWheel
import com.example.ui.navigation.DemichromeOSBottomNavigation
import com.example.ui.navigation.LiquidGlassDock
import com.example.ui.navigation.RetroScreen
import com.example.ui.navigation.WheelEdge
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.MonochromeTheme
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import com.example.ui.todo.TodoScreen
import com.example.ui.vault.IconVaultScreen
import com.example.ui.vault.VaultScreen

@Composable
fun DemichromeOSApp(
    viewModel: FocusViewModel,
    initialScreen: RetroScreen = RetroScreen.HOME,
    isPipModeActive: Boolean = false,
    onEnterPip: () -> Unit = {},
    onExitPip: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeId by viewModel.userPreferences.theme.collectAsState()
    val monochromeModeLevel by viewModel.monochromeModeLevel.collectAsState()
    val crtEffect by viewModel.userPreferences.crtEffect.collectAsState()
    val crtFlicker by viewModel.userPreferences.crtFlicker.collectAsState()
    val crtCurvature by viewModel.userPreferences.crtCurvature.collectAsState()
    val scanlineIntensity by viewModel.userPreferences.scanlineIntensity.collectAsState()
    val reducedMotion by viewModel.userPreferences.reducedMotion.collectAsState()
    val fontScale by viewModel.userPreferences.fontScale.collectAsState()
    val appScale by viewModel.userPreferences.appScale.collectAsState()
    val notchTimerPosition by viewModel.userPreferences.notchTimerPosition.collectAsState()

    val isZenMode by viewModel.isFocusModeActive.collectAsState()
    val isDeepFocus by viewModel.isDeepFocusActive.collectAsState()
    val timerState by viewModel.state.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val interceptionEvent by viewModel.interceptionEvent.collectAsState()

    val pendingRatingSession by viewModel.pendingRatingSession.collectAsState()
    val navigationStyle by viewModel.navigationStyle.collectAsState()
    val glassIntensity by viewModel.glassIntensity.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val blockerEnabled by viewModel.blockerEnabled.collectAsState()
    val floatingNotchTimerEnabled by viewModel.floatingNotchTimerEnabled.collectAsState()
    val edgeWheelHapticsEnabled by viewModel.edgeWheelHapticsEnabled.collectAsState()
    val edgeWheelSideStr by viewModel.edgeWheelSide.collectAsState()
    val edgeWheelSensitivity by viewModel.edgeWheelSensitivity.collectAsState()
    val edgeWheelPosition by viewModel.edgeWheelPosition.collectAsState()
    val dynamicIslandOffsetX by viewModel.dynamicIslandOffsetX.collectAsState()
    val dynamicIslandOffsetY by viewModel.dynamicIslandOffsetY.collectAsState()
    val dynamicIslandWidthScale by viewModel.dynamicIslandWidthScale.collectAsState()
    val dynamicIslandHeightScale by viewModel.dynamicIslandHeightScale.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val selectedTargetTask by viewModel.selectedTargetTask.collectAsState()
    val ambientSound by viewModel.ambientSound.collectAsState()
    val todaySessionCount by viewModel.todaySessionCount.collectAsState()

    val customBg by viewModel.userPreferences.customThemeBg.collectAsState()
    val customSurface by viewModel.userPreferences.customThemeSurface.collectAsState()
    val customTextPrimary by viewModel.userPreferences.customThemeTextPrimary.collectAsState()
    val customTextSecondary by viewModel.userPreferences.customThemeTextSecondary.collectAsState()
    val customAccent by viewModel.userPreferences.customThemeAccent.collectAsState()
    val customBorder by viewModel.userPreferences.customThemeBorder.collectAsState()

    val customColors = remember(customBg, customSurface, customTextPrimary, customTextSecondary, customAccent, customBorder) {
        com.example.ui.theme.CustomThemeColors(
            bg = Color(customBg),
            surface = Color(customSurface),
            textPrimary = Color(customTextPrimary),
            textSecondary = Color(customTextSecondary),
            textAccent = Color(customAccent),
            border = Color(customBorder)
        )
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }

    val currentDensity = LocalDensity.current
    val effectiveFontScale = (currentDensity.fontScale * fontScale).coerceIn(0.80f, 1.40f)
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = effectiveFontScale
        )
    ) {
        MonochromeTheme(themeId = themeId, customColors = customColors) {
            val palette = RetroTheme.palette
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? android.app.Activity)?.window
                    if (window != null) {
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        val isLight = palette.background.luminance() > 0.5f
                        insetsController.isAppearanceLightStatusBars = isLight
                        insetsController.isAppearanceLightNavigationBars = isLight
                        window.statusBarColor = palette.background.toArgb()
                        window.navigationBarColor = palette.background.toArgb()
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = palette.background
            ) {
                if (isPipModeActive) {
                    FocusPipScreen(
                        viewModel = viewModel,
                        onExitPip = onExitPip
                    )
                } else if (isDeepFocus) {
                    DeepFocusScreen(
                        viewModel = viewModel,
                        onEnterPip = onEnterPip
                    )
                } else if (isZenMode) {
                    FocusZenScreen(viewModel = viewModel)
                } else {
                    RetroCrtContainer(
                        enabled = crtEffect,
                        scanlineIntensity = scanlineIntensity,
                        flicker = crtFlicker,
                        crtCurvature = crtCurvature,
                        reducedMotion = reducedMotion,
                        modifier = Modifier
                            .fillMaxSize()
                            .demichromeMonochrome(monochromeModeLevel)
                    ) {
                        // Sync active Demichrome theme colors to the System Notch Overlay
                        androidx.compose.runtime.LaunchedEffect(palette) {
                            com.example.core.overlay.SystemNotchOverlayManager.updateTheme(
                                bgColor = palette.surface.toArgb(),
                                surfaceColor = palette.surfaceElevated.toArgb(),
                                borderColor = palette.border.toArgb(),
                                textColor = palette.textPrimary.toArgb(),
                                textSecondaryColor = palette.textSecondary.toArgb(),
                                accentColor = palette.accent.toArgb(),
                                buttonActiveTextColor = palette.buttonActiveText.toArgb()
                            )
                        }

                        // Inform system overlay when inside full-screen Focus view
                        androidx.compose.runtime.LaunchedEffect(currentScreen) {
                            com.example.core.overlay.SystemNotchOverlayManager.setInFocusScreen(currentScreen == RetroScreen.FOCUS)
                        }

                        // Real-time synchronization of calibration parameters
                        androidx.compose.runtime.LaunchedEffect(
                            dynamicIslandOffsetX,
                            dynamicIslandOffsetY,
                            dynamicIslandWidthScale,
                            dynamicIslandHeightScale
                        ) {
                            com.example.core.overlay.SystemNotchOverlayManager.updateCalibration(
                                context,
                                dynamicIslandOffsetX,
                                dynamicIslandOffsetY,
                                dynamicIslandWidthScale,
                                dynamicIslandHeightScale
                            )
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = palette.background,
                                contentWindowInsets = WindowInsets.safeDrawing,
                                bottomBar = {
                                    if (navigationStyle == NavigationStyle.NORMAL_DOCK) {
                                        DemichromeOSBottomNavigation(
                                            currentScreen = currentScreen,
                                            onNavigate = { currentScreen = it }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .padding(
                                            bottom = if (navigationStyle == NavigationStyle.LIQUID_GLASS_DOCK) 68.dp else 0.dp
                                        ),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Crossfade(
                                        targetState = currentScreen,
                                        animationSpec = if (reducedMotion) snap() else tween(250),
                                        label = "os_screen_transition"
                                    ) { screen ->
                                        when (screen) {
                                            RetroScreen.HOME -> {
                                                HomeScreen(
                                                    viewModel = viewModel,
                                                    onNavigate = { currentScreen = it }
                                                )
                                            }
                                            RetroScreen.FOCUS -> {
                                                FocusScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToVault = { currentScreen = RetroScreen.VAULT },
                                                    onNavigateToStats = { currentScreen = RetroScreen.STATS },
                                                    onNavigateToSettings = { currentScreen = RetroScreen.SETTINGS },
                                                    onNavigateToShield = { currentScreen = RetroScreen.SHIELD },
                                                    onNavigateToGuide = { currentScreen = RetroScreen.GUIDE },
                                                    onNavigateToTasks = { currentScreen = RetroScreen.TASKS },
                                                    onNavigateToMoney = { currentScreen = RetroScreen.MONEY },
                                                    onEnterPip = onEnterPip
                                                )
                                            }
                                            RetroScreen.TASKS -> {
                                                TodoScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToFocus = { currentScreen = RetroScreen.FOCUS }
                                                )
                                            }
                                            RetroScreen.HABITS -> {
                                                SanctuaryScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToFocus = { currentScreen = RetroScreen.FOCUS }
                                                )
                                            }
                                            RetroScreen.FLASHCARDS -> {
                                                FlashcardScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToFocus = { currentScreen = RetroScreen.FOCUS }
                                                )
                                            }
                                            RetroScreen.MONEY -> {
                                                DemichromeMoneyScreen(viewModel = viewModel)
                                            }
                                            RetroScreen.SHIELD -> {
                                                DistractionBlockerScreen(viewModel = viewModel)
                                            }
                                            RetroScreen.STATS -> {
                                                StatsScreen(viewModel = viewModel)
                                            }
                                            RetroScreen.SETTINGS -> {
                                                SettingsScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToGuide = { currentScreen = RetroScreen.GUIDE },
                                                    onNavigateToShield = { currentScreen = RetroScreen.SHIELD }
                                                )
                                            }
                                            RetroScreen.GUIDE -> {
                                                GuideScreen(
                                                    viewModel = viewModel,
                                                    onNavigateBack = { currentScreen = RetroScreen.FOCUS },
                                                    onEnterPip = onEnterPip
                                                )
                                            }
                                            RetroScreen.VAULT -> {
                                                VaultScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToIconVault = { currentScreen = RetroScreen.ICON_VAULT }
                                                )
                                            }
                                            RetroScreen.ICON_VAULT -> {
                                                IconVaultScreen(
                                                    onNavigateBack = { currentScreen = RetroScreen.VAULT }
                                                )
                                            }
                                            RetroScreen.MONO -> {
                                                MonoWorkspaceScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToIconVault = { currentScreen = RetroScreen.ICON_VAULT },
                                                    onNavigateToGuide = { currentScreen = RetroScreen.GUIDE }
                                                )
                                            }
                                        }
                                    }

                                    // Interception Dialog
                                    interceptionEvent?.let { event ->
                                        DistractionInterceptionDialog(
                                            event = event,
                                            viewModel = viewModel
                                        )
                                    }

                                    // Rating Dialog
                                    pendingRatingSession?.let { session ->
                                        SessionRatingDialog(
                                            session = session,
                                            onSaveRating = { rating, note ->
                                                viewModel.submitSessionRating(session.id, rating, note)
                                            },
                                            onDismiss = { viewModel.dismissSessionRating() }
                                        )
                                    }
                                }
                            }

                            // Demichrome Navigation: Edge Wheel, Normal Dock, or Liquid Glass Dock
                            when (navigationStyle) {
                                NavigationStyle.EDGE_WHEEL -> {
                                    DemichromeEdgeHapticWheel(
                                        currentScreen = currentScreen,
                                        onSelectUtility = { nextScreen -> currentScreen = nextScreen },
                                        hapticsEnabled = edgeWheelHapticsEnabled,
                                        edgeSide = if (edgeWheelSideStr.equals("LEFT", ignoreCase = true)) WheelEdge.LEFT else WheelEdge.RIGHT,
                                        sensitivity = edgeWheelSensitivity,
                                        verticalPlacement = edgeWheelPosition
                                    )
                                }
                                NavigationStyle.NORMAL_DOCK -> {
                                    // Dock is rendered automatically in Scaffold bottomBar
                                }
                                NavigationStyle.LIQUID_GLASS_DOCK -> {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        LiquidGlassDock(
                                            currentScreen = currentScreen,
                                            onNavigate = { nextScreen -> currentScreen = nextScreen },
                                            activeTasksCount = activeTasks.count { !it.isCompleted },
                                            isFocusTimerRunning = timerState == FocusState.RUNNING,
                                            isShieldActive = blockerEnabled,
                                            glassIntensity = glassIntensity
                                        )
                                    }
                                }
                            }

                            // Dynamic Island Mini-Player (Single Active Overlay Guarantee)
                            // If the user has granted overlay permission, SystemNotchOverlayManager manages the single system-wide overlay.
                            // If the user has not granted overlay permission, FloatingNotchTimer renders as an in-app overlay.
                            val hasOverlayPermission = remember(context) {
                                com.example.core.overlay.SystemNotchOverlayManager.hasOverlayPermission(context)
                            }
                            val isTimerActive = timerState == FocusState.RUNNING || timerState == FocusState.PAUSED || timerState == FocusState.COMPLETED
                            if (!hasOverlayPermission && (floatingNotchTimerEnabled || isTimerActive) && currentScreen != RetroScreen.FOCUS) {
                                val notchAlignment = when (notchTimerPosition) {
                                    com.example.data.NotchPosition.TOP_CENTER -> Alignment.TopCenter
                                    com.example.data.NotchPosition.TOP_RIGHT -> Alignment.TopEnd
                                    com.example.data.NotchPosition.TOP_LEFT -> Alignment.TopStart
                                    com.example.data.NotchPosition.BOTTOM_CENTER -> Alignment.BottomCenter
                                }
                                FloatingNotchTimer(
                                    remainingSeconds = remainingSeconds,
                                    state = timerState,
                                    enabled = true,
                                    isDeepFocus = isDeepFocus,
                                    notchPosition = notchTimerPosition,
                                    targetDurationMinutes = targetMinutes,
                                    activeTask = selectedTargetTask,
                                    ambientSound = ambientSound,
                                    todaySessionCount = todaySessionCount,
                                    calibrationOffsetX = dynamicIslandOffsetX,
                                    calibrationOffsetY = dynamicIslandOffsetY,
                                    calibrationWidthScale = dynamicIslandWidthScale,
                                    calibrationHeightScale = dynamicIslandHeightScale,
                                    onNavigateToFocus = { currentScreen = RetroScreen.FOCUS },
                                    onTogglePlayPause = { viewModel.toggleTimer() },
                                    onReset = { viewModel.resetTimer() },
                                    onAddFiveMinutes = { viewModel.addFiveMinutes() },
                                    onChangePosition = { newPos -> viewModel.userPreferences.setNotchTimerPosition(newPos) },
                                    modifier = Modifier.align(notchAlignment)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
