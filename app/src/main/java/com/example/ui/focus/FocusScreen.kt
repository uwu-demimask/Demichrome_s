package com.example.ui.focus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.accessibility.SystemDisplayHelper
import com.example.core.haptics.RetroHaptics
import com.example.ui.components.CustomDurationDialog
import com.example.ui.components.DemichromeWidgetsSection
import com.example.ui.components.ProductivityOverviewCard
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroHeader
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import com.example.ui.timer.TimerDisplay
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onNavigateToVault: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShield: () -> Unit = {},
    onNavigateToGuide: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToMoney: () -> Unit = {},
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()
    val state by viewModel.state.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val todayFocusSeconds by viewModel.todayFocusSeconds.collectAsState()
    val todaySessionCount by viewModel.todaySessionCount.collectAsState()
    val dailyGoalMinutes by viewModel.dailyGoalMinutes.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val weeklyHistory by viewModel.weeklyHistory.collectAsState()

    val selectedTargetTask by viewModel.selectedTargetTask.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()

    val timerStyle by viewModel.userPreferences.timerStyle.collectAsState()
    val monochromeMode by viewModel.userPreferences.monochromeMode.collectAsState()
    val vaultUnlocked by viewModel.userPreferences.vaultUnlocked.collectAsState()

    var showCustomDurationDialog by remember { mutableStateOf(false) }

    // Notification permission request for Android 13+ (API 33+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val presetDurations = listOf(25, 45, 60, 90)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Retro Header
            RetroHeader(
                title = "DEMICHROME",
                subtitle = "RETRO MINIMALIST FOCUS WORKSPACE"
            )

            // System Status Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                    .background(palette.surface, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = systemStatus,
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = if (monochromeMode) "[MONO]" else "[NORMAL]",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notification permission banner if missing on Android 13+
            AnimatedVisibility(
                visible = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, palette.accent, RoundedCornerShape(6.dp))
                        .background(palette.surface, RoundedCornerShape(6.dp))
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("notification_permission_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENABLE NOTIFICATION BAR TIMER",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "[ ALLOW ]",
                            color = palette.accent,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration selector chips
            if (state == FocusState.READY || state == FocusState.COMPLETED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetDurations.forEach { mins ->
                        RetroChip(
                            text = "${mins}M",
                            isSelected = targetMinutes == mins,
                            onClick = { viewModel.selectDuration(mins) },
                            modifier = Modifier.weight(1f),
                            testTag = "chip_duration_$mins"
                        )
                    }
                    RetroChip(
                        text = if (!presetDurations.contains(targetMinutes)) "${targetMinutes}M" else "CUSTOM",
                        isSelected = !presetDurations.contains(targetMinutes),
                        onClick = { showCustomDurationDialog = true },
                        modifier = Modifier.weight(1.2f),
                        testTag = "chip_duration_custom"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Target Objective Strip (To-Do List Integration)
            if (selectedTargetTask != null) {
                val target = selectedTargetTask!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, palette.textAccent.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .background(palette.surface, RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTasks() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("active_target_task_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                                    .background(palette.background)
                                    .clickable { viewModel.toggleTaskCompleted(target) }
                                    .testTag("btn_complete_target_task"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (target.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "TARGET OBJECTIVE",
                                        color = palette.textAccent,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "• 🎯 ${target.completedSessions}/${target.estimatedSessions} POMOS",
                                        color = palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 9.sp
                                    )
                                }
                                Text(
                                    text = target.title,
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Text(
                            text = "[ CHANGE ]",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (activeTasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(8.dp))
                        .background(palette.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTasks() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("select_target_task_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${activeTasks.size} Objectives in To-Do List",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "[ SELECT TARGET ]",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Central Retro Timer Display
            TimerDisplay(
                remainingSeconds = remainingSeconds,
                totalSeconds = targetMinutes * 60,
                state = state,
                timerStyle = timerStyle,
                onClickTimer = {
                    if (state == FocusState.READY) {
                        viewModel.startTimer()
                    } else if (state == FocusState.RUNNING) {
                        viewModel.pauseTimer()
                    } else if (state == FocusState.PAUSED) {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Primary Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    FocusState.READY -> {
                        RetroButton(
                            text = "START FOCUS",
                            onClick = { viewModel.startTimer() },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_start_focus"
                        )
                    }
                    FocusState.RUNNING -> {
                        RetroButton(
                            text = "PAUSE",
                            onClick = { viewModel.pauseTimer() },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_pause_focus"
                        )
                        RetroButton(
                            text = "RESET",
                            onClick = { viewModel.resetTimer() },
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_reset_focus"
                        )
                    }
                    FocusState.PAUSED -> {
                        RetroButton(
                            text = "RESUME",
                            onClick = { viewModel.startTimer() },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_resume_focus"
                        )
                        RetroButton(
                            text = "STOP & RESET",
                            onClick = { viewModel.resetTimer() },
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_stop_reset"
                        )
                    }
                    FocusState.COMPLETED -> {
                        RetroButton(
                            text = "NEW SESSION",
                            onClick = { viewModel.resetTimer() },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_new_session"
                        )
                    }
                }

                // PiP Pop Out Button
                RetroIconButton(
                    icon = Icons.Default.PictureInPicture,
                    contentDescription = "Picture in Picture",
                    onClick = onEnterPip,
                    isSelected = false,
                    testTag = "btn_pip_quick_toggle"
                )

                // Zen / Focus Mode toggle
                RetroIconButton(
                    icon = Icons.Default.CenterFocusStrong,
                    contentDescription = "Zen Focus Mode",
                    onClick = { viewModel.toggleFocusMode() },
                    isSelected = false,
                    testTag = "btn_zen_focus_toggle"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 07 Productivity Integration Daily Overview (Focus + Habits + Money)
            ProductivityOverviewCard(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
                onNavigateToTasks = onNavigateToTasks,
                onNavigateToMoney = onNavigateToMoney
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 06 Demichrome Interactive Widgets (Timer, Habits, Money, Tasks)
            DemichromeWidgetsSection(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
                onNavigateToTasks = onNavigateToTasks,
                onNavigateToMoney = onNavigateToMoney
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 07 Minimalist Notes in Room Database (Jot down thoughts during focus sessions)
            MinimalistNotesSection(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Launch Row for Deep Focus, Objectives & Blocker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroOutlineButton(
                    text = "DEEP FOCUS",
                    onClick = {
                        viewModel.setDeepFocusMode(true)
                        if (state == FocusState.READY) {
                            viewModel.startTimer()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Lock,
                    testTag = "btn_launch_deep_focus_quick"
                )

                RetroOutlineButton(
                    text = "OBJECTIVES",
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckBox,
                    testTag = "btn_launch_tasks_quick"
                )

                RetroOutlineButton(
                    text = "SHIELD APPS",
                    onClick = onNavigateToShield,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Tune,
                    testTag = "btn_launch_shield_quick"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distraction Shield Card
            val blockerEnabled by viewModel.blockerEnabled.collectAsState()
            val blockedPackages by viewModel.blockedPackages.collectAsState()
            val blockedAttemptsToday by viewModel.blockedAttemptsToday.collectAsState()

            RetroCard(
                title = "DISTRACTION SHIELD",
                badge = if (blockerEnabled) "ACTIVE" else "OFF",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (blockerEnabled) "${blockedPackages.size} Apps Shielded" else "Shield Standby",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (blockedAttemptsToday > 0) "$blockedAttemptsToday distraction(s) blocked today"
                            else "Blocks social media & games during timers",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    }

                    RetroOutlineButton(
                        text = "MANAGE",
                        onClick = onNavigateToShield,
                        testTag = "btn_manage_shield"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Stats & Weekly Graph Card
            RetroCard(
                title = "DAILY FOCUS STATS & LOG",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                val todayMinutes = (todayFocusSeconds / 60).toInt()
                val hours = todayMinutes / 60
                val mins = todayMinutes % 60
                val formattedTime = String.format(Locale.getDefault(), "%02dh %02dm", hours, mins)
                val progressPercent = if (dailyGoalMinutes > 0) {
                    ((todayMinutes * 100) / dailyGoalMinutes).coerceAtMost(999)
                } else 100
                val isGoalReached = todayMinutes >= dailyGoalMinutes

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY LOGGED",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = formattedTime,
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$todaySessionCount session(s)",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "STREAK",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (streakDays > 0) "${streakDays}_DAYS" else "0_DAYS",
                            color = palette.accent,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isGoalReached) "★ GOAL MET ★" else "${dailyGoalMinutes}m goal",
                            color = if (isGoalReached) palette.accent else palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Progress Bar
                val progressFloat = (progressPercent / 100f).coerceIn(0f, 1f)
                com.example.ui.timer.SmoothProgressBar(progress = progressFloat, palette = palette)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progressPercent% of daily target reached (${todayMinutes}m / ${dailyGoalMinutes}m)",
                    color = if (isGoalReached) palette.accent else palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Weekly 7-day mini bar graph
                if (weeklyHistory.isNotEmpty()) {
                    val maxMins = weeklyHistory.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(60) ?: 60
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyHistory.forEach { day ->
                            val isToday = day.dateString == viewModel.repository.getTodayDateString()
                            val heightRatio = (day.totalMinutes.toFloat() / maxMins).coerceIn(0.1f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .height((32 * heightRatio).dp)
                                        .background(
                                            when {
                                                isToday -> palette.accent
                                                day.totalMinutes > 0 -> palette.textPrimary
                                                else -> palette.surfaceBorder
                                            },
                                            RectangleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day.dayOfWeek.take(2),
                                    color = if (isToday) palette.accent else palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RetroOutlineButton(
                    text = "VIEW FULL DAILY STATS & HISTORY",
                    onClick = {
                        RetroHaptics.performClick(context)
                        viewModel.selectTodayStats()
                        onNavigateToStats()
                    },
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_open_daily_stats_full"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Monochrome Display Notice & Shortcut
            RetroCard(
                title = "MONOCHROME DISPLAY MODE",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "For full system-wide grayscale, Android requires the system Color Correction toggle. Tap below to launch your device settings directly.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                RetroButton(
                    text = "OPEN ANDROID GRAYSCALE SETTINGS",
                    onClick = { SystemDisplayHelper.openColorCorrectionSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_open_system_grayscale"
                )
            }
        }

        // Custom Duration Dialog
        if (showCustomDurationDialog) {
            CustomDurationDialog(
                initialMinutes = targetMinutes,
                onDismiss = { showCustomDurationDialog = false },
                onConfirm = { mins ->
                    viewModel.selectDuration(mins)
                    showCustomDurationDialog = false
                }
            )
        }
    }
}
