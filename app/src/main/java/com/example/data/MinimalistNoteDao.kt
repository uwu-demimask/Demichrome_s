package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MinimalistNoteDao {

    @Query("SELECT * FROM minimalist_notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<MinimalistNote>>

    @Query("SELECT * FROM minimalist_notes WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getNotesForSession(sessionId: Long): Flow<List<MinimalistNote>>

    @Query("SELECT * FROM minimalist_notes ORDER BY timestamp DESC LIMIT 1")
    fun getLatestNote(): Flow<MinimalistNote?>

    @Query("SELECT COUNT(*) FROM minimalist_notes")
    fun getNotesCount(): Flow<Int>

    @Query("SELECT * FROM minimalist_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): MinimalistNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: MinimalistNote): Long

    @Update
    suspend fun updateNote(note: MinimalistNote)

    @Delete
    suspend fun deleteNote(note: MinimalistNote)

    @Query("DELETE FROM minimalist_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE minimalist_notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM minimalist_notes")
    suspend fun clearAllNotes()
}
