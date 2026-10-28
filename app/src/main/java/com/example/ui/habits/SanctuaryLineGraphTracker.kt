package com.example.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.haptics.RetroHaptics
import com.example.data.DefaultSanctuaryMetric
import com.example.data.FocusSession
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.MetricEntry
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.theme.RetroTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dominic Hartt's Sanctuary Line Graph Data Tracker.
 * Renders an aligned, interactive line graph plotting numeric daily metrics (Focus Time, Energy, Sleep, Discipline %, Mood)
 * along the exact day timeline of the Column-Grid Matrix.
 */
@Composable
fun SanctuaryLineGraphTracker(
    yearMonth: String,
    selectedMetricKey: String,
    onSelectMetricKey: (String) -> Unit,
    metricEntries: List<MetricEntry>,
    habitLogs: List<HabitLog>,
    habits: List<Habit>,
    allFocusSessions: List<FocusSession>,
    onLogMetric: (metricKey: String, date: String, value: Float, label: String, unit: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val scrollState = rememberScrollState()

    var showEditDialogForDate by remember { mutableStateOf<String?>(null) }

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

    val metricDef = remember(selectedMetricKey) {
        DefaultSanctuaryMetric.fromKey(selectedMetricKey) ?: DefaultSanctuaryMetric.FOCUS_MINUTES
    }

    // Compute data points per day (1..daysInMonth)
    val dayPoints: List<DayMetricPoint> = remember(
        yearMonth,
        daysInMonth,
        selectedMetricKey,
        metricEntries,
        habitLogs,
        habits,
        allFocusSessions
    ) {
        val entryMap = metricEntries.filter { it.metricKey == selectedMetricKey }
            .associateBy { it.date }

        // Compute focus sessions minutes per date
        val focusMinutesByDate = allFocusSessions
            .filter { it.completedSuccessfully && it.dateString.startsWith(yearMonth) }
            .groupBy { it.dateString }
            .mapValues { (_, sessions) -> (sessions.sumOf { it.actualElapsedSeconds } / 60).toFloat() }

        // Compute habit discipline % per date
        val logMap = habitLogs.associate { "${it.habitId}_${it.date}" to it.isCompleted }
        val habitPercentByDate = (1..daysInMonth).associate { dayNum ->
            val dateStr = String.format(Locale.getDefault(), "%s-%02d", yearMonth, dayNum)
            if (habits.isNotEmpty()) {
                val completed = habits.count { logMap["${it.id}_$dateStr"] == true }
                val pct = (completed.toFloat() / habits.size) * 100f
                dateStr to pct
            } else {
                dateStr to 0f
            }
        }

        (1..daysInMonth).map { dayNum ->
            val dateStr = String.format(Locale.getDefault(), "%s-%02d", yearMonth, dayNum)
            val explicitEntry = entryMap[dateStr]

            val value: Float? = when (selectedMetricKey) {
                DefaultSanctuaryMetric.FOCUS_MINUTES.key -> {
                    explicitEntry?.value ?: focusMinutesByDate[dateStr]
                }
                DefaultSanctuaryMetric.PRODUCTIVITY.key -> {
                    explicitEntry?.value ?: habitPercentByDate[dateStr]
                }
                else -> {
                    explicitEntry?.value
                }
            }

            DayMetricPoint(
                dayNumber = dayNum,
                dateString = dateStr,
                value = value,
                hasValue = value != null
            )
        }
    }

    // Chart analytics metrics
    val valuesList = dayPoints.mapNotNull { it.value }
    val avgValue = if (valuesList.isNotEmpty()) valuesList.average().toFloat() else 0f
    val maxValue = if (valuesList.isNotEmpty()) valuesList.maxOrNull() ?: 0f else 0f
    val recordedDaysCount = valuesList.size

    // Min and Max range for chart scaling
    val chartMin = metricDef.minValue
    val chartMax = maxOf(metricDef.maxValue, maxValue)

    RetroCard(
        title = "SANCTUARY LINE GRAPH DATA TRACKER",
        badge = "${metricDef.label.uppercase()} [${valuesList.size}/$daysInMonth DAYS]",
        modifier = modifier.fillMaxWidth()
    ) {
        // Metric Switcher Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DefaultSanctuaryMetric.values().forEach { metric ->
                RetroChip(
                    text = metric.label.uppercase(),
                    isSelected = selectedMetricKey == metric.key,
                    onClick = {
                        RetroHaptics.performClick(context)
                        onSelectMetricKey(metric.key)
                    },
                    testTag = "chip_metric_${metric.key.lowercase()}"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analytics KPI Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                .background(palette.background)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MONTH AVERAGE",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f %s", avgValue, metricDef.unit),
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MONTH PEAK",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f %s", maxValue, metricDef.unit),
                    color = palette.textAccent,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LOGGED",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 9.sp
                )
                Text(
                    text = "$recordedDaysCount / $daysInMonth d",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Line Graph Chart with Horizontal Date Alignment matching the Matrix Grid
        val cellWidth = 34.dp
        val fixedHeaderWidth = 60.dp
        val totalChartWidth = fixedHeaderWidth + (cellWidth * daysInMonth)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                .background(palette.background)
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Fixed Y-Axis Labels Column
                Column(
                    modifier = Modifier
                        .width(fixedHeaderWidth)
                        .height(160.dp)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${chartMax.toInt()} ${metricDef.unit}",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 8.sp
                    )
                    Text(
                        text = "${((chartMax + chartMin) / 2).toInt()} ${metricDef.unit}",
                        color = palette.textSecondary.copy(alpha = 0.6f),
                        fontFamily = palette.fontFamily,
                        fontSize = 8.sp
                    )
                    Text(
                        text = "${chartMin.toInt()} ${metricDef.unit}",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 8.sp
                    )
                    Text(
                        text = "DAY >",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive Canvas Chart Area (Width matches Day columns)
                Column(
                    modifier = Modifier.width(cellWidth * daysInMonth)
                ) {
                    // Line Graph Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                    ) {
                        val lineColor = palette.textAccent
                        val gridColor = palette.surfaceBorder.copy(alpha = 0.5f)
                        val pointBg = palette.background

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val colW = w / daysInMonth
                            val range = if (chartMax > chartMin) (chartMax - chartMin) else 1f

                            // Draw Horizontal Grid Reference Lines
                            val gridSteps = 4
                            for (i in 0..gridSteps) {
                                val y = (h / gridSteps) * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 0.8f
                                )
                            }

                            // Calculate (x, y) coordinates for all valid data points
                            val validPoints = mutableListOf<Pair<Int, Offset>>()
                            dayPoints.forEachIndexed { index, point ->
                                val x = (index * colW) + (colW / 2f)
                                if (point.value != null) {
                                    val normalized = ((point.value - chartMin) / range).coerceIn(0f, 1f)
                                    val y = h - (normalized * (h - 16f)) - 8f
                                    validPoints.add(index to Offset(x, y))
                                }
                            }

                            // Draw continuous line segments & area gradient
                            if (validPoints.size >= 2) {
                                val linePath = Path()
                                val fillPath = Path()

                                validPoints.forEachIndexed { idx, (_, offset) ->
                                    if (idx == 0) {
                                        linePath.moveTo(offset.x, offset.y)
                                        fillPath.moveTo(offset.x, h)
                                        fillPath.lineTo(offset.x, offset.y)
                                    } else {
                                        linePath.lineTo(offset.x, offset.y)
                                        fillPath.lineTo(offset.x, offset.y)
                                    }
                                }

                                val lastPoint = validPoints.last().second
                                fillPath.lineTo(lastPoint.x, h)
                                fillPath.close()

                                // Soft area fill under line
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            lineColor.copy(alpha = 0.28f),
                                            lineColor.copy(alpha = 0.03f)
                                        )
                                    )
                                )

                                // Solid Line path
                                drawPath(
                                    path = linePath,
                                    color = lineColor,
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            // Draw Data Node Circles for each recorded point
                            validPoints.forEach { (dayIdx, offset) ->
                                val isToday = dayPoints[dayIdx].dateString == todayDateString

                                // Glow aura for today
                                if (isToday) {
                                    drawCircle(
                                        color = lineColor.copy(alpha = 0.35f),
                                        radius = 7.dp.toPx(),
                                        center = offset
                                    )
                                }

                                // Outer circle
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = offset
                                )
                                // Inner center circle
                                drawCircle(
                                    color = pointBg,
                                    radius = 2.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }

                    // Bottom Day Numbers Axis (Clickable to log/edit value for that day!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        dayPoints.forEach { point ->
                            val isToday = point.dateString == todayDateString

                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(25.dp)
                                    .clickable {
                                        RetroHaptics.performClick(context)
                                        showEditDialogForDate = point.dateString
                                    }
                                    .testTag("line_chart_node_${point.dayNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${point.dayNumber}",
                                    color = if (isToday) palette.textAccent else if (point.hasValue) palette.textPrimary else palette.textSecondary.copy(alpha = 0.5f),
                                    fontFamily = palette.fontFamily,
                                    fontWeight = if (isToday || point.hasValue) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Entry Trigger Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡 Tap any day on the timeline to record or adjust metrics",
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontSize = 10.sp
            )

            RetroButton(
                text = "+ LOG TODAY (${metricDef.label})",
                onClick = {
                    RetroHaptics.performClick(context)
                    showEditDialogForDate = todayDateString
                },
                isPrimary = true,
                testTag = "btn_log_today_metric"
            )
        }
    }

    // Modal Dialog to log or update daily metric for any date
    showEditDialogForDate?.let { dateStr ->
        val currentPoint = dayPoints.firstOrNull { it.dateString == dateStr }
        var inputVal by remember {
            mutableStateOf(currentPoint?.value ?: metricDef.defaultValue)
        }

        Dialog(onDismissRequest = { showEditDialogForDate = null }) {
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
                        Column {
                            Text(
                                text = "LOG SANCTUARY METRIC",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "> DATE: $dateStr • ${metricDef.label.uppercase()}",
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                        RetroIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { showEditDialogForDate = null }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Numeric Value Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.background)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f %s", inputVal, metricDef.unit),
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider for Value adjustment
                    Slider(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        valueRange = metricDef.minValue..metricDef.maxValue,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.buttonBorder,
                            activeTrackColor = palette.textAccent,
                            inactiveTrackColor = palette.surfaceBorder
                        ),
                        modifier = Modifier.testTag("slider_metric_value")
                    )

                    // Quick Increment Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val step = if (metricDef.maxValue > 50) 15f else 1f
                        RetroOutlineButton(
                            text = "- $step",
                            onClick = {
                                inputVal = (inputVal - step).coerceAtLeast(metricDef.minValue)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RetroOutlineButton(
                            text = "+ $step",
                            onClick = {
                                inputVal = (inputVal + step).coerceAtMost(metricDef.maxValue)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "CANCEL",
                            onClick = { showEditDialogForDate = null },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_cancel_metric_log"
                        )
                        RetroButton(
                            text = "SAVE ENTRY",
                            onClick = {
                                onLogMetric(
                                    selectedMetricKey,
                                    dateStr,
                                    inputVal,
                                    metricDef.label,
                                    metricDef.unit
                                )
                                showEditDialogForDate = null
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_save_metric_log"
                        )
                    }
                }
            }
        }
    }
}

data class DayMetricPoint(
    val dayNumber: Int,
    val dateString: String,
    val value: Float?,
    val hasValue: Boolean
)
