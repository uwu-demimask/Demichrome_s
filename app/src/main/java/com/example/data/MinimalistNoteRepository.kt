package com.example.data

import kotlinx.coroutines.flow.Flow

class MinimalistNoteRepository(
    private val noteDao: MinimalistNoteDao
) {
    val allNotes: Flow<List<MinimalistNote>> = noteDao.getAllNotes()

    val latestNote: Flow<MinimalistNote?> = noteDao.getLatestNote()

    val notesCount: Flow<Int> = noteDao.getNotesCount()

    fun getNotesForSession(sessionId: Long): Flow<List<MinimalistNote>> {
        return noteDao.getNotesForSession(sessionId)
    }

    suspend fun insertNote(
        content: String,
        sessionId: Long = 0,
        sessionDurationMinutes: Int = 0,
        tag: String = "FOCUS"
    ): Long {
        val note = MinimalistNote(
            content = content.trim(),
            timestamp = System.currentTimeMillis(),
            sessionId = sessionId,
            sessionDurationMinutes = sessionDurationMinutes,
            tag = tag,
            isPinned = false
        )
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: MinimalistNote) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun deleteNote(note: MinimalistNote) {
        noteDao.deleteNote(note)
    }

    suspend fun togglePin(note: MinimalistNote) {
        noteDao.setPinned(note.id, !note.isPinned)
    }

    suspend fun clearAllNotes() {
        noteDao.clearAllNotes()
    }
}
