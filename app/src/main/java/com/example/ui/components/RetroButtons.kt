package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.ui.theme.RetroTheme

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = "retro_button",
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        !enabled -> palette.surface.copy(alpha = 0.4f)
        isPressed || isPrimary -> palette.buttonActiveBackground
        else -> palette.surface
    }

    val contentColor = when {
        !enabled -> palette.textSecondary.copy(alpha = 0.4f)
        isPressed || isPrimary -> palette.buttonActiveText
        else -> palette.textPrimary
    }

    val borderColor = when {
        !enabled -> palette.surfaceBorder.copy(alpha = 0.3f)
        isPrimary -> palette.buttonActiveBackground
        else -> palette.surfaceBorder
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp)
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(backgroundColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button
            ) {
                RetroHaptics.performClick(context)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RetroOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "retro_outline_button",
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        !enabled -> palette.surface.copy(alpha = 0.3f)
        isPressed -> palette.surfaceBorder.copy(alpha = 0.5f)
        else -> palette.surface.copy(alpha = 0.6f)
    }

    val contentColor = when {
        !enabled -> palette.textSecondary.copy(alpha = 0.4f)
        else -> palette.textPrimary
    }

    val borderColor = when {
        !enabled -> palette.surfaceBorder.copy(alpha = 0.3f)
        isPressed -> palette.buttonBorder
        else -> palette.surfaceBorder
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 88.dp, minHeight = 44.dp)
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(backgroundColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button
            ) {
                RetroHaptics.performClick(context)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RetroChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "retro_chip",
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val backgroundColor = if (isSelected) palette.buttonActiveBackground else palette.surface
    val contentColor = if (isSelected) palette.buttonActiveText else palette.textPrimary
    val borderColor = if (isSelected) palette.buttonActiveBackground else palette.surfaceBorder

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 52.dp, minHeight = 48.dp)
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(backgroundColor, shape)
            .clickable(role = Role.RadioButton) {
                RetroHaptics.performClick(context)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontFamily = palette.fontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RetroIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "retro_icon_button",
    isSelected: Boolean = false,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val backgroundColor = if (isSelected) palette.buttonActiveBackground else palette.surface
    val iconColor = if (isSelected) palette.buttonActiveText else palette.textPrimary
    val borderColor = if (isSelected) palette.buttonActiveBackground else palette.surfaceBorder

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .clickable(role = Role.Button) {
                RetroHaptics.performClick(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun RetroToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    testTag: String = "retro_toggle"
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    Row(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Switch) {
                RetroHaptics.performClick(context)
                onCheckedChange(!checked)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Text(
                    text = label,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                if (description != null) {
                    Text(
                        text = description,
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .border(1.dp, if (checked) palette.buttonActiveBackground else palette.surfaceBorder, RoundedCornerShape(4.dp))
                .background(if (checked) palette.buttonActiveBackground else palette.surface, RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (checked) "ON" else "OFF",
                color = if (checked) palette.buttonActiveText else palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}
