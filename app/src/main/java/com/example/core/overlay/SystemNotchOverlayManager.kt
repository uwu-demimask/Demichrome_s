package com.example.core.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.core.timer.FocusTimerManager
import com.example.data.NotchPosition
import com.example.data.UserPreferences
import com.example.ui.timer.FocusState
import java.util.Locale

/**
 * System-wide floating HUD status timer overlay that stays pinned at the device notch / status bar
 * across ALL applications, home screen, and third-party apps.
 *
 * Built with pure high-performance Android Views, custom CRT border geometry, draggable touch snapping,
 * and an expandable retro mini-action dock.
 */
object SystemNotchOverlayManager {

    private var windowManager: WindowManager? = null
    private var overlayRootView: FrameLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var timeTextView: TextView? = null
    private var statusBadgeTextView: TextView? = null
    private var pulseDotView: View? = null
    private var actionDockLayout: LinearLayout? = null
    private var playPauseButtonText: TextView? = null
    private var deepFocusTag: TextView? = null
    private var capsuleCardLayout: LinearLayout? = null

    private var isExpanded = false
    private var currentPosition = NotchPosition.TOP_CENTER
    private var isCurrentlyDeepFocus = false
    private var lastState = FocusState.READY
    private var isInFocusScreen = false

    // Calibration settings
    private var calibrationOffsetX = 0
    private var calibrationOffsetY = 0
    private var calibrationWidthScale = 1.0f
    private var calibrationHeightScale = 1.0f

    // Theme colors synchronized with active Demichrome theme
    private var themeBgColor = 0xF50D0F14.toInt()
    private var themeSurfaceColor = 0xFF191A20.toInt()
    private var themeBorderColor = 0xFF424A5E.toInt()
    private var themeTextColor = 0xFFFFFFFF.toInt()
    private var themeTextSecondaryColor = 0xFF8FA0B8.toInt()
    private var themeAccentColor = 0xFF00FFCC.toInt()
    private var themeButtonActiveTextColor = 0xFF000000.toInt()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoCollapseRunnable = Runnable {
        collapseDock()
    }

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val touchSlopDp = 10

    /**
     * Updates active theme colors on the floating system overlay.
     */
    fun updateTheme(
        bgColor: Int,
        surfaceColor: Int,
        borderColor: Int,
        textColor: Int,
        textSecondaryColor: Int,
        accentColor: Int,
        buttonActiveTextColor: Int
    ) {
        themeBgColor = bgColor
        themeSurfaceColor = surfaceColor
        themeBorderColor = borderColor
        themeTextColor = textColor
        themeTextSecondaryColor = textSecondaryColor
        themeAccentColor = accentColor
        themeButtonActiveTextColor = buttonActiveTextColor

        mainHandler.post {
            val root = overlayRootView ?: return@post
            val context = root.context
            val activeBorder = if (isCurrentlyDeepFocus) themeAccentColor else themeBorderColor
            capsuleCardLayout?.background = createCapsuleDrawable(context, activeBorder, themeBgColor)
            timeTextView?.setTextColor(if (isCurrentlyDeepFocus) themeAccentColor else themeTextColor)
            statusBadgeTextView?.setTextColor(themeTextSecondaryColor)
            pulseDotView?.background = createCircleDrawable(themeAccentColor)
            playPauseButtonText?.setTextColor(themeAccentColor)

            val dock = actionDockLayout
            if (dock != null) {
                for (i in 0 until dock.childCount) {
                    val child = dock.getChildAt(i)
                    if (child is LinearLayout) {
                        child.background = createButtonDrawable(context)
                        val tv = child.getChildAt(0) as? TextView
                        when (i) {
                            0 -> tv?.setTextColor(themeAccentColor)
                            3 -> tv?.setTextColor(themeAccentColor)
                            4 -> tv?.setTextColor(themeTextSecondaryColor)
                            else -> tv?.setTextColor(themeTextColor)
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates calibration in real time on the existing overlay.
     */
    fun updateCalibration(
        context: Context,
        offsetX: Int,
        offsetY: Int,
        widthScale: Float,
        heightScale: Float
    ) {
        calibrationOffsetX = offsetX
        calibrationOffsetY = offsetY
        calibrationWidthScale = widthScale
        calibrationHeightScale = heightScale

        mainHandler.post {
            capsuleCardLayout?.scaleX = widthScale
            capsuleCardLayout?.scaleY = heightScale

            val root = overlayRootView ?: return@post
            val wm = windowManager ?: return@post
            val params = layoutParams ?: return@post

            applyPositionToParams(context, params, currentPosition)
            try {
                if (root.isAttachedToWindow) {
                    wm.updateViewLayout(root, params)
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Informs the overlay whether user is currently in the full-screen Focus view,
     * so it doesn't obstruct the main focus dial.
     */
    fun setInFocusScreen(inFocus: Boolean) {
        isInFocusScreen = inFocus
        mainHandler.post {
            overlayRootView?.visibility = if (inFocus) View.GONE else View.VISIBLE
        }
    }

    /**
     * Whether the system overlay view is currently attached to WindowManager.
     */
    fun isOverlayAttached(): Boolean = overlayRootView != null

    /**
     * Checks if the app has permission to draw on top of other apps.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Opens Android System settings so the user can grant "Display over other apps" permission.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(fallback)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Updates or creates the floating notch overlay.
     */
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    fun update(
        context: Context,
        remainingSeconds: Int,
        targetMinutes: Int,
        state: FocusState,
        isDeepFocus: Boolean,
        notchPosition: NotchPosition,
        enabled: Boolean
    ) {
        val shouldBeVisible = enabled && (state == FocusState.RUNNING || state == FocusState.PAUSED)

        if (!shouldBeVisible || !hasOverlayPermission(context)) {
            if (overlayRootView != null) {
                hide()
            }
            return
        }

        mainHandler.post {
            try {
                val wm = windowManager ?: (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager).also {
                    windowManager = it
                } ?: return@post

                currentPosition = notchPosition
                isCurrentlyDeepFocus = isDeepFocus
                lastState = state

                if (overlayRootView == null) {
                    createAndAttachOverlayView(context, wm, notchPosition)
                }

                updateContent(context, remainingSeconds, state, isDeepFocus)
            } catch (_: Exception) {}
        }
    }

    private fun updateContent(
        context: Context,
        remainingSeconds: Int,
        state: FocusState,
        isDeepFocus: Boolean
    ) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        timeTextView?.text = formattedTime
        val textColor = if (isDeepFocus) 0xFF00FFCC.toInt() else 0xFFFFFFFF.toInt()
        timeTextView?.setTextColor(textColor)

        // Status badge
        when (state) {
            FocusState.RUNNING -> {
                statusBadgeTextView?.text = if (isDeepFocus) "[LOCK]" else "[RUN]"
                statusBadgeTextView?.setTextColor(if (isDeepFocus) 0xFF00FFCC.toInt() else 0xFF8FA0B8.toInt())
                pulseDotView?.background = createCircleDrawable(if (isDeepFocus) 0xFF00FFCC.toInt() else 0xFF00E676.toInt())
                playPauseButtonText?.text = "❚❚ PAUSE"
            }
            FocusState.PAUSED -> {
                statusBadgeTextView?.text = "[PAUSE]"
                statusBadgeTextView?.setTextColor(0xFFFFC043.toInt())
                pulseDotView?.background = createCircleDrawable(0xFFFFC043.toInt())
                playPauseButtonText?.text = "▶ RESUME"
            }
            else -> {
                statusBadgeTextView?.text = "[READY]"
                statusBadgeTextView?.setTextColor(0xFF8FA0B8.toInt())
                pulseDotView?.background = createCircleDrawable(0xFF8FA0B8.toInt())
                playPauseButtonText?.text = "▶ START"
            }
        }

        // Update border color
        val borderColor = when {
            isDeepFocus -> 0xFF00FFCC.toInt()
            state == FocusState.PAUSED -> 0xFFFFC043.toInt()
            else -> 0xFF424A5E.toInt()
        }
        capsuleCardLayout?.background = createCapsuleDrawable(context, borderColor, 0xF50D0F14.toInt())
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun createAndAttachOverlayView(
        context: Context,
        wm: WindowManager,
        position: NotchPosition
    ) {
        val density = context.resources.displayMetrics.density
        val touchSlopPx = (touchSlopDp * density).toInt()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            flags,
            PixelFormat.TRANSLUCENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        applyPositionToParams(context, params, position)
        layoutParams = params

        // Root container
        val root = FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
        }

        // Capsule layout
        val capsule = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val borderColor = if (isCurrentlyDeepFocus) themeAccentColor else themeBorderColor
            background = createCapsuleDrawable(context, borderColor, themeBgColor)
            setPadding(dpToPx(context, 10), dpToPx(context, 5), dpToPx(context, 10), dpToPx(context, 5))
            elevation = dpToPx(context, 8).toFloat()
            scaleX = calibrationWidthScale
            scaleY = calibrationHeightScale
        }
        capsuleCardLayout = capsule

        // Header Row (Dot + Time + Badge)
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Pulse dot
        val dot = View(context).apply {
            val dotSize = dpToPx(context, 6)
            layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                marginEnd = dpToPx(context, 6)
            }
            background = createCircleDrawable(themeAccentColor)
        }
        pulseDotView = dot
        headerRow.addView(dot)

        // Monospace Time Readout
        val timeView = TextView(context).apply {
            text = "25:00"
            setTextColor(if (isCurrentlyDeepFocus) themeAccentColor else themeTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        timeTextView = timeView
        headerRow.addView(timeView)

        // Status badge text
        val badgeView = TextView(context).apply {
            text = "[RUN]"
            setTextColor(themeTextSecondaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dpToPx(context, 5), 0, 0, 0)
        }
        statusBadgeTextView = badgeView
        headerRow.addView(badgeView)

        capsule.addView(headerRow)

        // Collapsible Action Dock
        val dock = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(0, dpToPx(context, 6), 0, 0)
        }
        actionDockLayout = dock

        // Button 1: Play/Pause
        val btnPlayPause = createMiniButton(context, "❚❚ PAUSE", textColor = themeAccentColor) {
            RetroHaptics.performClick(context)
            if (lastState == FocusState.RUNNING) {
                FocusTimerManager.pauseTimer()
            } else {
                FocusTimerManager.startTimer()
            }
            resetAutoCollapseTimer()
        }
        playPauseButtonText = btnPlayPause.getChildAt(0) as? TextView
        dock.addView(btnPlayPause)

        // Button 2: +5M
        val btnAdd5 = createMiniButton(context, "+5M", textColor = themeTextColor) {
            RetroHaptics.performClick(context)
            FocusTimerManager.addFiveMinutes()
            resetAutoCollapseTimer()
        }
        dock.addView(btnAdd5)

        // Button 3: Cycle Position
        val btnPos = createMiniButton(context, "⇋ POS", textColor = themeTextColor) {
            RetroHaptics.performClick(context)
            cyclePosition(context)
            resetAutoCollapseTimer()
        }
        dock.addView(btnPos)

        // Button 4: Open Main App
        val btnOpen = createMiniButton(context, "⇱ OPEN", textColor = themeAccentColor) {
            RetroHaptics.performClick(context)
            collapseDock()
            val openIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
            context.startActivity(openIntent)
        }
        dock.addView(btnOpen)

        // Button 5: Reset
        val btnReset = createMiniButton(context, "↺ RST", textColor = themeTextSecondaryColor) {
            RetroHaptics.performClick(context)
            FocusTimerManager.resetTimer()
            hide()
        }
        dock.addView(btnReset)

        capsule.addView(dock)
        root.addView(capsule)

        // Dragging & Tapping Touch Listener
        capsule.setOnTouchListener { _, event ->
            val curParams = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = curParams.x
                    initialY = curParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (!isDragging && (kotlin.math.abs(dx) > touchSlopPx || kotlin.math.abs(dy) > touchSlopPx)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        curParams.x = initialX + dx
                        curParams.y = initialY + dy
                        try {
                            if (root.isAttachedToWindow) {
                                wm.updateViewLayout(root, curParams)
                            }
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click event -> toggle expanded dock
                        RetroHaptics.performClick(context)
                        toggleDock()
                    } else {
                        // Snapping logic on release
                        snapToNearestNotch(context, curParams)
                        try {
                            if (root.isAttachedToWindow) {
                                wm.updateViewLayout(root, curParams)
                            }
                        } catch (_: Exception) {}
                    }
                    true
                }
                else -> false
            }
        }

        try {
            val oldRoot = overlayRootView
            if (oldRoot != null) {
                try {
                    if (oldRoot.isAttachedToWindow) {
                        wm.removeView(oldRoot)
                    }
                } catch (_: Exception) {}
                overlayRootView = null
            }
            if (hasOverlayPermission(context)) {
                wm.addView(root, params)
                overlayRootView = root
                if (isInFocusScreen) {
                    root.visibility = View.GONE
                }
            }
        } catch (_: Exception) {}
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else dpToPx(context, 32)
    }

    private fun applyPositionToParams(
        context: Context,
        params: WindowManager.LayoutParams,
        position: NotchPosition
    ) {
        val statusBarHeight = getStatusBarHeight(context)
        // Default position sits comfortably beneath camera cutout/punch-hole
        val defaultTopOffsetPx = statusBarHeight + dpToPx(context, 10) + dpToPx(context, calibrationOffsetY)
        val bottomOffsetPx = dpToPx(context, 36) + dpToPx(context, -calibrationOffsetY)
        val sideOffsetPx = dpToPx(context, 16) + dpToPx(context, calibrationOffsetX)

        when (position) {
            NotchPosition.TOP_CENTER -> {
                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                params.x = dpToPx(context, calibrationOffsetX)
                params.y = defaultTopOffsetPx
            }
            NotchPosition.TOP_RIGHT -> {
                params.gravity = Gravity.TOP or Gravity.END
                params.x = sideOffsetPx
                params.y = defaultTopOffsetPx
            }
            NotchPosition.TOP_LEFT -> {
                params.gravity = Gravity.TOP or Gravity.START
                params.x = sideOffsetPx
                params.y = defaultTopOffsetPx
            }
            NotchPosition.BOTTOM_CENTER -> {
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.x = dpToPx(context, calibrationOffsetX)
                params.y = bottomOffsetPx
            }
        }
    }

    private fun snapToNearestNotch(context: Context, params: WindowManager.LayoutParams) {
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        // Determine if near top or bottom
        if (params.y < screenHeight / 3) {
            // Near top
            if (params.x < -screenWidth / 4) {
                currentPosition = NotchPosition.TOP_LEFT
            } else if (params.x > screenWidth / 4) {
                currentPosition = NotchPosition.TOP_RIGHT
            } else {
                currentPosition = NotchPosition.TOP_CENTER
            }
        } else if (params.y > (screenHeight * 2) / 3) {
            currentPosition = NotchPosition.BOTTOM_CENTER
        }

        applyPositionToParams(context, params, currentPosition)
        UserPreferences(context).setNotchTimerPosition(currentPosition)
    }

    private fun cyclePosition(context: Context) {
        val nextPos = when (currentPosition) {
            NotchPosition.TOP_CENTER -> NotchPosition.TOP_RIGHT
            NotchPosition.TOP_RIGHT -> NotchPosition.BOTTOM_CENTER
            NotchPosition.BOTTOM_CENTER -> NotchPosition.TOP_LEFT
            NotchPosition.TOP_LEFT -> NotchPosition.TOP_CENTER
        }
        currentPosition = nextPos
        UserPreferences(context).setNotchTimerPosition(nextPos)

        val root = overlayRootView ?: return
        val wm = windowManager ?: return
        val params = layoutParams ?: return

        applyPositionToParams(context, params, nextPos)
        try {
            if (root.isAttachedToWindow) {
                wm.updateViewLayout(root, params)
            }
        } catch (_: Exception) {}
    }

    private fun toggleDock() {
        if (isExpanded) {
            collapseDock()
        } else {
            expandDock()
        }
    }

    private fun expandDock() {
        isExpanded = true
        actionDockLayout?.visibility = View.VISIBLE
        resetAutoCollapseTimer()
    }

    private fun collapseDock() {
        isExpanded = false
        actionDockLayout?.visibility = View.GONE
        mainHandler.removeCallbacks(autoCollapseRunnable)
    }

    private fun resetAutoCollapseTimer() {
        mainHandler.removeCallbacks(autoCollapseRunnable)
        mainHandler.postDelayed(autoCollapseRunnable, 5000)
    }

    /**
     * Hides and destroys the system overlay view cleanly.
     */
    fun hide() {
        mainHandler.post {
            mainHandler.removeCallbacks(autoCollapseRunnable)
            try {
                val root = overlayRootView
                val wm = windowManager
                if (root != null && wm != null && root.isAttachedToWindow) {
                    wm.removeViewImmediate(root)
                }
            } catch (_: Exception) {} finally {
                overlayRootView = null
                layoutParams = null
                timeTextView = null
                statusBadgeTextView = null
                pulseDotView = null
                actionDockLayout = null
                playPauseButtonText = null
                capsuleCardLayout = null
                isExpanded = false
            }
        }
    }

    private fun createMiniButton(
        context: Context,
        label: String,
        textColor: Int = 0xFFE0E6ED.toInt(),
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = createButtonDrawable(context)
            setPadding(dpToPx(context, 6), dpToPx(context, 3), dpToPx(context, 6), dpToPx(context, 3))
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(context, 4)
            }
            layoutParams = params

            val text = TextView(context).apply {
                this.text = label
                this.setTextColor(textColor)
                this.textSize = 9.5f
                this.typeface = Typeface.MONOSPACE
                this.setTypeface(this.typeface, Typeface.BOLD)
            }
            addView(text)

            setOnClickListener { onClick() }
        }
    }

    private fun createCapsuleDrawable(context: Context, strokeColor: Int, bgColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 16).toFloat()
            setColor(bgColor)
            setStroke(dpToPx(context, 1), strokeColor)
        }
    }

    private fun createButtonDrawable(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 6).toFloat()
            setColor(themeSurfaceColor)
            setStroke(dpToPx(context, 1), themeBorderColor)
        }
    }

    private fun createCircleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}
