package com.example.data.sync

import com.example.data.AppConfigRecord
import com.example.data.DailyTask
import com.example.data.Expense
import com.example.data.Flashcard
import com.example.data.FlashcardDeck
import com.example.data.FocusSession
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.MetricEntry
import com.example.data.PlannerEntry
import com.example.data.TodoTask
import com.example.data.UserProfile

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

data class SyncResult(
    val success: Boolean,
    val message: String,
    val itemsSynced: Int = 0,
    val conflictsResolved: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class BackupMetadata(
    val appVersion: String = "1.0.0",
    val schemaVersion: Int = 7,
    val exportedAt: Long = System.currentTimeMillis(),
    val userId: String,
    val deviceModel: String = android.os.Build.MODEL ?: "Android Device"
)

data class CloudBackupPackage(
    val metadata: BackupMetadata,
    val profile: UserProfile? = null,
    val tasks: List<TodoTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val habitLogs: List<HabitLog> = emptyList(),
    val metrics: List<MetricEntry> = emptyList(),
    val focusSessions: List<FocusSession> = emptyList(),
    val flashcardDecks: List<FlashcardDeck> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),
    val dailyTasks: List<DailyTask> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val plannerEntries: List<PlannerEntry> = emptyList(),
    val appConfigs: List<AppConfigRecord> = emptyList()
)
