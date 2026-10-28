package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.luminance
import com.example.data.RetroThemeId

@Composable
fun MonochromeTheme(
    themeId: RetroThemeId = RetroThemeId.PURE_MONOCHROME,
    customColors: CustomThemeColors? = null,
    content: @Composable () -> Unit
) {
    val palette = RetroPalettes.getPalette(themeId, customColors)
    val isLight = palette.background.luminance() > 0.5f

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = palette.buttonBorder,
            onPrimary = palette.buttonActiveText,
            primaryContainer = palette.surface,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.textSecondary,
            onSecondary = palette.background,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceBorder,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.surfaceBorder
        )
    } else {
        darkColorScheme(
            primary = palette.buttonBorder,
            onPrimary = palette.buttonActiveText,
            primaryContainer = palette.surface,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.textSecondary,
            onSecondary = palette.background,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceBorder,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.surfaceBorder
        )
    }

    CompositionLocalProvider(LocalRetroPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RetroTypography,
            content = content
        )
    }
}
