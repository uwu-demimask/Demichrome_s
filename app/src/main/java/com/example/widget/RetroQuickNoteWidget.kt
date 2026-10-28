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
import com.example.core.haptics.RetroHaptics
import com.example.data.AppDatabase
import com.example.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RetroQuickNoteWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_CLEAR_QUICK_NOTE = "com.example.widget.ACTION_CLEAR_QUICK_NOTE"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, RetroQuickNoteWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
            if (appWidgetIds.isEmpty()) return

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_NAVIGATE_TO", "FOCUS")
            }
            val pendingOpenIntent = PendingIntent.getActivity(
                context,
                201,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val clearIntent = Intent(context, RetroQuickNoteWidget::class.java).apply {
                action = ACTION_CLEAR_QUICK_NOTE
            }
            val pendingClearIntent = PendingIntent.getBroadcast(
                context,
                202,
                clearIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Render placeholders first to ensure responsive UI
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.retro_quick_note_widget)
                views.setOnClickPendingIntent(R.id.note_widget_root, pendingOpenIntent)
                views.setOnClickPendingIntent(R.id.note_widget_content_container, pendingOpenIntent)
                views.setOnClickPendingIntent(R.id.note_widget_btn_open, pendingOpenIntent)
                views.setOnClickPendingIntent(R.id.note_widget_btn_clear, pendingClearIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            // Fetch live notes asynchronously from Room database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val notes = db.minimalistNoteDao().getAllNotes().firstOrNull() ?: emptyList()
                    val latestNote = notes.firstOrNull()
                    val prefs = UserPreferences(context)
                    val legacyScratchpad = prefs.quickNotes.value.trim()

                    val (displayText, badgeText) = when {
                        latestNote != null -> {
                            val text = "> [#${latestNote.tag}] ${latestNote.getFormattedTime()}\n${latestNote.content}"
                            val badge = "${notes.size} ${if (notes.size == 1) "NOTE" else "NOTES"}"
                            text to badge
                        }
                        legacyScratchpad.isNotBlank() -> {
                            val text = "> [MEMO]\n$legacyScratchpad"
                            val badge = "MEMO: ${legacyScratchpad.length}C"
                            text to badge
                        }
                        else -> {
                            "> No focus notes logged yet.\n> Tap to jot fleeting thoughts in Demichrome flow." to "NOTES: 0"
                        }
                    }

                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.retro_quick_note_widget)
                        views.setTextViewText(R.id.note_widget_text, displayText)
                        views.setTextViewText(R.id.note_widget_badge, badgeText)

                        views.setOnClickPendingIntent(R.id.note_widget_root, pendingOpenIntent)
                        views.setOnClickPendingIntent(R.id.note_widget_content_container, pendingOpenIntent)
                        views.setOnClickPendingIntent(R.id.note_widget_btn_open, pendingOpenIntent)
                        views.setOnClickPendingIntent(R.id.note_widget_btn_clear, pendingClearIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAllWidgets(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CLEAR_QUICK_NOTE) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    db.minimalistNoteDao().clearAllNotes()
                    val prefs = UserPreferences(context)
                    prefs.setQuickNotes("")
                    RetroHaptics.performClick(context)
                    updateAllWidgets(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
