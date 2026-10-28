package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Habit entity representing a tracked routine in Dominic Hartt's Sanctuary Matrix.
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "FOCUS", // FOCUS, DISCIPLINE, MIND, HEALTH, ROUTINE
    val targetDaysPerWeek: Int = 7,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

/**
 * Daily habit completion log for the Column-Grid Matrix.
 */
@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // format: "yyyy-MM-dd"
    val isCompleted: Boolean = true,
    val completedAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = completedAt,
    val updatedAt: Long = completedAt,
    val userId: String = "local_user"
)

/**
 * Daily numeric metric entry for the Sanctuary Line Graph Data Tracker.
 * Plotted in sync with the column dates (e.g., Focus Minutes, Energy 1-10, Sleep Hours, Mood 1-10, Productivity %).
 */
@Entity(
    tableName = "metric_entries",
    indices = [Index(value = ["metricKey", "date"], unique = true)]
)
data class MetricEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val metricKey: String, // "FOCUS_MINUTES", "ENERGY", "MOOD", "SLEEP_HOURS", "PRODUCTIVITY", "CUSTOM_..."
    val date: String, // format: "yyyy-MM-dd"
    val metricLabel: String = "Energy",
    val value: Float = 0f,
    val unit: String = "/10",
    val updatedAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = updatedAt,
    val userId: String = "local_user"
)

enum class DefaultSanctuaryMetric(
    val key: String,
    val label: String,
    val unit: String,
    val minValue: Float,
    val maxValue: Float,
    val defaultValue: Float
) {
    FOCUS_MINUTES("FOCUS_MINUTES", "Focus Time", "min", 0f, 360f, 60f),
    ENERGY("ENERGY", "Energy Level", "/10", 1f, 10f, 7f),
    SLEEP_HOURS("SLEEP_HOURS", "Sleep", "hrs", 4f, 12f, 7.5f),
    MOOD("MOOD", "Clarity / Mood", "/10", 1f, 10f, 8f),
    PRODUCTIVITY("PRODUCTIVITY", "Discipline %", "%", 0f, 100f, 85f);

    companion object {
        fun fromKey(key: String): DefaultSanctuaryMetric? = values().firstOrNull { it.key == key }
    }
}
