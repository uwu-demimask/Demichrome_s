package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.core.audio.RetroAudioSynthesizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RetroThemeId {
    DEMICHROME,
    DARK_TERMINAL,
    PAPER,
    NIGHT,
    CUSTOM,
    CRT,
    TERMINAL,
    INDUSTRIAL,
    PURE_MONOCHROME,
    CRT_GREEN,
    CRT_AMBER,
    TERMINAL_MATRIX,
    RETRO_WINDOWS,
    PIXEL_ARCADE,
    MINIMAL_EINK,
    DARK_PAPER
}

enum class TimerStyleId {
    CLASSIC,
    CRT,
    TERMINAL,
    MINIMAL
}

enum class NotchPosition {
    TOP_CENTER,
    TOP_RIGHT,
    TOP_LEFT,
    BOTTOM_CENTER
}

enum class MonochromeModeLevel(val title: String, val description: String, val saturation: Float) {
    OFF("OFF", "Normal full colors across display.", 1.0f),
    SUBTLE("SUBTLE", "Muted 35% desaturated retro palette.", 0.35f),
    FULL("FULL", "Pure 100% grayscale monochrome.", 0.0f)
}

enum class NavigationStyle(val label: String, val description: String) {
    EDGE_WHEEL("EDGE WHEEL", "Tactile rotary dial pinned to edge with haptic index notches"),
    NORMAL_DOCK("NORMAL DOCK", "Classic retro industrial bottom bar with status telemetry"),
    LIQUID_GLASS_DOCK("LIQUID GLASS DOCK", "Floating frosted glass capsule with specular border & fluid active pill")
}

class UserPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("monochrome_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<RetroThemeId> = _theme.asStateFlow()

    private val _navigationStyle = MutableStateFlow(loadNavigationStyle())
    val navigationStyle: StateFlow<NavigationStyle> = _navigationStyle.asStateFlow()

    private val _monochromeModeLevel = MutableStateFlow(loadMonochromeModeLevel())
    val monochromeModeLevel: StateFlow<MonochromeModeLevel> = _monochromeModeLevel.asStateFlow()

    private val _monochromeMode = MutableStateFlow(prefs.getBoolean(KEY_MONOCHROME_MODE, false))
    val monochromeMode: StateFlow<Boolean> = _monochromeMode.asStateFlow()

    private val _crtEffect = MutableStateFlow(prefs.getBoolean(KEY_CRT_EFFECT, false))
    val crtEffect: StateFlow<Boolean> = _crtEffect.asStateFlow()

    private val _scanlineIntensity = MutableStateFlow(prefs.getFloat(KEY_SCANLINE_INTENSITY, 0.25f))
    val scanlineIntensity: StateFlow<Float> = _scanlineIntensity.asStateFlow()

    private val _crtFlicker = MutableStateFlow(prefs.getBoolean(KEY_CRT_FLICKER, true))
    val crtFlicker: StateFlow<Boolean> = _crtFlicker.asStateFlow()

    private val _crtCurvature = MutableStateFlow(prefs.getBoolean(KEY_CRT_CURVATURE, true))
    val crtCurvature: StateFlow<Boolean> = _crtCurvature.asStateFlow()

    private val _fontScale = MutableStateFlow(prefs.getFloat(KEY_FONT_SCALE, 1.0f))
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _appScale = MutableStateFlow(prefs.getFloat(KEY_APP_SCALE, 1.0f))
    val appScale: StateFlow<Float> = _appScale.asStateFlow()

    private val _notchTimerPosition = MutableStateFlow(loadNotchPosition())
    val notchTimerPosition: StateFlow<NotchPosition> = _notchTimerPosition.asStateFlow()

    private val _timerStyle = MutableStateFlow(loadTimerStyle())
    val timerStyle: StateFlow<TimerStyleId> = _timerStyle.asStateFlow()

    private val _defaultDuration = MutableStateFlow(prefs.getInt(KEY_DEFAULT_DURATION, 25))
    val defaultDuration: StateFlow<Int> = _defaultDuration.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION_ENABLED, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean(KEY_KEEP_SCREEN_ON, true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _vaultUnlocked = MutableStateFlow(prefs.getBoolean(KEY_VAULT_UNLOCKED, false))
    val vaultUnlocked: StateFlow<Boolean> = _vaultUnlocked.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCED_MOTION, false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private val _hasSeenGuide = MutableStateFlow(prefs.getBoolean(KEY_HAS_SEEN_GUIDE, false))
    val hasSeenGuide: StateFlow<Boolean> = _hasSeenGuide.asStateFlow()

    private val _ambientSound = MutableStateFlow(loadAmbientSound())
    val ambientSound: StateFlow<RetroAudioSynthesizer.AmbientSoundType> = _ambientSound.asStateFlow()

    private val _strictExitLock = MutableStateFlow(prefs.getBoolean(KEY_STRICT_EXIT_LOCK, true))
    val strictExitLock: StateFlow<Boolean> = _strictExitLock.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt(KEY_DAILY_GOAL_MINUTES, 60))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _blockerEnabled = MutableStateFlow(prefs.getBoolean(KEY_BLOCKER_ENABLED, true))
    val blockerEnabled: StateFlow<Boolean> = _blockerEnabled.asStateFlow()

    private val _blockedPackages = MutableStateFlow(loadBlockedPackages())
    val blockedPackages: StateFlow<Set<String>> = _blockedPackages.asStateFlow()

    private val _blockerStrictMode = MutableStateFlow(prefs.getBoolean(KEY_BLOCKER_STRICT, false))
    val blockerStrictMode: StateFlow<Boolean> = _blockerStrictMode.asStateFlow()

    private val _blockedAttemptsToday = MutableStateFlow(prefs.getInt(KEY_BLOCKED_ATTEMPTS, 0))
    val blockedAttemptsToday: StateFlow<Int> = _blockedAttemptsToday.asStateFlow()

    private val _systemMonochromeOnFocus = MutableStateFlow(prefs.getBoolean(KEY_SYSTEM_MONOCHROME_FOCUS, false))
    val systemMonochromeOnFocus: StateFlow<Boolean> = _systemMonochromeOnFocus.asStateFlow()

    private val _floatingNotchTimerEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLOATING_NOTCH_TIMER, true))
    val floatingNotchTimerEnabled: StateFlow<Boolean> = _floatingNotchTimerEnabled.asStateFlow()

    private val _edgeWheelHapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_EDGE_WHEEL_HAPTICS_ENABLED, true))
    val edgeWheelHapticsEnabled: StateFlow<Boolean> = _edgeWheelHapticsEnabled.asStateFlow()

    private val _edgeWheelSide = MutableStateFlow(prefs.getString(KEY_EDGE_WHEEL_SIDE, "RIGHT") ?: "RIGHT")
    val edgeWheelSide: StateFlow<String> = _edgeWheelSide.asStateFlow()

    private val _edgeWheelSensitivity = MutableStateFlow(prefs.getFloat(KEY_EDGE_WHEEL_SENSITIVITY, 1.0f))
    val edgeWheelSensitivity: StateFlow<Float> = _edgeWheelSensitivity.asStateFlow()

    private val _edgeWheelPosition = MutableStateFlow(prefs.getString(KEY_EDGE_WHEEL_POSITION, "THUMB") ?: "THUMB")
    val edgeWheelPosition: StateFlow<String> = _edgeWheelPosition.asStateFlow()

    private val _dynamicIslandOffsetX = MutableStateFlow(prefs.getInt(KEY_DYNAMIC_ISLAND_OFFSET_X, 0))
    val dynamicIslandOffsetX: StateFlow<Int> = _dynamicIslandOffsetX.asStateFlow()

    private val _dynamicIslandOffsetY = MutableStateFlow(prefs.getInt(KEY_DYNAMIC_ISLAND_OFFSET_Y, 0))
    val dynamicIslandOffsetY: StateFlow<Int> = _dynamicIslandOffsetY.asStateFlow()

    private val _dynamicIslandWidthScale = MutableStateFlow(prefs.getFloat(KEY_DYNAMIC_ISLAND_WIDTH_SCALE, 1.0f))
    val dynamicIslandWidthScale: StateFlow<Float> = _dynamicIslandWidthScale.asStateFlow()

    private val _dynamicIslandHeightScale = MutableStateFlow(prefs.getFloat(KEY_DYNAMIC_ISLAND_HEIGHT_SCALE, 1.0f))
    val dynamicIslandHeightScale: StateFlow<Float> = _dynamicIslandHeightScale.asStateFlow()

    private val _webShieldEnabled = MutableStateFlow(prefs.getBoolean(KEY_WEB_SHIELD_ENABLED, true))
    val webShieldEnabled: StateFlow<Boolean> = _webShieldEnabled.asStateFlow()

    private val _webShieldBlockedDomains = MutableStateFlow(loadBlockedWebDomains())
    val webShieldBlockedDomains: StateFlow<Set<String>> = _webShieldBlockedDomains.asStateFlow()

    private val _quickNotes = MutableStateFlow(prefs.getString(KEY_QUICK_NOTES, "") ?: "")
    val quickNotes: StateFlow<String> = _quickNotes.asStateFlow()

    private val _dailyTaskLastResetDate = MutableStateFlow(prefs.getString(KEY_DAILY_TASK_LAST_RESET_DATE, "") ?: "")
    val dailyTaskLastResetDate: StateFlow<String> = _dailyTaskLastResetDate.asStateFlow()

    private val _glassIntensity = MutableStateFlow(prefs.getFloat(KEY_GLASS_INTENSITY, 0.75f))
    val glassIntensity: StateFlow<Float> = _glassIntensity.asStateFlow()

    private val _glassBlurEnabled = MutableStateFlow(prefs.getBoolean(KEY_GLASS_BLUR_ENABLED, true))
    val glassBlurEnabled: StateFlow<Boolean> = _glassBlurEnabled.asStateFlow()

    private val _cornerRadiusDp = MutableStateFlow(prefs.getInt(KEY_CORNER_RADIUS_DP, 22))
    val cornerRadiusDp: StateFlow<Int> = _cornerRadiusDp.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat(KEY_MONTHLY_BUDGET, 15000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _customThemeBg = MutableStateFlow(prefs.getLong(KEY_CUSTOM_BG, 0xFF101014L))
    val customThemeBg: StateFlow<Long> = _customThemeBg.asStateFlow()

    private val _customThemeSurface = MutableStateFlow(prefs.getLong(KEY_CUSTOM_SURFACE, 0xFF191A20L))
    val customThemeSurface: StateFlow<Long> = _customThemeSurface.asStateFlow()

    private val _customThemeTextPrimary = MutableStateFlow(prefs.getLong(KEY_CUSTOM_TEXT_PRIMARY, 0xFFF0F0F5L))
    val customThemeTextPrimary: StateFlow<Long> = _customThemeTextPrimary.asStateFlow()

    private val _customThemeTextSecondary = MutableStateFlow(prefs.getLong(KEY_CUSTOM_TEXT_SECONDARY, 0xFF8A8C99L))
    val customThemeTextSecondary: StateFlow<Long> = _customThemeTextSecondary.asStateFlow()

    private val _customThemeAccent = MutableStateFlow(prefs.getLong(KEY_CUSTOM_ACCENT, 0xFFFFFFFFL))
    val customThemeAccent: StateFlow<Long> = _customThemeAccent.asStateFlow()

    private val _customThemeBorder = MutableStateFlow(prefs.getLong(KEY_CUSTOM_BORDER, 0xFF383A44L))
    val customThemeBorder: StateFlow<Long> = _customThemeBorder.asStateFlow()

    private fun loadBlockedWebDomains(): Set<String> {
        val stored = prefs.getStringSet(KEY_WEB_SHIELD_DOMAINS, null)
        return stored ?: DEFAULT_BLOCKED_DOMAINS
    }

    private fun loadBlockedPackages(): Set<String> {
        val stored = prefs.getStringSet(KEY_BLOCKED_PACKAGES, null)
        return stored ?: DEFAULT_BLOCKED_PACKAGES
    }

    private fun loadAmbientSound(): RetroAudioSynthesizer.AmbientSoundType {
        val name = prefs.getString(KEY_AMBIENT_SOUND, RetroAudioSynthesizer.AmbientSoundType.NONE.name)
        return try {
            RetroAudioSynthesizer.AmbientSoundType.valueOf(name ?: RetroAudioSynthesizer.AmbientSoundType.NONE.name)
        } catch (_: Exception) {
            RetroAudioSynthesizer.AmbientSoundType.NONE
        }
    }

    private fun loadNotchPosition(): NotchPosition {
        val name = prefs.getString(KEY_NOTCH_POSITION, NotchPosition.TOP_CENTER.name)
        return try {
            NotchPosition.valueOf(name ?: NotchPosition.TOP_CENTER.name)
        } catch (_: Exception) {
            NotchPosition.TOP_CENTER
        }
    }

    private fun loadNavigationStyle(): NavigationStyle {
        val name = prefs.getString(KEY_NAVIGATION_STYLE, NavigationStyle.EDGE_WHEEL.name)
        return try {
            NavigationStyle.valueOf(name ?: NavigationStyle.EDGE_WHEEL.name)
        } catch (_: Exception) {
            NavigationStyle.EDGE_WHEEL
        }
    }

    private fun loadTheme(): RetroThemeId {
        val name = prefs.getString(KEY_THEME, RetroThemeId.DEMICHROME.name)
        return try {
            RetroThemeId.valueOf(name ?: RetroThemeId.DEMICHROME.name)
        } catch (_: Exception) {
            RetroThemeId.DEMICHROME
        }
    }

    private fun loadTimerStyle(): TimerStyleId {
        val name = prefs.getString(KEY_TIMER_STYLE, TimerStyleId.CLASSIC.name)
        return try {
            TimerStyleId.valueOf(name ?: TimerStyleId.CLASSIC.name)
        } catch (_: Exception) {
            TimerStyleId.CLASSIC
        }
    }

    fun setTheme(themeId: RetroThemeId) {
        prefs.edit().putString(KEY_THEME, themeId.name).apply()
        _theme.value = themeId
    }

    fun setMonochromeMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONOCHROME_MODE, enabled).apply()
        _monochromeMode.value = enabled
        _monochromeModeLevel.value = if (enabled) MonochromeModeLevel.FULL else MonochromeModeLevel.OFF
    }

    fun setMonochromeModeLevel(level: MonochromeModeLevel) {
        prefs.edit().putString(KEY_MONOCHROME_MODE_LEVEL, level.name).apply()
        _monochromeModeLevel.value = level
        val isEnabled = level != MonochromeModeLevel.OFF
        prefs.edit().putBoolean(KEY_MONOCHROME_MODE, isEnabled).apply()
        _monochromeMode.value = isEnabled
    }

    private fun loadMonochromeModeLevel(): MonochromeModeLevel {
        val saved = prefs.getString(KEY_MONOCHROME_MODE_LEVEL, null)
        if (saved != null) {
            return try { MonochromeModeLevel.valueOf(saved) } catch (_: Exception) { MonochromeModeLevel.OFF }
        }
        val legacy = prefs.getBoolean(KEY_MONOCHROME_MODE, false)
        return if (legacy) MonochromeModeLevel.FULL else MonochromeModeLevel.OFF
    }

    fun setCrtEffect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CRT_EFFECT, enabled).apply()
        _crtEffect.value = enabled
    }

    fun setScanlineIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_SCANLINE_INTENSITY, intensity).apply()
        _scanlineIntensity.value = intensity
    }

    fun setCrtFlicker(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CRT_FLICKER, enabled).apply()
        _crtFlicker.value = enabled
    }

    fun setTimerStyle(style: TimerStyleId) {
        prefs.edit().putString(KEY_TIMER_STYLE, style.name).apply()
        _timerStyle.value = style
    }

    fun setDefaultDuration(minutes: Int) {
        prefs.edit().putInt(KEY_DEFAULT_DURATION, minutes).apply()
        _defaultDuration.value = minutes
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _keepScreenOn.value = enabled
    }

    fun setVaultUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_VAULT_UNLOCKED, unlocked).apply()
        _vaultUnlocked.value = unlocked
    }

    fun setReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _reducedMotion.value = enabled
    }

    fun setHasSeenGuide(seen: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEN_GUIDE, seen).apply()
        _hasSeenGuide.value = seen
    }

    fun setAmbientSound(ambient: RetroAudioSynthesizer.AmbientSoundType) {
        prefs.edit().putString(KEY_AMBIENT_SOUND, ambient.name).apply()
        _ambientSound.value = ambient
    }

    fun setStrictExitLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_EXIT_LOCK, enabled).apply()
        _strictExitLock.value = enabled
    }

    fun setDailyGoalMinutes(minutes: Int) {
        val coerced = minutes.coerceIn(10, 480)
        prefs.edit().putInt(KEY_DAILY_GOAL_MINUTES, coerced).apply()
        _dailyGoalMinutes.value = coerced
    }

    fun setBlockerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCKER_ENABLED, enabled).apply()
        _blockerEnabled.value = enabled
    }

    fun setBlockerStrictMode(strict: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCKER_STRICT, strict).apply()
        _blockerStrictMode.value = strict
    }

    fun toggleBlockedPackage(packageName: String) {
        val current = _blockedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, current).apply()
        _blockedPackages.value = current
    }

    fun setBlockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, packages).apply()
        _blockedPackages.value = packages
    }

    fun incrementBlockedAttempts() {
        val count = _blockedAttemptsToday.value + 1
        prefs.edit().putInt(KEY_BLOCKED_ATTEMPTS, count).apply()
        _blockedAttemptsToday.value = count
    }

    fun resetBlockedAttempts() {
        prefs.edit().putInt(KEY_BLOCKED_ATTEMPTS, 0).apply()
        _blockedAttemptsToday.value = 0
    }

    fun setSystemMonochromeOnFocus(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYSTEM_MONOCHROME_FOCUS, enabled).apply()
        _systemMonochromeOnFocus.value = enabled
    }

    fun setFloatingNotchTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_NOTCH_TIMER, enabled).apply()
        _floatingNotchTimerEnabled.value = enabled
    }

    fun setEdgeWheelHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EDGE_WHEEL_HAPTICS_ENABLED, enabled).apply()
        _edgeWheelHapticsEnabled.value = enabled
    }

    fun setWebShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEB_SHIELD_ENABLED, enabled).apply()
        _webShieldEnabled.value = enabled
    }

    fun toggleBlockedWebDomain(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
        if (clean.isBlank()) return
        val current = _webShieldBlockedDomains.value.toMutableSet()
        if (current.contains(clean)) {
            current.remove(clean)
        } else {
            current.add(clean)
        }
        prefs.edit().putStringSet(KEY_WEB_SHIELD_DOMAINS, current).apply()
        _webShieldBlockedDomains.value = current
    }

    fun addBlockedWebDomain(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
        if (clean.isBlank()) return
        val current = _webShieldBlockedDomains.value.toMutableSet()
        current.add(clean)
        prefs.edit().putStringSet(KEY_WEB_SHIELD_DOMAINS, current).apply()
        _webShieldBlockedDomains.value = current
    }

    fun removeBlockedWebDomain(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
        val current = _webShieldBlockedDomains.value.toMutableSet()
        current.remove(clean)
        prefs.edit().putStringSet(KEY_WEB_SHIELD_DOMAINS, current).apply()
        _webShieldBlockedDomains.value = current
    }

    fun setBlockedWebDomains(domains: Set<String>) {
        val cleaned = domains.map { it.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.") }.filter { it.isNotBlank() }.toSet()
        prefs.edit().putStringSet(KEY_WEB_SHIELD_DOMAINS, cleaned).apply()
        _webShieldBlockedDomains.value = cleaned
    }

    fun setCrtCurvature(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CRT_CURVATURE, enabled).apply()
        _crtCurvature.value = enabled
    }

    fun setFontScale(scale: Float) {
        val coerced = scale.coerceIn(0.75f, 1.5f)
        prefs.edit().putFloat(KEY_FONT_SCALE, coerced).apply()
        _fontScale.value = coerced
    }

    fun setAppScale(scale: Float) {
        val coerced = scale.coerceIn(0.80f, 1.35f)
        prefs.edit().putFloat(KEY_APP_SCALE, coerced).apply()
        _appScale.value = coerced
    }

    fun setNavigationStyle(style: NavigationStyle) {
        prefs.edit().putString(KEY_NAVIGATION_STYLE, style.name).apply()
        _navigationStyle.value = style
    }

    fun setNotchTimerPosition(position: NotchPosition) {
        prefs.edit().putString(KEY_NOTCH_POSITION, position.name).apply()
        _notchTimerPosition.value = position
    }

    fun setQuickNotes(notes: String) {
        prefs.edit().putString(KEY_QUICK_NOTES, notes).apply()
        _quickNotes.value = notes
        try {
            com.example.widget.RetroQuickNoteWidget.updateAllWidgets(context)
        } catch (_: Exception) {}
    }

    fun setDailyTaskLastResetDate(date: String) {
        prefs.edit().putString(KEY_DAILY_TASK_LAST_RESET_DATE, date).apply()
        _dailyTaskLastResetDate.value = date
    }

    fun setGlassIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_GLASS_INTENSITY, intensity).apply()
        _glassIntensity.value = intensity
    }

    fun setGlassBlurEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLASS_BLUR_ENABLED, enabled).apply()
        _glassBlurEnabled.value = enabled
    }

    fun setCornerRadiusDp(radius: Int) {
        prefs.edit().putInt(KEY_CORNER_RADIUS_DP, radius).apply()
        _cornerRadiusDp.value = radius
    }

    fun setMonthlyBudget(budget: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        _monthlyBudget.value = budget
    }

    fun setCustomThemeColors(
        bg: Long,
        surface: Long,
        textPrimary: Long,
        textSecondary: Long,
        accent: Long,
        border: Long
    ) {
        prefs.edit()
            .putLong(KEY_CUSTOM_BG, bg)
            .putLong(KEY_CUSTOM_SURFACE, surface)
            .putLong(KEY_CUSTOM_TEXT_PRIMARY, textPrimary)
            .putLong(KEY_CUSTOM_TEXT_SECONDARY, textSecondary)
            .putLong(KEY_CUSTOM_ACCENT, accent)
            .putLong(KEY_CUSTOM_BORDER, border)
            .apply()
        _customThemeBg.value = bg
        _customThemeSurface.value = surface
        _customThemeTextPrimary.value = textPrimary
        _customThemeTextSecondary.value = textSecondary
        _customThemeAccent.value = accent
        _customThemeBorder.value = border
    }

    fun setDynamicIslandCalibration(
        offsetX: Int,
        offsetY: Int,
        widthScale: Float,
        heightScale: Float
    ) {
        prefs.edit()
            .putInt(KEY_DYNAMIC_ISLAND_OFFSET_X, offsetX)
            .putInt(KEY_DYNAMIC_ISLAND_OFFSET_Y, offsetY)
            .putFloat(KEY_DYNAMIC_ISLAND_WIDTH_SCALE, widthScale.coerceIn(0.6f, 1.6f))
            .putFloat(KEY_DYNAMIC_ISLAND_HEIGHT_SCALE, heightScale.coerceIn(0.6f, 1.6f))
            .apply()
        _dynamicIslandOffsetX.value = offsetX
        _dynamicIslandOffsetY.value = offsetY
        _dynamicIslandWidthScale.value = widthScale.coerceIn(0.6f, 1.6f)
        _dynamicIslandHeightScale.value = heightScale.coerceIn(0.6f, 1.6f)
    }

    fun resetDynamicIslandCalibration() {
        setDynamicIslandCalibration(0, 0, 1.0f, 1.0f)
    }

    fun setEdgeWheelSide(side: String) {
        val normalized = if (side.equals("LEFT", ignoreCase = true)) "LEFT" else "RIGHT"
        prefs.edit().putString(KEY_EDGE_WHEEL_SIDE, normalized).apply()
        _edgeWheelSide.value = normalized
    }

    fun setEdgeWheelSensitivity(sensitivity: Float) {
        val clamped = sensitivity.coerceIn(0.5f, 2.5f)
        prefs.edit().putFloat(KEY_EDGE_WHEEL_SENSITIVITY, clamped).apply()
        _edgeWheelSensitivity.value = clamped
    }

    fun setEdgeWheelPosition(pos: String) {
        val normalized = when (pos.uppercase()) {
            "UPPER" -> "UPPER"
            "CENTER" -> "CENTER"
            else -> "THUMB"
        }
        prefs.edit().putString(KEY_EDGE_WHEEL_POSITION, normalized).apply()
        _edgeWheelPosition.value = normalized
    }

    companion object {
        val DEFAULT_BLOCKED_DOMAINS = setOf(
            "youtube.com",
            "instagram.com",
            "reddit.com",
            "tiktok.com",
            "twitter.com",
            "x.com",
            "facebook.com",
            "netflix.com",
            "twitch.tv",
            "pinterest.com",
            "threads.net"
        )

        val DEFAULT_BLOCKED_PACKAGES = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.twitter.android",
            "com.facebook.katana",
            "com.reddit.frontpage",
            "com.snapchat.android",
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "tv.twitch.android.app",
            "com.discord",
            "com.android.chrome",
            "com.pinterest"
        )

        private const val KEY_THEME = "key_theme"
        private const val KEY_NAVIGATION_STYLE = "key_navigation_style"
        private const val KEY_MONOCHROME_MODE = "key_monochrome_mode"
        private const val KEY_MONOCHROME_MODE_LEVEL = "key_monochrome_mode_level"
        private const val KEY_CRT_EFFECT = "key_crt_effect"
        private const val KEY_SCANLINE_INTENSITY = "key_scanline_intensity"
        private const val KEY_CRT_FLICKER = "key_crt_flicker"
        private const val KEY_CRT_CURVATURE = "key_crt_curvature"
        private const val KEY_FONT_SCALE = "key_font_scale"
        private const val KEY_APP_SCALE = "key_app_scale"
        private const val KEY_NOTCH_POSITION = "key_notch_position"
        private const val KEY_TIMER_STYLE = "key_timer_style"
        private const val KEY_DEFAULT_DURATION = "key_default_duration"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_KEEP_SCREEN_ON = "key_keep_screen_on"
        private const val KEY_VAULT_UNLOCKED = "key_vault_unlocked"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"
        private const val KEY_HAS_SEEN_GUIDE = "key_has_seen_guide"
        private const val KEY_AMBIENT_SOUND = "key_ambient_sound"
        private const val KEY_STRICT_EXIT_LOCK = "key_strict_exit_lock"
        private const val KEY_DAILY_GOAL_MINUTES = "key_daily_goal_minutes"
        private const val KEY_BLOCKER_ENABLED = "key_blocker_enabled"
        private const val KEY_BLOCKER_STRICT = "key_blocker_strict"
        private const val KEY_BLOCKED_PACKAGES = "key_blocked_packages"
        private const val KEY_BLOCKED_ATTEMPTS = "key_blocked_attempts"
        private const val KEY_SYSTEM_MONOCHROME_FOCUS = "key_system_monochrome_focus"
        private const val KEY_FLOATING_NOTCH_TIMER = "key_floating_notch_timer"
        private const val KEY_EDGE_WHEEL_HAPTICS_ENABLED = "key_edge_wheel_haptics_enabled"
        private const val KEY_EDGE_WHEEL_SIDE = "key_edge_wheel_side"
        private const val KEY_EDGE_WHEEL_SENSITIVITY = "key_edge_wheel_sensitivity"
        private const val KEY_EDGE_WHEEL_POSITION = "key_edge_wheel_position"
        private const val KEY_DYNAMIC_ISLAND_OFFSET_X = "key_dynamic_island_offset_x"
        private const val KEY_DYNAMIC_ISLAND_OFFSET_Y = "key_dynamic_island_offset_y"
        private const val KEY_DYNAMIC_ISLAND_WIDTH_SCALE = "key_dynamic_island_width_scale"
        private const val KEY_DYNAMIC_ISLAND_HEIGHT_SCALE = "key_dynamic_island_height_scale"
        private const val KEY_WEB_SHIELD_ENABLED = "key_web_shield_enabled"
        private const val KEY_WEB_SHIELD_DOMAINS = "key_web_shield_domains"
        private const val KEY_QUICK_NOTES = "key_quick_notes"
        private const val KEY_DAILY_TASK_LAST_RESET_DATE = "key_daily_task_last_reset_date"
        private const val KEY_GLASS_INTENSITY = "key_glass_intensity"
        private const val KEY_GLASS_BLUR_ENABLED = "key_glass_blur_enabled"
        private const val KEY_CORNER_RADIUS_DP = "key_corner_radius_dp"
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_CUSTOM_BG = "key_custom_bg"
        private const val KEY_CUSTOM_SURFACE = "key_custom_surface"
        private const val KEY_CUSTOM_TEXT_PRIMARY = "key_custom_text_primary"
        private const val KEY_CUSTOM_TEXT_SECONDARY = "key_custom_text_secondary"
        private const val KEY_CUSTOM_ACCENT = "key_custom_accent"
        private const val KEY_CUSTOM_BORDER = "key_custom_border"
    }
}
