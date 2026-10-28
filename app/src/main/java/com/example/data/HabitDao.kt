package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    suspend fun getActiveHabitCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>): List<Long>

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // Habit logs queries
    @Query("SELECT * FROM habit_logs WHERE date LIKE :yearMonth || '%'")
    fun getLogsForMonth(yearMonth: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: Long, date: String): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogs(): Flow<List<HabitLog>>

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Transaction
    suspend fun toggleHabitCompletion(habitId: Long, date: String): Boolean {
        val existing = getLog(habitId, date)
        return if (existing != null && existing.isCompleted) {
            deleteLog(habitId, date)
            false
        } else {
            insertOrUpdateLog(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    // Metric entries for Line Graph Data Tracker
    @Query("SELECT * FROM metric_entries WHERE date LIKE :yearMonth || '%' ORDER BY date ASC")
    fun getMetricEntriesForMonth(yearMonth: String): Flow<List<MetricEntry>>

    @Query("SELECT * FROM metric_entries WHERE metricKey = :metricKey AND date LIKE :yearMonth || '%' ORDER BY date ASC")
    fun getMetricEntriesForTypeAndMonth(metricKey: String, yearMonth: String): Flow<List<MetricEntry>>

    @Query("SELECT * FROM metric_entries WHERE date = :date")
    fun getMetricEntriesForDate(date: String): Flow<List<MetricEntry>>

    @Query("SELECT * FROM metric_entries WHERE metricKey = :metricKey AND date = :date LIMIT 1")
    suspend fun getMetricEntry(metricKey: String, date: String): MetricEntry?

    @Query("SELECT * FROM metric_entries")
    fun getAllMetricEntries(): Flow<List<MetricEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetricEntry(entry: MetricEntry)

    @Query("DELETE FROM metric_entries WHERE metricKey = :metricKey AND date = :date")
    suspend fun deleteMetricEntry(metricKey: String, date: String)
}
