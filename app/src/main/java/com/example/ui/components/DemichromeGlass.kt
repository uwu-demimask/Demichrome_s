package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonochromeModeLevel
import com.example.ui.theme.RetroPalette
import com.example.ui.theme.RetroTheme
import com.example.data.RetroThemeId

/**
 * DEMICHROME LIQUID GLASS SYSTEM
 *
 * A restrained, futuristic monochrome computer interface viewed through modern liquid glass:
 * - Rounded corners throughout (cards, search, widgets, live timer, bottom controls)
 * - Translucent surfaces with soft refraction and realistic light highlights
 * - 1px gradient optical border with specular highlight along top/left edge
 * - Clean contrast that keeps Demichrome's typography crisp and legible
 */
object DemichromeGlassDefaults {
    val CornerRadiusLarge: Dp = 22.dp
    val CornerRadiusMedium: Dp = 16.dp
    val CornerRadiusSmall: Dp = 10.dp
    val CornerRadiusPill: Dp = 28.dp

    fun glassSurfaceColor(palette: RetroPalette, intensity: Float = 0.75f): Color {
        val isLight = palette.background.luminance() > 0.5f
        return if (isLight) {
            palette.surface.copy(alpha = (0.85f * intensity).coerceIn(0.30f, 0.96f))
        } else {
            palette.surface.copy(alpha = (0.80f * intensity).coerceIn(0.25f, 0.95f))
        }
    }

    fun glassBorderBrush(palette: RetroPalette): Brush {
        val isLight = palette.background.luminance() > 0.5f
        val highlightColor = if (isLight) palette.textPrimary else palette.textAccent
        return Brush.linearGradient(
            colors = listOf(
                highlightColor.copy(alpha = if (isLight) 0.35f else 0.28f),
                palette.surfaceBorder.copy(alpha = 0.22f),
                palette.surfaceBorder.copy(alpha = 0.08f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    }
}

/**
 * Primary Liquid Glass Container Card for Widgets, Focus Panels, System Cards
 */
@Composable
fun DemichromeGlassCard(
    modifier: Modifier = Modifier,
    palette: RetroPalette = RetroTheme.palette,
    cornerRadius: Dp = DemichromeGlassDefaults.CornerRadiusLarge,
    glassIntensity: Float = 0.75f,
    elevation: Dp = 6.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val surfaceColor = DemichromeGlassDefaults.glassSurfaceColor(palette, glassIntensity)
    val borderBrush = DemichromeGlassDefaults.glassBorderBrush(palette)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.982f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "glass_card_press"
    )

    Surface(
        modifier = (if (testTag != null) modifier.testTag(testTag) else modifier)
            .scale(pressScale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = palette.textPrimary.copy(alpha = 0.08f),
                spotColor = palette.textPrimary.copy(alpha = 0.12f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = surfaceColor,
        border = if (borderColor != null) BorderStroke(borderWidth, SolidColor(borderColor)) else BorderStroke(borderWidth, borderBrush)
    ) {
        Box(modifier = Modifier) {
            // Subtle optical specular refraction hairline at top inner edge
            Canvas(modifier = Modifier.matchParentSize()) {
                val isLight = palette.background.luminance() > 0.5f
                val specularColor = if (!isLight) palette.textAccent.copy(alpha = 0.22f) else palette.textPrimary.copy(alpha = 0.15f)
                drawLine(
                    color = specularColor,
                    start = Offset(cornerRadius.toPx() * 0.6f, 1.5f),
                    end = Offset(size.width - cornerRadius.toPx() * 0.6f, 1.5f),
                    strokeWidth = 1.2f
                )
            }
            content()
        }
    }
}

/**
 * Rounded Liquid Glass Pill (Search bars, quick filters, action badges)
 */
@Composable
fun DemichromeGlassPill(
    modifier: Modifier = Modifier,
    palette: RetroPalette = RetroTheme.palette,
    cornerRadius: Dp = DemichromeGlassDefaults.CornerRadiusPill,
    glassIntensity: Float = 0.80f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    DemichromeGlassCard(
        modifier = modifier,
        palette = palette,
        cornerRadius = cornerRadius,
        glassIntensity = glassIntensity,
        elevation = 3.dp,
        onClick = onClick,
        content = content
    )
}

/**
 * Tactile Liquid Glass Button for Demichrome [ PHONE ], [ CAMERA ], [ PAUSE ], [ STOP ]
 */
@Composable
fun DemichromeGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    palette: RetroPalette = RetroTheme.palette,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPrimary: Boolean = false,
    cornerRadius: Dp = DemichromeGlassDefaults.CornerRadiusMedium,
    glassIntensity: Float = 0.75f,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    fontFamily: FontFamily = palette.fontFamily,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(cornerRadius)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(100),
        label = "glass_btn_scale"
    )

    val bgColor = if (isPrimary) {
        palette.buttonActiveBackground
    } else {
        palette.buttonBackground.copy(alpha = (0.85f * glassIntensity).coerceIn(0.20f, 0.95f))
    }

    val textColor = if (isPrimary) {
        palette.buttonActiveText
    } else {
        palette.buttonText
    }

    val borderBrush = if (isPrimary) {
        SolidColor(palette.buttonActiveBackground)
    } else {
        DemichromeGlassDefaults.glassBorderBrush(palette)
    }

    Surface(
        modifier = (if (testTag != null) modifier.testTag(testTag) else modifier)
            .scale(scale)
            .shadow(
                elevation = if (isPrimary) 4.dp else 2.dp,
                shape = shape,
                ambientColor = palette.textPrimary.copy(alpha = 0.08f),
                spotColor = palette.textPrimary.copy(alpha = 0.12f)
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderBrush)
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                color = textColor,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Clean, hairline retro divider for Demichrome layout sections
 */
@Composable
fun DemichromeThinDivider(
    palette: RetroPalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(palette.surfaceBorder.copy(alpha = 0.45f))
    )
}

/**
 * Applies Android hardware-accelerated monochrome color matrix to any container
 * OFF: Full colors (saturation 1.0f)
 * SUBTLE: 35% saturation (subdued, soft retro toning)
 * FULL: 0% saturation (pure high-contrast monochrome)
 */
fun Modifier.demichromeMonochrome(level: MonochromeModeLevel): Modifier {
    if (level == MonochromeModeLevel.OFF) return this
    val matrix = android.graphics.ColorMatrix().apply {
        setSaturation(level.saturation)
    }
    val filter = android.graphics.ColorMatrixColorFilter(matrix)
    val paint = android.graphics.Paint().apply {
        colorFilter = filter
    }
    return this.drawWithContent {
        drawIntoCanvas { canvas ->
            val count = canvas.nativeCanvas.saveLayer(null, paint)
            drawContent()
            canvas.nativeCanvas.restoreToCount(count)
        }
    }
}
