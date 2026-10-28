package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * High-performance, pixel-precise CRT Phosphor & Scanline container.
 * Renders authentic cathode ray tube scanlines, tube curvature vignette,
 * phosphor micro-flicker, and respects Reduced Motion accessibility.
 */
@Composable
fun RetroCrtContainer(
    enabled: Boolean = false,
    scanlineIntensity: Float = 0.25f,
    flicker: Boolean = false,
    crtCurvature: Boolean = true,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    // Phosphor micro-flicker animation (subtle 60Hz cathode variation)
    val infiniteTransition = rememberInfiniteTransition(label = "crt_phosphor_transition")

    val flickerAlpha by if (flicker && !reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.982f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 95, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "crt_micro_flicker"
        )
    } else {
        androidx.compose.runtime.rememberUpdatedState(1.0f)
    }

    // Subtle raster beam sweep
    val beamProgress by if (!reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "crt_beam_sweep"
        )
    } else {
        androidx.compose.runtime.rememberUpdatedState(0f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(flickerAlpha)
            .drawWithContent {
                // 1. Draw child UI content first
                drawContent()

                // 2. Draw CRT horizontal scanlines
                val step = 3.5.dp.toPx()
                val lineThickness = 1.25.dp.toPx()
                val lineAlpha = (scanlineIntensity * 0.5f).coerceIn(0.04f, 0.9f)
                val scanlineColor = Color.Black.copy(alpha = lineAlpha)

                var y = 0f
                val h = size.height
                val w = size.width

                while (y < h) {
                    drawLine(
                        color = scanlineColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = lineThickness
                    )
                    y += step
                }

                // 3. Draw subtle raster sweep beam (if not reduced motion)
                if (!reducedMotion && scanlineIntensity > 0.08f) {
                    val beamY = beamProgress * (h + 120f) - 60f
                    val beamHeight = 70.dp.toPx()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = (scanlineIntensity * 0.05f).coerceAtMost(0.08f)),
                                Color.Transparent
                            ),
                            startY = beamY,
                            endY = beamY + beamHeight
                        ),
                        topLeft = Offset(0f, beamY),
                        size = Size(w, beamHeight)
                    )
                }

                // 4. Draw Cathode Tube Vignette & Edge Curvature Shading
                if (crtCurvature) {
                    val vignetteAlpha = (scanlineIntensity * 0.4f).coerceIn(0.08f, 0.65f)
                    val maxDim = maxOf(w, h)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = vignetteAlpha * 0.4f),
                                Color.Black.copy(alpha = vignetteAlpha)
                            ),
                            center = Offset(w / 2f, h / 2f),
                            radius = maxDim * 0.70f
                        ),
                        size = size
                    )
                }
            }
    ) {
        content()
    }
}
