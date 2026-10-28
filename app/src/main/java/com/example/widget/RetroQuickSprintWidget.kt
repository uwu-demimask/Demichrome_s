package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.timer.FocusTimerManager

class RetroQuickSprintWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_QUICK_25_SPRINT = "com.example.widget.ACTION_QUICK_25_SPRINT"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, RetroQuickSprintWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
            if (appWidgetIds.isEmpty()) return

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_START_SPRINT_MINUTES", 25)
                putExtra("EXTRA_NAVIGATE_TO", "FOCUS")
            }
            val pendingActivityIntent = PendingIntent.getActivity(
                context,
                203,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val sprintBroadcastIntent = Intent(context, RetroQuickSprintWidget::class.java).apply {
                action = ACTION_QUICK_25_SPRINT
            }
            val pendingSprintIntent = PendingIntent.getBroadcast(
                context,
                204,
                sprintBroadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.retro_quick_sprint_widget)
                views.setOnClickPendingIntent(R.id.quick_sprint_widget_root, pendingActivityIntent)
                views.setOnClickPendingIntent(R.id.quick_sprint_subtext, pendingSprintIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_25_SPRINT) {
            FocusTimerManager.init(context)
            FocusTimerManager.selectDuration(25)
            FocusTimerManager.startTimer()
            updateAllWidgets(context)
            RetroFocusWidget.updateAllWidgets(
                context = context,
                remainingSeconds = FocusTimerManager.remainingSeconds.value,
                targetMinutes = FocusTimerManager.targetMinutes.value,
                state = FocusTimerManager.state.value,
                isDeepFocus = FocusTimerManager.isDeepFocusActive.value
            )
        }
    }
}
