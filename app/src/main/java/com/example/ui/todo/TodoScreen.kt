package com.example.ui.todo

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskPriority
import com.example.data.TodoTask
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroHeader
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.tasks.DailyRoutineSection
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.SmoothProgressBar

enum class TaskFilter(val label: String) {
    ACTIVE("ACTIVE"),
    ALL("ALL"),
    COMPLETED("DONE")
}

@Composable
fun TodoScreen(
    viewModel: FocusViewModel,
    onNavigateToFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val keyboardController = LocalSoftwareKeyboardController.current

    val allTasks by viewModel.allTasks.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val activeTaskId by viewModel.activeTaskId.collectAsState()

    var filter by remember { mutableStateOf(TaskFilter.ACTIVE) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FOCUS") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var estimatedSessions by remember { mutableIntStateOf(1) }

    var editingTask by remember { mutableStateOf<TodoTask?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val displayedTasks = when (filter) {
        TaskFilter.ACTIVE -> activeTasks
        TaskFilter.ALL -> allTasks
        TaskFilter.COMPLETED -> completedTasks
    }

    val totalCount = allTasks.size
    val doneCount = completedTasks.size
    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f

    val categories = listOf("FOCUS", "WORK", "STUDY", "CODE", "LIFE")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        RetroHeader(
            title = "OBJECTIVES",
            subtitle = "FOCUS TO-DO REGISTER"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Daily Focus Routine Tracker (24-hour reset)
            item {
                DailyRoutineSection(
                    viewModel = viewModel,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Task Progress Summary Card
            item {
                RetroCard(
                    title = "SESSION TARGET SUMMARY",
                    badge = "$doneCount / $totalCount DONE"
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeTasks.isNotEmpty()) "${activeTasks.size} Active Objectives" else "All Caught Up!",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            if (completedTasks.isNotEmpty()) {
                                RetroOutlineButton(
                                    text = "CLEAR DONE",
                                    onClick = { showClearConfirm = true },
                                    icon = Icons.Default.DeleteSweep,
                                    testTag = "btn_clear_completed_tasks"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SmoothProgressBar(progress = progress, palette = palette)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(progress * 100).toInt()}% of registered objectives completed",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick Add Task Form
            item {
                RetroCard(
                    title = "REGISTER NEW OBJECTIVE",
                    badge = "+ NEW"
                ) {
                    Column {
                        // Text input field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                                .background(palette.background)
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            if (newTaskTitle.isEmpty()) {
                                Text(
                                    text = "Enter a task to focus on...",
                                    color = palette.textSecondary.copy(alpha = 0.5f),
                                    fontFamily = palette.fontFamily,
                                    fontSize = 13.sp
                                )
                            }

                            BasicTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_task_title"),
                                textStyle = TextStyle(
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(palette.textPrimary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newTaskTitle.isNotBlank()) {
                                            viewModel.addTask(
                                                title = newTaskTitle,
                                                priority = selectedPriority,
                                                estimatedSessions = estimatedSessions,
                                                category = selectedCategory
                                            )
                                            newTaskTitle = ""
                                            keyboardController?.hide()
                                        }
                                    }
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Chips
                        Text(
                            text = "CATEGORY:",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            1.dp,
                                            if (isSelected) palette.textPrimary else palette.surfaceBorder,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(if (isSelected) palette.surfaceBorder.copy(alpha = 0.4f) else palette.background)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) palette.textPrimary else palette.textSecondary,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Priority & Est. Pomodoros Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority Selector
                            Column {
                                Text(
                                    text = "PRIORITY:",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TaskPriority.values().forEach { prio ->
                                        val isSel = selectedPriority == prio
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSel) palette.buttonActiveBackground else palette.surfaceBorder.copy(alpha = 0.2f))
                                                .clickable { selectedPriority = prio }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = prio.label,
                                                color = if (isSel) palette.buttonActiveText else palette.textSecondary,
                                                fontFamily = palette.fontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            // Est. Sessions Stepper
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "EST. POMOS:",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(palette.surfaceBorder.copy(alpha = 0.3f))
                                            .clickable { if (estimatedSessions > 1) estimatedSessions-- },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Text(
                                        text = "$estimatedSessions 🎯",
                                        color = palette.textPrimary,
                                        fontFamily = palette.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(palette.surfaceBorder.copy(alpha = 0.3f))
                                            .clickable { if (estimatedSessions < 10) estimatedSessions++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        RetroButton(
                            text = "ADD OBJECTIVE",
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTask(
                                        title = newTaskTitle,
                                        priority = selectedPriority,
                                        estimatedSessions = estimatedSessions,
                                        category = selectedCategory
                                    )
                                    newTaskTitle = ""
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Add,
                            isPrimary = true,
                            enabled = newTaskTitle.isNotBlank(),
                            testTag = "btn_submit_new_task"
                        )
                    }
                }
            }

            // Filter Tabs Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.values().forEach { tab ->
                        val isSelected = filter == tab
                        val count = when (tab) {
                            TaskFilter.ACTIVE -> activeTasks.size
                            TaskFilter.ALL -> allTasks.size
                            TaskFilter.COMPLETED -> completedTasks.size
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) palette.textPrimary else palette.surfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (isSelected) palette.surfaceBorder.copy(alpha = 0.35f) else palette.surface
                                )
                                .clickable { filter = tab }
                                .testTag("filter_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tab.label} ($count)",
                                color = if (isSelected) palette.textPrimary else palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Empty State
            if (displayedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (filter) {
                                    TaskFilter.ACTIVE -> "NO ACTIVE OBJECTIVES"
                                    TaskFilter.COMPLETED -> "NO COMPLETED TASKS YET"
                                    TaskFilter.ALL -> "REGISTER IS EMPTY"
                                },
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (filter) {
                                    TaskFilter.ACTIVE -> "Add a task above to anchor your focus timer."
                                    TaskFilter.COMPLETED -> "Finish focus blocks to check off objectives."
                                    TaskFilter.ALL -> "Type your first objective above."
                                },
                                color = palette.textSecondary.copy(alpha = 0.7f),
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Task List
            items(displayedTasks, key = { it.id }) { task ->
                val isSelectedForTimer = activeTaskId == task.id

                TaskItemCard(
                    task = task,
                    isTargeted = isSelectedForTimer,
                    palette = palette,
                    onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                    onSelectForTimer = {
                        if (isSelectedForTimer) {
                            viewModel.selectTaskForFocus(null)
                        } else {
                            viewModel.selectTaskForFocus(task.id)
                            onNavigateToFocus()
                        }
                    },
                    onEdit = { editingTask = task },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Edit Task Dialog
    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            palette = palette,
            categories = categories,
            onDismiss = { editingTask = null },
            onSave = { updated ->
                viewModel.updateTask(updated)
                editingTask = null
            }
        )
    }

    // Clear Completed Confirm Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "CLEAR COMPLETED TASKS?",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "This will remove all ${completedTasks.size} completed objectives from the register.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                RetroButton(
                    text = "CLEAR ALL",
                    onClick = {
                        viewModel.clearCompletedTasks()
                        showClearConfirm = false
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                RetroOutlineButton(
                    text = "CANCEL",
                    onClick = { showClearConfirm = false }
                )
            }
        )
    }
}

@Composable
private fun TaskItemCard(
    task: TodoTask,
    isTargeted: Boolean,
    palette: com.example.ui.theme.RetroPalette,
    onToggleComplete: () -> Unit,
    onSelectForTimer: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isTargeted) palette.textAccent else palette.surfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .background(palette.surface, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        1.dp,
                        if (task.isCompleted) palette.textPrimary else palette.surfaceBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (task.isCompleted) palette.textPrimary else palette.background
                    )
                    .clickable { onToggleComplete() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = palette.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    color = if (task.isCompleted) palette.textSecondary.copy(alpha = 0.6f) else palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags & stats row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.surfaceBorder.copy(alpha = 0.4f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Priority Tag
                    val prioColor = when (task.priority) {
                        TaskPriority.URGENT -> palette.textAccent
                        TaskPriority.HIGH -> palette.textPrimary
                        TaskPriority.MEDIUM -> palette.textSecondary
                        TaskPriority.LOW -> palette.textSecondary.copy(alpha = 0.6f)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .border(0.5.dp, prioColor, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = task.priority.label,
                            color = prioColor,
                            fontFamily = palette.fontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pomodoro Progress Dots
                    Text(
                        text = "🎯 ${task.completedSessions}/${task.estimatedSessions}",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp
                    )

                    if (isTargeted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(palette.buttonActiveBackground)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE TARGET",
                                color = palette.buttonActiveText,
                                fontFamily = palette.fontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (!task.isCompleted) {
                    IconButton(
                        onClick = onSelectForTimer,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_target_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = if (isTargeted) "Clear target" else "Focus on this",
                            tint = if (isTargeted) palette.textAccent else palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = palette.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTaskDialog(
    task: TodoTask,
    palette: com.example.ui.theme.RetroPalette,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (TodoTask) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes) }
    var priority by remember { mutableStateOf(task.priority) }
    var category by remember { mutableStateOf(task.category) }
    var estSessions by remember { mutableIntStateOf(task.estimatedSessions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(
                text = "EDIT OBJECTIVE",
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Title Field
                Column {
                    Text("TITLE:", color = palette.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.background)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(color = palette.textPrimary, fontFamily = palette.fontFamily, fontSize = 12.sp),
                            cursorBrush = SolidColor(palette.textPrimary),
                            singleLine = true
                        )
                    }
                }

                // Notes Field
                Column {
                    Text("NOTES / SUBTEXT:", color = palette.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                            .background(palette.background)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            textStyle = TextStyle(color = palette.textPrimary, fontFamily = palette.fontFamily, fontSize = 12.sp),
                            cursorBrush = SolidColor(palette.textPrimary)
                        )
                    }
                }

                // Category selection
                Column {
                    Text("CATEGORY:", color = palette.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) palette.buttonActiveBackground else palette.surfaceBorder.copy(alpha = 0.2f))
                                    .clickable { category = cat }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) palette.buttonActiveText else palette.textSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text("PRIORITY:", color = palette.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TaskPriority.values().forEach { prio ->
                            val isSel = priority == prio
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) palette.buttonActiveBackground else palette.surfaceBorder.copy(alpha = 0.2f))
                                    .clickable { priority = prio }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = prio.label,
                                    color = if (isSel) palette.buttonActiveText else palette.textSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            RetroButton(
                text = "SAVE CHANGES",
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task.copy(
                                title = title.trim(),
                                notes = notes.trim(),
                                priority = priority,
                                category = category,
                                estimatedSessions = estSessions
                            )
                        )
                    }
                },
                isPrimary = true
            )
        },
        dismissButton = {
            RetroOutlineButton(
                text = "CANCEL",
                onClick = onDismiss
            )
        }
    )
}
