package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.data.TaskPriority
import com.example.data.TodoTask
import com.example.data.sync.SyncStatus
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.navigation.RetroScreen
import com.example.ui.theme.RetroPalette
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import com.example.ui.timer.SmoothProgressBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: FocusViewModel,
    onNavigate: (RetroScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus state
    val timerState by viewModel.state.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val todayFocusSeconds by viewModel.todayFocusSeconds.collectAsState()
    val dailyGoalMinutes by viewModel.dailyGoalMinutes.collectAsState()
    val todaySessionCount by viewModel.todaySessionCount.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val selectedTargetTask by viewModel.selectedTargetTask.collectAsState()

    // Tasks & Habits & Sync
    val allTasks by viewModel.allTasks.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val habits by viewModel.activeHabits.collectAsState()
    val habitLogs by viewModel.habitLogsForMonth.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val todaySpent by viewModel.todaySpent.collectAsState()

    var quickTaskTitle by remember { mutableStateOf("") }

    // Live Clock State
    var currentTimeFormatted by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()).uppercase())
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()).uppercase()
            delay(1000L)
        }
    }

    val todayDayDateFormatted = remember {
        SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date()).uppercase()
    }
    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val todayFocusMins = (todayFocusSeconds / 60).toInt()
    val progressFraction = if (dailyGoalMinutes > 0) {
        (todayFocusMins.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val habitsCompletedToday = habits.count { habit ->
        habitLogs.any { it.habitId == habit.id && it.date == todayDateString && it.isCompleted }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. TOP IDENTITY & CONTEXT HEADER
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "demichrome",
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp,
                                fontFamily = palette.fontFamily
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent)
                            )
                        }
                        Text(
                            text = "Make your phone into a tool.",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = palette.fontFamily,
                            letterSpacing = 0.2.sp
                        )
                    }

                    // User Account & Sync Status Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(palette.surfaceElevated)
                            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(percent = 50))
                            .clickable {
                                RetroHaptics.performLight(context)
                                onNavigate(RetroScreen.SETTINGS)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("home_account_avatar_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val syncIcon = when (syncStatus) {
                            SyncStatus.SYNCED -> Icons.Default.CloudDone
                            SyncStatus.OFFLINE -> Icons.Default.CloudOff
                            else -> Icons.Default.CloudDone
                        }
                        Icon(
                            imageVector = syncIcon,
                            contentDescription = "Sync Status",
                            tint = if (syncStatus == SyncStatus.SYNCED) palette.accent else palette.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )

                        Text(
                            text = if (currentUser.isAnonymous) "Guest" else currentUser.displayName.take(8),
                            color = palette.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = palette.fontFamily
                        )

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(palette.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = palette.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. SCENIC DATE, DIGITAL CLOCK & WEATHER CARD
            // ==========================================
            item {
                HomeScenicClockBanner(
                    dateString = todayDayDateFormatted,
                    clockString = currentTimeFormatted,
                    palette = palette
                )
            }

            // ==========================================
            // 3. RECOMMENDED ACTION CARD
            // ==========================================
            item {
                HomeRecommendedActionCard(
                    timerState = timerState,
                    remainingSeconds = remainingSeconds,
                    selectedTargetTask = selectedTargetTask,
                    activeTasks = activeTasks,
                    palette = palette,
                    onStartFocus = {
                        RetroHaptics.performClick(context)
                        viewModel.startTimer()
                        onNavigate(RetroScreen.FOCUS)
                    },
                    onToggleTimer = {
                        RetroHaptics.performClick(context)
                        viewModel.toggleTimer()
                    },
                    onOpenFocus = {
                        RetroHaptics.performClick(context)
                        onNavigate(RetroScreen.FOCUS)
                    }
                )
            }

            // ==========================================
            // 4. TODAY'S PROGRESS (4 Key Metric Columns: Focus, Habits, Tasks, Money)
            // ==========================================
            item {
                HomeTodayProgressCard(
                    todayFocusMins = todayFocusMins.toInt(),
                    dailyGoalMinutes = dailyGoalMinutes,
                    habitsCompleted = habitsCompletedToday,
                    totalHabits = habits.size,
                    tasksCompleted = completedTasks.size,
                    totalTasks = allTasks.size,
                    todaySpent = todaySpent,
                    streakDays = streakDays,
                    palette = palette,
                    onNavigateMoney = { onNavigate(RetroScreen.MONEY) }
                )
            }

            // ==========================================
            // 5. MOTIVATIONAL NOTIFICATION / QUOTE CARD
            // ==========================================
            item {
                HomeQuoteCard(palette = palette)
            }

            // ==========================================
            // 6. OBJECTIVES DUE TODAY (Priority Tasks)
            // ==========================================
            item {
                HomeTasksSection(
                    activeTasks = activeTasks,
                    palette = palette,
                    quickTaskTitle = quickTaskTitle,
                    onQuickTaskTitleChange = { quickTaskTitle = it },
                    onAddTask = {
                        if (quickTaskTitle.isNotBlank()) {
                            RetroHaptics.performClick(context)
                            viewModel.addTask(
                                title = quickTaskTitle.trim(),
                                priority = TaskPriority.MEDIUM,
                                estimatedSessions = 1,
                                category = "FOCUS"
                            )
                            quickTaskTitle = ""
                            keyboardController?.hide()
                        }
                    },
                    onToggleComplete = { task ->
                        RetroHaptics.performClick(context)
                        viewModel.toggleTaskCompleted(task)
                    },
                    onSelectTaskForFocus = { task ->
                        RetroHaptics.performClick(context)
                        viewModel.selectTaskForFocus(task.id)
                        onNavigate(RetroScreen.FOCUS)
                    },
                    onViewAll = {
                        RetroHaptics.performClick(context)
                        onNavigate(RetroScreen.TASKS)
                    }
                )
            }

            // ==========================================
            // 7. TODAY'S HABITS
            // ==========================================
            item {
                HomeHabitsSection(
                    habits = habits,
                    habitLogs = habitLogs,
                    todayDateString = todayDateString,
                    habitsCompletedToday = habitsCompletedToday,
                    palette = palette,
                    onToggleHabit = { habitId ->
                        RetroHaptics.performClick(context)
                        viewModel.toggleHabit(habitId, todayDateString)
                    },
                    onViewAll = {
                        RetroHaptics.performClick(context)
                        onNavigate(RetroScreen.HABITS)
                    }
                )
            }

            // ==========================================
            // 8. QUICK ACTIONS GRID
            // ==========================================
            item {
                HomeQuickActionsGrid(
                    palette = palette,
                    onNavigate = { screen ->
                        RetroHaptics.performClick(context)
                        onNavigate(screen)
                    }
                )
            }

            // ==========================================
            // 9. FOCUS PLAYLIST / AMBIENT AUDIO MINI-PLAYER
            // ==========================================
            item {
                HomeFocusPlaylistCard(
                    viewModel = viewModel,
                    palette = palette
                )
            }
        }
    }
}

/**
 * Scenic date, big digital clock, and minimal line-art landscape card.
 */
@Composable
private fun HomeScenicClockBanner(
    dateString: String,
    clockString: String,
    palette: RetroPalette,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.surfaceElevated,
                        palette.surface
                    )
                )
            )
            .border(BorderStroke(1.2.dp, palette.border), cardShape)
            .padding(18.dp)
            .testTag("home_scenic_clock_banner")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Date & Weather Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Clear Weather",
                        tint = palette.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "26°C • Clear",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Big Bold Digital Clock
            Text(
                text = clockString,
                color = palette.textPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("home_clock_text")
            )

            // Minimalist Vector Scenery Art (Mount Fuji / Sun horizon)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surface.copy(alpha = 0.5f))
            ) {
                val w = size.width
                val h = size.height
                val lineColor = palette.border
                val accentColor = palette.accent.copy(alpha = 0.5f)
                val sunColor = palette.accent.copy(alpha = 0.25f)

                // Sun
                drawCircle(
                    color = sunColor,
                    radius = h * 0.45f,
                    center = Offset(w * 0.72f, h * 0.5f)
                )

                // Mountain range outline
                val mountainPath = Path().apply {
                    moveTo(0f, h * 0.9f)
                    lineTo(w * 0.25f, h * 0.55f)
                    lineTo(w * 0.42f, h * 0.78f)
                    lineTo(w * 0.65f, h * 0.25f) // Peak
                    lineTo(w * 0.88f, h * 0.88f)
                    lineTo(w, h * 0.75f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = mountainPath,
                    color = palette.accentSoft.copy(alpha = 0.35f)
                )

                drawPath(
                    path = mountainPath,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Horizon ground line
                drawLine(
                    color = accentColor,
                    start = Offset(0f, h - 2.dp.toPx()),
                    end = Offset(w, h - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

/**
 * Recommended Action Card (e.g. "Start a 25-minute Focus Sprint").
 */
@Composable
private fun HomeRecommendedActionCard(
    timerState: FocusState,
    remainingSeconds: Int,
    selectedTargetTask: TodoTask?,
    activeTasks: List<TodoTask>,
    palette: RetroPalette,
    onStartFocus: () -> Unit,
    onToggleTimer: () -> Unit,
    onOpenFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(18.dp)
    val isTimerActive = timerState == FocusState.RUNNING || timerState == FocusState.PAUSED
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(cardShape)
            .background(palette.surfaceElevated)
            .border(BorderStroke(1.2.dp, palette.border), cardShape)
            .padding(18.dp)
            .testTag("home_hero_action_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTimerActive) "SESSION IN PROGRESS" else "RECOMMENDED ACTION",
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (isTimerActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.accent)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (timerState == FocusState.RUNNING) "RUNNING" else "PAUSED",
                            color = palette.buttonActiveText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (isTimerActive) {
                // Active Session Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeStr,
                            color = palette.textPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        val taskName = selectedTargetTask?.title ?: "Deep Work Sprint"
                        Text(
                            text = "🎯 $taskName",
                            color = palette.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(palette.accent)
                                .clickable(onClick = onToggleTimer)
                                .testTag("home_hero_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (timerState == FocusState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Timer",
                                tint = palette.buttonActiveText,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(palette.surface)
                                .border(BorderStroke(1.2.dp, palette.border), CircleShape)
                                .clickable(onClick = onOpenFocus)
                                .testTag("home_hero_open_focus"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Focus",
                                tint = palette.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Sprint Invitation
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Start a 25-minute Focus Sprint",
                        color = palette.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val nextTask = activeTasks.firstOrNull()
                    Text(
                        text = if (nextTask != null) "Target: ${nextTask.title}" else "Target: Deep uninterrupted focus",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RetroButton(
                            text = "START FOCUS",
                            onClick = onStartFocus,
                            isPrimary = true,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f),
                            testTag = "home_btn_start_focus"
                        )
                        RetroButton(
                            text = "TIMER",
                            onClick = onOpenFocus,
                            isPrimary = false,
                            icon = Icons.Default.Timer,
                            modifier = Modifier.width(108.dp),
                            testTag = "home_btn_view_timer"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Today's Progress Card with 4 metrics: Focus, Habits, Tasks, Money (as well as overall streak badge).
 */
@Composable
private fun HomeTodayProgressCard(
    todayFocusMins: Int,
    dailyGoalMinutes: Int,
    habitsCompleted: Int,
    totalHabits: Int,
    tasksCompleted: Int,
    totalTasks: Int,
    todaySpent: Double,
    streakDays: Int,
    palette: RetroPalette,
    onNavigateMoney: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    RetroCard(
        title = "TODAY'S PROGRESS",
        badge = "$streakDays DAY STREAK",
        modifier = modifier.testTag("home_progress_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Focus Column
                MetricColumn(
                    title = "FOCUS",
                    value = "${todayFocusMins}m",
                    subtitle = "/${dailyGoalMinutes}m",
                    icon = Icons.Default.Timer,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                // Habits Column
                MetricColumn(
                    title = "HABITS",
                    value = "$habitsCompleted",
                    subtitle = "/$totalHabits",
                    icon = Icons.Default.EventRepeat,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                // Tasks Column
                MetricColumn(
                    title = "TASKS",
                    value = "$tasksCompleted",
                    subtitle = "/$totalTasks",
                    icon = Icons.Default.CheckBox,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                // Money Column
                MetricColumn(
                    title = "SPENT",
                    value = "₹${todaySpent.toInt()}",
                    subtitle = "today",
                    icon = Icons.Default.Paid,
                    palette = palette,
                    onClick = onNavigateMoney,
                    modifier = Modifier.weight(1f)
                )
            }

            // Visual Progress Bar
            val fraction = if (dailyGoalMinutes > 0) {
                (todayFocusMins.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
            } else 0f
            SmoothProgressBar(
                progress = fraction,
                palette = palette,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricColumn(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: RetroPalette,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Column(
        modifier = clickableModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = palette.accent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            color = palette.textSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtitle,
                color = palette.textSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Minimalist Motivational Quote Note Card.
 */
@Composable
private fun HomeQuoteCard(
    palette: RetroPalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceElevated)
            .border(BorderStroke(1.dp, palette.border.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("home_quote_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.accent)
            )
            Column {
                Text(
                    text = "\"Small steps build great systems.\"",
                    color = palette.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Text(
                    text = "— demichrome architecture",
                    color = palette.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Objectives Due Today / Priority Tasks Section.
 */
@Composable
private fun HomeTasksSection(
    activeTasks: List<TodoTask>,
    palette: RetroPalette,
    quickTaskTitle: String,
    onQuickTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onSelectTaskForFocus: (TodoTask) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    RetroCard(
        title = "OBJECTIVES DUE TODAY",
        badge = "${activeTasks.size} REMAINING",
        modifier = modifier.testTag("home_tasks_preview_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Inline Task Adder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surface)
                    .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (quickTaskTitle.isEmpty()) {
                        Text(
                            text = "Add a new objective...",
                            color = palette.textSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = quickTaskTitle,
                        onValueChange = onQuickTaskTitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_quick_task_input"),
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(palette.accent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onAddTask() })
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (quickTaskTitle.isNotBlank()) palette.accent else palette.surfaceElevated)
                        .clickable(enabled = quickTaskTitle.isNotBlank(), onClick = onAddTask)
                        .testTag("home_quick_task_submit"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = if (quickTaskTitle.isNotBlank()) palette.buttonActiveText else palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (activeTasks.isEmpty()) {
                Text(
                    text = "All clear! No pending objectives for today.",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                activeTasks.take(3).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.surface)
                            .border(BorderStroke(1.dp, palette.border.copy(alpha = 0.7f)), RoundedCornerShape(10.dp))
                            .clickable { onSelectTaskForFocus(task) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(BorderStroke(1.2.dp, palette.border), RoundedCornerShape(6.dp))
                                    .background(if (task.isCompleted) palette.accent else palette.surfaceElevated)
                                    .clickable { onToggleComplete(task) }
                                    .testTag("home_task_check_${task.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = palette.buttonActiveText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Text(
                                text = task.title,
                                color = palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }

                        // Priority Badge
                        val prioColor = when (task.priority) {
                            TaskPriority.URGENT, TaskPriority.HIGH -> palette.danger
                            TaskPriority.MEDIUM -> palette.accent
                            TaskPriority.LOW -> palette.textSecondary
                        }
                        Text(
                            text = task.priority.name,
                            color = prioColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // View all button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIEW ALL OBJECTIVES →",
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable(onClick = onViewAll)
                        .testTag("home_view_all_tasks")
                )

                Text(
                    text = "+ TAP TO FOCUS",
                    color = palette.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Today's Habits Section with interactive completion and streaks.
 */
@Composable
private fun HomeHabitsSection(
    habits: List<com.example.data.Habit>,
    habitLogs: List<com.example.data.HabitLog>,
    todayDateString: String,
    habitsCompletedToday: Int,
    palette: RetroPalette,
    onToggleHabit: (Long) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    RetroCard(
        title = "TODAY'S HABITS",
        badge = "$habitsCompletedToday / ${habits.size} DONE",
        modifier = modifier.testTag("home_habits_preview_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (habits.isEmpty()) {
                Text(
                    text = "No habits defined yet. Build your daily routines in Habits.",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                habits.take(3).forEach { habit ->
                    val isDone = habitLogs.any {
                        it.habitId == habit.id && it.date == todayDateString && it.isCompleted
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.surface)
                            .border(BorderStroke(1.dp, palette.border.copy(alpha = 0.7f)), RoundedCornerShape(10.dp))
                            .clickable { onToggleHabit(habit.id) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) palette.accent else palette.surfaceElevated)
                                    .border(
                                        BorderStroke(1.2.dp, if (isDone) palette.accent else palette.border),
                                        CircleShape
                                    )
                                    .testTag("home_habit_toggle_${habit.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = palette.buttonActiveText,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Text(
                                text = habit.title,
                                color = palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${habit.targetDaysPerWeek}D/WK",
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text = "OPEN HABIT JOURNAL & MATRIX →",
                color = palette.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable(onClick = onViewAll)
                    .padding(top = 4.dp)
                    .testTag("home_view_all_habits")
            )
        }
    }
}

/**
 * Quick Actions Grid (4 main shortcuts: Flashcards, Tasks, Shield, Habits).
 */
@Composable
private fun HomeQuickActionsGrid(
    palette: RetroPalette,
    onNavigate: (RetroScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "QUICK SUITE ACTIONS",
            color = palette.textSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionTile(
                title = "FLASHCARDS",
                icon = Icons.Default.Style,
                palette = palette,
                onClick = { onNavigate(RetroScreen.FLASHCARDS) },
                modifier = Modifier.weight(1f),
                testTag = "home_action_cards"
            )

            QuickActionTile(
                title = "OBJECTIVES",
                icon = Icons.Default.CheckBox,
                palette = palette,
                onClick = { onNavigate(RetroScreen.TASKS) },
                modifier = Modifier.weight(1f),
                testTag = "home_action_tasks"
            )

            QuickActionTile(
                title = "SHIELD",
                icon = Icons.Default.Shield,
                palette = palette,
                onClick = { onNavigate(RetroScreen.SHIELD) },
                modifier = Modifier.weight(1f),
                testTag = "home_action_shield"
            )

            QuickActionTile(
                title = "ROUTINES",
                icon = Icons.Default.EventRepeat,
                palette = palette,
                onClick = { onNavigate(RetroScreen.HABITS) },
                modifier = Modifier.weight(1f),
                testTag = "home_action_habits"
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: RetroPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceElevated)
            .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = palette.accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Focus Playlist & Lo-fi Beats Card
 * Renders the music mini-player docked on the Demichrome Home dashboard.
 * Dynamically styled with [RetroTheme.palette].
 */
@Composable
fun HomeFocusPlaylistCard(
    viewModel: FocusViewModel,
    palette: RetroPalette = RetroTheme.palette,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val ambientSound by viewModel.userPreferences.ambientSound.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }

    val playlistTracks = listOf(
        RetroAudioSynthesizer.AmbientSoundType.BINAURAL_432HZ to "Lo-fi Beats (432Hz)",
        RetroAudioSynthesizer.AmbientSoundType.WARM_BROWN_NOISE to "Deep Brown Noise",
        RetroAudioSynthesizer.AmbientSoundType.MECHANICAL_TICK to "Mechanical Clockwork"
    )

    val currentTrackIndex = playlistTracks.indexOfFirst { it.first == ambientSound }.coerceAtLeast(0)
    val currentTrackName = playlistTracks[currentTrackIndex].second

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .border(BorderStroke(1.2.dp, palette.border), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("home_focus_playlist_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.accentSoft)
                        .border(BorderStroke(1.dp, palette.border), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Focus Playlist",
                        tint = palette.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "FOCUS PLAYLIST",
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTrackName,
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Transport Controls: |<<  Play/Pause  >>
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Previous Track
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(BorderStroke(1.dp, palette.border), CircleShape)
                        .clickable {
                            val nextIdx = (currentTrackIndex - 1 + playlistTracks.size) % playlistTracks.size
                            val selected = playlistTracks[nextIdx].first
                            viewModel.userPreferences.setAmbientSound(selected)
                            if (isPlaying) {
                                RetroAudioSynthesizer.startAmbient(selected, 0.25f, coroutineScope)
                            }
                        }
                        .testTag("playlist_prev_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "|<",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            isPlaying = !isPlaying
                            if (isPlaying) {
                                val target = if (ambientSound == RetroAudioSynthesizer.AmbientSoundType.NONE) {
                                    RetroAudioSynthesizer.AmbientSoundType.BINAURAL_432HZ
                                } else {
                                    ambientSound
                                }
                                viewModel.userPreferences.setAmbientSound(target)
                                RetroAudioSynthesizer.startAmbient(target, 0.25f, coroutineScope)
                            } else {
                                RetroAudioSynthesizer.stopAmbient()
                            }
                        }
                        .testTag("playlist_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Playlist Audio",
                        tint = palette.buttonActiveText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Next Track
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(BorderStroke(1.dp, palette.border), CircleShape)
                        .clickable {
                            val nextIdx = (currentTrackIndex + 1) % playlistTracks.size
                            val selected = playlistTracks[nextIdx].first
                            viewModel.userPreferences.setAmbientSound(selected)
                            if (isPlaying) {
                                RetroAudioSynthesizer.startAmbient(selected, 0.25f, coroutineScope)
                            }
                        }
                        .testTag("playlist_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">|",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
