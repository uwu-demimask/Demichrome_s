package com.example.core.blocker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import com.example.core.timer.FocusTimerManager

object DistractionOverlayManager {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("InflateParams", "SetTextI18n")
    fun showOverlay(context: Context, packageName: String, appName: String) {
        if (!DistractionBlockerManager.hasOverlayPermission(context)) return

        mainHandler.post {
            try {
                if (overlayView != null) {
                    hideOverlay()
                }

                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return@post
                windowManager = wm

                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                // Build modern retro blocking view dynamically
                val rootLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setBackgroundColor(0xF00A0B0E.toInt()) // 94% opaque dark terminal background
                    setPadding(48, 64, 48, 64)
                }

                // Inner card
                val cardLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(36, 40, 36, 40)
                    setBackgroundResource(R.drawable.bg_widget_retro)
                }

                // Shield badge
                val badgeText = TextView(context).apply {
                    text = "⚠️ DISTRACTION SHIELD ENGAGED"
                    setTextColor(0xFFFFC043.toInt())
                    textSize = 11f
                    typeface = Typeface.MONOSPACE
                    setPadding(16, 8, 16, 8)
                    setBackgroundResource(R.drawable.bg_widget_badge)
                }
                cardLayout.addView(badgeText)

                // App Title
                val titleText = TextView(context).apply {
                    text = "${appName.uppercase()} IS LOCKED"
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = 18f
                    typeface = Typeface.MONOSPACE
                    gravity = Gravity.CENTER
                    setPadding(0, 24, 0, 8)
                }
                cardLayout.addView(titleText)

                // Timer remaining & message
                val remainingSecs = FocusTimerManager.remainingSeconds.value
                val mins = remainingSecs / 60
                val secs = remainingSecs % 60
                val timerString = String.format("%02d:%02d", mins, secs)

                val messageText = TextView(context).apply {
                    text = "Active Focus Session in progress.\n$timerString remaining on clock.\n\nShielded apps will automatically unlock after the timer completes."
                    setTextColor(0xFFA0A8B8.toInt())
                    textSize = 12f
                    typeface = Typeface.MONOSPACE
                    gravity = Gravity.CENTER
                    setPadding(0, 8, 0, 32)
                }
                cardLayout.addView(messageText)

                // Return to Focus Button
                val returnButton = Button(context).apply {
                    text = "RETURN TO DEMICHROME FOCUS"
                    setTextColor(0xFF0E1015.toInt())
                    textSize = 12f
                    typeface = Typeface.MONOSPACE
                    setBackgroundResource(R.drawable.bg_widget_button_accent)
                    setPadding(32, 16, 32, 16)
                    setOnClickListener {
                        hideOverlay()
                        val appIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            putExtra("EXTRA_INTERCEPTED_PKG", packageName)
                            putExtra("EXTRA_INTERCEPTED_NAME", appName)
                        }
                        context.startActivity(appIntent)
                    }
                }
                cardLayout.addView(returnButton)

                rootLayout.addView(cardLayout)
                wm.addView(rootLayout, params)
                overlayView = rootLayout
            } catch (_: Exception) {}
        }
    }

    fun hideOverlay() {
        mainHandler.post {
            try {
                if (overlayView != null && windowManager != null) {
                    windowManager?.removeView(overlayView)
                    overlayView = null
                }
            } catch (_: Exception) {}
        }
    }
}
