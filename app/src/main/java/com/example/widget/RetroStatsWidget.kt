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
import com.example.data.AppDatabase
import com.example.data.FocusRepository
import com.example.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RetroStatsWidget : AppWidgetProvider() {

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, RetroStatsWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
            if (appWidgetIds.isEmpty()) return

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Render immediate layout first to guarantee widget responsiveness
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.retro_stats_widget)
                views.setOnClickPendingIntent(R.id.stats_widget_root, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            // Asynchronously fetch real-time session & streak metrics from repository
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = FocusRepository(db.focusDao(), db.taskDao(), db.habitDao())
                    val prefs = UserPreferences(context)

                    val todaySecs = repo.getTodayFocusSeconds().firstOrNull() ?: 0L
                    val streak = repo.getStreakDays().firstOrNull() ?: 0
                    val completedSessions = repo.getTodaySessionCount().firstOrNull() ?: 0
                    val blockerEnabled = prefs.blockerEnabled.value
                    val blockedCount = prefs.blockedPackages.value.size

                    val totalMins = (todaySecs / 60).toInt()
                    val hours = totalMins / 60
                    val mins = totalMins % 60
                    val timeStr = String.format("%02dh %02dm", hours, mins)

                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.retro_stats_widget)
                        views.setTextViewText(R.id.stats_widget_today_time, timeStr)
                        views.setTextViewText(
                            R.id.stats_widget_streak_count,
                            "★ $streak ${if (streak == 1) "DAY" else "DAYS"}"
                        )
                        views.setTextViewText(
                            R.id.stats_widget_shield_badge,
                            if (blockerEnabled) "🛡️ SHIELDS: $blockedCount" else "🛡️ SHIELDS: OFF"
                        )
                        views.setTextViewText(
                            R.id.stats_widget_footer,
                            "🎯 $completedSessions SESSIONS COMPLETED TODAY"
                        )
                        views.setOnClickPendingIntent(R.id.stats_widget_root, pendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
}

