package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "minimalist_notes")
data class MinimalistNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: Long = 0,
    val sessionDurationMinutes: Int = 0,
    val tag: String = "FOCUS",
    val isPinned: Boolean = false
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
