package com.example.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.blocker.BlockableApp
import com.example.core.blocker.DistractionBlockerManager
import com.example.core.blocker.InterceptionEvent
import com.example.core.haptics.RetroHaptics
import com.example.core.timer.FocusTimerManager
import com.example.data.AppDatabase
import com.example.data.DayFocusSummary
import com.example.data.DefaultSanctuaryMetric
import com.example.data.DetailedDailyStats
import com.example.data.Expense
import com.example.data.FocusRepository
import com.example.data.FocusSession
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.MetricEntry
import com.example.data.NavigationStyle
import com.example.data.RetroThemeId
import com.example.data.TaskPriority
import com.example.data.TimerStyleId
import com.example.data.TodoTask
import com.example.data.account.AccountManager
import com.example.data.account.UserAccount
import com.example.data.sync.SyncEngine
import com.example.data.sync.SyncResult
import com.example.data.sync.SyncStatus
import com.example.data.UserPreferences
import com.example.ui.timer.FocusState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.DailyTask
import com.example.data.MonochromeModeLevel
import com.example.data.Flashcard
import com.example.data.FlashcardDeck
import com.example.data.SessionQualityInsights
import com.example.data.MinimalistNote
import com.example.data.MinimalistNoteRepository

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val userPreferences = UserPreferences(context)
    private val database = AppDatabase.getDatabase(context)
    val repository = FocusRepository(
        database.focusDao(),
        database.taskDao(),
        database.habitDao(),
        database.flashcardDao(),
        database.dailyTaskDao(),
        database.expenseDao(),
        database.plannerDao()
    )
    val noteRepository = MinimalistNoteRepository(database.minimalistNoteDao())

    val accountManager = AccountManager.getInstance(context)
    val syncEngine = SyncEngine(context, database, accountManager)

    val currentUser: StateFlow<UserAccount> = accountManager.currentUser
    val syncStatus: StateFlow<SyncStatus> = syncEngine.syncStatus
    val lastSyncedAt: StateFlow<Long> = syncEngine.lastSyncedAt
    val isOnline: StateFlow<Boolean> = syncEngine.isOnline
    val lastSyncResult: StateFlow<SyncResult?> = syncEngine.lastSyncResult
    val allPlannerEntries = repository.allPlannerEntries

    fun performSync() {
        viewModelScope.launch {
            syncEngine.performSync()
        }
    }

    fun backupToCloud() {
        viewModelScope.launch {
            syncEngine.backupToCloud()
        }
    }

    fun restoreFromCloud(backupJson: String? = null) {
        viewModelScope.launch {
            syncEngine.restoreFromCloudBackup(backupJson)
        }
    }

    fun signIn(email: String, pass: String, onResult: (Result<UserAccount>) -> Unit) {
        viewModelScope.launch {
            val res = accountManager.signIn(email, pass)
            if (res.isSuccess) {
                res.getOrNull()?.userId?.let { uid ->
                    syncEngine.associateGuestDataToUser(uid)
                }
                syncEngine.performSync()
            }
            onResult(res)
        }
    }

    fun createAccount(email: String, pass: String, name: String, onResult: (Result<UserAccount>) -> Unit) {
        viewModelScope.launch {
            val res = accountManager.createAccount(email, pass, name)
            if (res.isSuccess) {
                res.getOrNull()?.userId?.let { uid ->
                    syncEngine.associateGuestDataToUser(uid)
                }
                syncEngine.performSync()
            }
            onResult(res)
        }
    }

    fun recoverAccount(email: String, newPass: String, onResult: (Result<UserAccount>) -> Unit) {
        viewModelScope.launch {
            val res = accountManager.recoverAccount(email, newPass)
            if (res.isSuccess) {
                res.getOrNull()?.userId?.let { uid ->
                    syncEngine.associateGuestDataToUser(uid)
                }
                syncEngine.performSync()
            }
            onResult(res)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut()
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            accountManager.updateDisplayName(name)
        }
    }

    fun getBackendConfigurationStatus(): String {
        return syncEngine.getBackendConfigurationStatus()
    }

    init {
        FocusTimerManager.init(application)
        DistractionBlockerManager.startMonitoring(context, userPreferences)
        viewModelScope.launch {
            repository.ensureDefaultHabitsSeeded()
            repository.seedStarterDecksIfEmpty()
            repository.ensureDefaultDailyTasksSeeded()
            repository.seedStarterExpensesIfEmpty()
            repository.seedStarterPlannerEntriesIfEmpty()
            checkAndPerformDailyReset()
        }
    }

    val targetMinutes: StateFlow<Int> = FocusTimerManager.targetMinutes
    val remainingSeconds: StateFlow<Int> = FocusTimerManager.remainingSeconds
    val state: StateFlow<FocusState> = FocusTimerManager.state
    val isDeepFocusActive: StateFlow<Boolean> = FocusTimerManager.isDeepFocusActive
    val systemStatus: StateFlow<String> = FocusTimerManager.systemStatus
    val ambientSound: StateFlow<com.example.core.audio.RetroAudioSynthesizer.AmbientSoundType> = userPreferences.ambientSound

    // Distraction Blocker flows
    val interceptionEvent: StateFlow<InterceptionEvent?> = DistractionBlockerManager.interceptionEvent
    val isBlockerMonitoring: StateFlow<Boolean> = DistractionBlockerManager.isMonitoring
    val blockerEnabled: StateFlow<Boolean> = userPreferences.blockerEnabled
    val blockedPackages: StateFlow<Set<String>> = userPreferences.blockedPackages
    val blockerStrictMode: StateFlow<Boolean> = userPreferences.blockerStrictMode
    val blockedAttemptsToday: StateFlow<Int> = userPreferences.blockedAttemptsToday

    // To-Do Task Management flows
    val allTasks: StateFlow<List<TodoTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TodoTask>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TodoTask>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTaskId: StateFlow<Long?> = FocusTimerManager.activeTaskId

    val selectedTargetTask: StateFlow<TodoTask?> = combine(
        allTasks,
        activeTaskId
    ) { tasks, selectedId ->
        if (selectedId != null) tasks.firstOrNull { it.id == selectedId } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addTask(
        title: String,
        notes: String = "",
        priority: TaskPriority = TaskPriority.MEDIUM,
        estimatedSessions: Int = 1,
        category: String = "FOCUS"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TodoTask(
                title = title.trim(),
                notes = notes.trim(),
                priority = priority,
                estimatedSessions = estimatedSessions.coerceIn(1, 20),
                category = category.trim().ifBlank { "FOCUS" }
            )
            val newId = repository.insertTask(task)
            // If no task is selected for focus, auto-select this new task
            if (activeTaskId.value == null && newId > 0) {
                FocusTimerManager.setActiveTaskId(newId)
            }
            FocusTimerManager.setSystemStatus("> TASK ADDED: ${title.trim().take(20)}")
            RetroHaptics.performClick(context)
        }
    }

    fun toggleTaskCompleted(task: TodoTask) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            repository.setTaskCompleted(task.id, nextState)
            if (nextState) {
                RetroHaptics.performCompletionBuzz(context)
                FocusTimerManager.setSystemStatus("> TASK COMPLETED: ${task.title.take(18)}")
                // If this completed task was selected, deselect or clear
                if (activeTaskId.value == task.id) {
                    FocusTimerManager.setActiveTaskId(null)
                }
            } else {
                RetroHaptics.performClick(context)
                FocusTimerManager.setSystemStatus("> TASK REOPENED")
            }
        }
    }

    fun updateTask(task: TodoTask) {
        viewModelScope.launch {
            repository.updateTask(task)
            RetroHaptics.performClick(context)
        }
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (activeTaskId.value == task.id) {
                FocusTimerManager.setActiveTaskId(null)
            }
            FocusTimerManager.setSystemStatus("> TASK DELETED")
            RetroHaptics.performClick(context)
        }
    }

    fun selectTaskForFocus(taskId: Long?) {
        FocusTimerManager.setActiveTaskId(taskId)
        if (taskId != null) {
            val task = allTasks.value.firstOrNull { it.id == taskId }
            FocusTimerManager.setSystemStatus(if (task != null) "> TARGET: ${task.title.take(18)}" else "> TARGET SELECTED")
        } else {
            FocusTimerManager.setSystemStatus("> TARGET CLEARED")
        }
        RetroHaptics.performClick(context)
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
            FocusTimerManager.setSystemStatus("> COMPLETED TASKS CLEARED")
            RetroHaptics.performClick(context)
        }
    }

    // Daily Focus Routine Tasks (Resets every 24 hours / daily)
    val allDailyTasks: StateFlow<List<DailyTask>> = repository.allDailyTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTasksCompletedCount: StateFlow<Int> = allDailyTasks.map { list ->
        list.count { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyTasksTotalCount: StateFlow<Int> = allDailyTasks.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun checkAndPerformDailyReset() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastReset = userPreferences.dailyTaskLastResetDate.value
            if (lastReset != todayStr) {
                repository.resetDailyTasksIfNewDay(todayStr)
                userPreferences.setDailyTaskLastResetDate(todayStr)
            }
        }
    }

    fun toggleDailyTask(task: DailyTask) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.toggleDailyTaskCompletion(task, todayStr)
            if (!task.isCompleted) {
                RetroHaptics.performCompletionBuzz(context)
                FocusTimerManager.setSystemStatus("> DAILY ROUTINE CHECKED: ${task.title.take(16)}")
            } else {
                RetroHaptics.performClick(context)
                FocusTimerManager.setSystemStatus("> DAILY ROUTINE UNCHECKED")
            }
        }
    }

    fun addDailyTask(title: String, description: String = "", category: String = "ROUTINE") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = DailyTask(
                title = title.trim(),
                description = description.trim(),
                category = category.trim().ifBlank { "ROUTINE" },
                sortOrder = (allDailyTasks.value.maxOfOrNull { it.sortOrder } ?: 0) + 1
            )
            repository.insertDailyTask(task)
            FocusTimerManager.setSystemStatus("> ROUTINE TASK ADDED: ${title.trim().take(18)}")
            RetroHaptics.performClick(context)
        }
    }

    fun deleteDailyTask(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyTaskById(id)
            FocusTimerManager.setSystemStatus("> ROUTINE TASK REMOVED")
            RetroHaptics.performClick(context)
        }
    }

    fun updateDailyTask(task: DailyTask) {
        viewModelScope.launch {
            repository.updateDailyTask(task)
            RetroHaptics.performClick(context)
        }
    }

    fun resetDailyTasksManually() {
        viewModelScope.launch {
            repository.resetAllDailyTasksManually()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            userPreferences.setDailyTaskLastResetDate(todayStr)
            FocusTimerManager.setSystemStatus("> DAILY ROUTINE RESET")
            RetroHaptics.performClick(context)
        }
    }

    val theme: StateFlow<RetroThemeId> = userPreferences.theme

    fun setTheme(themeId: RetroThemeId) {
        userPreferences.setTheme(themeId)
        RetroHaptics.performClick(context)
    }

    val glassIntensity: StateFlow<Float> = userPreferences.glassIntensity
    val glassBlurEnabled: StateFlow<Boolean> = userPreferences.glassBlurEnabled
    val cornerRadiusDp: StateFlow<Int> = userPreferences.cornerRadiusDp

    fun setGlassIntensity(intensity: Float) {
        userPreferences.setGlassIntensity(intensity)
    }

    fun setGlassBlurEnabled(enabled: Boolean) {
        userPreferences.setGlassBlurEnabled(enabled)
    }

    fun setCornerRadiusDp(radius: Int) {
        userPreferences.setCornerRadiusDp(radius)
        RetroHaptics.performClick(context)
    }

    // Sanctuary Habit Matrix & Line Graph Data Tracker
    private val currentMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val _selectedHabitMonth = MutableStateFlow(currentMonthFormat.format(Date()))
    val selectedHabitMonth: StateFlow<String> = _selectedHabitMonth.asStateFlow()

    val activeHabits: StateFlow<List<Habit>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val habitLogsForMonth: StateFlow<List<HabitLog>> = _selectedHabitMonth
        .flatMapLatest { month -> repository.getHabitLogsForMonth(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMetricKey = MutableStateFlow(DefaultSanctuaryMetric.FOCUS_MINUTES.key)
    val selectedMetricKey: StateFlow<String> = _selectedMetricKey.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val metricEntriesForMonth: StateFlow<List<MetricEntry>> = _selectedHabitMonth
        .flatMapLatest { month -> repository.getMetricEntriesForMonth(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectHabitMonth(yearMonth: String) {
        _selectedHabitMonth.value = yearMonth
    }

    fun previousHabitMonth() {
        val cal = Calendar.getInstance()
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(_selectedHabitMonth.value)
            if (date != null) cal.time = date
        } catch (_: Exception) {}
        cal.add(Calendar.MONTH, -1)
        _selectedHabitMonth.value = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    fun nextHabitMonth() {
        val cal = Calendar.getInstance()
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(_selectedHabitMonth.value)
            if (date != null) cal.time = date
        } catch (_: Exception) {}
        cal.add(Calendar.MONTH, 1)
        _selectedHabitMonth.value = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    fun resetToCurrentMonth() {
        _selectedHabitMonth.value = currentMonthFormat.format(Date())
    }

    fun toggleHabit(habitId: Long, date: String) {
        viewModelScope.launch {
            val completed = repository.toggleHabitCompletion(habitId, date)
            if (completed) {
                RetroHaptics.performClick(context)
                FocusTimerManager.setSystemStatus("> HABIT CHECKED: $date")
            } else {
                RetroHaptics.performClick(context)
                FocusTimerManager.setSystemStatus("> HABIT UNCHECKED: $date")
            }
        }
    }

    fun addHabit(title: String, category: String = "FOCUS", targetDays: Int = 7) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(
                title = title.trim(),
                category = category.trim().ifBlank { "FOCUS" },
                targetDaysPerWeek = targetDays.coerceIn(1, 7)
            )
            repository.insertHabit(habit)
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> NEW HABIT: ${title.trim().take(18)}")
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> HABIT REMOVED")
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            RetroHaptics.performClick(context)
        }
    }

    fun selectMetricKey(key: String) {
        _selectedMetricKey.value = key
    }

    fun logMetricEntry(metricKey: String, date: String, value: Float, label: String, unit: String) {
        viewModelScope.launch {
            val entry = MetricEntry(
                metricKey = metricKey,
                date = date,
                metricLabel = label,
                value = value,
                unit = unit
            )
            repository.insertOrUpdateMetricEntry(entry)
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> LOGGED $label: $value $unit")
        }
    }

    fun deleteMetricEntry(metricKey: String, date: String) {
        viewModelScope.launch {
            repository.deleteMetricEntry(metricKey, date)
            RetroHaptics.performClick(context)
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        return DistractionBlockerManager.hasUsageStatsPermission(context)
    }

    fun hasOverlayPermission(): Boolean {
        return DistractionBlockerManager.hasOverlayPermission(context)
    }

    fun openUsageAccessSettings() {
        DistractionBlockerManager.openUsageAccessSettings(context)
    }

    fun openOverlaySettings() {
        DistractionBlockerManager.openOverlaySettings(context)
    }

    fun toggleBlockerEnabled() {
        val next = !blockerEnabled.value
        userPreferences.setBlockerEnabled(next)
        FocusTimerManager.setSystemStatus(if (next) "> DISTRACTION SHIELD ENGAGED" else "> DISTRACTION SHIELD OFF")
    }

    fun setBlockerStrictMode(strict: Boolean) {
        userPreferences.setBlockerStrictMode(strict)
    }

    fun toggleBlockedPackage(packageName: String) {
        userPreferences.toggleBlockedPackage(packageName)
    }

    fun setBlockedPackages(packages: Set<String>) {
        userPreferences.setBlockedPackages(packages)
    }

    fun blockAllApps() {
        val allPkgs = getBlockableApps().map { it.packageName }.toSet()
        userPreferences.setBlockedPackages(allPkgs)
        FocusTimerManager.setSystemStatus("> ALL APPS BLOCKED")
    }

    fun unblockAllApps() {
        userPreferences.setBlockedPackages(emptySet())
        FocusTimerManager.setSystemStatus("> SHIELD CLEARED")
    }

    fun blockCategory(category: com.example.core.blocker.AppCategory) {
        val categoryPkgs = getBlockableApps()
            .filter { category == com.example.core.blocker.AppCategory.ALL || it.category == category }
            .map { it.packageName }
            .toSet()
        val merged = userPreferences.blockedPackages.value + categoryPkgs
        userPreferences.setBlockedPackages(merged)
        FocusTimerManager.setSystemStatus("> SHIELDED ${category.displayName.uppercase()}")
    }

    fun unblockCategory(category: com.example.core.blocker.AppCategory) {
        val categoryPkgs = getBlockableApps()
            .filter { category == com.example.core.blocker.AppCategory.ALL || it.category == category }
            .map { it.packageName }
            .toSet()
        val filtered = userPreferences.blockedPackages.value - categoryPkgs
        userPreferences.setBlockedPackages(filtered)
        FocusTimerManager.setSystemStatus("> ALLOWED ${category.displayName.uppercase()}")
    }

    fun applyShieldProfile(profileName: String) {
        val all = getBlockableApps()
        val packagesToBlock = when (profileName) {
            "DEEP_WORK" -> all.filter {
                it.category == com.example.core.blocker.AppCategory.SOCIAL ||
                it.category == com.example.core.blocker.AppCategory.VIDEO ||
                it.category == com.example.core.blocker.AppCategory.GAMES ||
                it.category == com.example.core.blocker.AppCategory.BROWSERS
            }.map { it.packageName }.toSet()
            "STUDY" -> all.filter {
                it.category == com.example.core.blocker.AppCategory.SOCIAL ||
                it.category == com.example.core.blocker.AppCategory.VIDEO ||
                it.category == com.example.core.blocker.AppCategory.GAMES
            }.map { it.packageName }.toSet()
            "DETOX" -> all.filter {
                it.category == com.example.core.blocker.AppCategory.SOCIAL
            }.map { it.packageName }.toSet()
            else -> all.map { it.packageName }.toSet()
        }
        userPreferences.setBlockedPackages(packagesToBlock)
        FocusTimerManager.setSystemStatus("> PROFILE: $profileName ACTIVE")
    }

    fun resetShieldToDefaults() {
        userPreferences.setBlockedPackages(UserPreferences.DEFAULT_BLOCKED_PACKAGES)
        FocusTimerManager.setSystemStatus("> SHIELD DEFAULTS RESTORED")
    }

    fun dismissInterception() {
        DistractionBlockerManager.dismissInterception()
    }

    fun simulateDistractionInterception(packageName: String, appName: String) {
        DistractionBlockerManager.simulateInterception(packageName, appName, userPreferences)
    }

    fun getBlockableApps(): List<BlockableApp> {
        return DistractionBlockerManager.getInstalledAndPresetApps(context, blockedPackages.value)
    }

    private val _isZenModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isZenModeActive.asStateFlow()

    // Statistics flows
    val dailyGoalMinutes: StateFlow<Int> = userPreferences.dailyGoalMinutes

    val todayFocusSeconds: StateFlow<Long> = repository.getTodayFocusSeconds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todaySessionCount: StateFlow<Int> = repository.getTodaySessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val streakDays: StateFlow<Int> = repository.getStreakDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklyHistory: StateFlow<List<DayFocusSummary>> = repository.getWeeklyHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history14Days: StateFlow<List<DayFocusSummary>> = repository.get14DayHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSessions: StateFlow<List<FocusSession>> = repository.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<FocusSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCompletedSessions: StateFlow<Int> = repository.totalCompletedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalFocusSeconds: StateFlow<Long> = repository.totalFocusSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Selected date for deep dive in Daily Stats
    private val _selectedStatsDate = MutableStateFlow(repository.getTodayDateString())
    val selectedStatsDate: StateFlow<String> = _selectedStatsDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateStats: StateFlow<DetailedDailyStats?> = combine(
        _selectedStatsDate,
        dailyGoalMinutes
    ) { date: String, goal: Int -> Pair(date, goal) }
        .flatMapLatest { pair ->
            repository.getDetailedDailyStats(pair.first, pair.second)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectStatsDate(dateString: String) {
        _selectedStatsDate.value = dateString
    }

    fun setDailyGoalMinutes(minutes: Int) {
        userPreferences.setDailyGoalMinutes(minutes)
        FocusTimerManager.setSystemStatus("> DAILY GOAL: ${minutes}M")
    }

    fun navigateStatsDay(deltaDays: Int) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val current = sdf.parse(_selectedStatsDate.value) ?: java.util.Date()
            val cal = java.util.Calendar.getInstance()
            cal.time = current
            cal.add(java.util.Calendar.DAY_OF_YEAR, deltaDays)
            val today = repository.getTodayDateString()
            val newDateStr = sdf.format(cal.time)
            // Don't navigate into future
            if (newDateStr <= today) {
                _selectedStatsDate.value = newDateStr
            }
        } catch (_: Exception) {}
    }

    fun selectTodayStats() {
        _selectedStatsDate.value = repository.getTodayDateString()
    }

    fun selectDuration(minutes: Int) {
        FocusTimerManager.selectDuration(minutes)
    }

    fun startTimer() {
        FocusTimerManager.startTimer()
    }

    fun pauseTimer() {
        FocusTimerManager.pauseTimer()
    }

    fun toggleTimer() {
        if (state.value == FocusState.RUNNING) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun resetTimer() {
        FocusTimerManager.resetTimer()
    }

    fun addFiveMinutes() {
        FocusTimerManager.addFiveMinutes()
    }

    fun toggleDeepFocusMode() {
        FocusTimerManager.toggleDeepFocusMode()
    }

    fun setDeepFocusMode(active: Boolean) {
        FocusTimerManager.setDeepFocusMode(active)
    }

    fun toggleFocusMode() {
        val next = !_isZenModeActive.value
        _isZenModeActive.value = next
        if (userPreferences.vibrationEnabled.value) {
            RetroHaptics.performClick(context)
        }
    }

    fun exitFocusMode() {
        _isZenModeActive.value = false
    }

    val monochromeModeLevel: StateFlow<MonochromeModeLevel> = userPreferences.monochromeModeLevel

    fun setMonochromeModeLevel(level: MonochromeModeLevel) {
        userPreferences.setMonochromeModeLevel(level)
        RetroHaptics.performClick(context)
        FocusTimerManager.setSystemStatus("> MONOCHROME: ${level.name}")
    }

    fun toggleMonochromeMode() {
        val current = userPreferences.monochromeModeLevel.value
        val next = when (current) {
            MonochromeModeLevel.OFF -> MonochromeModeLevel.SUBTLE
            MonochromeModeLevel.SUBTLE -> MonochromeModeLevel.FULL
            MonochromeModeLevel.FULL -> MonochromeModeLevel.OFF
        }
        setMonochromeModeLevel(next)
    }

    fun toggleCrt() {
        val current = userPreferences.crtEffect.value
        userPreferences.setCrtEffect(!current)
        FocusTimerManager.setSystemStatus(if (!current) "> CRT SCANLINES ACTIVE" else "> CRT SCANLINES OFF")
    }

    fun unlockVault() {
        userPreferences.setVaultUnlocked(true)
        FocusTimerManager.setSystemStatus("> VAULT UNLOCKED")
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            FocusTimerManager.setSystemStatus("> DATABASE CLEARED")
        }
    }

    // Session Quality Rating flows & methods
    val pendingRatingSession: StateFlow<FocusSession?> = FocusTimerManager.pendingRatingSession
    val ratedSessions: StateFlow<List<FocusSession>> = repository.ratedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sessionQualityInsights: StateFlow<SessionQualityInsights> = repository.getSessionQualityInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionQualityInsights())

    fun submitSessionRating(sessionId: Long, rating: Int, note: String) {
        FocusTimerManager.submitSessionRating(sessionId, rating, note.trim())
        RetroHaptics.performCompletionBuzz(context)
    }

    fun dismissSessionRating() {
        FocusTimerManager.clearPendingRating()
        RetroHaptics.performClick(context)
    }

    val appScale: StateFlow<Float> = userPreferences.appScale

    fun setAppScale(scale: Float) {
        userPreferences.setAppScale(scale)
        FocusTimerManager.setSystemStatus(String.format(Locale.getDefault(), "> APP SCALE: %.2fX", scale))
        RetroHaptics.performClick(context)
    }

    // Floating / Notch Timer Controls
    val floatingNotchTimerEnabled: StateFlow<Boolean> = userPreferences.floatingNotchTimerEnabled

    // Navigation & Dock Controls
    val navigationStyle: StateFlow<NavigationStyle> = userPreferences.navigationStyle

    fun setNavigationStyle(style: NavigationStyle) {
        userPreferences.setNavigationStyle(style)
        FocusTimerManager.setSystemStatus("> NAV STYLE: ${style.label}")
        RetroHaptics.performClick(context)
    }

    val edgeWheelHapticsEnabled: StateFlow<Boolean> = userPreferences.edgeWheelHapticsEnabled
    val edgeWheelSide: StateFlow<String> = userPreferences.edgeWheelSide
    val edgeWheelSensitivity: StateFlow<Float> = userPreferences.edgeWheelSensitivity
    val edgeWheelPosition: StateFlow<String> = userPreferences.edgeWheelPosition

    val dynamicIslandOffsetX: StateFlow<Int> = userPreferences.dynamicIslandOffsetX
    val dynamicIslandOffsetY: StateFlow<Int> = userPreferences.dynamicIslandOffsetY
    val dynamicIslandWidthScale: StateFlow<Float> = userPreferences.dynamicIslandWidthScale
    val dynamicIslandHeightScale: StateFlow<Float> = userPreferences.dynamicIslandHeightScale

    fun setEdgeWheelSide(side: String) {
        userPreferences.setEdgeWheelSide(side)
        RetroHaptics.performTick(context)
    }

    fun setEdgeWheelPosition(position: String) {
        userPreferences.setEdgeWheelPosition(position)
        RetroHaptics.performTick(context)
    }

    fun setEdgeWheelSensitivity(sensitivity: Float) {
        userPreferences.setEdgeWheelSensitivity(sensitivity)
    }

    fun setDynamicIslandCalibration(offsetX: Int, offsetY: Int, widthScale: Float, heightScale: Float) {
        userPreferences.setDynamicIslandCalibration(offsetX, offsetY, widthScale, heightScale)
    }

    fun resetDynamicIslandCalibration() {
        userPreferences.resetDynamicIslandCalibration()
        RetroHaptics.performTick(context)
    }

    fun setEdgeWheelHapticsEnabled(enabled: Boolean) {
        userPreferences.setEdgeWheelHapticsEnabled(enabled)
        if (enabled) {
            RetroHaptics.performTick(context)
        }
    }

    fun setFloatingNotchTimerEnabled(enabled: Boolean) {
        userPreferences.setFloatingNotchTimerEnabled(enabled)
        FocusTimerManager.setSystemStatus(if (enabled) "> FLOATING NOTCH TIMER ON" else "> FLOATING NOTCH TIMER OFF")
        RetroHaptics.performClick(context)
    }

    fun toggleFloatingNotchTimer() {
        val next = !floatingNotchTimerEnabled.value
        setFloatingNotchTimerEnabled(next)
    }

    // Web Shield flows & methods
    val webShieldEnabled: StateFlow<Boolean> = userPreferences.webShieldEnabled
    val webShieldBlockedDomains: StateFlow<Set<String>> = userPreferences.webShieldBlockedDomains

    fun toggleWebShieldEnabled() {
        val next = !webShieldEnabled.value
        userPreferences.setWebShieldEnabled(next)
        FocusTimerManager.setSystemStatus(if (next) "> WEB SHIELD ENGAGED" else "> WEB SHIELD OFF")
        RetroHaptics.performClick(context)
    }

    fun toggleBlockedWebDomain(domain: String) {
        userPreferences.toggleBlockedWebDomain(domain)
        RetroHaptics.performClick(context)
    }

    fun addBlockedWebDomain(domain: String) {
        userPreferences.addBlockedWebDomain(domain)
        FocusTimerManager.setSystemStatus("> DOMAIN SHIELDED")
        RetroHaptics.performClick(context)
    }

    fun removeBlockedWebDomain(domain: String) {
        userPreferences.removeBlockedWebDomain(domain)
        FocusTimerManager.setSystemStatus("> DOMAIN ALLOWED")
        RetroHaptics.performClick(context)
    }

    fun resetWebShieldDefaults() {
        userPreferences.setBlockedWebDomains(UserPreferences.DEFAULT_BLOCKED_DOMAINS)
        FocusTimerManager.setSystemStatus("> WEB SHIELD DEFAULTS RESTORED")
        RetroHaptics.performClick(context)
    }

    // Flashcards flows & methods
    val allDecks: StateFlow<List<FlashcardDeck>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCardsForDeck(deckId: Long): kotlinx.coroutines.flow.Flow<List<Flashcard>> {
        return repository.getCardsForDeck(deckId)
    }

    fun createDeck(title: String, description: String = "", category: String = "STUDY") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val deck = FlashcardDeck(
                title = title.trim(),
                description = description.trim(),
                category = category.trim().ifBlank { "STUDY" }
            )
            repository.insertDeck(deck)
            FocusTimerManager.setSystemStatus("> DECK CREATED: ${title.trim().take(18)}")
            RetroHaptics.performClick(context)
        }
    }

    fun updateDeck(deck: FlashcardDeck) {
        viewModelScope.launch {
            repository.updateDeck(deck)
            RetroHaptics.performClick(context)
        }
    }

    fun deleteDeck(deck: FlashcardDeck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
            FocusTimerManager.setSystemStatus("> DECK DELETED")
            RetroHaptics.performClick(context)
        }
    }

    fun addCard(deckId: Long, front: String, back: String, hint: String = "") {
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            val card = Flashcard(
                deckId = deckId,
                front = front.trim(),
                back = back.trim(),
                hint = hint.trim()
            )
            repository.insertCard(card)
            FocusTimerManager.setSystemStatus("> CARD ADDED")
            RetroHaptics.performClick(context)
        }
    }

    fun updateCard(card: Flashcard) {
        viewModelScope.launch {
            repository.updateCard(card)
            RetroHaptics.performClick(context)
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteCard(card)
            RetroHaptics.performClick(context)
        }
    }

    fun markCardReviewed(cardId: Long, isKnown: Boolean) {
        viewModelScope.launch {
            repository.updateCardReviewStatus(cardId, isKnown)
            RetroHaptics.performClick(context)
        }
    }

    fun resetDeckProgress(deckId: Long) {
        viewModelScope.launch {
            repository.resetDeckProgress(deckId)
            FocusTimerManager.setSystemStatus("> DECK PROGRESS RESET")
            RetroHaptics.performClick(context)
        }
    }

    // Minimalist Notes in Room Database (Focus Scratchpad)
    val minimalistNotes: StateFlow<List<MinimalistNote>> = noteRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMinimalistNote: StateFlow<MinimalistNote?> = noteRepository.latestNote
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val minimalistNotesCount: StateFlow<Int> = noteRepository.notesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun saveMinimalistNote(content: String, tag: String = "FOCUS") {
        if (content.isBlank()) return
        viewModelScope.launch {
            val targetMins = targetMinutes.value
            val currentSessionTimestamp = System.currentTimeMillis()
            noteRepository.insertNote(
                content = content.trim(),
                sessionId = currentSessionTimestamp,
                sessionDurationMinutes = targetMins,
                tag = tag
            )
            // Also sync to legacy scratchpad for compatibility
            userPreferences.setQuickNotes(content.trim())
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> MINIMALIST NOTE SAVED")
            com.example.widget.RetroQuickNoteWidget.updateAllWidgets(context)
        }
    }

    fun deleteMinimalistNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
            RetroHaptics.performTick(context)
            com.example.widget.RetroQuickNoteWidget.updateAllWidgets(context)
        }
    }

    fun togglePinMinimalistNote(note: MinimalistNote) {
        viewModelScope.launch {
            noteRepository.togglePin(note)
            RetroHaptics.performClick(context)
        }
    }

    fun clearAllMinimalistNotes() {
        viewModelScope.launch {
            noteRepository.clearAllNotes()
            userPreferences.setQuickNotes("")
            RetroHaptics.performTick(context)
            com.example.widget.RetroQuickNoteWidget.updateAllWidgets(context)
        }
    }

    // Quick notes / Scratchpad in MONO section
    val quickNotes: StateFlow<String> = userPreferences.quickNotes

    fun setQuickNotes(notes: String) {
        userPreferences.setQuickNotes(notes)
        if (notes.isNotBlank()) {
            saveMinimalistNote(notes, tag = "SCRATCHPAD")
        }
    }

    // Session Export Operations
    fun getExportDataCsv(): String {
        return repository.exportSessionsToCsv(allSessions.value)
    }

    fun getExportDataJson(): String {
        return repository.exportSessionsToJson(allSessions.value)
    }

    fun getExportDataMarkdown(): String {
        return repository.exportSessionsToMarkdown(
            sessions = allSessions.value,
            totalSeconds = totalFocusSeconds.value,
            streak = streakDays.value
        )
    }

    fun shareExport(context: android.content.Context, format: String) {
        val (text, mimeType, title) = when (format.uppercase()) {
            "CSV" -> Triple(getExportDataCsv(), "text/csv", "Demichrome_Focus_Sessions.csv")
            "JSON" -> Triple(getExportDataJson(), "application/json", "Demichrome_Focus_Sessions.json")
            else -> Triple(getExportDataMarkdown(), "text/plain", "Demichrome_Focus_Report.txt")
        }

        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_TITLE, title)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Demichrome Focus Export ($format)")
            type = mimeType
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Export Demichrome Data ($format)")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }

    /**
     * Executes interactive terminal command easter egg
     */
    fun executeCommand(command: String): String {
        val clean = command.trim().lowercase()
        return when {
            clean == "help" -> "COMMANDS: tasks, vault, matrix, crt, mono, zen, beep, stats, reset, clear, about"
            clean == "tasks" || clean == "todo" -> {
                "ACTIVE TASKS: ${activeTasks.value.size} | COMPLETED: ${completedTasks.value.size}"
            }
            clean == "vault" || clean == "vault --open" -> {
                unlockVault()
                "VAULT UNLOCKED. ACCESS GRANTED."
            }
            clean == "matrix" -> {
                userPreferences.setTheme(RetroThemeId.TERMINAL_MATRIX)
                userPreferences.setTimerStyle(TimerStyleId.TERMINAL)
                "MATRIX TERMINAL THEME APPLIED."
            }
            clean == "crt" || clean == "crt on" -> {
                userPreferences.setCrtEffect(true)
                userPreferences.setTheme(RetroThemeId.CRT_GREEN)
                "CRT PHOSPHOR ACTIVE."
            }
            clean == "mono" || clean == "pure" -> {
                userPreferences.setTheme(RetroThemeId.PURE_MONOCHROME)
                "PURE MONOCHROME OLED ACTIVE."
            }
            clean == "zen" -> {
                toggleFocusMode()
                "ZEN FOCUS MODE TOGGLED."
            }
            clean == "beep" -> {
                viewModelScope.launch { RetroAudioSynthesizer.playStartChirp() }
                "SYNTHESIZER CHIRP TEST OK."
            }
            clean == "stats" -> {
                "TODAY: ${todayFocusSeconds.value / 60}m | SESSIONS: ${todaySessionCount.value} | STREAK: ${streakDays.value}d"
            }
            clean.startsWith("focus ") -> {
                val mins = clean.removePrefix("focus ").trim().toIntOrNull()
                if (mins != null && mins in 1..180) {
                    selectDuration(mins)
                    "TIMER SET TO $mins MINUTES."
                } else {
                    "INVALID DURATION. USE: focus <1-180>"
                }
            }
            clean == "clear" -> {
                "TERMINAL BUFFER CLEARED."
            }
            clean == "about" -> {
                "DEMICHROME v1.0 — A quiet retro digital workspace that makes your phone less distracting."
            }
            else -> "COMMAND NOT RECOGNIZED. TYPE 'help' FOR COMMANDS."
        }
    }

    // ==========================================
    // DEMICHROME MONEY / EXPENSE TRACKER
    // ==========================================
    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBudget: StateFlow<Double> = userPreferences.monthlyBudget

    val todayExpenses: StateFlow<List<Expense>> = allExpenses.map { list ->
        val cal = java.util.Calendar.getInstance()
        val todayYear = cal.get(java.util.Calendar.YEAR)
        val todayDay = cal.get(java.util.Calendar.DAY_OF_YEAR)
        list.filter {
            val itemCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            itemCal.get(java.util.Calendar.YEAR) == todayYear && itemCal.get(java.util.Calendar.DAY_OF_YEAR) == todayDay
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySpent: StateFlow<Double> = todayExpenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthExpenses: StateFlow<List<Expense>> = allExpenses.map { list ->
        val cal = java.util.Calendar.getInstance()
        val currentYear = cal.get(java.util.Calendar.YEAR)
        val currentMonth = cal.get(java.util.Calendar.MONTH)
        list.filter {
            val itemCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            itemCal.get(java.util.Calendar.YEAR) == currentYear && itemCal.get(java.util.Calendar.MONTH) == currentMonth
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthSpent: StateFlow<Double> = currentMonthExpenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addExpense(
        amount: Double,
        category: String,
        note: String = "",
        paymentMode: String = "UPI",
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category.trim().uppercase(),
                    note = note.trim(),
                    paymentMode = paymentMode.trim().uppercase(),
                    timestamp = timestamp
                )
            )
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> EXPENSE LOGGED: ₹${amount.toInt()} [${category.uppercase()}]")
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> EXPENSE UPDATED: ₹${expense.amount.toInt()}")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            RetroHaptics.performClick(context)
            FocusTimerManager.setSystemStatus("> EXPENSE REMOVED")
        }
    }

    fun setMonthlyBudget(budget: Double) {
        userPreferences.setMonthlyBudget(budget)
        RetroHaptics.performClick(context)
        FocusTimerManager.setSystemStatus("> BUDGET SET: ₹${budget.toInt()}")
    }

    // ==========================================
    // 07 PRODUCTIVITY INTEGRATION OVERVIEW
    // ==========================================
    val productivitySummary: StateFlow<ProductivityOverview> = combine(
        todayFocusSeconds,
        habitLogsForMonth,
        activeHabits,
        todaySpent
    ) { focusSecs, habitLogs, habits, spent ->
        val totalSecs = focusSecs
        val hours = (totalSecs / 3600).toInt()
        val mins = ((totalSecs % 3600) / 60).toInt()
        val focusFormatted = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        val todayDateStr = repository.getTodayDateString()
        val completedHabitsCount = habitLogs.count { it.date == todayDateStr && it.isCompleted }
        val totalHabitsCount = habits.size

        ProductivityOverview(
            focusHoursFormatted = focusFormatted,
            habitsProgress = "$completedHabitsCount/$totalHabitsCount",
            spentToday = spent,
            spentTodayFormatted = "₹${spent.toInt()}"
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProductivityOverview("0m", "0/0", 0.0, "₹0")
    )
}

data class ProductivityOverview(
    val focusHoursFormatted: String,
    val habitsProgress: String,
    val spentToday: Double,
    val spentTodayFormatted: String
)
