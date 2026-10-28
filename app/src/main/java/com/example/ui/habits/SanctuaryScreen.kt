package com.example.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.haptics.RetroHaptics
import com.example.data.Habit
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SanctuaryScreen(
    viewModel: FocusViewModel,
    onNavigateToFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val selectedMonth by viewModel.selectedHabitMonth.collectAsState()
    val habits by viewModel.activeHabits.collectAsState()
    val habitLogs by viewModel.habitLogsForMonth.collectAsState()
    val selectedMetricKey by viewModel.selectedMetricKey.collectAsState()
    val metricEntries by viewModel.metricEntriesForMonth.collectAsState()
    val allSessions by viewModel.repository.allSessions.collectAsState(initial = emptyList())
    val streakDays by viewModel.streakDays.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }

    // Format display month title (e.g. "AUGUST 2026")
    val displayMonthTitle = remember(selectedMonth) {
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
            if (date != null) {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date).uppercase()
            } else {
                selectedMonth
            }
        } catch (_: Exception) {
            selectedMonth
        }
    }

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val currentYearMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
    val isViewingCurrentMonth = selectedMonth == currentYearMonth

    // Calculate Month Overall Discipline Completion %
    val monthStats = remember(habits, habitLogs, selectedMonth) {
        val cal = Calendar.getInstance()
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
            if (date != null) cal.time = date
        } catch (_: Exception) {}
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val totalOpportunities = habits.size * daysInMonth
        val totalCompleted = habitLogs.count { it.isCompleted }
        val percentage = if (totalOpportunities > 0) (totalCompleted * 100) / totalOpportunities else 0

        // Completed today
        val completedToday = habits.count { habit ->
            habitLogs.any { it.habitId == habit.id && it.date == todayDateString && it.isCompleted }
        }

        Triple(percentage, completedToday, habits.size)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SANCTUARY HABIT JOURNAL",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "> DOMINIC HARTT MATRIX & DATA TRACKER",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (!isViewingCurrentMonth) {
                        RetroOutlineButton(
                            text = "CURRENT",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.resetToCurrentMonth()
                            },
                            icon = Icons.Default.Today,
                            testTag = "btn_jump_to_current_month"
                        )
                    }
                }
            }

            // Month Navigation Selector
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, palette.surfaceBorder, RectangleShape)
                        .background(palette.surface, RectangleShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RetroIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            onClick = { viewModel.previousHabitMonth() },
                            testTag = "btn_prev_month"
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayMonthTitle,
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${monthStats.first}% DISCIPLINE SCORE • ${monthStats.second}/${monthStats.third} TODAY",
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        RetroIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            onClick = { viewModel.nextHabitMonth() },
                            testTag = "btn_next_month"
                        )
                    }
                }
            }

            // Quick Stats Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Month Score
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.surface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "CONSISTENCY",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${monthStats.first}%",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Today's Progress
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.surface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S HABITS",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${monthStats.second} / ${monthStats.third}",
                                color = if (monthStats.second == monthStats.third && monthStats.third > 0) palette.textAccent else palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Active Focus Streak
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.surface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE STREAK",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${streakDays} DAYS",
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Dominic Hartt's Column-Grid (Matrix) Habit Tracker
            item {
                RetroCard(
                    title = "MINIMALIST COLUMN-GRID HABIT MATRIX",
                    badge = "${habits.size} ACTIVE ROUTINES"
                ) {
                    Text(
                        text = "Track your daily core routines across the entire month. Tap any matrix block to mark completion.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SanctuaryHabitMatrix(
                        habits = habits,
                        habitLogs = habitLogs,
                        yearMonth = selectedMonth,
                        onToggleHabit = { habitId, date ->
                            viewModel.toggleHabit(habitId, date)
                        },
                        onDeleteHabit = { habit ->
                            viewModel.deleteHabit(habit)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RetroOutlineButton(
                        text = "+ ADD NEW HABIT ROUTINE",
                        onClick = {
                            RetroHaptics.performClick(context)
                            showAddHabitDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_open_add_habit_dialog"
                    )
                }
            }

            // Dominic Hartt's Line Graph Data Tracker
            item {
                SanctuaryLineGraphTracker(
                    yearMonth = selectedMonth,
                    selectedMetricKey = selectedMetricKey,
                    onSelectMetricKey = { viewModel.selectMetricKey(it) },
                    metricEntries = metricEntries,
                    habitLogs = habitLogs,
                    habits = habits,
                    allFocusSessions = allSessions,
                    onLogMetric = { key, date, value, label, unit ->
                        viewModel.logMetricEntry(key, date, value, label, unit)
                    }
                )
            }

            // Sanctuary Principles Guide
            item {
                RetroCard(title = "SANCTUARY JOURNAL PROTOCOL") {
                    Text(
                        text = "1. THE TWO-DAY RULE\nNever allow two consecutive blank days on your matrix. Missing one day is human; missing two starts a new destructive habit.",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. CORRELATION ANALYSIS\nObserve how your Line Graph (Focus Time & Energy) mirrors your Habit Matrix. Peak focus invariably follows high routine consistency.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "3. MINIMALIST EXECUTION\nTrack only the 4 to 7 non-negotiable keystones that make everything else effortless.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Dialog to Add a New Habit
    if (showAddHabitDialog) {
        var habitTitle by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("FOCUS") }
        var targetDays by remember { mutableStateOf(7) }

        val categories = listOf("FOCUS", "DISCIPLINE", "MIND", "HEALTH", "ROUTINE")

        Dialog(onDismissRequest = { showAddHabitDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, palette.buttonBorder, RoundedCornerShape(8.dp))
                    .background(palette.surface)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CREATE HABIT ROUTINE",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        RetroIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { showAddHabitDialog = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "HABIT TITLE",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = habitTitle,
                        onValueChange = { habitTitle = it },
                        placeholder = {
                            Text(
                                "e.g., Read 20 Pages, Cold Shower, Deep Work...",
                                color = palette.textSecondary.copy(alpha = 0.5f),
                                fontFamily = palette.fontFamily,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            focusedBorderColor = palette.buttonBorder,
                            unfocusedBorderColor = palette.surfaceBorder,
                            cursorColor = palette.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_habit_title"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CATEGORY",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            RetroChip(
                                text = cat,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                modifier = Modifier.weight(1f),
                                testTag = "chip_category_${cat.lowercase()}"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "CANCEL",
                            onClick = { showAddHabitDialog = false },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_cancel_add_habit"
                        )
                        RetroButton(
                            text = "CREATE HABIT",
                            onClick = {
                                if (habitTitle.isNotBlank()) {
                                    viewModel.addHabit(
                                        title = habitTitle,
                                        category = selectedCategory,
                                        targetDays = targetDays
                                    )
                                    showAddHabitDialog = false
                                }
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_confirm_add_habit"
                        )
                    }
                }
            }
        }
    }
}
