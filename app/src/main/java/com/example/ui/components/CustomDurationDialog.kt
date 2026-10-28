package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RetroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDurationDialog(
    initialMinutes: Int = 30,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val palette = RetroTheme.palette
    var selectedMinutes by remember { mutableIntStateOf(initialMinutes.coerceIn(1, 180)) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .testTag("custom_duration_dialog")
                .fillMaxWidth(0.9f)
                .border(2.dp, palette.buttonBorder, RectangleShape)
                .background(palette.background, RectangleShape)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SET DURATION",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "CUSTOM FOCUS SESSION",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Number display
                Box(
                    modifier = Modifier
                        .border(1.dp, palette.surfaceBorder, RectangleShape)
                        .background(palette.surface, RectangleShape)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "$selectedMinutes MIN",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Adjustment buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroButton(
                        text = "-10",
                        onClick = { selectedMinutes = (selectedMinutes - 10).coerceAtLeast(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "btn_minus_10"
                    )
                    RetroButton(
                        text = "-5",
                        onClick = { selectedMinutes = (selectedMinutes - 5).coerceAtLeast(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "btn_minus_5"
                    )
                    RetroButton(
                        text = "+5",
                        onClick = { selectedMinutes = (selectedMinutes + 5).coerceAtMost(180) },
                        modifier = Modifier.weight(1f),
                        testTag = "btn_plus_5"
                    )
                    RetroButton(
                        text = "+10",
                        onClick = { selectedMinutes = (selectedMinutes + 10).coerceAtMost(180) },
                        modifier = Modifier.weight(1f),
                        testTag = "btn_plus_10"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetroButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_cancel_duration"
                    )
                    RetroButton(
                        text = "ACCEPT",
                        onClick = { onConfirm(selectedMinutes) },
                        isPrimary = true,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_accept_duration"
                    )
                }
            }
        }
    }
}
