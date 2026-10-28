package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.haptics.RetroHaptics
import com.example.ui.theme.RetroTheme

@Composable
fun RetroHeader(
    title: String = "DEMICHROME",
    subtitle: String = "> SYSTEM READY",
    onSecretUnlockVault: () -> Unit = {},
    onQuickToggleMonochrome: (() -> Unit)? = null,
    onOpenGuide: (() -> Unit)? = null,
    onEnterPip: (() -> Unit)? = null,
    isMonochromeActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    // 3-tap secret trigger
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .testTag("app_header_title_box")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime < 600) {
                        tapCount++
                        if (tapCount >= 3) {
                            RetroHaptics.performHeavyClick(context)
                            tapCount = 0
                            onSecretUnlockVault()
                        } else {
                            RetroHaptics.performClick(context)
                        }
                    } else {
                        tapCount = 1
                    }
                    lastTapTime = now
                }
        ) {
            Text(
                text = title,
                color = palette.textPrimary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = palette.textSecondary,
                fontFamily = palette.fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onOpenGuide != null) {
                RetroIconButton(
                    icon = Icons.Default.HelpOutline,
                    contentDescription = "User Handbook & Guide",
                    onClick = onOpenGuide,
                    testTag = "header_guide_button"
                )
            }

            if (onEnterPip != null) {
                RetroIconButton(
                    icon = Icons.Default.PictureInPicture,
                    contentDescription = "Floating Picture in Picture Timer",
                    onClick = onEnterPip,
                    testTag = "header_pip_button"
                )
            }

            if (onQuickToggleMonochrome != null) {
                RetroIconButton(
                    icon = if (isMonochromeActive) Icons.Default.Visibility else Icons.Default.Tune,
                    contentDescription = "Toggle Monochrome Mode",
                    onClick = onQuickToggleMonochrome,
                    isSelected = isMonochromeActive,
                    testTag = "header_monochrome_toggle"
                )
            }
        }
    }
}
