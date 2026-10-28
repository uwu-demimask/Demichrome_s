package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.core.haptics.RetroHaptics
import com.example.ui.theme.RetroTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Edge placement for the Demichrome rotary wheel.
 */
enum class WheelEdge {
    LEFT,
    RIGHT
}

/**
 * Metadata definition for utilities accessible via the rotary navigation wheel.
 */
data class UtilityItem(
    val screen: RetroScreen,
    val title: String,
    val code: String,
    val subtitle: String
)

val DEMICHROME_UTILITIES = listOf(
    UtilityItem(RetroScreen.FOCUS, "FOCUS TIMER", "01/FCS", "DEEP FLOW ENGINE"),
    UtilityItem(RetroScreen.HABITS, "HABITS", "02/HBT", "SANCTUARY MATRIX"),
    UtilityItem(RetroScreen.TASKS, "TASKS", "03/TSK", "DAILY ROUTINES"),
    UtilityItem(RetroScreen.FLASHCARDS, "FLASH CARDS", "04/CRD", "ACTIVE RECALL"),
    UtilityItem(RetroScreen.MONEY, "MONEY", "05/MNY", "LEDGER DISCIPLINE"),
    UtilityItem(RetroScreen.SHIELD, "APP SHIELD", "06/SHL", "DISTRACTION BARRIER"),
    UtilityItem(RetroScreen.STATS, "STATISTICS", "07/STA", "PRODUCTIVITY TELEMETRY"),
    UtilityItem(RetroScreen.SETTINGS, "SETTINGS", "08/SET", "SYSTEM PARAMETERS")
)

/**
 * DEMICHROME EDGE HAPTIC ROTARY NAVIGATION WHEEL
 *
 * Primary Navigation system replacing the bottom navigation bar.
 *
 * Interaction model:
 * - Lives partially off-screen on the phone's left or right edge.
 * - Tactile knurled edge disc affordance peeks out when inactive.
 * - Entire side edge acts as a swipe/touch detection zone, ensuring effortless access
 *   even when scrolled deep into long pages (Settings, Ledger, Tasks).
 * - Dragging vertically or rotating cycles through Demichrome sections with angular snapping.
 * - Subtle haptic tick on moving between steps, stronger click when selection locks in.
 * - Does NOT modify timer duration or settings values.
 * - Retracts smoothly after 2.5s of inactivity or upon outside tap.
 * - Large enough for comfortable single-handed thumb operation.
 * - Asymmetric Nothing/Pixel-inspired physical rotary styling.
 */
@Composable
fun DemichromeEdgeHapticWheel(
    currentScreen: RetroScreen = RetroScreen.FOCUS,
    onSelectUtility: (RetroScreen) -> Unit = {},
    hapticsEnabled: Boolean = true,
    edgeSide: WheelEdge = WheelEdge.RIGHT,
    sensitivity: Float = 1.0f,
    verticalPlacement: String = "THUMB",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette
    val coroutineScope = rememberCoroutineScope()

    val utilities = remember { DEMICHROME_UTILITIES }

    val initialIndex = remember(currentScreen) {
        val idx = utilities.indexOfFirst { it.screen == currentScreen }
        if (idx >= 0) idx else 0
    }

    var currentStepIndex by remember { mutableIntStateOf(initialIndex) }
    var lastHapticIndex by remember { mutableIntStateOf(initialIndex) }
    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(currentScreen) {
        val idx = utilities.indexOfFirst { it.screen == currentScreen }
        if (idx >= 0 && idx != currentStepIndex) {
            currentStepIndex = idx
            lastHapticIndex = idx
        }
    }

    // Stable vertical resting bias calculated from user placement
    val restingBiasY = when (verticalPlacement.uppercase()) {
        "UPPER" -> -0.35f
        "CENTER" -> 0.0f
        "LOWER" -> 0.35f
        else -> 0.20f // Natural ergonomic thumb reach zone
    }
    var targetBiasY by remember { mutableFloatStateOf(restingBiasY) }

    LaunchedEffect(verticalPlacement, restingBiasY) {
        targetBiasY = restingBiasY
    }

    val animatedBiasY by animateFloatAsState(
        targetValue = targetBiasY,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = Spring.StiffnessLow
        ),
        label = "wheel_bias_y"
    )

    // Degrees per section along the physical rotary arc
    val stepDegrees = 20f
    val rotationAnim = remember { Animatable(initialIndex * stepDegrees) }
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = coroutineScope.launch {
            delay(2500)
            isActive = false
            targetBiasY = restingBiasY
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        val isRight = edgeSide == WheelEdge.RIGHT

        // Outside touch dismiss scrim when active
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (hapticsEnabled) {
                            RetroHaptics.performTick(context)
                        }
                        isActive = false
                        targetBiasY = restingBiasY
                    }
                    .testTag("edge_wheel_scrim")
            )
        }

        // Tactile edge gesture detection zone aligned with handle position
        if (!isActive) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 140.dp)
                    .align(
                        BiasAlignment(
                            horizontalBias = if (isRight) 1.0f else -1.0f,
                            verticalBias = restingBiasY
                        )
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { _ ->
                                if (hapticsEnabled) {
                                    RetroHaptics.performTick(context)
                                }
                                isActive = true
                                targetBiasY = restingBiasY
                                coroutineScope.launch {
                                    rotationAnim.snapTo(currentStepIndex * stepDegrees)
                                }
                                scheduleAutoHide()
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (hapticsEnabled) {
                            RetroHaptics.performTick(context)
                        }
                        isActive = true
                        targetBiasY = restingBiasY
                        coroutineScope.launch {
                            rotationAnim.snapTo(currentStepIndex * stepDegrees)
                        }
                        scheduleAutoHide()
                    }
                    .testTag("edge_wheel_touch_strip")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentAlignment = BiasAlignment(
                horizontalBias = if (isRight) 1.0f else -1.0f,
                verticalBias = animatedBiasY
            )
        ) {
            // Emerged Rotary Wheel Container
            AnimatedVisibility(
                visible = isActive,
                enter = slideInHorizontally(initialOffsetX = { if (isRight) it else -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { if (isRight) it else -it }) + fadeOut()
            ) {
                EmergedUtilityWheel(
                    isRight = isRight,
                    utilities = utilities,
                    currentIndex = currentStepIndex,
                    currentRotation = rotationAnim.value,
                    stepDegrees = stepDegrees,
                    onDragDelta = { dy ->
                        autoHideJob?.cancel()
                        val sensitivityMultiplier = (0.24f * sensitivity).coerceIn(0.08f, 0.5f)
                        val deltaAngle = -dy * sensitivityMultiplier
                        val newAngle = (rotationAnim.value + deltaAngle).coerceIn(
                            0f,
                            (utilities.size - 1) * stepDegrees
                        )

                        coroutineScope.launch {
                            rotationAnim.snapTo(newAngle)
                        }

                        val calculatedIndex = (newAngle / stepDegrees).roundToInt()
                            .coerceIn(0, utilities.size - 1)

                        if (calculatedIndex != currentStepIndex) {
                            val previousIndex = currentStepIndex
                            currentStepIndex = calculatedIndex
                            if (hapticsEnabled) {
                                if (calculatedIndex != previousIndex) {
                                    RetroHaptics.performClick(context)
                                } else {
                                    RetroHaptics.performTick(context)
                                }
                            }
                            lastHapticIndex = calculatedIndex
                            onSelectUtility(utilities[calculatedIndex].screen)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            val targetSnapAngle = currentStepIndex * stepDegrees
                            rotationAnim.animateTo(
                                targetValue = targetSnapAngle,
                                animationSpec = spring(
                                    dampingRatio = 0.76f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            if (hapticsEnabled) {
                                RetroHaptics.performClick(context)
                            }
                        }
                        scheduleAutoHide()
                    }
                )
            }

            // Inactive / Resting Tactile Edge Affordance
            if (!isActive) {
                RestingEdgeHandle(
                    isRight = isRight,
                    onActivate = {
                        if (hapticsEnabled) {
                            RetroHaptics.performTick(context)
                        }
                        isActive = true
                        targetBiasY = restingBiasY
                        coroutineScope.launch {
                            rotationAnim.snapTo(currentStepIndex * stepDegrees)
                        }
                        scheduleAutoHide()
                    }
                )
            }
        }
    }
}

/**
 * Tactile knurled edge disc visible when the wheel is resting.
 */
@Composable
private fun RestingEdgeHandle(
    isRight: Boolean,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val shape = if (isRight) {
        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
    } else {
        RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = modifier
            .size(width = 18.dp, height = 96.dp)
            .shadow(
                elevation = 5.dp,
                shape = shape,
                ambientColor = palette.shadow.copy(alpha = 0.45f),
                spotColor = palette.shadow.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(palette.surface)
            .border(1.2.dp, palette.surfaceBorder, shape)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { _ ->
                        onActivate()
                    },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                    }
                )
            }
            .clickable {
                onActivate()
            }
            .testTag("edge_wheel_resting_handle"),
        contentAlignment = Alignment.Center
    ) {
        // Physical rotary knurled rib pattern
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(7) { idx ->
                Box(
                    modifier = Modifier
                        .size(width = if (idx == 3) 8.dp else 5.dp, height = 2.dp)
                        .background(if (idx == 3) palette.accent else palette.textSecondary.copy(alpha = 0.55f))
                )
            }
        }
    }
}

/**
 * The Emerged Physical Rotary Utility Wheel.
 * Renders an expansive circular arc partially extending past the display boundary, with radial ticks and active HUD readout.
 */
@Composable
private fun EmergedUtilityWheel(
    isRight: Boolean,
    utilities: List<UtilityItem>,
    currentIndex: Int,
    currentRotation: Float,
    stepDegrees: Float,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUtility = utilities.getOrElse(currentIndex) { utilities.first() }

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    }
                )
            }
            .testTag("demichrome_edge_haptic_wheel"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isRight) {
            // Utility HUD Readout (Left of wheel on right-edge placement)
            UtilityHudReadout(
                utility = currentUtility,
                currentIndex = currentIndex,
                totalCount = utilities.size,
                isRight = true
            )
            // Physical Rotary Dial Arc Canvas
            RotaryDialArcCanvas(
                isRight = true,
                currentRotation = currentRotation,
                stepDegrees = stepDegrees,
                stepCount = utilities.size
            )
        } else {
            // Physical Rotary Dial Arc Canvas
            RotaryDialArcCanvas(
                isRight = false,
                currentRotation = currentRotation,
                stepDegrees = stepDegrees,
                stepCount = utilities.size
            )
            // Utility HUD Readout (Right of wheel on left-edge placement)
            UtilityHudReadout(
                utility = currentUtility,
                currentIndex = currentIndex,
                totalCount = utilities.size,
                isRight = false
            )
        }
    }
}

/**
 * Compact technical HUD badge showing active utility info.
 */
@Composable
private fun UtilityHudReadout(
    utility: UtilityItem,
    currentIndex: Int,
    totalCount: Int,
    isRight: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = palette.shadow.copy(alpha = 0.5f),
                spotColor = palette.shadow.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(palette.surfaceElevated)
            .border(1.2.dp, palette.surfaceBorder, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("edge_wheel_hud_readout"),
        horizontalAlignment = if (isRight) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
            )
            Text(
                text = ":: ${utility.code} :: ${currentIndex + 1}/$totalCount",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = palette.accent,
                letterSpacing = 1.sp
            )
        }

        // Active utility title
        Text(
            text = utility.title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = palette.textPrimary,
            letterSpacing = 1.2.sp
        )

        // Utility subtitle description
        Text(
            text = utility.subtitle,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 9.5.sp,
            color = palette.textSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Pixel-like ASCII indicator track
        Text(
            text = buildString {
                append("[ ")
                for (i in 0 until totalCount) {
                    if (i == currentIndex) append("■ ") else append("· ")
                }
                append("]")
            },
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            color = palette.textPrimary.copy(alpha = 0.75f),
            letterSpacing = 1.sp
        )
    }
}

/**
 * Canvas drawing the physical circular rotary wheel dial sitting partially outside the screen.
 * Dimensions enlarged for comfortable one-handed operation.
 */
@Composable
private fun RotaryDialArcCanvas(
    isRight: Boolean,
    currentRotation: Float,
    stepDegrees: Float,
    stepCount: Int,
    modifier: Modifier = Modifier
) {
    val palette = RetroTheme.palette
    val wheelRadiusDp = 145.dp
    val wheelWidthDp = 76.dp

    Box(
        modifier = modifier
            .size(width = wheelWidthDp, height = wheelRadiusDp * 2)
            .testTag("rotary_dial_arc_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radiusPx = size.height / 2f

            // Center of virtual rotary dial placed outside the screen boundary
            val centerX = if (isRight) canvasWidth + (radiusPx * 0.45f) else -(radiusPx * 0.45f)
            val centerY = canvasHeight / 2f

            // 1. Draw outer circular bezel arc
            val arcBoundingSize = Size(radiusPx * 2, radiusPx * 2)
            val arcTopLeft = Offset(centerX - radiusPx, centerY - radiusPx)
            val startAngle = if (isRight) 115f else -65f
            val sweepAngle = 130f

            drawArc(
                color = palette.surface,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = arcTopLeft,
                size = arcBoundingSize
            )

            drawArc(
                color = palette.surfaceBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcBoundingSize,
                style = Stroke(width = 2.5f)
            )

            // 2. Draw inner concentric milled guideline
            val innerRadius = radiusPx * 0.76f
            val innerTopLeft = Offset(centerX - innerRadius, centerY - innerRadius)
            drawArc(
                color = palette.surfaceBorder.copy(alpha = 0.45f),
                startAngle = startAngle + 5f,
                sweepAngle = sweepAngle - 10f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = 1.2f)
            )

            // 3. Draw segmented physical radial tick marks with asymmetric Nothing-style accents
            val totalTicks = 40
            val tickAngleSpacing = sweepAngle / totalTicks

            for (i in 0..totalTicks) {
                val tickAngleDeg = (startAngle + i * tickAngleSpacing + (currentRotation * 0.70f)) % 360f
                val rad = Math.toRadians(tickAngleDeg.toDouble())

                val isMajorIndex = i % 5 == 0
                val isAccentIndex = i % 10 == 0
                val tickLen = when {
                    isAccentIndex -> 18.dp.toPx()
                    isMajorIndex -> 12.dp.toPx()
                    else -> 6.dp.toPx()
                }

                val outerX = (centerX + radiusPx * cos(rad)).toFloat()
                val outerY = (centerY + radiusPx * sin(rad)).toFloat()
                val innerX = (centerX + (radiusPx - tickLen) * cos(rad)).toFloat()
                val innerY = (centerY + (radiusPx - tickLen) * sin(rad)).toFloat()

                if (outerX in 0f..canvasWidth || innerX in 0f..canvasWidth) {
                    drawLine(
                        color = when {
                            isAccentIndex -> palette.accent
                            isMajorIndex -> palette.textPrimary
                            else -> palette.textSecondary.copy(alpha = 0.45f)
                        },
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isAccentIndex) 2.5f else if (isMajorIndex) 1.8f else 1.0f
                    )
                }
            }

            // 4. Center index indicator cursor (Focal Apex mark)
            val cursorX = if (isRight) canvasWidth - 8.dp.toPx() else 8.dp.toPx()
            val cursorY = centerY

            val pointerPath = Path().apply {
                if (isRight) {
                    moveTo(cursorX - 10.dp.toPx(), cursorY - 7.dp.toPx())
                    lineTo(cursorX, cursorY)
                    lineTo(cursorX - 10.dp.toPx(), cursorY + 7.dp.toPx())
                } else {
                    moveTo(cursorX + 10.dp.toPx(), cursorY - 7.dp.toPx())
                    lineTo(cursorX, cursorY)
                    lineTo(cursorX + 10.dp.toPx(), cursorY + 7.dp.toPx())
                }
                close()
            }

            drawPath(
                path = pointerPath,
                color = palette.accent
            )
        }
    }
}
