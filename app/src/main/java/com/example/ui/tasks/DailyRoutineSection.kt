package com.example.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.DailyTask
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.SmoothProgressBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyRoutineSection(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val keyboardController = LocalSoftwareKeyboardController.current

    val dailyTasks by viewModel.allDailyTasks.collectAsState()
    val completedCount by viewModel.dailyTasksCompletedCount.collectAsState()
    val totalCount by viewModel.dailyTasksTotalCount.collectAsState()

    var newRoutineTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ROUTINE") }
    var showResetDialog by remember { mutableStateOf(false) }

    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val todayFormatted = remember {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
    }

    RetroCard(
        title = "DAILY FOCUS ROUTINE",
        badge = "RESETS IN 24H",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header stats and 24h reset indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$todayFormatted • $completedCount/$totalCount DONE",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Minimal focus habits that reset every 24 hours.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp
                    )
                }

                if (totalCount > 0) {
                    RetroOutlineButton(
                        text = "RESET",
                        onClick = { showResetDialog = true },
                        icon = Icons.Default.Refresh,
                        testTag = "btn_reset_daily_routine"
                    )
                }
            }

            // Progress bar
            SmoothProgressBar(progress = progress, palette = palette)

            // Routine checklist items
            if (dailyTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No daily focus routine items configured.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    dailyTasks.forEach { task ->
                        DailyTaskItemRow(
                            task = task,
                            palette = palette,
                            onToggle = { viewModel.toggleDailyTask(task) },
                            onDelete = { viewModel.deleteDailyTask(task.id) }
                        )
                    }
                }
            }

            // Quick Add Input field
            if (!isCompact) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                            .background(palette.background)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        if (newRoutineTitle.isEmpty()) {
                            Text(
                                text = "Add daily routine task (e.g. Read 20m)...",
                                color = palette.textSecondary.copy(alpha = 0.5f),
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }

                        BasicTextField(
                            value = newRoutineTitle,
                            onValueChange = { newRoutineTitle = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_daily_task"),
                            textStyle = TextStyle(
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(palette.textPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newRoutineTitle.isNotBlank()) {
                                        viewModel.addDailyTask(
                                            title = newRoutineTitle,
                                            category = selectedCategory
                                        )
                                        newRoutineTitle = ""
                                        keyboardController?.hide()
                                    }
                                }
                            )
                        )
                    }

                    RetroButton(
                        text = "ADD",
                        onClick = {
                            if (newRoutineTitle.isNotBlank()) {
                                viewModel.addDailyTask(
                                    title = newRoutineTitle,
                                    category = selectedCategory
                                )
                                newRoutineTitle = ""
                                keyboardController?.hide()
                            }
                        },
                        isPrimary = true,
                        enabled = newRoutineTitle.isNotBlank(),
                        testTag = "btn_add_daily_task"
                    )
                }
            }
        }
    }

    // Reset Routine Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "RESET DAILY ROUTINE?",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "This will uncheck all $completedCount completed routine items so you can start a fresh 24-hour cycle.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                RetroButton(
                    text = "RESET 24H CYCLE",
                    onClick = {
                        viewModel.resetDailyTasksManually()
                        showResetDialog = false
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                RetroOutlineButton(
                    text = "CANCEL",
                    onClick = { showResetDialog = false }
                )
            }
        )
    }
}

@Composable
fun DailyTaskItemRow(
    task: DailyTask,
    palette: com.example.ui.theme.RetroPalette,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
            .background(if (task.isCompleted) palette.surface.copy(alpha = 0.5f) else palette.surface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("daily_task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        1.dp,
                        if (task.isCompleted) palette.textPrimary else palette.surfaceBorder,
                        RoundedCornerShape(4.dp)
                    )
                    .background(if (task.isCompleted) palette.textPrimary else palette.background)
                    .clickable { onToggle() }
                    .testTag("daily_task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = palette.background,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) palette.textSecondary.copy(alpha = 0.6f) else palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = palette.textSecondary.copy(alpha = 0.7f),
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Streak Badge (if streak > 0)
            if (task.streakDays > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.surfaceBorder.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔥 ${task.streakDays}d",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Category tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .border(0.5.dp, palette.surfaceBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = task.category,
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 8.sp
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
                    .testTag("btn_delete_daily_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = palette.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
