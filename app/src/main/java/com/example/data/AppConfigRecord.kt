package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * App Configuration entity for backing up and synchronizing App Preferences,
 * Theme Preferences, Notification Settings, and Shield configurations.
 */
@Entity(tableName = "app_config_records")
data class AppConfigRecord(
    @PrimaryKey
    val configKey: String, // "THEME_CONFIG", "SHIELD_CONFIG", "NOTIFICATIONS_CONFIG", "GENERAL_CONFIG"
    val jsonContent: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Dao
interface AppConfigDao {

    @Query("SELECT * FROM app_config_records WHERE configKey = :key LIMIT 1")
    fun getConfigFlow(key: String): Flow<AppConfigRecord?>

    @Query("SELECT * FROM app_config_records WHERE configKey = :key LIMIT 1")
    suspend fun getConfig(key: String): AppConfigRecord?

    @Query("SELECT * FROM app_config_records")
    suspend fun getAllConfigs(): List<AppConfigRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppConfigRecord)

    @Query("DELETE FROM app_config_records WHERE configKey = :key")
    suspend fun deleteConfig(key: String)
}
