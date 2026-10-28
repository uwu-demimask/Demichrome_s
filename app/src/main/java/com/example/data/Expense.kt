package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Personal & Hostel Expense Entry for Demichrome OS.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMode: String = "UPI",
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = timestamp,
    val updatedAt: Long = timestamp,
    val userId: String = "local_user"
)

object ExpenseCategories {
    const val FOOD = "FOOD"
    const val SNACKS_TEA = "SNACKS / TEA"
    const val MESS_CANTEEN = "MESS / CANTEEN"
    const val GROCERIES = "GROCERIES"
    const val TRAVEL = "TRAVEL / AUTO / CAB"
    const val STUDY = "STUDY / BOOKS"
    const val RENT_BILLS = "RENT / BILLS"
    const val ENTERTAINMENT = "ENTERTAINMENT"
    const val OTHER = "OTHER"

    val ALL = listOf(
        FOOD,
        SNACKS_TEA,
        MESS_CANTEEN,
        GROCERIES,
        TRAVEL,
        STUDY,
        RENT_BILLS,
        ENTERTAINMENT,
        OTHER
    )
}

object PaymentModes {
    const val CASH = "CASH"
    const val UPI = "UPI"
    const val CARD = "CARD"

    val ALL = listOf(UPI, CASH, CARD)
}
