package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM todo_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TodoTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodoTask): Long

    @Update
    suspend fun updateTask(task: TodoTask)

    @Delete
    suspend fun deleteTask(task: TodoTask)

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE todo_tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE todo_tasks SET completedSessions = completedSessions + 1 WHERE id = :id")
    suspend fun incrementCompletedSessions(id: Long)

    @Query("DELETE FROM todo_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()

    @Query("SELECT COUNT(*) FROM todo_tasks WHERE isCompleted = 0")
    fun getActiveTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM todo_tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>
}
