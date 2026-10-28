package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Queue of offline changes waiting to be synchronized with the remote backend.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val queueId: Long = 0,
    val entityType: String, // "TASK", "HABIT", "FOCUS", "FLASHCARD", "DAILY_TASK", "EXPENSE", "PLANNER", "CONFIG"
    val syncId: String,
    val action: String, // "UPSERT", "DELETE"
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getAllPendingQueueFlow(): Flow<List<SyncQueueItem>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getAllPendingItems(): List<SyncQueueItem>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueItem(item: SyncQueueItem): Long

    @Query("DELETE FROM sync_queue WHERE queueId = :queueId")
    suspend fun dequeueItem(queueId: Long)

    @Query("DELETE FROM sync_queue WHERE queueId IN (:queueIds)")
    suspend fun dequeueItems(queueIds: List<Long>)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
