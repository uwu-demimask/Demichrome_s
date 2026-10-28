package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMillis DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE dateString = :dateString")
    fun getSessionsForDate(dateString: String): Flow<List<FocusSession>>

    @Query("SELECT SUM(actualElapsedSeconds) FROM focus_sessions WHERE completedSuccessfully = 1")
    fun getTotalFocusSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completedSuccessfully = 1")
    fun getTotalCompletedSessionsCount(): Flow<Int>

    @Query("SELECT SUM(actualElapsedSeconds) FROM focus_sessions WHERE dateString = :dateString")
    fun getFocusSecondsForDate(dateString: String): Flow<Long?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE dateString = :dateString")
    fun getSessionCountForDate(dateString: String): Flow<Int>

    @Query("UPDATE focus_sessions SET rating = :rating, note = :note WHERE id = :id")
    suspend fun updateSessionRating(id: Long, rating: Int, note: String)

    @Query("SELECT * FROM focus_sessions WHERE rating > 0 ORDER BY startTimeMillis DESC")
    fun getRatedSessions(): Flow<List<FocusSession>>

    @Query("SELECT AVG(rating) FROM focus_sessions WHERE rating > 0")
    fun getAverageRating(): Flow<Float?>

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}
