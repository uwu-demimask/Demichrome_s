package com.example.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.ui.theme.RetroTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dominic Hartt's Sanctuary Column-Grid (Matrix) Habit Tracker.
 * Renders habits as rows and month days as columns with minimalist fill-block matrix cells,
 * streak calculations, habit completion percentages, and daily totals.
 */
@Composable
fun SanctuaryHabitMatrix(
    habits: List<Habit>,
    habitLogs: List<HabitLog>,
    yearMonth: String, // "yyyy-MM"
    onToggleHabit: (habitId: Long, date: String) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val scrollState = rememberScrollState()

    // Calculate days in the selected month
    val daysInMonth = remember(yearMonth) {
        val cal = Calendar.getInstance()
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(yearMonth)
            if (date != null) cal.time = date
        } catch (_: Exception) {}
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Pre-index logs for O(1) cell lookup: Map<"habitId_date", Boolean>
    val logMap = remember(habitLogs) {
        habitLogs.associate { "${it.habitId}_${it.date}" to it.isCompleted }
    }

    // Days list: 1..daysInMonth with day-of-week letter (e.g. 1 -> "M", 2 -> "T", etc.)
    val dayHeaders = remember(yearMonth, daysInMonth) {
        val cal = Calendar.getInstance()
        (1..daysInMonth).map { dayNum ->
            val dateStr = String.format(Locale.getDefault(), "%s-%02d", yearMonth, dayNum)
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) {}
            val dayOfWeek = SimpleDateFormat("EEEEE", Locale.getDefault()).format(cal.time) // Single letter M, T, W...
            DayInfo(dayNumber = dayNum, dateString = dateStr, dayLetter = dayOfWeek)
        }
    }

    // Calculate daily completion counts for bottom summary row
    val dailyCompletions = remember(habits, habitLogs, dayHeaders) {
        dayHeaders.associate { day ->
            val completedCount = habits.count { habit ->
                logMap["${habit.id}_${day.dateString}"] == true
            }
            day.dateString to completedCount
        }
    }

    if (habits.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, palette.surfaceBorder)
                .background(palette.surface)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NO HABITS DEFINED",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Add your core routines to build your Sanctuary Habit Matrix.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(palette.surface)
    ) {
        // Horizontal Scrollable Matrix Container
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Fixed/Left Habit Header & Titles Column
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .background(palette.surface)
                ) {
                    // Header cell for Habit Column
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .border(0.5.dp, palette.surfaceBorder)
                            .background(palette.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "HABIT ROUTINE",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Habit Rows (Titles & Categories)
                    habits.forEach { habit ->
                        var showMenu by remember { mutableStateOf(false) }

                        // Calculate habit completion percentage for this month
                        val completedInMonth = dayHeaders.count { day ->
                            logMap["${habit.id}_${day.dateString}"] == true
                        }
                        val monthPercentage = if (daysInMonth > 0) (completedInMonth * 100) / daysInMonth else 0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(0.5.dp, palette.surfaceBorder)
                                .background(palette.surface)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = habit.title,
                                        color = palette.textPrimary,
                                        fontFamily = palette.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = habit.category,
                                            color = palette.textAccent,
                                            fontFamily = palette.fontFamily,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• $completedInMonth/$daysInMonth ($monthPercentage%)",
                                            color = palette.textSecondary,
                                            fontFamily = palette.fontFamily,
                                            fontSize = 8.sp
                                        )
                                    }
                                }

                                Box {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = palette.textSecondary.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                RetroHaptics.performClick(context)
                                                showMenu = true
                                            }
                                    )

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(palette.surface)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "DELETE HABIT",
                                                    color = Color.Red.copy(alpha = 0.85f),
                                                    fontFamily = palette.fontFamily,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                onDeleteHabit(habit)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Total Summary Cell
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(0.5.dp, palette.surfaceBorder)
                            .background(palette.surface.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "COMPLETED",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Grid Days Columns (1..daysInMonth)
                dayHeaders.forEach { day ->
                    val isToday = day.dateString == todayDateString

                    Column(
                        modifier = Modifier
                            .width(34.dp)
                            .background(if (isToday) palette.textAccent.copy(alpha = 0.08f) else Color.Transparent)
                    ) {
                        // Day Header Cell
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .border(0.5.dp, palette.surfaceBorder)
                                .background(
                                    if (isToday) palette.textAccent.copy(alpha = 0.18f) else palette.surface.copy(alpha = 0.9f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${day.dayNumber}",
                                    color = if (isToday) palette.textAccent else palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = day.dayLetter,
                                    color = if (isToday) palette.textAccent else palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Habit Matrix Cells for this day
                        habits.forEach { habit ->
                            val isCompleted = logMap["${habit.id}_${day.dateString}"] == true

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(
                                        width = if (isToday) 1.dp else 0.5.dp,
                                        color = if (isToday) palette.textAccent.copy(alpha = 0.5f) else palette.surfaceBorder
                                    )
                                    .clickable {
                                        onToggleHabit(habit.id, day.dateString)
                                    }
                                    .testTag("habit_cell_${habit.id}_${day.dayNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    // Minimalist Dominic Hartt retro solid block fill
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(palette.buttonBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = palette.buttonText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                } else {
                                    // Subtle uncompleted dot
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(
                                                if (isToday) palette.textAccent.copy(alpha = 0.6f) else palette.surfaceBorder
                                            )
                                    )
                                }
                            }
                        }

                        // Bottom Day Total Cell
                        val completedTodayCount = dailyCompletions[day.dateString] ?: 0
                        val totalHabits = habits.size
                        val dayRatio = if (totalHabits > 0) completedTodayCount.toFloat() / totalHabits else 0f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .border(0.5.dp, palette.surfaceBorder)
                                .background(palette.surface.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$completedTodayCount",
                                    color = if (completedTodayCount == totalHabits && totalHabits > 0) palette.textAccent else palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = if (completedTodayCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )

                                // Miniature progress bar tick
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(palette.surfaceBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(dayRatio)
                                            .height(3.dp)
                                            .background(palette.textAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DayInfo(
    val dayNumber: Int,
    val dateString: String,
    val dayLetter: String
)
