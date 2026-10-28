package com.example.ui.settings

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.haptics.RetroHaptics
import com.example.core.overlay.SystemNotchOverlayManager
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.theme.RetroTheme
import java.util.Locale

/**
 * Interactive Calibration Dialog for Dynamic Island Mini-Player.
 * Allows manual precision tuning of X/Y positioning and Width/Height scaling
 * with live real-time phone cutout preview.
 */
@Composable
fun DynamicIslandCalibrationDialog(
    initialOffsetX: Int,
    initialOffsetY: Int,
    initialWidthScale: Float,
    initialHeightScale: Float,
    onSave: (offsetX: Int, offsetY: Int, widthScale: Float, heightScale: Float) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val hasPermission = remember(context) { SystemNotchOverlayManager.hasOverlayPermission(context) }

    var currentOffsetX by remember { mutableIntStateOf(initialOffsetX) }
    var currentOffsetY by remember { mutableIntStateOf(initialOffsetY) }
    var currentWidthScale by remember { mutableFloatStateOf(initialWidthScale) }
    var currentHeightScale by remember { mutableFloatStateOf(initialHeightScale) }

    androidx.compose.runtime.LaunchedEffect(currentOffsetX, currentOffsetY, currentWidthScale, currentHeightScale) {
        SystemNotchOverlayManager.updateCalibration(
            context,
            currentOffsetX,
            currentOffsetY,
            currentWidthScale,
            currentHeightScale
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 500.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(16.dp))
                .testTag("dynamic_island_calibration_dialog"),
            color = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DYNAMIC ISLAND // CALIBRATION",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Fine-tune cutout positioning & HUD scale",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!hasPermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surfaceElevated)
                            .border(1.dp, palette.accent, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .testTag("calibration_overlay_permission_banner")
                    ) {
                        Text(
                            text = "⚠ SYSTEM OVERLAY PERMISSION REQUIRED",
                            color = palette.accent,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "To enable the live system-wide Dynamic Island overlay over other applications, grant 'Display over other apps' permission.",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RetroButton(
                            text = "GRANT OVERLAY PERMISSION",
                            onClick = { SystemNotchOverlayManager.openOverlaySettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_grant_overlay_calibration"
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // LIVE PHONE PREVIEW FRAME
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(14.dp))
                        .testTag("calibration_live_preview_frame"),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Simulated status bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "09:41",
                            color = palette.textSecondary.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "100%",
                            color = palette.textSecondary.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }

                    // Simulated Camera Cutout (Punch-hole)
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0A0C))
                            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.5f), CircleShape)
                            .testTag("preview_camera_cutout")
                    )

                    // Live Calibrated Dynamic Island Mini-Player
                    val previewTopOffset = (28 + (currentOffsetY * 0.45f).toInt()).coerceIn(24, 85)
                    val previewLeftOffset = (currentOffsetX * 0.45f).toInt().coerceIn(-60, 60)

                    Box(
                        modifier = Modifier
                            .offset(x = previewLeftOffset.dp, y = previewTopOffset.dp)
                            .size(
                                width = (112 * currentWidthScale).dp,
                                height = (28 * currentHeightScale).dp
                            )
                            .shadow(4.dp, RoundedCornerShape(percent = 50))
                            .clip(RoundedCornerShape(percent = 50))
                            .background(palette.surface)
                            .border(1.dp, palette.accent, RoundedCornerShape(percent = 50))
                            .padding(horizontal = 8.dp)
                            .testTag("preview_island_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(9.dp)
                            )
                            Text(
                                text = "24:43",
                                color = palette.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = (10 * currentHeightScale).coerceIn(8f, 13f).sp
                            )
                            Text(
                                text = "[RUN]",
                                color = palette.accent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = (8 * currentHeightScale).coerceIn(7f, 10f).sp
                            )
                        }
                    }

                    // Bottom info label
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "X: %+ddp  Y: %+ddp  W: %.2fX  H: %.2fX",
                                currentOffsetX,
                                currentOffsetY,
                                currentWidthScale,
                                currentHeightScale
                            ),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CALIBRATION CONTROL: X OFFSET
                CalibrationControlRow(
                    label = "X HORIZONTAL OFFSET",
                    valueDisplay = String.format(Locale.getDefault(), "%+d dp", currentOffsetX),
                    sliderValue = currentOffsetX.toFloat(),
                    valueRange = -60f..60f,
                    onMinusClick = {
                        currentOffsetX = (currentOffsetX - 4).coerceAtLeast(-60)
                        RetroHaptics.performTick(context)
                    },
                    onPlusClick = {
                        currentOffsetX = (currentOffsetX + 4).coerceAtMost(60)
                        RetroHaptics.performTick(context)
                    },
                    onSliderChange = {
                        currentOffsetX = it.toInt()
                    },
                    testTagPrefix = "calib_offset_x"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CALIBRATION CONTROL: Y OFFSET
                CalibrationControlRow(
                    label = "Y VERTICAL OFFSET",
                    valueDisplay = String.format(Locale.getDefault(), "%+d dp", currentOffsetY),
                    sliderValue = currentOffsetY.toFloat(),
                    valueRange = -30f..80f,
                    onMinusClick = {
                        currentOffsetY = (currentOffsetY - 4).coerceAtLeast(-30)
                        RetroHaptics.performTick(context)
                    },
                    onPlusClick = {
                        currentOffsetY = (currentOffsetY + 4).coerceAtMost(80)
                        RetroHaptics.performTick(context)
                    },
                    onSliderChange = {
                        currentOffsetY = it.toInt()
                    },
                    testTagPrefix = "calib_offset_y"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CALIBRATION CONTROL: WIDTH SCALE
                CalibrationControlRow(
                    label = "WIDTH SCALE",
                    valueDisplay = String.format(Locale.getDefault(), "%.2fX", currentWidthScale),
                    sliderValue = currentWidthScale,
                    valueRange = 0.70f..1.40f,
                    onMinusClick = {
                        currentWidthScale = ((currentWidthScale - 0.05f) * 100).toInt() / 100f
                        currentWidthScale = currentWidthScale.coerceAtLeast(0.70f)
                        RetroHaptics.performTick(context)
                    },
                    onPlusClick = {
                        currentWidthScale = ((currentWidthScale + 0.05f) * 100).toInt() / 100f
                        currentWidthScale = currentWidthScale.coerceAtMost(1.40f)
                        RetroHaptics.performTick(context)
                    },
                    onSliderChange = {
                        currentWidthScale = ((it * 100).toInt()) / 100f
                    },
                    testTagPrefix = "calib_scale_w"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CALIBRATION CONTROL: HEIGHT SCALE
                CalibrationControlRow(
                    label = "HEIGHT SCALE",
                    valueDisplay = String.format(Locale.getDefault(), "%.2fX", currentHeightScale),
                    sliderValue = currentHeightScale,
                    valueRange = 0.70f..1.40f,
                    onMinusClick = {
                        currentHeightScale = ((currentHeightScale - 0.05f) * 100).toInt() / 100f
                        currentHeightScale = currentHeightScale.coerceAtLeast(0.70f)
                        RetroHaptics.performTick(context)
                    },
                    onPlusClick = {
                        currentHeightScale = ((currentHeightScale + 0.05f) * 100).toInt() / 100f
                        currentHeightScale = currentHeightScale.coerceAtMost(1.40f)
                        RetroHaptics.performTick(context)
                    },
                    onSliderChange = {
                        currentHeightScale = ((it * 100).toInt()) / 100f
                    },
                    testTagPrefix = "calib_scale_h"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroOutlineButton(
                        text = "RESET",
                        onClick = {
                            currentOffsetX = 0
                            currentOffsetY = 0
                            currentWidthScale = 1.0f
                            currentHeightScale = 1.0f
                            onResetDefault()
                            RetroHaptics.performTick(context)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calib_reset_btn")
                    )

                    RetroButton(
                        text = "SAVE & APPLY",
                        onClick = {
                            onSave(currentOffsetX, currentOffsetY, currentWidthScale, currentHeightScale)
                            RetroHaptics.performClick(context)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("calib_save_btn")
                    )
                }
            }
        }
    }
}

/**
 * Reusable row for calibration adjustments with fine-tuning minus/plus buttons and slider.
 */
@Composable
private fun CalibrationControlRow(
    label: String,
    valueDisplay: String,
    sliderValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    onSliderChange: (Float) -> Unit,
    testTagPrefix: String
) {
    val palette = RetroTheme.palette

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueDisplay,
                color = palette.accent,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Minus button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.surfaceElevated)
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                    .clickable { onMinusClick() }
                    .testTag("${testTagPrefix}_minus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Slider
            Slider(
                value = sliderValue.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onSliderChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = palette.accent,
                    activeTrackColor = palette.accent,
                    inactiveTrackColor = palette.surfaceBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTagPrefix}_slider")
            )

            // Plus button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.surfaceElevated)
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                    .clickable { onPlusClick() }
                    .testTag("${testTagPrefix}_plus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
