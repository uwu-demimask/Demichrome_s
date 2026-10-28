package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.haptics.RetroHaptics
import com.example.data.FocusSession
import com.example.ui.theme.RetroTheme
import java.util.Locale

@Composable
fun SessionRatingDialog(
    session: FocusSession,
    onSaveRating: (rating: Int, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    var selectedRating by remember { mutableIntStateOf(5) }
    var sessionNote by remember { mutableStateOf("") }

    val durationMinutes = session.targetDurationMinutes
    val actualMins = session.actualElapsedSeconds / 60
    val formattedDuration = String.format(Locale.getDefault(), "%02d:00 FOCUSED", durationMinutes)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, palette.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .background(palette.background)
                .padding(22.dp)
                .testTag("session_rating_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header ASCII / Title
                Text(
                    text = "┌──────────────────────────┐\n│     SESSION COMPLETE     │\n└──────────────────────────┘",
                    color = palette.textAccent,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                Text(
                    text = formattedDuration,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "How was your focus session quality?",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // 1 to 5 Star Rating Bubble Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val isSelected = selectedRating == star
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) palette.textPrimary else palette.surfaceBorder.copy(alpha = 0.4f),
                                    RoundedCornerShape(14.dp)
                                )
                                .background(
                                    if (isSelected) palette.textPrimary.copy(alpha = 0.15f) else palette.surface.copy(alpha = 0.6f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    RetroHaptics.performClick(context)
                                    selectedRating = star
                                }
                                .testTag("btn_rating_$star"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$star",
                                    color = if (isSelected) palette.textPrimary else palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "★",
                                    color = if (isSelected) palette.textPrimary else palette.textSecondary.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Rating descriptor label
                val ratingLabel = when (selectedRating) {
                    1 -> "1 / 5 • High Distraction / Low Focus"
                    2 -> "2 / 5 • Difficult / Fractured"
                    3 -> "3 / 5 • Moderate Progress"
                    4 -> "4 / 5 • Solid Concentrated Work"
                    5 -> "5 / 5 • Deep Flow State"
                    else -> "$selectedRating / 5"
                }

                Text(
                    text = ratingLabel,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                // Optional Note Input Field
                OutlinedTextField(
                    value = sessionNote,
                    onValueChange = { sessionNote = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_session_note"),
                    placeholder = {
                        Text(
                            text = "Add note (e.g. Completed bio chapter 4)...",
                            color = palette.textSecondary.copy(alpha = 0.5f),
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.textPrimary,
                        unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        cursorColor = palette.textPrimary
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DemichromeGlassButton(
                        text = "SKIP",
                        onClick = onDismiss,
                        palette = palette,
                        isPrimary = false,
                        cornerRadius = 14.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    )

                    DemichromeGlassButton(
                        text = "SAVE RATING",
                        onClick = {
                            onSaveRating(selectedRating, sessionNote)
                        },
                        palette = palette,
                        isPrimary = true,
                        cornerRadius = 14.dp,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                    )
                }
            }
        }
    }
}
