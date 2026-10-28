package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FocusSession::class,
        TodoTask::class,
        Habit::class,
        HabitLog::class,
        MetricEntry::class,
        FlashcardDeck::class,
        Flashcard::class,
        DailyTask::class,
        Expense::class,
        UserProfile::class,
        PlannerEntry::class,
        AppConfigRecord::class,
        SyncQueueItem::class,
        MinimalistNote::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(PriorityConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun plannerDao(): PlannerDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun minimalistNoteDao(): MinimalistNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `minimalist_notes` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`content` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`sessionId` INTEGER NOT NULL, " +
                        "`sessionDurationMinutes` INTEGER NOT NULL, " +
                        "`tag` TEXT NOT NULL DEFAULT 'FOCUS', " +
                        "`isPinned` INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Alter todo_tasks
                db.execSQL("ALTER TABLE `todo_tasks` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `todo_tasks` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `todo_tasks` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 2. Alter focus_sessions
                db.execSQL("ALTER TABLE `focus_sessions` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `focus_sessions` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `focus_sessions` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `focus_sessions` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 3. Alter habits
                db.execSQL("ALTER TABLE `habits` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `habits` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `habits` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 4. Alter habit_logs
                db.execSQL("ALTER TABLE `habit_logs` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `habit_logs` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `habit_logs` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `habit_logs` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 5. Alter metric_entries
                db.execSQL("ALTER TABLE `metric_entries` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `metric_entries` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `metric_entries` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 6. Alter flashcard_decks
                db.execSQL("ALTER TABLE `flashcard_decks` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `flashcard_decks` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `flashcard_decks` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 7. Alter flashcards
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 8. Alter daily_tasks
                db.execSQL("ALTER TABLE `daily_tasks` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `daily_tasks` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `daily_tasks` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 9. Alter expenses
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")

                // 10. Create user_profiles
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_profiles` (" +
                        "`userId` TEXT NOT NULL, " +
                        "`email` TEXT NOT NULL, " +
                        "`displayName` TEXT NOT NULL, " +
                        "`isAnonymous` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`lastSyncedAt` INTEGER NOT NULL, " +
                        "`photoUrl` TEXT NOT NULL, " +
                        "PRIMARY KEY(`userId`))"
                )

                // 11. Create planner_entries
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `planner_entries` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`syncId` TEXT NOT NULL, " +
                        "`timeSlot` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`category` TEXT NOT NULL, " +
                        "`isCompleted` INTEGER NOT NULL, " +
                        "`date` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`userId` TEXT NOT NULL)"
                )

                // 12. Create app_config_records
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `app_config_records` (" +
                        "`configKey` TEXT NOT NULL, " +
                        "`jsonContent` TEXT NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`userId` TEXT NOT NULL, " +
                        "PRIMARY KEY(`configKey`))"
                )

                // 13. Create sync_queue
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sync_queue` (" +
                        "`queueId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`entityType` TEXT NOT NULL, " +
                        "`syncId` TEXT NOT NULL, " +
                        "`action` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`userId` TEXT NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monochrome_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
