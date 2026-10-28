package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val targetDurationMinutes: Int,
    val actualElapsedSeconds: Int,
    val completedSuccessfully: Boolean = true,
    val tag: String = "FOCUS",
    val styleUsed: String = "Classic",
    val dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startTimeMillis)),
    val rating: Int = 0, // 0 = unrated, 1..5
    val note: String = "",
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = startTimeMillis,
    val updatedAt: Long = startTimeMillis,
    val userId: String = "local_user"
)

data class SessionQualityInsights(
    val averageRating: Float = 0f,
    val totalRatedSessions: Int = 0,
    val bestFocusTime: String = "Morning",
    val ratingCounts: Map<Int, Int> = emptyMap()
)

data class DayFocusSummary(
    val dateString: String,
    val dayOfWeek: String,
    val totalMinutes: Int,
    val sessionCount: Int,
    val completedCount: Int = sessionCount
)

data class DetailedDailyStats(
    val dateString: String,
    val formattedDate: String,
    val isToday: Boolean,
    val totalFocusSeconds: Long,
    val totalFocusMinutes: Int,
    val totalSessions: Int,
    val completedSessions: Int,
    val cancelledSessions: Int,
    val completionRatePercent: Int,
    val averageSessionMinutes: Int,
    val longestSessionMinutes: Int,
    val goalMinutes: Int,
    val goalProgressPercent: Int,
    val goalAchieved: Boolean,
    val morningMinutes: Int,
    val afternoonMinutes: Int,
    val eveningMinutes: Int,
    val deepFocusCount: Int,
    val sessions: List<FocusSession>
)
