package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_decks")
data class FlashcardDeck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "STUDY",
    val createdAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val hint: String = "",
    val isKnown: Boolean = false,
    val timesReviewed: Int = 0,
    val lastReviewedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

data class DeckWithStats(
    val deck: FlashcardDeck,
    val totalCards: Int,
    val knownCards: Int,
    val reviewNeededCards: Int
) {
    val masteryPercent: Int
        get() = if (totalCards > 0) ((knownCards * 100) / totalCards) else 0
}
