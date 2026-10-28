package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.timer.FocusTimerManager
import com.example.ui.timer.FocusState

class RetroFocusWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_TOGGLE = "com.example.widget.ACTION_WIDGET_TOGGLE"
        const val ACTION_WIDGET_RESET = "com.example.widget.ACTION_WIDGET_RESET"
        const val ACTION_WIDGET_ADD_5 = "com.example.widget.ACTION_WIDGET_ADD_5"

        fun updateAllWidgets(
            context: Context,
            remainingSeconds: Int,
            targetMinutes: Int,
            state: FocusState,
            isDeepFocus: Boolean = false,
            subtext: String? = null
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, RetroFocusWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return

            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val formattedTime = String.format("%02d:%02d", mins, secs)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.retro_focus_widget)

                // Timer digits
                views.setTextViewText(R.id.widget_timer_text, formattedTime)

                // Status Badge & Controls
                when (state) {
                    FocusState.RUNNING -> {
                        views.setTextViewText(R.id.widget_status_badge, if (isDeepFocus) "DEEP LOCK" else "FOCUSING")
                        views.setTextViewText(R.id.widget_btn_toggle, "⏸ PAUSE")
                        views.setTextViewText(
                            R.id.widget_subtext,
                            subtext ?: "🛡️ SHIELDS ENGAGED // APPS LOCKED"
                        )
                    }
                    FocusState.PAUSED -> {
                        views.setTextViewText(R.id.widget_status_badge, "PAUSED")
                        views.setTextViewText(R.id.widget_btn_toggle, "▶ RESUME")
                        views.setTextViewText(
                            R.id.widget_subtext,
                            subtext ?: "⚠️ TIMER PAUSED // TAP TO RESUME"
                        )
                    }
                    FocusState.COMPLETED -> {
                        views.setTextViewText(R.id.widget_status_badge, "COMPLETED")
                        views.setTextViewText(R.id.widget_btn_toggle, "▶ RESTART")
                        views.setTextViewText(
                            R.id.widget_subtext,
                            subtext ?: "🎉 SESSION COMPLETE // SHIELDS OFF"
                        )
                    }
                    FocusState.READY -> {
                        views.setTextViewText(R.id.widget_status_badge, "READY")
                        views.setTextViewText(R.id.widget_btn_toggle, "▶ START")
                        views.setTextViewText(
                            R.id.widget_subtext,
                            subtext ?: "🛡️ SHIELDS ARMED // TAP TO FOCUS"
                        )
                    }
                }

                // Intent to open Demichrome
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingOpenIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingOpenIntent)
                views.setOnClickPendingIntent(R.id.widget_timer_container, pendingOpenIntent)

                // Intent for Toggle (Start / Pause / Resume)
                val toggleIntent = Intent(context, RetroFocusWidget::class.java).apply {
                    action = ACTION_WIDGET_TOGGLE
                }
                val pendingToggleIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_toggle, pendingToggleIntent)

                // Intent for Reset
                val resetIntent = Intent(context, RetroFocusWidget::class.java).apply {
                    action = ACTION_WIDGET_RESET
                }
                val pendingResetIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_reset, pendingResetIntent)

                // Intent for +5 MIN
                val add5Intent = Intent(context, RetroFocusWidget::class.java).apply {
                    action = ACTION_WIDGET_ADD_5
                }
                val pendingAdd5Intent = PendingIntent.getBroadcast(
                    context,
                    3,
                    add5Intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add_5, pendingAdd5Intent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        FocusTimerManager.init(context)
        updateAllWidgets(
            context = context,
            remainingSeconds = FocusTimerManager.remainingSeconds.value,
            targetMinutes = FocusTimerManager.targetMinutes.value,
            state = FocusTimerManager.state.value,
            isDeepFocus = FocusTimerManager.isDeepFocusActive.value
        )
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        FocusTimerManager.init(context)
        updateAllWidgets(
            context = context,
            remainingSeconds = FocusTimerManager.remainingSeconds.value,
            targetMinutes = FocusTimerManager.targetMinutes.value,
            state = FocusTimerManager.state.value,
            isDeepFocus = FocusTimerManager.isDeepFocusActive.value
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        FocusTimerManager.init(context)

        when (intent.action) {
            ACTION_WIDGET_TOGGLE -> {
                when (FocusTimerManager.state.value) {
                    FocusState.RUNNING -> FocusTimerManager.pauseTimer()
                    FocusState.PAUSED, FocusState.READY, FocusState.COMPLETED -> FocusTimerManager.startTimer()
                }
                updateAllWidgets(
                    context = context,
                    remainingSeconds = FocusTimerManager.remainingSeconds.value,
                    targetMinutes = FocusTimerManager.targetMinutes.value,
                    state = FocusTimerManager.state.value,
                    isDeepFocus = FocusTimerManager.isDeepFocusActive.value
                )
            }
            ACTION_WIDGET_RESET -> {
                FocusTimerManager.resetTimer()
                updateAllWidgets(
                    context = context,
                    remainingSeconds = FocusTimerManager.remainingSeconds.value,
                    targetMinutes = FocusTimerManager.targetMinutes.value,
                    state = FocusTimerManager.state.value,
                    isDeepFocus = FocusTimerManager.isDeepFocusActive.value
                )
            }
            ACTION_WIDGET_ADD_5 -> {
                val currentMins = FocusTimerManager.targetMinutes.value
                val newMins = if (currentMins >= 60) 25 else currentMins + 5
                FocusTimerManager.selectDuration(newMins)
                updateAllWidgets(
                    context = context,
                    remainingSeconds = FocusTimerManager.remainingSeconds.value,
                    targetMinutes = FocusTimerManager.targetMinutes.value,
                    state = FocusTimerManager.state.value,
                    isDeepFocus = FocusTimerManager.isDeepFocusActive.value
                )
            }
        }
    }
}
