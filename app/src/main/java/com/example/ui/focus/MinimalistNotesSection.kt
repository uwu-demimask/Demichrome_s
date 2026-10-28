package com.example.ui.focus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.data.MinimalistNote
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.theme.RetroTheme

@Composable
fun MinimalistNotesSection(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val notes by viewModel.minimalistNotes.collectAsState()
    var noteInputText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("IDEA") }
    var isExpanded by remember { mutableStateOf(true) }

    val tags = listOf("IDEA", "TASK", "INSIGHT", "THOUGHT")

    fun submitNote() {
        if (noteInputText.isNotBlank()) {
            viewModel.saveMinimalistNote(noteInputText, selectedTag)
            noteInputText = ""
        }
    }

    RetroCard(
        title = "MINIMALIST NOTES // SESSION SCRATCHPAD",
        badge = if (notes.isNotEmpty()) "${notes.size} LOGGED" else "EMPTY",
        modifier = modifier.testTag("minimalist_notes_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header collapse toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JOT THOUGHTS WITHOUT BREAKING FLOW",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse notes" else "Expand notes",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tag selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            RetroChip(
                                text = "#$tag",
                                isSelected = selectedTag == tag,
                                onClick = {
                                    selectedTag = tag
                                    RetroHaptics.performTick(context)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "chip_note_tag_$tag"
                            )
                        }
                    }

                    // Input field + Submit action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = noteInputText,
                            onValueChange = { noteInputText = it },
                            placeholder = {
                                Text(
                                    text = "Jot down a quick thought or idea...",
                                    color = palette.textSecondary.copy(alpha = 0.6f),
                                    fontFamily = palette.fontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_minimalist_note"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitNote() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.buttonActiveBackground,
                                unfocusedBorderColor = palette.surfaceBorder,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary,
                                cursorColor = palette.textAccent
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )

                        RetroButton(
                            text = "+ JOT",
                            onClick = { submitNote() },
                            isPrimary = true,
                            modifier = Modifier.testTag("btn_save_minimalist_note")
                        )
                    }

                    // Notes list
                    if (notes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.surface.copy(alpha = 0.4f))
                                .border(1.dp, palette.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "> No focus notes logged yet.\n> Capture insights and fleeting thoughts safely in local Room database.",
                                color = palette.textSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            notes.take(8).forEach { note ->
                                MinimalistNoteItem(
                                    note = note,
                                    onDelete = { viewModel.deleteMinimalistNote(note.id) },
                                    onTogglePin = { viewModel.togglePinMinimalistNote(note) },
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Demichrome Note", note.content))
                                        RetroHaptics.performTick(context)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalistNoteItem(
    note: MinimalistNote,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = if (note.isPinned) 1.5.dp else 1.dp,
                color = if (note.isPinned) palette.textAccent else palette.surfaceBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .background(if (note.isPinned) palette.surfaceElevated else palette.surface)
            .padding(10.dp)
            .testTag("minimalist_note_item_${note.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Note meta bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${note.tag}",
                            color = palette.accent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• ${note.getFormattedTime()} (${note.sessionDurationMinutes}M)",
                        color = palette.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp
                    )

                    if (note.isPinned) {
                        Text(
                            text = "★ PINNED",
                            color = palette.textAccent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Action buttons: Copy, Pin, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy note",
                        tint = palette.textSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCopy() }
                    )
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                        tint = if (note.isPinned) palette.textAccent else palette.textSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onTogglePin() }
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = palette.textSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onDelete() }
                    )
                }
            }

            // Note content text
            Text(
                text = note.content,
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 17.sp
            )
        }
    }
}
