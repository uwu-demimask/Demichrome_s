package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Represents a lightweight daily focus routine task that automatically resets every 24 hours.
 */
@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val lastCompletedDate: String = "", // format: "yyyy-MM-dd"
    val streakDays: Int = 0,
    val category: String = "ROUTINE", // ROUTINE, FOCUS, MIND, HEALTH, SHUTDOWN
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Dao
interface DailyTaskDao {

    @Query("SELECT * FROM daily_tasks ORDER BY sortOrder ASC, id ASC")
    fun getAllDailyTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE id = :id")
    suspend fun getDailyTaskById(id: Long): DailyTask?

    @Query("SELECT COUNT(*) FROM daily_tasks")
    suspend fun getDailyTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTask(task: DailyTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateDailyTask(task: DailyTask)

    @Delete
    suspend fun deleteDailyTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteDailyTaskById(id: Long)

    @Query("UPDATE daily_tasks SET isCompleted = :isCompleted, lastCompletedDate = :date, streakDays = :streak WHERE id = :id")
    suspend fun setTaskStatus(id: Long, isCompleted: Boolean, date: String, streak: Int)

    @Query("UPDATE daily_tasks SET isCompleted = 0 WHERE lastCompletedDate != :todayDate")
    suspend fun resetIncompleteForDate(todayDate: String)

    @Query("UPDATE daily_tasks SET isCompleted = 0")
    suspend fun resetAllDailyTasks()
}
