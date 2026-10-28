package com.example

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.os.DemichromeOSApp
import com.example.ui.focus.FocusViewModel
import com.example.ui.navigation.RetroScreen
import com.example.ui.timer.FocusState

/**
 * DEMICHROME OS
 *
 * Dedicated productivity and life-management operating system.
 * Contains:
 * - Focus Timer / Pomodoro / Zen Sprint
 * - Tasks & 24H Daily Routine Planner
 * - Habit Tracker Sanctuary
 * - Spaced-Repetition Flashcards
 * - Demichrome Money & Expense Tracking
 * - App & Web Distraction Shield
 * - Productivity Statistics
 * - Settings & Retro Monochrome CRT Themes
 *
 * Made by PMAK. "Make your phone quieter."
 */
open class MainActivity : ComponentActivity() {

    protected val viewModel: FocusViewModel by viewModels()
    private var isPipModeActive by mutableStateOf(false)
    private var initialScreen by mutableStateOf(RetroScreen.FOCUS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLinkIntent(intent)

        setContent {
            val keepScreenOn by viewModel.userPreferences.keepScreenOn.collectAsState()
            val timerState by viewModel.state.collectAsState()

            DisposableEffect(keepScreenOn, timerState) {
                if (keepScreenOn && timerState == FocusState.RUNNING) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            DemichromeOSApp(
                viewModel = viewModel,
                initialScreen = initialScreen,
                isPipModeActive = isPipModeActive,
                onEnterPip = { enterPipMode() },
                onExitPip = { isPipModeActive = false }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)

        val pkg = intent.getStringExtra("EXTRA_INTERCEPTED_PKG")
        val name = intent.getStringExtra("EXTRA_INTERCEPTED_NAME")
        if (pkg != null && name != null) {
            viewModel.simulateDistractionInterception(pkg, name)
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val navExtra = intent?.getStringExtra("EXTRA_NAVIGATE_TO")
        if (navExtra != null) {
            initialScreen = when (navExtra.uppercase()) {
                "FOCUS" -> RetroScreen.FOCUS
                "TASKS" -> RetroScreen.TASKS
                "HABITS" -> RetroScreen.HABITS
                "MONEY" -> RetroScreen.MONEY
                "CARDS", "FLASHCARDS" -> RetroScreen.FLASHCARDS
                "SHIELD" -> RetroScreen.SHIELD
                "STATS" -> RetroScreen.STATS
                "SETTINGS" -> RetroScreen.SETTINGS
                "MONO" -> RetroScreen.MONO
                else -> RetroScreen.FOCUS
            }
        }

        val sprintMins = intent?.getIntExtra("EXTRA_START_SPRINT_MINUTES", -1) ?: -1
        if (sprintMins > 0) {
            viewModel.selectDuration(sprintMins)
            viewModel.startTimer()
        }

        val uri = intent?.data ?: return
        if (uri.scheme == "demichrome") {
            initialScreen = when (uri.host?.lowercase()) {
                "focus" -> RetroScreen.FOCUS
                "tasks" -> RetroScreen.TASKS
                "habits" -> RetroScreen.HABITS
                "money" -> RetroScreen.MONEY
                "flashcards" -> RetroScreen.FLASHCARDS
                "shield" -> RetroScreen.SHIELD
                "stats" -> RetroScreen.STATS
                "settings" -> RetroScreen.SETTINGS
                else -> RetroScreen.FOCUS
            }
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(4, 3))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val currentState = viewModel.state.value
        if (currentState == FocusState.RUNNING || currentState == FocusState.PAUSED) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipModeActive = isInPictureInPictureMode
    }
}
