package com.example.ui.money

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.Expense
import com.example.ui.components.DemichromeGlassButton
import com.example.ui.components.DemichromeGlassCard
import com.example.ui.components.DemichromeGlassDefaults
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Supported Standard Categories
val EXPENSE_CATEGORIES = listOf(
    "FOOD / MESS",
    "TRAVEL",
    "STUDY / BOOKS",
    "RENT / HOSTEL",
    "SHOPPING",
    "ENTERTAINMENT",
    "OTHER"
)

val PAYMENT_MODES = listOf("UPI", "CASH", "CARD")

fun getCategoryIcon(cat: String): ImageVector {
    val upper = cat.uppercase()
    return when {
        upper.contains("FOOD") || upper.contains("MESS") -> Icons.Default.Restaurant
        upper.contains("TRAVEL") -> Icons.Default.DirectionsCar
        upper.contains("STUDY") || upper.contains("BOOK") -> Icons.Default.Book
        upper.contains("RENT") || upper.contains("HOSTEL") -> Icons.Default.Home
        upper.contains("SHOP") -> Icons.Default.ShoppingBag
        upper.contains("ENTERTAIN") || upper.contains("MOVIE") -> Icons.Default.Movie
        else -> Icons.Default.AttachMoney
    }
}

@Composable
fun DemichromeMoneyScreen(viewModel: FocusViewModel) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val expenses by viewModel.allExpenses.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val todaySpent by viewModel.todaySpent.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    var selectedFilterTab by remember { mutableStateOf("THIS MONTH") } // ALL, THIS MONTH, TODAY

    // Filter expenses
    val filteredExpenses = remember(expenses, selectedFilterTab) {
        val cal = Calendar.getInstance()
        val curYear = cal.get(Calendar.YEAR)
        val curMonth = cal.get(Calendar.MONTH)
        val curDay = cal.get(Calendar.DAY_OF_YEAR)

        when (selectedFilterTab) {
            "TODAY" -> expenses.filter {
                val itemCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                itemCal.get(Calendar.YEAR) == curYear && itemCal.get(Calendar.DAY_OF_YEAR) == curDay
            }
            "THIS MONTH" -> expenses.filter {
                val itemCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                itemCal.get(Calendar.YEAR) == curYear && itemCal.get(Calendar.MONTH) == curMonth
            }
            else -> expenses
        }
    }

    // Month Total Calculations
    val monthExpenses = remember(expenses) {
        val cal = Calendar.getInstance()
        val curYear = cal.get(Calendar.YEAR)
        val curMonth = cal.get(Calendar.MONTH)
        expenses.filter {
            val itemCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            itemCal.get(Calendar.YEAR) == curYear && itemCal.get(Calendar.MONTH) == curMonth
        }
    }

    val totalSpent = monthExpenses.sumOf { it.amount }
    val remaining = monthlyBudget - totalSpent
    val percentUsed = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0

    // Average daily spend
    val daysInMonthPassed = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    val avgDailySpend = totalSpent / daysInMonthPassed

    // Category breakdown
    val categoryTotals = remember(monthExpenses) {
        val map = mutableMapOf<String, Double>()
        for (e in monthExpenses) {
            map[e.category] = (map[e.category] ?: 0.0) + e.amount
        }
        map.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("money_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXPENSE LEDGER",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "PERSONAL / HOSTEL BUDGET OS",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemichromeGlassButton(
                    text = "BUDGET",
                    onClick = {
                        RetroHaptics.performClick(context)
                        showBudgetDialog = true
                    },
                    palette = palette,
                    fontSize = 10.sp,
                    cornerRadius = 8.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                )

                DemichromeGlassButton(
                    text = "+ ADD",
                    onClick = {
                        RetroHaptics.performClick(context)
                        showAddDialog = true
                    },
                    palette = palette,
                    isPrimary = true,
                    fontSize = 10.sp,
                    cornerRadius = 8.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // BUDGET WARNING BANNER (50%, 75%, 90%, 100%+)
            // ==========================================
            if (percentUsed >= 50 && monthlyBudget > 0) {
                item {
                    val (warningText, warningColor) = when {
                        percentUsed >= 100 -> Pair("⚠️ CRITICAL: BUDGET EXCEEDED BY ₹${(totalSpent - monthlyBudget).toInt()}", Color(0xFFFF5252))
                        percentUsed >= 90 -> Pair("⚠️ WARNING: 90% BUDGET REACHED! ONLY ₹${remaining.toInt()} LEFT", Color(0xFFFF7043))
                        percentUsed >= 75 -> Pair("⚡ NOTICE: 75% OF MONTHLY BUDGET CONSUMED", Color(0xFFFFB74D))
                        else -> Pair("ℹ️ INFO: 50% OF MONTHLY BUDGET REACHED", palette.textAccent)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, warningColor, RoundedCornerShape(8.dp))
                            .background(warningColor.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = warningColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warningText,
                                color = warningColor,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // DASHBOARD METRICS (A & D)
            // ==========================================
            item {
                DemichromeGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    palette = palette,
                    cornerRadius = 14.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: Budget vs Remaining vs Total Spent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL BUDGET",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${monthlyBudget.toInt()}",
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SPENT SO FAR",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${totalSpent.toInt()}",
                                    color = palette.textAccent,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "REMAINING",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${remaining.toInt()}",
                                    color = if (remaining >= 0) palette.textPrimary else Color(0xFFFF5252),
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        // Progress Bar & Percentage
                        val progressFrac = if (monthlyBudget > 0) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BUDGET UTILIZATION",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "$percentUsed%",
                                    color = if (percentUsed > 100) Color(0xFFFF5252) else palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressFrac },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (percentUsed >= 100) Color(0xFFFF5252) else palette.textAccent,
                                trackColor = palette.surfaceBorder.copy(alpha = 0.3f)
                            )
                        }

                        // Secondary Row: Spent Today & Average Daily Spend
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, palette.surfaceBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .background(palette.surfaceBorder.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SPENT TODAY: ₹${todaySpent.toInt()}",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "AVG DAILY: ₹${avgDailySpend.toInt()}/DAY",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SPENDING BREAKDOWN BY CATEGORY (E)
            // ==========================================
            if (categoryTotals.isNotEmpty()) {
                item {
                    DemichromeGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        palette = palette,
                        cornerRadius = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SPENDING BREAKDOWN",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )

                            categoryTotals.forEach { (cat, amt) ->
                                val catPercent = if (totalSpent > 0) ((amt / totalSpent) * 100).toInt() else 0
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIcon(cat),
                                                contentDescription = null,
                                                tint = palette.textAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = cat,
                                                color = palette.textPrimary,
                                                fontFamily = palette.fontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Text(
                                            text = "₹${amt.toInt()} ($catPercent%)",
                                            color = palette.textPrimary,
                                            fontFamily = palette.fontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { (catPercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = palette.textPrimary.copy(alpha = 0.8f),
                                        trackColor = palette.surfaceBorder.copy(alpha = 0.25f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // FILTER TABS (THIS MONTH / TODAY / ALL)
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("THIS MONTH", "TODAY", "ALL").forEach { tab ->
                        val isSelected = selectedFilterTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) palette.textAccent else palette.surfaceBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) palette.buttonActiveBackground else palette.buttonBackground)
                                .clickable {
                                    RetroHaptics.performClick(context)
                                    selectedFilterTab = tab
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) palette.buttonActiveText else palette.buttonText,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TRANSACTION HISTORY LIST (C)
            // ==========================================
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO TRANSACTIONS LOGGED YET",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseTransactionCard(
                        expense = expense,
                        onEdit = { expenseToEdit = expense },
                        onDelete = { expenseToDelete = expense }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // ==========================================
    // DIALOG: SET CUSTOM BUDGET (A)
    // ==========================================
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(monthlyBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "SET MONTHLY BUDGET",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your monthly allowance or expense limit:",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Budget Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            focusedBorderColor = palette.textAccent,
                            unfocusedBorderColor = palette.surfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5000, 10000, 15000, 20000).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { budgetInput = preset.toString() }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${preset / 1000}k",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = budgetInput.toDoubleOrNull()
                        if (amt != null && amt >= 0) {
                            viewModel.setMonthlyBudget(amt)
                            showBudgetDialog = false
                        }
                    }
                ) {
                    Text("SAVE", color = palette.textAccent, fontFamily = palette.fontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("CANCEL", color = palette.textSecondary, fontFamily = palette.fontFamily)
                }
            }
        )
    }

    // ==========================================
    // DIALOG: ADD EXPENSE (B)
    // ==========================================
    if (showAddDialog) {
        ExpenseEditorDialog(
            title = "ADD NEW EXPENSE",
            initialAmount = "",
            initialCategory = "FOOD / MESS",
            initialNote = "",
            initialPaymentMode = "UPI",
            onDismiss = { showAddDialog = false },
            onSave = { amt, cat, note, mode ->
                viewModel.addExpense(amt, cat, note, mode)
                showAddDialog = false
            }
        )
    }

    // ==========================================
    // DIALOG: EDIT EXPENSE (C)
    // ==========================================
    expenseToEdit?.let { exp ->
        ExpenseEditorDialog(
            title = "EDIT EXPENSE",
            initialAmount = exp.amount.toInt().toString(),
            initialCategory = exp.category,
            initialNote = exp.note,
            initialPaymentMode = exp.paymentMode,
            onDismiss = { expenseToEdit = null },
            onSave = { amt, cat, note, mode ->
                viewModel.updateExpense(
                    exp.copy(
                        amount = amt,
                        category = cat,
                        note = note,
                        paymentMode = mode
                    )
                )
                expenseToEdit = null
            }
        )
    }

    // ==========================================
    // DIALOG: DELETE CONFIRMATION (C)
    // ==========================================
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "DELETE EXPENSE",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "Remove transaction ₹${exp.amount.toInt()} (${exp.category})?",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(exp)
                        expenseToDelete = null
                    }
                ) {
                    Text("DELETE", color = Color(0xFFFF5252), fontFamily = palette.fontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("CANCEL", color = palette.textSecondary, fontFamily = palette.fontFamily)
                }
            }
        )
    }
}

/**
 * Individual Transaction Card in Ledger
 */
@Composable
fun ExpenseTransactionCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = RetroTheme.palette
    val formattedDate = remember(expense.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(expense.timestamp))
    }

    DemichromeGlassCard(
        modifier = Modifier.fillMaxWidth(),
        palette = palette,
        cornerRadius = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceBorder.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = null,
                        tint = palette.textAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (expense.note.isNotBlank()) expense.note else expense.category,
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${expense.category} • $formattedDate",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp
                        )

                        // Payment mode badge
                        Box(
                            modifier = Modifier
                                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = expense.paymentMode,
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${expense.amount.toInt()}",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = palette.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = palette.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Full Add/Edit Expense Dialog (B & C)
 */
@Composable
fun ExpenseEditorDialog(
    title: String,
    initialAmount: String,
    initialCategory: String,
    initialNote: String,
    initialPaymentMode: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, category: String, note: String, paymentMode: String) -> Unit
) {
    val palette = RetroTheme.palette
    var amountText by remember { mutableStateOf(initialAmount) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var noteText by remember { mutableStateOf(initialNote) }
    var selectedPaymentMode by remember { mutableStateOf(initialPaymentMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(
                text = title,
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.textAccent,
                        unfocusedBorderColor = palette.surfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector
                Text(
                    text = "CATEGORY:",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val rows = EXPENSE_CATEGORIES.chunked(3)
                    rows.forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowCats.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            1.dp,
                                            if (isSelected) palette.textAccent else palette.surfaceBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .background(if (isSelected) palette.buttonActiveBackground else palette.buttonBackground)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.replace(" / ", "/"),
                                        color = if (isSelected) palette.buttonActiveText else palette.buttonText,
                                        fontFamily = palette.fontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note / Place (e.g. Canteen)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.textAccent,
                        unfocusedBorderColor = palette.surfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Mode Selector
                Text(
                    text = "PAYMENT MODE:",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PAYMENT_MODES.forEach { mode ->
                        val isSelected = selectedPaymentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) palette.textAccent else palette.surfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .background(if (isSelected) palette.buttonActiveBackground else palette.buttonBackground)
                                .clickable { selectedPaymentMode = mode }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) palette.buttonActiveText else palette.buttonText,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onSave(amt, selectedCategory, noteText, selectedPaymentMode)
                    }
                }
            ) {
                Text("SAVE", color = palette.textAccent, fontFamily = palette.fontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = palette.textSecondary, fontFamily = palette.fontFamily)
            }
        }
    )
}
