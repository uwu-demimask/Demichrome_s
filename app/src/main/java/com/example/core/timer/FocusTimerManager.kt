package com.example.core.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.core.overlay.SystemNotchOverlayManager
import com.example.data.AppDatabase
import com.example.data.FocusRepository
import com.example.data.FocusSession
import com.example.data.NotchPosition
import com.example.data.UserPreferences
import com.example.service.FocusTimerService
import com.example.ui.timer.FocusState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Singleton timer manager coordinator that bridges UI (Compose), Picture-in-Picture,
 * Foreground Notification Service, Distraction Blocker, and Background Audio Synthesizer.
 */
object FocusTimerManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var appContext: Context? = null
    private var userPreferences: UserPreferences? = null
    private var repository: FocusRepository? = null

    private val _targetMinutes = MutableStateFlow(25)
    val targetMinutes: StateFlow<Int> = _targetMinutes.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _state = MutableStateFlow(FocusState.READY)
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private val _isDeepFocusActive = MutableStateFlow(false)
    val isDeepFocusActive: StateFlow<Boolean> = _isDeepFocusActive.asStateFlow()

    private val _systemStatus = MutableStateFlow("> SYSTEM READY")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    private val _activeTaskId = MutableStateFlow<Long?>(null)
    val activeTaskId: StateFlow<Long?> = _activeTaskId.asStateFlow()

    private val _pendingRatingSession = MutableStateFlow<FocusSession?>(null)
    val pendingRatingSession: StateFlow<FocusSession?> = _pendingRatingSession.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeMillis: Long = 0L
    private var elapsedSecondsInCurrentSession: Int = 0

    fun init(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app
        val prefs = UserPreferences(app)
        userPreferences = prefs
        val db = AppDatabase.getDatabase(app)
        repository = FocusRepository(db.focusDao(), db.taskDao())

        val defaultDur = prefs.defaultDuration.value
        _targetMinutes.value = defaultDur
        _remainingSeconds.value = defaultDur * 60
    }

    fun setSystemStatus(status: String) {
        _systemStatus.value = status
    }

    fun setActiveTaskId(id: Long?) {
        _activeTaskId.value = id
    }

    fun selectDuration(minutes: Int) {
        if (_state.value == FocusState.RUNNING) return
        _targetMinutes.value = minutes
        _remainingSeconds.value = minutes * 60
        _state.value = FocusState.READY
        _systemStatus.value = "> DURATION SET [${minutes}m]"

        appContext?.let { context ->
            com.example.widget.RetroFocusWidget.updateAllWidgets(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = _isDeepFocusActive.value
            )
        }
    }

    fun addFiveMinutes() {
        _remainingSeconds.value += 5 * 60
        _targetMinutes.value += 5
        _systemStatus.value = "> EXTENDED +5 MIN"
        val context = appContext ?: return
        if (_state.value == FocusState.RUNNING || _state.value == FocusState.PAUSED) {
            FocusTimerService.updateNotification(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = _isDeepFocusActive.value
            )
            val curPos = userPreferences?.notchTimerPosition?.value ?: NotchPosition.TOP_CENTER
            val curEnabled = userPreferences?.floatingNotchTimerEnabled?.value ?: true
            SystemNotchOverlayManager.update(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = _isDeepFocusActive.value,
                notchPosition = curPos,
                enabled = curEnabled
            )
        }
        com.example.widget.RetroFocusWidget.updateAllWidgets(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )
    }

    fun startTimer() {
        if (_state.value == FocusState.RUNNING) return

        val context = appContext ?: return
        val prefs = userPreferences

        if (_state.value == FocusState.READY || _state.value == FocusState.COMPLETED) {
            _remainingSeconds.value = _targetMinutes.value * 60
            sessionStartTimeMillis = System.currentTimeMillis()
            elapsedSecondsInCurrentSession = 0
        }

        _state.value = FocusState.RUNNING
        _systemStatus.value = if (_isDeepFocusActive.value) "> DEEP FOCUS ACTIVE" else "> FOCUS SESSION ACTIVE"

        if (prefs?.soundEnabled?.value == true) {
            scope.launch { RetroAudioSynthesizer.playStartChirp() }
        }
        if (prefs?.vibrationEnabled?.value == true) {
            RetroHaptics.performClick(context)
        }

        if (prefs?.systemMonochromeOnFocus?.value == true) {
            com.example.core.accessibility.SystemDisplayHelper.setDeviceMonochrome(context, true)
        }

        if (prefs != null) {
            RetroAudioSynthesizer.startAmbient(
                prefs.ambientSound.value,
                volume = 0.25f,
                coroutineScope = scope
            )
        }

        startForegroundService()

        // Push floating system notch overlay update (floats above all external apps)
        val notchPos = prefs?.notchTimerPosition?.value ?: NotchPosition.TOP_CENTER
        val floatingEnabled = prefs?.floatingNotchTimerEnabled?.value ?: true
        SystemNotchOverlayManager.update(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value,
            notchPosition = notchPos,
            enabled = floatingEnabled
        )

        // Push widget update
        com.example.widget.RetroFocusWidget.updateAllWidgets(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                elapsedSecondsInCurrentSession += 1

                val mins = _remainingSeconds.value / 60
                val secs = _remainingSeconds.value % 60
                val formatted = String.format("%02d:%02d", mins, secs)

                // Update system-wide floating notch HUD every second
                val curNotchPos = userPreferences?.notchTimerPosition?.value ?: NotchPosition.TOP_CENTER
                val curFloatingEnabled = userPreferences?.floatingNotchTimerEnabled?.value ?: true
                SystemNotchOverlayManager.update(
                    context = context,
                    remainingSeconds = _remainingSeconds.value,
                    targetMinutes = _targetMinutes.value,
                    state = _state.value,
                    isDeepFocus = _isDeepFocusActive.value,
                    notchPosition = curNotchPos,
                    enabled = curFloatingEnabled
                )

                // Update notification & home screen widget
                if (_remainingSeconds.value % 5 == 0) {
                    FocusTimerService.updateNotification(
                        context = context,
                        remainingSeconds = _remainingSeconds.value,
                        targetMinutes = _targetMinutes.value,
                        state = _state.value,
                        isDeepFocus = _isDeepFocusActive.value
                    )
                    com.example.widget.RetroFocusWidget.updateAllWidgets(
                        context = context,
                        remainingSeconds = _remainingSeconds.value,
                        targetMinutes = _targetMinutes.value,
                        state = _state.value,
                        isDeepFocus = _isDeepFocusActive.value
                    )
                }

                if (_remainingSeconds.value % 60 == 0 && _remainingSeconds.value > 0) {
                    _systemStatus.value = "> FOCUS ACTIVE: $formatted REMAINING"
                }

                if (_remainingSeconds.value == 0) {
                    onSessionCompleted()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        if (_state.value != FocusState.RUNNING) return
        val context = appContext ?: return
        val prefs = userPreferences

        timerJob?.cancel()
        _state.value = FocusState.PAUSED
        _systemStatus.value = "> TIMER PAUSED"

        RetroAudioSynthesizer.stopAmbient()

        if (prefs?.soundEnabled?.value == true) {
            scope.launch { RetroAudioSynthesizer.playPauseTone() }
        }
        if (prefs?.vibrationEnabled?.value == true) {
            RetroHaptics.performClick(context)
        }

        FocusTimerService.updateNotification(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )

        val notchPos = prefs?.notchTimerPosition?.value ?: NotchPosition.TOP_CENTER
        val floatingEnabled = prefs?.floatingNotchTimerEnabled?.value ?: true
        SystemNotchOverlayManager.update(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value,
            notchPosition = notchPos,
            enabled = floatingEnabled
        )

        com.example.widget.RetroFocusWidget.updateAllWidgets(
            context = context,
            remainingSeconds = _remainingSeconds.value,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )
    }

    fun resetTimer() {
        val context = appContext
        timerJob?.cancel()
        RetroAudioSynthesizer.stopAmbient()

        val elapsed = elapsedSecondsInCurrentSession
        if (elapsed > 60 && _state.value != FocusState.COMPLETED) {
            saveSessionRecord(completed = false, actualSeconds = elapsed)
        }

        _remainingSeconds.value = _targetMinutes.value * 60
        _state.value = FocusState.READY
        elapsedSecondsInCurrentSession = 0
        _systemStatus.value = "> TIMER RESET"

        // Hide floating system notch overlay
        SystemNotchOverlayManager.hide()

        // Dismiss any distraction blocking overlay & unlock apps
        com.example.core.blocker.DistractionBlockerManager.dismissInterception()

        if (userPreferences?.systemMonochromeOnFocus?.value == true && context != null) {
            com.example.core.accessibility.SystemDisplayHelper.setDeviceMonochrome(context, false)
        }

        if (context != null) {
            if (userPreferences?.vibrationEnabled?.value == true) {
                RetroHaptics.performClick(context)
            }
            stopForegroundService()
            com.example.widget.RetroFocusWidget.updateAllWidgets(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = _isDeepFocusActive.value
            )
            com.example.widget.RetroStatsWidget.updateAllWidgets(context)
        }
    }

    fun toggleDeepFocusMode() {
        val next = !_isDeepFocusActive.value
        setDeepFocusMode(next)
    }

    fun setDeepFocusMode(active: Boolean) {
        _isDeepFocusActive.value = active
        val context = appContext
        val prefs = userPreferences

        if (active) {
            _systemStatus.value = "> DEEP FOCUS ACTIVE [LOCK ENGAGED]"
            if (_state.value == FocusState.RUNNING && prefs != null) {
                RetroAudioSynthesizer.startAmbient(prefs.ambientSound.value, volume = 0.25f, coroutineScope = scope)
            }
        } else {
            _systemStatus.value = "> DEEP FOCUS DISENGAGED"
            RetroAudioSynthesizer.stopAmbient()
        }

        if (context != null && prefs?.vibrationEnabled?.value == true) {
            RetroHaptics.performHeavyClick(context)
        }

        if (context != null && (_state.value == FocusState.RUNNING || _state.value == FocusState.PAUSED)) {
            FocusTimerService.updateNotification(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = active
            )
            val curPos = prefs?.notchTimerPosition?.value ?: NotchPosition.TOP_CENTER
            val curEnabled = prefs?.floatingNotchTimerEnabled?.value ?: true
            SystemNotchOverlayManager.update(
                context = context,
                remainingSeconds = _remainingSeconds.value,
                targetMinutes = _targetMinutes.value,
                state = _state.value,
                isDeepFocus = active,
                notchPosition = curPos,
                enabled = curEnabled
            )
        }
    }

    private fun onSessionCompleted() {
        val context = appContext ?: return
        val prefs = userPreferences

        _state.value = FocusState.COMPLETED
        _systemStatus.value = "> SESSION COMPLETE"

        // Hide floating system notch overlay
        SystemNotchOverlayManager.hide()

        if (prefs?.systemMonochromeOnFocus?.value == true) {
            com.example.core.accessibility.SystemDisplayHelper.setDeviceMonochrome(context, false)
        }

        RetroAudioSynthesizer.stopAmbient()

        val durationMin = _targetMinutes.value
        val actualSecs = durationMin * 60
        saveSessionRecord(completed = true, actualSeconds = actualSecs)

        // Increment session count on active task if one is selected
        _activeTaskId.value?.let { taskId ->
            scope.launch {
                repository?.incrementTaskSessions(taskId)
            }
        }

        if (prefs?.soundEnabled?.value == true) {
            scope.launch { RetroAudioSynthesizer.playCompletionArpeggio() }
        }
        if (prefs?.vibrationEnabled?.value == true) {
            RetroHaptics.performCompletionBuzz(context)
        }

        FocusTimerService.updateNotification(
            context = context,
            remainingSeconds = 0,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )

        // Automatically dismiss any active shield interception & unlock all apps
        com.example.core.blocker.DistractionBlockerManager.dismissInterception()

        // Push immediate updates to all home screen widgets
        com.example.widget.RetroFocusWidget.updateAllWidgets(
            context = context,
            remainingSeconds = 0,
            targetMinutes = _targetMinutes.value,
            state = _state.value,
            isDeepFocus = _isDeepFocusActive.value
        )
        com.example.widget.RetroStatsWidget.updateAllWidgets(context)
    }

    private fun saveSessionRecord(completed: Boolean, actualSeconds: Int) {
        val repo = repository ?: return
        val prefs = userPreferences ?: return
        scope.launch {
            val session = FocusSession(
                startTimeMillis = if (sessionStartTimeMillis > 0) sessionStartTimeMillis else System.currentTimeMillis(),
                targetDurationMinutes = _targetMinutes.value,
                actualElapsedSeconds = actualSeconds,
                completedSuccessfully = completed,
                tag = if (_isDeepFocusActive.value) "DEEP_FOCUS" else "FOCUS",
                styleUsed = prefs.timerStyle.value.name
            )
            val id = repo.saveSession(session)
            if (completed && actualSeconds >= 60) {
                _pendingRatingSession.value = session.copy(id = id)
            }
        }
    }

    fun submitSessionRating(sessionId: Long, rating: Int, note: String) {
        scope.launch {
            repository?.updateSessionRating(sessionId, rating, note)
            _pendingRatingSession.value = null
            _systemStatus.value = "> SESSION RATED [★ $rating/5]"
        }
    }

    fun clearPendingRating() {
        _pendingRatingSession.value = null
    }

    private fun startForegroundService() {
        val context = appContext ?: return
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START_FOREGROUND
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {
        }
    }

    private fun stopForegroundService() {
        val context = appContext ?: return
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP_FOREGROUND
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {
        }
    }
}
