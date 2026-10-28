package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class TaskPriority(val label: String, val weight: Int) {
    LOW("LOW", 1),
    MEDIUM("MED", 2),
    HIGH("HIGH", 3),
    URGENT("URGENT", 4);

    companion object {
        fun fromString(value: String): TaskPriority {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

class PriorityConverter {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = TaskPriority.fromString(value)
}

@Entity(tableName = "todo_tasks")
data class TodoTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val estimatedSessions: Int = 1,
    val completedSessions: Int = 0,
    val category: String = "FOCUS",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)
