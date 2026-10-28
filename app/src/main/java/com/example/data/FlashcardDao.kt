package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeck): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeck)

    @Delete
    suspend fun deleteDeck(deck: FlashcardDeck)

    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<FlashcardDeck>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId LIMIT 1")
    fun getDeckById(deckId: Long): Flow<FlashcardDeck?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard): Long

    @Update
    suspend fun updateCard(card: Flashcard)

    @Delete
    suspend fun deleteCard(card: Flashcard)

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    fun getCardsForDeck(deckId: Long): Flow<List<Flashcard>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getCardCountForDeck(deckId: Long): Flow<Int>

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun getAllCards(): Flow<List<Flashcard>>

    @Query("UPDATE flashcards SET isKnown = :isKnown, timesReviewed = timesReviewed + 1, lastReviewedAt = :timestamp WHERE id = :cardId")
    suspend fun updateCardReviewStatus(cardId: Long, isKnown: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE flashcards SET isKnown = 0 WHERE deckId = :deckId")
    suspend fun resetDeckProgress(deckId: Long)

    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Long)
}
