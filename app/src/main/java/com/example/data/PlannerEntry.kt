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
import java.util.UUID

/**
 * Planner Data Entry for Demichrome 24H Daily Routine and Timeline Planner.
 */
@Entity(tableName = "planner_entries")
data class PlannerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val timeSlot: String, // e.g., "08:00 - 09:30"
    val title: String,
    val description: String = "",
    val category: String = "FOCUS", // FOCUS, WORK, STUDY, BREAK, ROUTINE
    val isCompleted: Boolean = false,
    val date: String, // format: "yyyy-MM-dd"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Dao
interface PlannerDao {

    @Query("SELECT * FROM planner_entries WHERE date = :date ORDER BY timeSlot ASC, id ASC")
    fun getEntriesForDate(date: String): Flow<List<PlannerEntry>>

    @Query("SELECT * FROM planner_entries ORDER BY date DESC, timeSlot ASC")
    fun getAllEntries(): Flow<List<PlannerEntry>>

    @Query("SELECT * FROM planner_entries WHERE syncId = :syncId LIMIT 1")
    suspend fun getEntryBySyncId(syncId: String): PlannerEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PlannerEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<PlannerEntry>)

    @Update
    suspend fun updateEntry(entry: PlannerEntry)

    @Delete
    suspend fun deleteEntry(entry: PlannerEntry)

    @Query("DELETE FROM planner_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("UPDATE planner_entries SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setEntryCompleted(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())
}
