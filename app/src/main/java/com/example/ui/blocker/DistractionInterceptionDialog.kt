package com.example.ui.blocker

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.blocker.InterceptionEvent
import com.example.core.haptics.RetroHaptics
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionInterceptionDialog(
    event: InterceptionEvent,
    viewModel: FocusViewModel
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isStrictMode by viewModel.blockerStrictMode.collectAsState()

    var frictionCountdown by remember { mutableIntStateOf(if (isStrictMode) 10 else 3) }
    var isBypassAllowed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RetroHaptics.performHeavyClick(context)
        while (frictionCountdown > 0) {
            delay(1000)
            frictionCountdown--
        }
        isBypassAllowed = true
    }

    BasicAlertDialog(
        onDismissRequest = { viewModel.dismissInterception() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("distraction_interception_dialog")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, palette.textAccent, RoundedCornerShape(8.dp))
                .background(palette.surface, RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, palette.textAccent, RoundedCornerShape(4.dp))
                        .background(palette.textAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = palette.textAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DISTRACTION SHIELD INTERCEPT",
                            color = palette.textAccent,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // App Name Title
                Text(
                    text = "${event.appName.uppercase()} IS LOCKED",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                // Countdown Timer Display Badge
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                val timeString = String.format("%02d:%02d", mins, secs)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                        .background(palette.background, RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$timeString REMAINING IN SPRINT",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Message
                Text(
                    text = "Shielded apps are not allowed to open while your focus timer is running. Once the timer ends, this app and all other shield apps will automatically unlock.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // Mindful reminder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                        .background(palette.background, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Take a slow breath. Protect your attention.",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary: Return to Demichrome Focus
                RetroButton(
                    text = "RETURN TO FOCUS",
                    onClick = { viewModel.dismissInterception() },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary: Friction Emergency Bypass
                if (!isBypassAllowed) {
                    Text(
                        text = "Pause before bypass: ${frictionCountdown}s",
                        color = palette.textSecondary.copy(alpha = 0.7f),
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp
                    )
                } else {
                    RetroOutlineButton(
                        text = "EMERGENCY 1-MIN BYPASS",
                        onClick = { viewModel.dismissInterception() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
