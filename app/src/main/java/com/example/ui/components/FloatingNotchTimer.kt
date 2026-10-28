package com.example.ui.components

import android.graphics.Rect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.ViewCompat
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.data.NotchPosition
import com.example.data.TodoTask
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.FocusState
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Metric container for calculating the safe top region around display cutouts dynamically.
 */
data class DynamicIslandSafeMetrics(
    val hasCutout: Boolean,
    val cutoutBottomDp: Dp,
    val statusBarHeightDp: Dp,
    val safeTopPaddingDp: Dp,
    val safeHorizontalMarginDp: Dp,
    val navBarBottomDp: Dp
)

/**
 * SYSTEM-LEVEL SAFE REGION CALCULATOR:
 *
 * Dynamically queries Android WindowInsets and DisplayCutout APIs.
 * Calculates safe top and side regions so the mini-player never overlays
 * physical camera punch-holes, teardrop notches, or system indicators on any device.
 */
@Composable
fun rememberSafeDynamicIslandMetrics(): DynamicIslandSafeMetrics {
    val density = LocalDensity.current
    val view = LocalView.current

    // Query insets directly from Compose WindowInsets
    val statusBarTopPx = WindowInsets.statusBars.getTop(density)
    val cutoutTopPx = WindowInsets.displayCutout.getTop(density)
    val statusBarTop = with(density) { statusBarTopPx.toDp() }
    val cutoutTop = with(density) { cutoutTopPx.toDp() }

    // Query physical display cutout bounds via ViewCompat DisplayCutoutCompat
    val windowInsetsCompat = ViewCompat.getRootWindowInsets(view)
    val displayCutoutCompat = windowInsetsCompat?.displayCutout
    val boundingRects: List<Rect> = displayCutoutCompat?.boundingRects ?: emptyList()

    val topCutouts = boundingRects.filter { rect ->
        rect.top <= 160 && rect.bottom > 0
    }
    val physicalCutoutBottomDp = if (topCutouts.isNotEmpty()) {
        with(density) { topCutouts.maxOf { it.bottom }.toDp() }
    } else {
        0.dp
    }
    val safeInsetTopDp = displayCutoutCompat?.safeInsetTop?.let { with(density) { it.toDp() } } ?: 0.dp

    val maxDetectedTop = maxOf(statusBarTop, maxOf(cutoutTop, maxOf(physicalCutoutBottomDp, safeInsetTopDp)))

    // GUARANTEED SAFE POSITIONING BELOW CAMERA / CUTOUT:
    // Noticeably farther away than before with a comfortable empty space:
    // Minimum 72.dp from top of screen, or maxDetectedTop + 24.dp.
    val comfortableGap = 24.dp
    val safeTopPaddingDp = maxOf(maxDetectedTop + comfortableGap, 72.dp)

    val gestureInsets = WindowInsets.systemGestures
    val navBarInsets = WindowInsets.navigationBars
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val leftGestureDp = with(density) { gestureInsets.getLeft(density, layoutDirection).toDp() }
    val rightGestureDp = with(density) { gestureInsets.getRight(density, layoutDirection).toDp() }
    val navBarBottomDp = with(density) { navBarInsets.getBottom(density).toDp() }
    val safeHorizontalMarginDp = maxOf(16.dp, maxOf(leftGestureDp, rightGestureDp) + 8.dp)

    return remember(
        statusBarTop,
        cutoutTop,
        physicalCutoutBottomDp,
        safeInsetTopDp,
        safeTopPaddingDp,
        safeHorizontalMarginDp,
        navBarBottomDp
    ) {
        DynamicIslandSafeMetrics(
            hasCutout = cutoutTop > 0.dp || physicalCutoutBottomDp > 0.dp || safeInsetTopDp > 0.dp,
            cutoutBottomDp = maxDetectedTop,
            statusBarHeightDp = statusBarTop,
            safeTopPaddingDp = safeTopPaddingDp,
            safeHorizontalMarginDp = safeHorizontalMarginDp,
            navBarBottomDp = navBarBottomDp
        )
    }
}

/**
 * COLLAPSED STATE:
 *
 * A compact blue/white pill containing:
 * [ Focus icon ] 24:37 [ pause ]
 *
 * Small, responsive, and unobtrusive.
 */
@Composable
private fun CollapsedIslandPill(
    formattedTime: String,
    state: FocusState,
    widthScale: Float = 1.0f,
    heightScale: Float = 1.0f,
    onTogglePlayPause: () -> Unit,
    onExpand: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val pillBackground = Brush.horizontalGradient(
        colors = listOf(
            palette.surfaceElevated,
            palette.surface
        )
    )
    val pillBorder = BorderStroke(1.dp, palette.border)
    val pillShape = RoundedCornerShape(percent = 50)

    val displayText = when {
        formattedTime.isNotBlank() && formattedTime != "00:00" -> formattedTime
        state == FocusState.COMPLETED -> "DONE ✓"
        state == FocusState.READY -> "FOCUS"
        else -> formattedTime.ifBlank { "25:00" }
    }

    Row(
        modifier = modifier
            .defaultMinSize(
                minWidth = (138 * widthScale).dp,
                minHeight = (36 * heightScale).dp
            )
            .shadow(
                elevation = 6.dp,
                shape = pillShape,
                ambientColor = palette.shadow.copy(alpha = 0.4f),
                spotColor = palette.shadow.copy(alpha = 0.4f)
            )
            .clip(pillShape)
            .background(pillBackground)
            .border(pillBorder, pillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                RetroHaptics.performTick(context)
                onNavigateToFocus()
            }
            .padding(horizontal = 11.dp, vertical = 5.dp)
            .testTag("dynamic_island_collapsed_pill"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active focus indicator icon
        Icon(
            imageVector = Icons.Default.Adjust,
            contentDescription = "Focus Indicator",
            tint = if (state == FocusState.RUNNING) palette.accent else palette.textSecondary,
            modifier = Modifier
                .size(13.dp)
                .testTag("dynamic_island_focus_icon")
        )

        // Time display
        Text(
            text = if (state == FocusState.COMPLETED) "SESSION COMPLETE" else displayText,
            color = if (state == FocusState.COMPLETED) palette.success else palette.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            modifier = Modifier.testTag("dynamic_island_time_text")
        )

        // Status indicator tag [RUNNING] / [PAUSED]
        if (state == FocusState.RUNNING || state == FocusState.PAUSED) {
            Text(
                text = if (state == FocusState.RUNNING) "[RUNNING]" else "[PAUSED]",
                color = if (state == FocusState.RUNNING) palette.accent else palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // [ play / pause button ]
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(palette.accent)
                .border(1.dp, palette.border, CircleShape)
                .clickable {
                    RetroHaptics.performClick(context)
                    onTogglePlayPause()
                }
                .testTag("dynamic_island_toggle_pause"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state == FocusState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state == FocusState.RUNNING) "Pause" else "Resume",
                tint = palette.buttonActiveText,
                modifier = Modifier.size(13.dp)
            )
        }

        // Expand chevron for quick HUD controls
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable {
                    RetroHaptics.performTick(context)
                    onExpand()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand HUD",
                tint = palette.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * EXPANDED STATE:
 *
 * FOCUS SESSION
 * 24:37
 *
 * Current task:
 * Physics Revision
 *
 * [ Pause ] [ Stop ]
 */
@Composable
private fun ExpandedIslandCard(
    formattedTime: String,
    state: FocusState,
    activeTaskTitle: String,
    onTogglePlayPause: () -> Unit,
    onReset: () -> Unit,
    onCollapse: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val cardBackground = Brush.verticalGradient(
        colors = listOf(
            palette.surfaceElevated,
            palette.surface
        )
    )
    val cardBorder = BorderStroke(1.2.dp, palette.border)
    val cardShape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 340.dp)
            .shadow(
                elevation = 16.dp,
                shape = cardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(cardShape)
            .background(cardBackground)
            .border(cardBorder, cardShape)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("dynamic_island_expanded_card"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: FOCUS SESSION + Collapse Indicator
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
                    imageVector = Icons.Default.Adjust,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "FOCUS SESSION",
                    color = palette.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(palette.border.copy(alpha = 0.4f))
                    .clickable {
                        RetroHaptics.performClick(context)
                        onCollapse()
                    }
                    .testTag("dynamic_island_collapse_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Collapse Mini Player",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Countdown Timer: 24:37
        Text(
            text = formattedTime,
            color = palette.textPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("expanded_timer_text")
        )

        // Current task block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surfaceElevated)
                .border(BorderStroke(1.dp, palette.border.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
                .clickable {
                    onNavigateToFocus()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("dynamic_island_task_block"),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Current task:",
                color = palette.textSecondary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = activeTaskTitle,
                color = palette.textPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("expanded_task_title")
            )
        }

        // Action Buttons: [ Pause ] [ Stop ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // [ Pause / Resume ]
            Button(
                onClick = {
                    RetroHaptics.performClick(context)
                    onTogglePlayPause()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.buttonActiveText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("dynamic_island_pause_button")
            ) {
                Icon(
                    imageVector = if (state == FocusState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state == FocusState.RUNNING) "Pause" else "Resume",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // [ Stop ]
            Button(
                onClick = {
                    RetroHaptics.performClick(context)
                    onReset()
                    onCollapse()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.danger.copy(alpha = 0.12f),
                    contentColor = palette.danger
                ),
                border = BorderStroke(1.dp, palette.danger.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("dynamic_island_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Stop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * DEMICHROME FOCUS TIMER DYNAMIC ISLAND MINI-PLAYER
 *
 * Features:
 * - Dynamic system-level WindowInsets and DisplayCutout detection
 * - Zero overlap with camera punch-holes or notches
 * - Single source of truth (subscribes to actual timer state)
 * - Smooth animation between collapsed and expanded states
 * - Fully responsive across different Android devices, resolutions, and orientations
 */
@Composable
fun FloatingNotchTimer(
    remainingSeconds: Int,
    state: FocusState,
    enabled: Boolean,
    isDeepFocus: Boolean = false,
    notchPosition: NotchPosition = NotchPosition.TOP_CENTER,
    targetDurationMinutes: Int = 25,
    activeTask: TodoTask? = null,
    ambientSound: RetroAudioSynthesizer.AmbientSoundType = RetroAudioSynthesizer.AmbientSoundType.NONE,
    todaySessionCount: Int = 0,
    initiallyExpanded: Boolean = false,
    calibrationOffsetX: Int = 0,
    calibrationOffsetY: Int = 0,
    calibrationWidthScale: Float = 1.0f,
    calibrationHeightScale: Float = 1.0f,
    onNavigateToFocus: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onReset: () -> Unit = {},
    onAddFiveMinutes: () -> Unit = {},
    onChangePosition: (NotchPosition) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val isVisible = enabled && (state == FocusState.RUNNING || state == FocusState.PAUSED || state == FocusState.COMPLETED || state == FocusState.READY)
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    // Auto-collapse on timer complete
    LaunchedEffect(state) {
        if (state == FocusState.COMPLETED) {
            delay(4000)
            if (isExpanded) isExpanded = false
        }
    }

    val metrics = rememberSafeDynamicIslandMetrics()

    val isBottom = notchPosition == NotchPosition.BOTTOM_CENTER
    val enterAnim = if (isBottom) slideInVertically(initialOffsetY = { it }) + fadeIn() else slideInVertically(initialOffsetY = { -it }) + fadeIn()
    val exitAnim = if (isBottom) slideOutVertically(targetOffsetY = { it }) + fadeOut() else slideOutVertically(targetOffsetY = { -it }) + fadeOut()

    val calibratedTopPadding = (metrics.safeTopPaddingDp + calibrationOffsetY.dp).coerceAtLeast(16.dp)

    val positionPadding = when (notchPosition) {
        NotchPosition.TOP_CENTER -> Modifier
            .offset(x = calibrationOffsetX.dp)
            .padding(
                top = calibratedTopPadding,
                start = metrics.safeHorizontalMarginDp,
                end = metrics.safeHorizontalMarginDp
            )
        NotchPosition.TOP_LEFT -> Modifier
            .offset(x = calibrationOffsetX.dp)
            .padding(
                top = calibratedTopPadding,
                start = metrics.safeHorizontalMarginDp
            )
        NotchPosition.TOP_RIGHT -> Modifier
            .offset(x = calibrationOffsetX.dp)
            .padding(
                top = calibratedTopPadding,
                end = metrics.safeHorizontalMarginDp
            )
        NotchPosition.BOTTOM_CENTER -> Modifier
            .offset(x = calibrationOffsetX.dp)
            .padding(
                bottom = (metrics.navBarBottomDp + 56.dp + calibrationOffsetY.dp).coerceAtLeast(16.dp),
                start = metrics.safeHorizontalMarginDp,
                end = metrics.safeHorizontalMarginDp
            )
    }

    val alignment = when (notchPosition) {
        NotchPosition.TOP_CENTER -> Alignment.TopCenter
        NotchPosition.TOP_LEFT -> Alignment.TopStart
        NotchPosition.TOP_RIGHT -> Alignment.TopEnd
        NotchPosition.BOTTOM_CENTER -> Alignment.BottomCenter
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = enterAnim,
        exit = exitAnim,
        modifier = modifier
            .then(positionPadding)
            .zIndex(500f)
    ) {
        val totalSecs = if (remainingSeconds > 0) remainingSeconds else (targetDurationMinutes * 60)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        val activeTaskTitle = activeTask?.title?.ifBlank { "Focus Session" } ?: "Focus Session"

        Box(
            modifier = Modifier
                .wrapContentSize(align = alignment)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            if (isExpanded) {
                ExpandedIslandCard(
                    formattedTime = formattedTime,
                    state = state,
                    activeTaskTitle = activeTaskTitle,
                    onTogglePlayPause = onTogglePlayPause,
                    onReset = onReset,
                    onCollapse = { isExpanded = false },
                    onNavigateToFocus = onNavigateToFocus
                )
            } else {
                CollapsedIslandPill(
                    formattedTime = formattedTime,
                    state = state,
                    widthScale = calibrationWidthScale,
                    heightScale = calibrationHeightScale,
                    onTogglePlayPause = onTogglePlayPause,
                    onExpand = { isExpanded = true },
                    onNavigateToFocus = onNavigateToFocus
                )
            }
        }
    }
}

/**
 * Direct semantic alias for the Dynamic Island Mini-Player.
 */
@Composable
fun DynamicIslandMiniPlayer(
    remainingSeconds: Int,
    state: FocusState,
    modifier: Modifier = Modifier,
    activeTask: TodoTask? = null,
    targetDurationMinutes: Int = 25,
    onNavigateToFocus: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    FloatingNotchTimer(
        remainingSeconds = remainingSeconds,
        state = state,
        enabled = true,
        notchPosition = NotchPosition.TOP_CENTER,
        targetDurationMinutes = targetDurationMinutes,
        activeTask = activeTask,
        onNavigateToFocus = onNavigateToFocus,
        onTogglePlayPause = onTogglePlayPause,
        onReset = onReset,
        modifier = modifier
    )
}
