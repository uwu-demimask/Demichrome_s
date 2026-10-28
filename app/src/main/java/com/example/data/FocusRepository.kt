package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusRepository(
    private val focusDao: FocusDao,
    private val taskDao: TaskDao? = null,
    private val habitDao: HabitDao? = null,
    private val flashcardDao: FlashcardDao? = null,
    private val dailyTaskDao: DailyTaskDao? = null,
    private val expenseDao: ExpenseDao? = null,
    private val plannerDao: PlannerDao? = null
) {

    val allPlannerEntries: Flow<List<PlannerEntry>> = plannerDao?.getAllEntries() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPlannerEntriesForDate(date: String): Flow<List<PlannerEntry>> =
        plannerDao?.getEntriesForDate(date) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertPlannerEntry(entry: PlannerEntry): Long = plannerDao?.insertEntry(entry) ?: -1L
    suspend fun updatePlannerEntry(entry: PlannerEntry) = plannerDao?.updateEntry(entry)
    suspend fun deletePlannerEntry(entry: PlannerEntry) = plannerDao?.deleteEntry(entry)
    suspend fun setPlannerEntryCompleted(id: Long, isCompleted: Boolean) =
        plannerDao?.setEntryCompleted(id, isCompleted)

    suspend fun seedStarterPlannerEntriesIfEmpty() {
        if (plannerDao == null) return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val existing = plannerDao.getAllEntries()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            plannerDao.insertEntry(
                PlannerEntry(
                    timeSlot = "08:30 - 10:00",
                    title = "Deep Focus: Core Study & Notes",
                    description = "High-priority revision with zero distractions",
                    category = "STUDY",
                    date = todayStr
                )
            )
            plannerDao.insertEntry(
                PlannerEntry(
                    timeSlot = "11:00 - 12:30",
                    title = "Project Work / Problem Solving",
                    description = "Execute tasks in 25-minute Pomodoro cycles",
                    category = "FOCUS",
                    date = todayStr
                )
            )
            plannerDao.insertEntry(
                PlannerEntry(
                    timeSlot = "16:00 - 17:30",
                    title = "Review & Flashcards Mastery",
                    description = "Spaced repetition review on vocabulary & formulas",
                    category = "STUDY",
                    date = todayStr
                )
            )
        }
    }

    val allExpenses: Flow<List<Expense>> = expenseDao?.getAllExpenses() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertExpense(expense: Expense): Long = expenseDao?.insertExpense(expense) ?: -1L
    suspend fun updateExpense(expense: Expense) = expenseDao?.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao?.deleteExpense(expense)

    suspend fun seedStarterExpensesIfEmpty() {
        if (expenseDao == null) return
        val existing = expenseDao.getAllExpenses()
        // Check if empty via first emission
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L
            expenseDao.insertExpense(Expense(amount = 5500.0, category = ExpenseCategories.RENT_BILLS, note = "Hostel Room & Mess Advance", timestamp = now - dayMillis * 3, paymentMode = PaymentModes.UPI))
            expenseDao.insertExpense(Expense(amount = 320.0, category = ExpenseCategories.FOOD, note = "Dinner with friends", timestamp = now - dayMillis, paymentMode = PaymentModes.UPI))
            expenseDao.insertExpense(Expense(amount = 140.0, category = ExpenseCategories.SNACKS_TEA, note = "Chai & Samosa break", timestamp = now - 3600000 * 4, paymentMode = PaymentModes.CASH))
            expenseDao.insertExpense(Expense(amount = 650.0, category = ExpenseCategories.STUDY, note = "Data Structures textbook & notebook", timestamp = now - 3600000 * 2, paymentMode = PaymentModes.CARD))
        }
    }

    val allSessions: Flow<List<FocusSession>> = focusDao.getAllSessions()
    val recentSessions: Flow<List<FocusSession>> = focusDao.getRecentSessions(30)
    val totalCompletedSessions: Flow<Int> = focusDao.getTotalCompletedSessionsCount()
    val totalFocusSeconds: Flow<Long> = focusDao.getTotalFocusSeconds().map { it ?: 0L }
    val ratedSessions: Flow<List<FocusSession>> = focusDao.getRatedSessions()
    val averageRating: Flow<Float?> = focusDao.getAverageRating()

    // Flashcards flows & operations
    val allDecks: Flow<List<FlashcardDeck>> = flashcardDao?.getAllDecks() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun ensureDefaultDecksSeeded() {
        if (flashcardDao == null) return
        val existing = flashcardDao.getAllDecks()
        // Simple seed if empty
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val all = flashcardDao.getAllCards()
            // We can check and seed starter decks if needed
        }
    }

    suspend fun seedStarterDecksIfEmpty() {
        if (flashcardDao == null) return
        val defaultDecks = listOf(
            FlashcardDeck(title = "CORE CS & ALGORITHMS", description = "Time complexity, data structures & system concepts", category = "TECH"),
            FlashcardDeck(title = "RETRO DESIGN PRINCIPLES", description = "Minimalism, monochrome contrast & typography", category = "DESIGN"),
            FlashcardDeck(title = "SPANISH ESSENTIALS", description = "High-frequency vocabulary & phrases", category = "LANGUAGE")
        )
        val deck1Id = flashcardDao.insertDeck(defaultDecks[0])
        val deck2Id = flashcardDao.insertDeck(defaultDecks[1])
        val deck3Id = flashcardDao.insertDeck(defaultDecks[2])

        // Cards for Deck 1
        flashcardDao.insertCard(Flashcard(deckId = deck1Id, front = "What is the average time complexity of QuickSort?", back = "O(N log N). Worst case is O(N^2) when poorly partitioned.", hint = "Divide and conquer"))
        flashcardDao.insertCard(Flashcard(deckId = deck1Id, front = "What is the difference between Process and Thread?", back = "A process has its own isolated memory space. Threads within a process share the same memory heap and resources.", hint = "Memory isolation"))
        flashcardDao.insertCard(Flashcard(deckId = deck1Id, front = "What is the CAP Theorem?", back = "A distributed system can only guarantee at most 2 of 3: Consistency, Availability, Partition tolerance.", hint = "Distributed systems trade-off"))
        flashcardDao.insertCard(Flashcard(deckId = deck1Id, front = "What is a Deadlock in concurrency?", back = "A state where two or more threads are blocked forever, each waiting for a lock held by the other.", hint = "Circular waiting"))

        // Cards for Deck 2
        flashcardDao.insertCard(Flashcard(deckId = deck2Id, front = "What is the 80/20 Rule in UI minimalism?", back = "80% of users' attention and utility comes from 20% of essential interface elements. Remove the remaining noise.", hint = "Pareto principle"))
        flashcardDao.insertCard(Flashcard(deckId = deck2Id, front = "Why use high-contrast monochrome for focus?", back = "Color triggers dopamine micro-stimulations. Pure monochrome reduces visual fatigue and cognitive friction.", hint = "Sensory reduction"))
        flashcardDao.insertCard(Flashcard(deckId = deck2Id, front = "What is negative space (whitespace)?", back = "Unmarked area surrounding visual components that provides breathing room and establishes visual hierarchy.", hint = "Breathing room"))

        // Cards for Deck 3
        flashcardDao.insertCard(Flashcard(deckId = deck3Id, front = "El enfoque", back = "Focus / Concentration", hint = "Productivity noun"))
        flashcardDao.insertCard(Flashcard(deckId = deck3Id, front = "La tranquilidad", back = "Peace / Calmness / Quietness", hint = "Mind state"))
        flashcardDao.insertCard(Flashcard(deckId = deck3Id, front = "Paso a paso", back = "Step by step / Gradually", hint = "Progress phrase"))
    }

    fun getCardsForDeck(deckId: Long): Flow<List<Flashcard>> =
        flashcardDao?.getCardsForDeck(deckId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getCardCountForDeck(deckId: Long): Flow<Int> =
        flashcardDao?.getCardCountForDeck(deckId) ?: kotlinx.coroutines.flow.flowOf(0)

    suspend fun insertDeck(deck: FlashcardDeck): Long = flashcardDao?.insertDeck(deck) ?: -1L
    suspend fun updateDeck(deck: FlashcardDeck) = flashcardDao?.updateDeck(deck)
    suspend fun deleteDeck(deck: FlashcardDeck) = flashcardDao?.deleteDeck(deck)

    suspend fun insertCard(card: Flashcard): Long = flashcardDao?.insertCard(card) ?: -1L
    suspend fun updateCard(card: Flashcard) = flashcardDao?.updateCard(card)
    suspend fun deleteCard(card: Flashcard) = flashcardDao?.deleteCard(card)
    suspend fun updateCardReviewStatus(cardId: Long, isKnown: Boolean) =
        flashcardDao?.updateCardReviewStatus(cardId, isKnown)
    suspend fun resetDeckProgress(deckId: Long) = flashcardDao?.resetDeckProgress(deckId)

    // Rating and Quality Insights
    suspend fun updateSessionRating(sessionId: Long, rating: Int, note: String) {
        focusDao.updateSessionRating(sessionId, rating, note)
    }

    fun getSessionQualityInsights(): Flow<SessionQualityInsights> {
        return focusDao.getAllSessions().map { sessions ->
            val rated = sessions.filter { it.rating in 1..5 }
            if (rated.isEmpty()) {
                return@map SessionQualityInsights(
                    averageRating = 0f,
                    totalRatedSessions = 0,
                    bestFocusTime = "Morning",
                    ratingCounts = emptyMap()
                )
            }
            val avg = rated.map { it.rating }.average().toFloat()
            val counts = (1..5).associateWith { r -> rated.count { it.rating == r } }

            // Calculate best focus time based on highest average ratings by time of day
            var morningRatings = mutableListOf<Int>()
            var afternoonRatings = mutableListOf<Int>()
            var eveningRatings = mutableListOf<Int>()
            val cal = Calendar.getInstance()

            rated.forEach { session ->
                cal.timeInMillis = session.startTimeMillis
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                when {
                    hour < 12 -> morningRatings.add(session.rating)
                    hour < 18 -> afternoonRatings.add(session.rating)
                    else -> eveningRatings.add(session.rating)
                }
            }

            val avgMorn = if (morningRatings.isNotEmpty()) morningRatings.average() else 0.0
            val avgAft = if (afternoonRatings.isNotEmpty()) afternoonRatings.average() else 0.0
            val avgEve = if (eveningRatings.isNotEmpty()) eveningRatings.average() else 0.0

            val bestTime = when {
                avgMorn >= avgAft && avgMorn >= avgEve && avgMorn > 0.0 -> "Morning (06:00 - 12:00)"
                avgAft >= avgMorn && avgAft >= avgEve && avgAft > 0.0 -> "Afternoon (12:00 - 18:00)"
                avgEve > 0.0 -> "Evening / Night (18:00+)"
                else -> "Morning"
            }

            SessionQualityInsights(
                averageRating = (Math.round(avg * 10f) / 10f),
                totalRatedSessions = rated.size,
                bestFocusTime = bestTime,
                ratingCounts = counts
            )
        }
    }

    // Habit & Sanctuary Matrix flows
    val activeHabits: Flow<List<Habit>> = habitDao?.getAllActiveHabits() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val allHabits: Flow<List<Habit>> = habitDao?.getAllHabits() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun ensureDefaultHabitsSeeded() {
        if (habitDao == null) return
        if (habitDao.getActiveHabitCount() == 0) {
            val defaultHabits = listOf(
                Habit(title = "Morning Deep Work", category = "FOCUS", targetDaysPerWeek = 7, sortOrder = 0),
                Habit(title = "Zero Phone Distractions", category = "DISCIPLINE", targetDaysPerWeek = 7, sortOrder = 1),
                Habit(title = "Read 20+ Pages", category = "MIND", targetDaysPerWeek = 7, sortOrder = 2),
                Habit(title = "Daily Exercise / Workout", category = "HEALTH", targetDaysPerWeek = 5, sortOrder = 3),
                Habit(title = "Mindfulness / Meditation", category = "MIND", targetDaysPerWeek = 7, sortOrder = 4),
                Habit(title = "Digital Wind-Down (10PM)", category = "ROUTINE", targetDaysPerWeek = 7, sortOrder = 5)
            )
            habitDao.insertHabits(defaultHabits)
        }
    }

    suspend fun insertHabit(habit: Habit): Long = habitDao?.insertHabit(habit) ?: -1L
    suspend fun updateHabit(habit: Habit) = habitDao?.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao?.deleteHabit(habit)
    suspend fun deleteHabitById(id: Long) = habitDao?.deleteHabitById(id)

    fun getHabitLogsForMonth(yearMonth: String): Flow<List<HabitLog>> =
        habitDao?.getLogsForMonth(yearMonth) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun toggleHabitCompletion(habitId: Long, date: String): Boolean =
        habitDao?.toggleHabitCompletion(habitId, date) ?: false

    // Metric Line Graph tracking
    fun getMetricEntriesForMonth(yearMonth: String): Flow<List<MetricEntry>> =
        habitDao?.getMetricEntriesForMonth(yearMonth) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getMetricEntriesForTypeAndMonth(metricKey: String, yearMonth: String): Flow<List<MetricEntry>> =
        habitDao?.getMetricEntriesForTypeAndMonth(metricKey, yearMonth) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertOrUpdateMetricEntry(entry: MetricEntry) =
        habitDao?.insertOrUpdateMetricEntry(entry)

    suspend fun deleteMetricEntry(metricKey: String, date: String) =
        habitDao?.deleteMetricEntry(metricKey, date)

    // Task flows & methods
    val allTasks: Flow<List<TodoTask>> = taskDao?.getAllTasks() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val activeTasks: Flow<List<TodoTask>> = taskDao?.getActiveTasks() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val completedTasks: Flow<List<TodoTask>> = taskDao?.getCompletedTasks() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val activeTaskCount: Flow<Int> = taskDao?.getActiveTaskCount() ?: kotlinx.coroutines.flow.flowOf(0)
    val completedTaskCount: Flow<Int> = taskDao?.getCompletedTaskCount() ?: kotlinx.coroutines.flow.flowOf(0)

    suspend fun insertTask(task: TodoTask): Long {
        return taskDao?.insertTask(task) ?: -1L
    }

    suspend fun updateTask(task: TodoTask) {
        taskDao?.updateTask(task)
    }

    suspend fun deleteTask(task: TodoTask) {
        taskDao?.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao?.deleteTaskById(id)
    }

    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao?.setTaskCompleted(id, isCompleted, completedAt)
    }

    suspend fun incrementTaskSessions(id: Long) {
        taskDao?.incrementCompletedSessions(id)
    }

    suspend fun clearCompletedTasks() {
        taskDao?.clearCompletedTasks()
    }

    suspend fun getTaskById(id: Long): TodoTask? {
        return taskDao?.getTaskById(id)
    }

    // Daily Tasks (24-hour reset routine)
    val allDailyTasks: Flow<List<DailyTask>> = dailyTaskDao?.getAllDailyTasks() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun ensureDefaultDailyTasksSeeded() {
        if (dailyTaskDao == null) return
        if (dailyTaskDao.getDailyTaskCount() == 0) {
            val defaults = listOf(
                DailyTask(title = "Morning Deep Work Sprint (45m)", description = "Uninterrupted focus block before midday", category = "FOCUS", sortOrder = 0),
                DailyTask(title = "Zero Phone Notifications Window", description = "DND enabled during core work hours", category = "DISCIPLINE", sortOrder = 1),
                DailyTask(title = "Execute Top 3 Priority Objectives", description = "Make tangible progress on main tasks", category = "ROUTINE", sortOrder = 2),
                DailyTask(title = "Mindful Physical Recharge / Walk", description = "Step away from screens for 15+ minutes", category = "HEALTH", sortOrder = 3),
                DailyTask(title = "Evening Focus Review & Shutdown", description = "Log today's accomplishments & prep tomorrow", category = "SHUTDOWN", sortOrder = 4)
            )
            dailyTaskDao.insertDailyTasks(defaults)
        }
    }

    suspend fun insertDailyTask(task: DailyTask): Long = dailyTaskDao?.insertDailyTask(task) ?: -1L
    suspend fun updateDailyTask(task: DailyTask) = dailyTaskDao?.updateDailyTask(task)
    suspend fun deleteDailyTask(task: DailyTask) = dailyTaskDao?.deleteDailyTask(task)
    suspend fun deleteDailyTaskById(id: Long) = dailyTaskDao?.deleteDailyTaskById(id)

    suspend fun toggleDailyTaskCompletion(task: DailyTask, todayDate: String) {
        if (dailyTaskDao == null) return
        val newCompleted = !task.isCompleted
        val newStreak = if (newCompleted) {
            // Check if last completed was yesterday for streak
            if (task.lastCompletedDate.isNotBlank()) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                if (task.lastCompletedDate == yesterday) task.streakDays + 1 else if (task.lastCompletedDate == todayDate) task.streakDays else 1
            } else 1
        } else {
            task.streakDays
        }
        val dateToSave = if (newCompleted) todayDate else task.lastCompletedDate
        dailyTaskDao.setTaskStatus(task.id, newCompleted, dateToSave, newStreak)
    }

    suspend fun resetDailyTasksIfNewDay(todayDate: String) {
        dailyTaskDao?.resetIncompleteForDate(todayDate)
    }

    suspend fun resetAllDailyTasksManually() {
        dailyTaskDao?.resetAllDailyTasks()
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getSessionsForToday(): Flow<List<FocusSession>> {
        return focusDao.getSessionsForDate(getTodayDateString())
    }

    fun getTodayFocusSeconds(): Flow<Long> {
        return focusDao.getFocusSecondsForDate(getTodayDateString()).map { it ?: 0L }
    }

    fun getTodaySessionCount(): Flow<Int> {
        return focusDao.getSessionCountForDate(getTodayDateString())
    }

    /**
     * Calculates weekly daily breakdown for the last 7 days
     */
    fun getWeeklyHistory(): Flow<List<DayFocusSummary>> {
        return getHistoryDays(7)
    }

    /**
     * Calculates 14-day history for the daily stats calendar/chart
     */
    fun get14DayHistory(): Flow<List<DayFocusSummary>> {
        return getHistoryDays(14)
    }

    private fun getHistoryDays(daysCount: Int): Flow<List<DayFocusSummary>> {
        return focusDao.getAllSessions().map { sessions ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

            val historyList = mutableListOf<DayFocusSummary>()

            for (i in (daysCount - 1) downTo 0) {
                val dayCal = Calendar.getInstance()
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = dateFormat.format(dayCal.time)
                val dayName = dayNameFormat.format(dayCal.time).uppercase(Locale.getDefault())

                val matchingSessions = sessions.filter { it.dateString == dateStr }
                val totalSecs = matchingSessions.sumOf { it.actualElapsedSeconds }
                val totalMins = totalSecs / 60
                val completed = matchingSessions.count { it.completedSuccessfully }

                historyList.add(
                    DayFocusSummary(
                        dateString = dateStr,
                        dayOfWeek = dayName,
                        totalMinutes = totalMins,
                        sessionCount = matchingSessions.size,
                        completedCount = completed
                    )
                )
            }
            historyList
        }
    }

    /**
     * Generates a comprehensive detailed breakdown for any given date.
     */
    fun getDetailedDailyStats(dateString: String, dailyGoalMinutes: Int): Flow<DetailedDailyStats> {
        return focusDao.getAllSessions().map { allSessions ->
            val daySessions = allSessions.filter { it.dateString == dateString }
            val totalSeconds = daySessions.sumOf { it.actualElapsedSeconds.toLong() }
            val totalMins = (totalSeconds / 60).toInt()
            val totalCount = daySessions.size
            val completed = daySessions.count { it.completedSuccessfully }
            val cancelled = totalCount - completed
            val completionRate = if (totalCount > 0) ((completed * 100) / totalCount) else 100
            val avgMins = if (totalCount > 0) (totalMins / totalCount) else 0
            val longestMins = daySessions.maxOfOrNull { it.actualElapsedSeconds / 60 } ?: 0

            val goalProgress = if (dailyGoalMinutes > 0) {
                ((totalMins * 100) / dailyGoalMinutes).coerceAtMost(999)
            } else 100

            // Breakdown by time of day
            var morningSecs = 0
            var afternoonSecs = 0
            var eveningSecs = 0
            val cal = Calendar.getInstance()

            daySessions.forEach { session ->
                cal.timeInMillis = session.startTimeMillis
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                when {
                    hour < 12 -> morningSecs += session.actualElapsedSeconds
                    hour < 18 -> afternoonSecs += session.actualElapsedSeconds
                    else -> eveningSecs += session.actualElapsedSeconds
                }
            }

            val deepCount = daySessions.count { it.tag.contains("DEEP", ignoreCase = true) || it.styleUsed.contains("Deep", ignoreCase = true) }

            // Format date header
            val isToday = dateString == getTodayDateString()
            val parsedDate = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            } catch (_: Exception) { null }
            val formattedDate = if (parsedDate != null) {
                SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(parsedDate)
            } else dateString

            DetailedDailyStats(
                dateString = dateString,
                formattedDate = formattedDate,
                isToday = isToday,
                totalFocusSeconds = totalSeconds,
                totalFocusMinutes = totalMins,
                totalSessions = totalCount,
                completedSessions = completed,
                cancelledSessions = cancelled,
                completionRatePercent = completionRate,
                averageSessionMinutes = avgMins,
                longestSessionMinutes = longestMins,
                goalMinutes = dailyGoalMinutes,
                goalProgressPercent = goalProgress,
                goalAchieved = totalMins >= dailyGoalMinutes,
                morningMinutes = morningSecs / 60,
                afternoonMinutes = afternoonSecs / 60,
                eveningMinutes = eveningSecs / 60,
                deepFocusCount = deepCount,
                sessions = daySessions
            )
        }
    }

    /**
     * Calculates consecutive day focus streak
     */
    fun getStreakDays(): Flow<Int> {
        return focusDao.getAllSessions().map { sessions ->
            if (sessions.isEmpty()) return@map 0

            val distinctDates = sessions.map { it.dateString }.distinct().toSet()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            var streak = 0
            val todayStr = dateFormat.format(calendar.time)

            // If today has sessions, start checking from today, else start from yesterday
            if (distinctDates.contains(todayStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(calendar.time)
                if (!distinctDates.contains(yesterdayStr)) {
                    return@map 0
                }
            }

            while (true) {
                val dateStr = dateFormat.format(calendar.time)
                if (distinctDates.contains(dateStr)) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            streak
        }
    }

    suspend fun saveSession(session: FocusSession): Long {
        return focusDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        focusDao.deleteSessionById(id)
    }

    suspend fun clearHistory() {
        focusDao.clearAll()
    }

    /**
     * Exports focus sessions to standard CSV format
     */
    fun exportSessionsToCsv(sessions: List<FocusSession>): String {
        val sb = StringBuilder()
        sb.append("id,date,start_time,target_duration_mins,actual_elapsed_secs,completed,tag,style,rating,note\n")
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sessions.forEach { s ->
            val timeFormatted = timeFormat.format(Date(s.startTimeMillis))
            val cleanTag = s.tag.replace("\"", "\"\"")
            val cleanStyle = s.styleUsed.replace("\"", "\"\"")
            val cleanNote = s.note.replace("\"", "\"\"").replace("\n", " ")
            sb.append("${s.id},\"${s.dateString}\",\"$timeFormatted\",${s.targetDurationMinutes},${s.actualElapsedSeconds},${s.completedSuccessfully},\"$cleanTag\",\"$cleanStyle\",${s.rating},\"$cleanNote\"\n")
        }
        return sb.toString()
    }

    /**
     * Exports focus sessions to structured JSON format
     */
    fun exportSessionsToJson(sessions: List<FocusSession>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sessions.forEachIndexed { index, s ->
            val timeFormatted = timeFormat.format(Date(s.startTimeMillis))
            val cleanTag = s.tag.replace("\\", "\\\\").replace("\"", "\\\"")
            val cleanStyle = s.styleUsed.replace("\\", "\\\\").replace("\"", "\\\"")
            val cleanNote = s.note.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            sb.append("  {\n")
            sb.append("    \"id\": ${s.id},\n")
            sb.append("    \"date\": \"${s.dateString}\",\n")
            sb.append("    \"timestamp\": \"$timeFormatted\",\n")
            sb.append("    \"targetMinutes\": ${s.targetDurationMinutes},\n")
            sb.append("    \"actualSeconds\": ${s.actualElapsedSeconds},\n")
            sb.append("    \"completed\": ${s.completedSuccessfully},\n")
            sb.append("    \"tag\": \"$cleanTag\",\n")
            sb.append("    \"style\": \"$cleanStyle\",\n")
            sb.append("    \"rating\": ${s.rating},\n")
            sb.append("    \"note\": \"$cleanNote\"\n")
            sb.append("  }${if (index < sessions.size - 1) "," else ""}\n")
        }
        sb.append("]")
        return sb.toString()
    }

    /**
     * Exports formatted ASCII / Markdown Ledger Report
     */
    fun exportSessionsToMarkdown(
        sessions: List<FocusSession>,
        totalSeconds: Long = sessions.sumOf { it.actualElapsedSeconds.toLong() },
        streak: Int = 0
    ): String {
        val totalHours = totalSeconds / 3600
        val totalMins = (totalSeconds % 3600) / 60
        val completedCount = sessions.count { it.completedSuccessfully }
        val completionRate = if (sessions.isNotEmpty()) (completedCount * 100) / sessions.size else 0

        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("DEMICHROME // FOCUS SESSION LEDGER & AUDIT REPORT\n")
        sb.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("====================================================\n\n")

        sb.append("## EXECUTIVE SUMMARY\n")
        sb.append("- Total Focus Time: ${totalHours}h ${totalMins}m (${totalSeconds}s)\n")
        sb.append("- Total Logged Sessions: ${sessions.size}\n")
        sb.append("- Successful Completions: $completedCount ($completionRate%)\n")
        sb.append("- Consecutive Streak: $streak days\n\n")

        sb.append("## SESSION LOG HISTORY\n")
        if (sessions.isEmpty()) {
            sb.append("No recorded focus sessions.\n")
        } else {
            sb.append("| Date | Time | Duration | Status | Tag | Rating | Note |\n")
            sb.append("|------|------|----------|--------|-----|--------|------|\n")
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            sessions.forEach { s ->
                val time = timeFmt.format(Date(s.startTimeMillis))
                val mins = s.actualElapsedSeconds / 60
                val secs = s.actualElapsedSeconds % 60
                val dur = "${mins}m ${secs}s"
                val status = if (s.completedSuccessfully) "COMPLETED" else "EARLY_STOP"
                val stars = if (s.rating > 0) "★".repeat(s.rating) else "-"
                val note = if (s.note.isNotBlank()) s.note.replace("|", "/") else "-"
                sb.append("| ${s.dateString} | $time | $dur (${s.targetDurationMinutes}M) | $status | ${s.tag} | $stars | $note |\n")
            }
        }
        sb.append("\n====================================================\n")
        sb.append("DEMICHROME DIGITAL WORKSPACE // PMAK\n")
        sb.append("====================================================\n")
        return sb.toString()
    }
}
