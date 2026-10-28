package com.example.ui.stats

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.core.haptics.RetroHaptics
import com.example.data.DayFocusSummary
import com.example.data.DetailedDailyStats
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val selectedDate by viewModel.selectedStatsDate.collectAsState()
    val dailyStats by viewModel.selectedDateStats.collectAsState()
    val dailyGoalMinutes by viewModel.dailyGoalMinutes.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val history14Days by viewModel.history14Days.collectAsState()
    val totalFocusSeconds by viewModel.totalFocusSeconds.collectAsState()
    val totalCompletedSessions by viewModel.totalCompletedSessions.collectAsState()
    val sessionQualityInsights by viewModel.sessionQualityInsights.collectAsState()

    val totalHours = totalFocusSeconds / 3600
    val totalMins = (totalFocusSeconds % 3600) / 60

    val todayDateString = viewModel.repository.getTodayDateString()
    val isViewingToday = selectedDate == todayDateString

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
                            text = "DEMICHROME STATS",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "> DAILY OFFLINE FOCUS ANALYTICS",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (!isViewingToday) {
                        RetroOutlineButton(
                            text = "TODAY",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.selectTodayStats()
                            },
                            icon = Icons.Default.Today,
                            testTag = "btn_jump_to_today"
                        )
                    }
                }
            }

            // Interactive Day Selector Header
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
                        IconButton(
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.navigateStatsDay(-1)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_stats_prev_day")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dailyStats?.formattedDate?.uppercase(Locale.getDefault())
                                    ?: selectedDate,
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isViewingToday) "[ TODAY'S LOG ]" else "[ HISTORICAL ARCHIVE ]",
                                color = if (isViewingToday) palette.accent else palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.navigateStatsDay(1)
                            },
                            enabled = !isViewingToday,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_stats_next_day")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = if (!isViewingToday) palette.textPrimary else palette.textSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 1. Daily Goal & Progress Meter Card
            item {
                val stats = dailyStats
                val mins = stats?.totalFocusMinutes ?: 0
                val progress = stats?.goalProgressPercent ?: 0
                val achieved = stats?.goalAchieved ?: false

                RetroCard(title = if (isViewingToday) "DAILY GOAL PROGRESS" else "DAILY TARGET OUTCOME") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOGGED TIME",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02dh %02dm", mins / 60, mins % 60),
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DAILY TARGET",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${dailyGoalMinutes} MINS",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Retro ASCII Meter
                    val filledBlocks = (progress / 10).coerceIn(0, 10)
                    val emptyBlocks = (10 - filledBlocks).coerceAtLeast(0)
                    val meterString = "[ " + "█".repeat(filledBlocks) + "░".repeat(emptyBlocks) + " ] $progress%"

                    Text(
                        text = meterString,
                        color = if (achieved) palette.accent else palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            achieved -> "★ GOAL REACHED! (+${mins - dailyGoalMinutes}m bonus work logged)"
                            mins == 0 -> "No focus activity recorded for this date."
                            else -> "${dailyGoalMinutes - mins}m remaining to hit daily goal."
                        },
                        color = if (achieved) palette.accent else palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )

                    if (isViewingToday) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ADJUST DAILY TARGET:",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30, 60, 90, 120, 180).forEach { targetMins ->
                                RetroChip(
                                    text = "${targetMins}m",
                                    isSelected = dailyGoalMinutes == targetMins,
                                    onClick = { viewModel.setDailyGoalMinutes(targetMins) },
                                    modifier = Modifier.weight(1f),
                                    testTag = "chip_goal_${targetMins}m"
                                )
                            }
                        }
                    }
                }
            }

            // 2. 4-Metric Grid for Selected Day
            item {
                val stats = dailyStats
                val totalSecs = stats?.totalFocusSeconds ?: 0L
                val sessions = stats?.totalSessions ?: 0
                val completed = stats?.completedSessions ?: 0
                val rate = stats?.completionRatePercent ?: 100
                val avgMins = stats?.averageSessionMinutes ?: 0
                val longestMins = stats?.longestSessionMinutes ?: 0

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RetroCard(
                            title = "SESSIONS",
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$sessions TOTAL",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$completed completed / ${sessions - completed} stopped",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }

                        RetroCard(
                            title = "COMPLETION",
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$rate%",
                                color = if (rate >= 80) palette.accent else palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (rate == 100) "Flawless focus" else "Target rate",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RetroCard(
                            title = "AVG SESSION",
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${avgMins} MINS",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Per focus block",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }

                        RetroCard(
                            title = "PEAK BLOCK",
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${longestMins} MINS",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Longest single sprint",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 3. Time of Day Distribution
            item {
                val stats = dailyStats
                val morning = stats?.morningMinutes ?: 0
                val afternoon = stats?.afternoonMinutes ?: 0
                val evening = stats?.eveningMinutes ?: 0
                val total = (morning + afternoon + evening).coerceAtLeast(1)

                RetroCard(title = "TIME OF DAY DISTRIBUTION") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeSegmentBar(
                            label = "MORNING   [00:00 - 12:00]",
                            minutes = morning,
                            percentage = (morning * 100) / total,
                            palette = palette
                        )
                        TimeSegmentBar(
                            label = "AFTERNOON [12:00 - 18:00]",
                            minutes = afternoon,
                            percentage = (afternoon * 100) / total,
                            palette = palette
                        )
                        TimeSegmentBar(
                            label = "EVENING   [18:00 - 24:00]",
                            minutes = evening,
                            percentage = (evening * 100) / total,
                            palette = palette
                        )
                    }
                }
            }

            // 4. 14-Day Activity Heatmap & Interactive Calendar
            item {
                RetroCard(title = "14-DAY ACTIVITY CALENDAR (TAP TO INSPECT)") {
                    if (history14Days.isNotEmpty()) {
                        val maxMins = history14Days.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(60) ?: 60

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            history14Days.forEach { day ->
                                val isSelected = day.dateString == selectedDate
                                val heightRatio = (day.totalMinutes.toFloat() / maxMins).coerceIn(0.1f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            RetroHaptics.performClick(context)
                                            viewModel.selectStatsDate(day.dateString)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    // Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((50 * heightRatio).dp)
                                            .background(
                                                when {
                                                    isSelected -> palette.accent
                                                    day.totalMinutes > 0 -> palette.textPrimary
                                                    else -> palette.surfaceBorder
                                                },
                                                RectangleShape
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Day of week label
                                    Text(
                                        text = day.dayOfWeek.take(1),
                                        color = if (isSelected) palette.accent else palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 9.sp
                                    )

                                    // Day number
                                    Text(
                                        text = day.dateString.takeLast(2),
                                        color = if (isSelected) palette.textPrimary else palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Session Quality & Rating Insights
            item {
                RetroCard(
                    title = "SESSION QUALITY & INSIGHTS",
                    badge = "${sessionQualityInsights.totalRatedSessions} RATED"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AVERAGE SCORE",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "★ %.1f / 5.0", sessionQualityInsights.averageRating),
                                color = palette.accent,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${sessionQualityInsights.totalRatedSessions} sessions rated",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BEST FOCUS WINDOW",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = sessionQualityInsights.bestFocusTime,
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Peak productivity period",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 5. Daily Session Ledger for Selected Date
            item {
                val sessions = dailyStats?.sessions ?: emptyList()
                Text(
                    text = "DAY SESSIONS LEDGER (${sessions.size})",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val daySessions = dailyStats?.sessions ?: emptyList()
            if (daySessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.surfaceBorder, RectangleShape)
                            .background(palette.surface, RectangleShape)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO SESSIONS LOGGED ON THIS DATE.",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                items(daySessions) { session ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.startTimeMillis))
                    val mins = session.actualElapsedSeconds / 60
                    val secs = session.actualElapsedSeconds % 60

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.surfaceBorder, RectangleShape)
                            .background(palette.surface, RectangleShape)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (session.completedSuccessfully) palette.accent else palette.textSecondary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "$timeFormat  —  ${session.targetDurationMinutes}M ${session.tag}",
                                            color = palette.textPrimary,
                                            fontFamily = palette.fontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (session.rating > 0) {
                                            Text(
                                                text = "★".repeat(session.rating),
                                                color = palette.accent,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (session.completedSuccessfully) {
                                            "Completed full ${mins}m ${secs}s (${session.styleUsed})"
                                        } else {
                                            "Stopped early at ${mins}m (${session.styleUsed})"
                                        },
                                        color = palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 11.sp
                                    )
                                    if (!session.note.isNullOrBlank()) {
                                        Text(
                                            text = "> \"${session.note}\"",
                                            color = palette.textAccent,
                                            fontFamily = palette.fontFamily,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            RetroIconButton(
                                icon = Icons.Default.DeleteOutline,
                                contentDescription = "Delete session",
                                onClick = { viewModel.deleteSession(session.id) },
                                testTag = "btn_delete_session_${session.id}"
                            )
                        }
                    }
                }
            }

            // 6. Lifetime Overview Card
            item {
                RetroCard(title = "LIFETIME OVERVIEW") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL FOCUS",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02dh %02dm", totalHours, totalMins),
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "$totalCompletedSessions completed sessions",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CURRENT STREAK",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${streakDays} DAYS",
                                color = palette.accent,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Consecutive quiet days",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Export Session Ledger
            item {
                RetroCard(
                    title = "DATA EXPORT & LEDGER",
                    badge = "OFFLINE"
                ) {
                    Text(
                        text = "Export and share your complete focus logs in standard open formats.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "EXPORT CSV",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.shareExport(context, "CSV")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "stats_btn_export_csv"
                        )
                        RetroButton(
                            text = "EXPORT JSON",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.shareExport(context, "JSON")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "stats_btn_export_json"
                        )
                        RetroOutlineButton(
                            text = "REPORT",
                            onClick = {
                                RetroHaptics.performClick(context)
                                viewModel.shareExport(context, "MARKDOWN")
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "stats_btn_export_report"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TimeSegmentBar(
    label: String,
    minutes: Int,
    percentage: Int,
    palette: com.example.ui.theme.RetroPalette
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontSize = 11.sp
            )
            Text(
                text = "${minutes}m ($percentage%)",
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        val filled = (percentage / 10).coerceIn(0, 10)
        val empty = (10 - filled).coerceAtLeast(0)
        Text(
            text = "[" + "█".repeat(filled) + "░".repeat(empty) + "]",
            color = if (minutes > 0) palette.textPrimary else palette.surfaceBorder,
            fontFamily = palette.fontFamily,
            fontSize = 11.sp
        )
    }
}
