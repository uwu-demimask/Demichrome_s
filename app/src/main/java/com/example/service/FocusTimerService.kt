package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.timer.FocusTimerManager
import com.example.ui.timer.FocusState

class FocusTimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createInterceptionNotificationChannel()
        com.example.core.blocker.DistractionBlockerManager.startMonitoring(
            applicationContext,
            com.example.data.UserPreferences(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        com.example.core.blocker.DistractionBlockerManager.startMonitoring(
            applicationContext,
            com.example.data.UserPreferences(applicationContext)
        )
        val action = intent?.action
        when (action) {
            ACTION_START_FOREGROUND -> {
                val notification = buildNotification(
                    remainingSeconds = FocusTimerManager.remainingSeconds.value,
                    targetMinutes = FocusTimerManager.targetMinutes.value,
                    state = FocusTimerManager.state.value,
                    isDeepFocus = FocusTimerManager.isDeepFocusActive.value
                )
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_STOP_FOREGROUND -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TOGGLE_PLAY_PAUSE -> {
                if (FocusTimerManager.state.value == FocusState.RUNNING) {
                    FocusTimerManager.pauseTimer()
                } else {
                    FocusTimerManager.startTimer()
                }
            }
            ACTION_RESET -> {
                FocusTimerManager.resetTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TOGGLE_DEEP_FOCUS -> {
                FocusTimerManager.toggleDeepFocusMode()
            }
            ACTION_ADD_FIVE_MINUTES -> {
                FocusTimerManager.addFiveMinutes()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Demichrome Focus System",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Demichrome live focus sprint timer and notch hud"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createInterceptionNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_INTERCEPTION_ID,
                "Demichrome Distraction Shield",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alert when a blocked app is intercepted during focus"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        remainingSeconds: Int,
        targetMinutes: Int,
        state: FocusState,
        isDeepFocus: Boolean
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Play / Pause
        val toggleIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_TOGGLE_PLAY_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: +5M
        val addFiveIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_ADD_FIVE_MINUTES
        }
        val addFivePendingIntent = PendingIntent.getService(
            this,
            4,
            addFiveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Reset
        val resetIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = PendingIntent.getService(
            this,
            2,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Toggle Deep Focus
        val deepIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_TOGGLE_DEEP_FOCUS
        }
        val deepPendingIntent = PendingIntent.getService(
            this,
            3,
            deepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val totalSecs = (targetMinutes * 60).coerceAtLeast(1)
        val elapsedSecs = (totalSecs - remainingSeconds).coerceIn(0, totalSecs)
        val progressFrac = (elapsedSecs.toFloat() / totalSecs).coerceIn(0f, 1f)
        val progressPercent = (progressFrac * 100).toInt()

        val filledBlocks = if (state == FocusState.COMPLETED) 16 else (progressFrac * 16).toInt().coerceIn(0, 16)
        val pixelBar = "█".repeat(filledBlocks) + "░".repeat(16 - filledBlocks)

        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeString = String.format("%02d:%02d", mins, secs)

        val modeLabel = if (isDeepFocus) "DEEP WORK" else "ACTIVE SPRINT"
        val title = when (state) {
            FocusState.COMPLETED -> "DEMICHROME // 01 COMPLETE"
            FocusState.PAUSED -> "[ FOCUS 01 ] $timeString (PAUSED)"
            else -> "[ FOCUS 01 ] $timeString"
        }

        val subtitle = when (state) {
            FocusState.COMPLETED -> "████████████████ [COMPLETED ${targetMinutes}M]"
            FocusState.PAUSED -> "$pixelBar [PAUSED // TAP RESUME]"
            else -> "$pixelBar [$modeLabel]"
        }

        val playPauseTitle = if (state == FocusState.RUNNING) "PAUSE" else "RESUME"
        val deepTitle = if (isDeepFocus) "UNLOCK" else "SHIELD"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(state == FocusState.RUNNING || state == FocusState.PAUSED)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progressPercent, false)
            .addAction(android.R.drawable.ic_media_play, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+5M", addFivePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", resetPendingIntent)
            .addAction(android.R.drawable.ic_lock_lock, deepTitle, deepPendingIntent)

        return builder.build()
    }

    companion object {
        const val CHANNEL_ID = "monochrome_focus_channel"
        const val CHANNEL_INTERCEPTION_ID = "monochrome_interception_channel"
        const val NOTIFICATION_ID = 1001
        const val INTERCEPTION_NOTIFICATION_ID = 1002

        const val ACTION_START_FOREGROUND = "com.example.service.ACTION_START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.example.service.ACTION_STOP_FOREGROUND"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.service.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_RESET = "com.example.service.ACTION_RESET"
        const val ACTION_TOGGLE_DEEP_FOCUS = "com.example.service.ACTION_TOGGLE_DEEP_FOCUS"
        const val ACTION_ADD_FIVE_MINUTES = "com.example.service.ACTION_ADD_FIVE_MINUTES"

        fun showInterceptionNotification(
            context: Context,
            packageName: String,
            appName: String
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra("EXTRA_INTERCEPTED_PKG", packageName)
                putExtra("EXTRA_INTERCEPTED_NAME", appName)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                INTERCEPTION_NOTIFICATION_ID,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_INTERCEPTION_ID)
                .setContentTitle("⚠️ DISTRACTION SHIELD INTERCEPTED")
                .setContentText("$appName is blocked during active focus session! Tap to return.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .build()

            try {
                notificationManager.notify(INTERCEPTION_NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }

        fun updateNotification(
            context: Context,
            remainingSeconds: Int,
            targetMinutes: Int,
            state: FocusState,
            isDeepFocus: Boolean
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val toggleIntent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_TOGGLE_PLAY_PAUSE
            }
            val togglePendingIntent = PendingIntent.getService(
                context,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val addFiveIntent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_ADD_FIVE_MINUTES
            }
            val addFivePendingIntent = PendingIntent.getService(
                context,
                4,
                addFiveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val resetIntent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_RESET
            }
            val resetPendingIntent = PendingIntent.getService(
                context,
                2,
                resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val deepIntent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_TOGGLE_DEEP_FOCUS
            }
            val deepPendingIntent = PendingIntent.getService(
                context,
                3,
                deepIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val totalSecs = (targetMinutes * 60).coerceAtLeast(1)
            val elapsedSecs = (totalSecs - remainingSeconds).coerceIn(0, totalSecs)
            val progressFrac = (elapsedSecs.toFloat() / totalSecs).coerceIn(0f, 1f)
            val progressPercent = (progressFrac * 100).toInt()

            val filledBlocks = if (state == FocusState.COMPLETED) 16 else (progressFrac * 16).toInt().coerceIn(0, 16)
            val pixelBar = "█".repeat(filledBlocks) + "░".repeat(16 - filledBlocks)

            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val timeString = String.format("%02d:%02d", mins, secs)

            val modeLabel = if (isDeepFocus) "DEEP WORK" else "ACTIVE SPRINT"
            val title = when (state) {
                FocusState.COMPLETED -> "DEMICHROME // 01 COMPLETE"
                FocusState.PAUSED -> "[ FOCUS 01 ] $timeString (PAUSED)"
                else -> "[ FOCUS 01 ] $timeString"
            }

            val subtitle = when (state) {
                FocusState.COMPLETED -> "████████████████ [COMPLETED ${targetMinutes}M]"
                FocusState.PAUSED -> "$pixelBar [PAUSED // TAP RESUME]"
                else -> "$pixelBar [$modeLabel]"
            }

            val playPauseTitle = if (state == FocusState.RUNNING) "PAUSE" else "RESUME"
            val deepTitle = if (isDeepFocus) "UNLOCK" else "SHIELD"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(state == FocusState.RUNNING || state == FocusState.PAUSED)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progressPercent, false)
                .addAction(android.R.drawable.ic_media_play, playPauseTitle, togglePendingIntent)
                .addAction(android.R.drawable.ic_input_add, "+5M", addFivePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", resetPendingIntent)
                .addAction(android.R.drawable.ic_lock_lock, deepTitle, deepPendingIntent)
                .build()

            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
    }
}
