package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.Habit
import com.example.data.TodoTask
import com.example.ui.focus.FocusViewModel
import com.example.ui.money.ExpenseEditorDialog
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState

/**
 * 07 PRODUCTIVITY INTEGRATION OVERVIEW
 * Shows:
 * TODAY'S SUMMARY:
 * Focus: 3h 20m
 * Habits: 4/5
 * Spent: ₹340
 */
@Composable
fun ProductivityOverviewCard(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToMoney: () -> Unit = {}
) {
    val palette = RetroTheme.palette
    val summary by viewModel.productivitySummary.collectAsState()

    DemichromeGlassCard(
        modifier = modifier.fillMaxWidth().testTag("productivity_overview_card"),
        palette = palette,
        cornerRadius = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SUMMARY",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "[ 24H PRODUCTIVITY OS ]",
                    color = palette.textSecondary.copy(alpha = 0.7f),
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Focus metric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, palette.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .background(palette.surfaceBorder.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Timelapse,
                                contentDescription = null,
                                tint = palette.textAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "FOCUS",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = summary.focusHoursFormatted,
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Habits metric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, palette.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .background(palette.surfaceBorder.copy(alpha = 0.12f))
                        .clickable { onNavigateToTasks() }
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = palette.textAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "HABITS",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = summary.habitsProgress,
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Money metric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, palette.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .background(palette.surfaceBorder.copy(alpha = 0.12f))
                        .clickable { onNavigateToMoney() }
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = palette.textAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "SPENT",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = summary.spentTodayFormatted,
                            color = palette.textAccent,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 06 DEMICHROME WIDGETS SECTION
 * Interactive in-app widgets for:
 * 1. Focus Timer Widget
 * 2. Habit Widget
 * 3. Money Widget
 * 4. Quick Tasks Widget
 */
@Composable
fun DemichromeWidgetsSection(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToMoney: () -> Unit = {}
) {
    val palette = RetroTheme.palette
    var selectedWidgetIndex by remember { mutableStateOf(0) }
    val widgetTitles = listOf("FOCUS TIMER", "HABITS", "MONEY", "TASKS")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section header & tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEMICHROME WIDGETS",
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                widgetTitles.forEachIndexed { index, title ->
                    val isSelected = selectedWidgetIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                1.dp,
                                if (isSelected) palette.textAccent else palette.surfaceBorder.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .background(if (isSelected) palette.buttonActiveBackground else palette.surface.copy(alpha = 0.3f))
                            .clickable { selectedWidgetIndex = index }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) palette.buttonActiveText else palette.buttonText,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Active widget display
        when (selectedWidgetIndex) {
            0 -> FocusTimerWidgetCard(viewModel = viewModel)
            1 -> HabitWidgetCard(viewModel = viewModel)
            2 -> MoneyWidgetCard(viewModel = viewModel, onNavigateToMoney = onNavigateToMoney)
            3 -> QuickTasksWidgetCard(viewModel = viewModel, onNavigateToTasks = onNavigateToTasks)
        }
    }
}

/**
 * 1. FOCUS TIMER WIDGET
 * Shows current timer status / remaining time / quick start
 */
@Composable
fun FocusTimerWidgetCard(viewModel: FocusViewModel) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val state by viewModel.state.collectAsState()
    val targetMinutes by viewModel.targetMinutes.collectAsState()

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    DemichromeGlassCard(
        modifier = Modifier.fillMaxWidth().testTag("widget_focus_timer"),
        palette = palette,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (state) {
                                    FocusState.RUNNING -> palette.textAccent
                                    FocusState.PAUSED -> Color(0xFFFFB74D)
                                    else -> palette.surfaceBorder
                                }
                            )
                    )
                    Text(
                        text = "STATUS: ${state.name}",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "TARGET: ${targetMinutes}M",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
            }

            // Big Timer Digits & Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormatted,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DemichromeGlassButton(
                        text = if (state == FocusState.RUNNING) "PAUSE" else "START",
                        onClick = {
                            viewModel.toggleTimer()
                        },
                        palette = palette,
                        isPrimary = true,
                        fontSize = 11.sp,
                        cornerRadius = 8.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    )

                    DemichromeGlassButton(
                        text = "RESET",
                        onClick = {
                            viewModel.resetTimer()
                        },
                        palette = palette,
                        isPrimary = false,
                        fontSize = 11.sp,
                        cornerRadius = 8.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Quick Duration Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(15, 25, 45, 60).forEach { presetMins ->
                    val isCurrent = targetMinutes == presetMins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                1.dp,
                                if (isCurrent) palette.textAccent else palette.surfaceBorder.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .background(if (isCurrent) palette.textAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                viewModel.selectDuration(presetMins)
                                RetroHaptics.performClick(context)
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${presetMins}m",
                            color = if (isCurrent) palette.textAccent else palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. HABIT WIDGET
 * Quick habit check-off for today
 */
@Composable
fun HabitWidgetCard(viewModel: FocusViewModel) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val activeHabits by viewModel.activeHabits.collectAsState()
    val habitLogs by viewModel.habitLogsForMonth.collectAsState()
    val todayDateStr = remember { viewModel.repository.getTodayDateString() }

    DemichromeGlassCard(
        modifier = Modifier.fillMaxWidth().testTag("widget_habits"),
        palette = palette,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S HABITS",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = todayDateStr,
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
            }

            if (activeHabits.isEmpty()) {
                Text(
                    text = "No active habits seeded. Open Habits to add.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp
                )
            } else {
                activeHabits.take(3).forEach { habit ->
                    val isCompletedToday = habitLogs.any { it.habitId == habit.id && it.date == todayDateStr && it.isCompleted }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .background(if (isCompletedToday) palette.textAccent.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable {
                                viewModel.toggleHabit(habit.id, todayDateStr)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (isCompletedToday) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (isCompletedToday) palette.textAccent else palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = habit.title,
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                textDecoration = if (isCompletedToday) TextDecoration.LineThrough else null,
                                fontWeight = if (isCompletedToday) FontWeight.Normal else FontWeight.Medium
                            )
                        }

                        Text(
                            text = if (isCompletedToday) "[DONE]" else "[TAP]",
                            color = if (isCompletedToday) palette.textAccent else palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. MONEY WIDGET
 * Shows remaining budget and quick "Add Expense"
 */
@Composable
fun MoneyWidgetCard(
    viewModel: FocusViewModel,
    onNavigateToMoney: () -> Unit = {}
) {
    val palette = RetroTheme.palette
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()
    val todaySpent by viewModel.todaySpent.collectAsState()

    val totalSpent = monthExpenses.sumOf { it.amount }
    val remaining = monthlyBudget - totalSpent
    val percentUsed = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0

    var showQuickAddDialog by remember { mutableStateOf(false) }

    DemichromeGlassCard(
        modifier = Modifier.fillMaxWidth().testTag("widget_money"),
        palette = palette,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BUDGET OVERVIEW",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                DemichromeGlassButton(
                    text = "+ EXPENSE",
                    onClick = { showQuickAddDialog = true },
                    palette = palette,
                    isPrimary = true,
                    fontSize = 9.sp,
                    cornerRadius = 6.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REMAINING BALANCE",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "₹${remaining.toInt()}",
                        color = if (remaining >= 0) palette.textPrimary else Color(0xFFFF5252),
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SPENT TODAY",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "₹${todaySpent.toInt()}",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (percentUsed >= 100) Color(0xFFFF5252) else palette.textAccent,
                trackColor = palette.surfaceBorder.copy(alpha = 0.3f)
            )
        }
    }

    if (showQuickAddDialog) {
        ExpenseEditorDialog(
            title = "QUICK ADD EXPENSE",
            initialAmount = "",
            initialCategory = "FOOD / MESS",
            initialNote = "",
            initialPaymentMode = "UPI",
            onDismiss = { showQuickAddDialog = false },
            onSave = { amt, cat, note, mode ->
                viewModel.addExpense(amt, cat, note, mode)
                showQuickAddDialog = false
            }
        )
    }
}

/**
 * 4. QUICK TASKS WIDGET
 * Shows top pending tasks with instant toggle
 */
@Composable
fun QuickTasksWidgetCard(
    viewModel: FocusViewModel,
    onNavigateToTasks: () -> Unit = {}
) {
    val palette = RetroTheme.palette
    val tasks by viewModel.activeTasks.collectAsState()

    DemichromeGlassCard(
        modifier = Modifier.fillMaxWidth().testTag("widget_tasks"),
        palette = palette,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PENDING TASKS",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${tasks.size} PENDING",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tasks.isEmpty()) {
                Text(
                    text = "All tasks completed! Great focus session.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp
                )
            } else {
                tasks.take(3).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleTaskCompleted(task) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (task.isCompleted) palette.textAccent else palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = task.title,
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "[CHECK]",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
