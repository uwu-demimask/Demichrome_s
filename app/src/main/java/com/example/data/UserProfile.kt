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
 * User Profile entity for Demichrome persistent accounts.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = 0L,
    val photoUrl: String = ""
)

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfile?

    @Query("SELECT * FROM user_profiles ORDER BY updatedAt DESC LIMIT 1")
    fun getActiveUserFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getActiveUser(): UserProfile?

    @Query("SELECT * FROM user_profiles")
    suspend fun getAllProfiles(): List<UserProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteProfileById(userId: String)

    @Query("UPDATE user_profiles SET lastSyncedAt = :timestamp WHERE userId = :userId")
    suspend fun updateLastSynced(userId: String, timestamp: Long)
}
