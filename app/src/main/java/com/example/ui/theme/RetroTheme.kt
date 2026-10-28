package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.data.RetroThemeId

data class RetroPalette(
    val id: RetroThemeId,
    val name: String,
    val description: String,
    val background: Color,
    val surface: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textAccent: Color,
    val buttonBackground: Color,
    val buttonBorder: Color,
    val buttonText: Color,
    val buttonActiveBackground: Color,
    val buttonActiveText: Color,
    val crtColor: Color,
    val phosphorGlow: Boolean,
    val fontFamily: FontFamily = FontFamily.SansSerif,
    val isBeveled: Boolean = false,
    val hasDottedGrid: Boolean = false,
    // Centralized Demichrome 2.0 Design Tokens
    val surfaceElevated: Color = surface,
    val accentSoft: Color = surfaceBorder.copy(alpha = 0.35f),
    val success: Color = Color(0xFF10B981),
    val warning: Color = Color(0xFFF59E0B),
    val danger: Color = Color(0xFFEF4444),
    val shadow: Color = Color(0x1A0F172A),
    val divider: Color = surfaceBorder.copy(alpha = 0.4f),
    val icon: Color = textAccent
) {
    val border: Color get() = surfaceBorder
    val accent: Color get() = textAccent
    val surfaceVariant: Color get() = surfaceBorder
}

object RetroPalettes {

    val DemichromeBlue = RetroPalette(
        id = RetroThemeId.DEMICHROME,
        name = "00 — Demichrome Blue",
        description = "Primary Demichrome visual language. Crisp white/light-blue background, soft blue surfaces, medium blue actions, deep navy typography.",
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        surfaceBorder = Color(0xFFBFDBFE),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        textAccent = Color(0xFF2563EB),
        buttonBackground = Color(0xFFEFF6FF),
        buttonBorder = Color(0xFF93C5FD),
        buttonText = Color(0xFF1E40AF),
        buttonActiveBackground = Color(0xFF2563EB),
        buttonActiveText = Color(0xFFFFFFFF),
        crtColor = Color(0xFF2563EB),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif,
        hasDottedGrid = true,
        surfaceElevated = Color(0xFFEFF6FF),
        accentSoft = Color(0xFFDBEAFE),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        danger = Color(0xFFEF4444),
        shadow = Color(0x1A1E3A8A),
        divider = Color(0xFFE2E8F0),
        icon = Color(0xFF2563EB)
    )

    val Paper = RetroPalette(
        id = RetroThemeId.PAPER,
        name = "01 — Paper",
        description = "The main Demichrome identity. Warm off-white background, fine scanlines, technical grid, black typography.",
        background = Color(0xFFF5F4EE),
        surface = Color(0xFFEBE9E1),
        surfaceBorder = Color(0xFF141416),
        textPrimary = Color(0xFF141416),
        textSecondary = Color(0xFF5A5852),
        textAccent = Color(0xFF000000),
        buttonBackground = Color(0xFFEBE9E1),
        buttonBorder = Color(0xFF141416),
        buttonText = Color(0xFF141416),
        buttonActiveBackground = Color(0xFF141416),
        buttonActiveText = Color(0xFFF5F4EE),
        crtColor = Color(0xFF141416),
        phosphorGlow = false,
        fontFamily = FontFamily.Monospace,
        hasDottedGrid = true
    )

    val Crt = RetroPalette(
        id = RetroThemeId.CRT,
        name = "02 — CRT",
        description = "Dark monochrome display. Subtle screen texture, soft grayscale glow, CRT scanlines.",
        background = Color(0xFF101014),
        surface = Color(0xFF191A20),
        surfaceBorder = Color(0xFF383A44),
        textPrimary = Color(0xFFF0F0F5),
        textSecondary = Color(0xFF8A8C99),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF191A20),
        buttonBorder = Color(0xFFF0F0F5),
        buttonText = Color(0xFFF0F0F5),
        buttonActiveBackground = Color(0xFFF0F0F5),
        buttonActiveText = Color(0xFF101014),
        crtColor = Color(0xFFF0F0F5),
        phosphorGlow = true,
        fontFamily = FontFamily.Monospace,
        hasDottedGrid = false
    )

    val Terminal = RetroPalette(
        id = RetroThemeId.TERMINAL,
        name = "03 — Terminal",
        description = "Command-line inspired interface. High contrast console typography.",
        background = Color(0xFF08090A),
        surface = Color(0xFF121417),
        surfaceBorder = Color(0xFF2C3038),
        textPrimary = Color(0xFFECECF0),
        textSecondary = Color(0xFF7A808C),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF121417),
        buttonBorder = Color(0xFFECECF0),
        buttonText = Color(0xFFECECF0),
        buttonActiveBackground = Color(0xFFECECF0),
        buttonActiveText = Color(0xFF08090A),
        crtColor = Color(0xFFECECF0),
        phosphorGlow = true,
        fontFamily = FontFamily.Monospace
    )

    val Industrial = RetroPalette(
        id = RetroThemeId.INDUSTRIAL,
        name = "04 — Industrial",
        description = "Technical control-panel aesthetic. Dense grid, measurement markings, structured information blocks.",
        background = Color(0xFF16171A),
        surface = Color(0xFF202227),
        surfaceBorder = Color(0xFF424652),
        textPrimary = Color(0xFFE8E8EE),
        textSecondary = Color(0xFF9094A0),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF202227),
        buttonBorder = Color(0xFF424652),
        buttonText = Color(0xFFE8E8EE),
        buttonActiveBackground = Color(0xFFE8E8EE),
        buttonActiveText = Color(0xFF16171A),
        crtColor = Color(0xFFE8E8EE),
        phosphorGlow = false,
        fontFamily = FontFamily.Monospace,
        hasDottedGrid = true
    )

    val PureMonochrome = RetroPalette(
        id = RetroThemeId.PURE_MONOCHROME,
        name = "Pure Monochrome",
        description = "Deep OLED true black with crisp high-contrast white typography.",
        background = Color(0xFF000000),
        surface = Color(0xFF101012),
        surfaceBorder = Color(0xFF28282E),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA0A0AA),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF141418),
        buttonBorder = Color(0xFF3E3E48),
        buttonText = Color(0xFFFFFFFF),
        buttonActiveBackground = Color(0xFFFFFFFF),
        buttonActiveText = Color(0xFF000000),
        crtColor = Color(0xFFFFFFFF),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif
    )

    val CrtGreen = RetroPalette(
        id = RetroThemeId.CRT_GREEN,
        name = "CRT Phosphor Green",
        description = "Vintage 1982 IBM 5151 green phosphor terminal aesthetic.",
        background = Color(0xFF051105),
        surface = Color(0xFF0D220D),
        surfaceBorder = Color(0xFF1E521E),
        textPrimary = Color(0xFF33FF33),
        textSecondary = Color(0xFF22AA22),
        textAccent = Color(0xFF66FF66),
        buttonBackground = Color(0xFF0D220D),
        buttonBorder = Color(0xFF33FF33),
        buttonText = Color(0xFF33FF33),
        buttonActiveBackground = Color(0xFF33FF33),
        buttonActiveText = Color(0xFF051105),
        crtColor = Color(0xFF33FF33),
        phosphorGlow = true,
        fontFamily = FontFamily.Monospace
    )

    val CrtAmber = RetroPalette(
        id = RetroThemeId.CRT_AMBER,
        name = "CRT Phosphor Amber",
        description = "Warm 1980s monochrome orange-amber phosphor glow.",
        background = Color(0xFF120700),
        surface = Color(0xFF241100),
        surfaceBorder = Color(0xFF5A2E00),
        textPrimary = Color(0xFFFFB000),
        textSecondary = Color(0xFFB37B00),
        textAccent = Color(0xFFFFCC33),
        buttonBackground = Color(0xFF241100),
        buttonBorder = Color(0xFFFFB000),
        buttonText = Color(0xFFFFB000),
        buttonActiveBackground = Color(0xFFFFB000),
        buttonActiveText = Color(0xFF120700),
        crtColor = Color(0xFFFFB000),
        phosphorGlow = true,
        fontFamily = FontFamily.Monospace
    )

    val TerminalMatrix = RetroPalette(
        id = RetroThemeId.TERMINAL_MATRIX,
        name = "Terminal VT100",
        description = "Classic Unix mainframe workstation console.",
        background = Color(0xFF0A0F0D),
        surface = Color(0xFF141F1A),
        surfaceBorder = Color(0xFF2A4237),
        textPrimary = Color(0xFF00FF88),
        textSecondary = Color(0xFF00AA5B),
        textAccent = Color(0xFF70FFB8),
        buttonBackground = Color(0xFF141F1A),
        buttonBorder = Color(0xFF00FF88),
        buttonText = Color(0xFF00FF88),
        buttonActiveBackground = Color(0xFF00FF88),
        buttonActiveText = Color(0xFF0A0F0D),
        crtColor = Color(0xFF00FF88),
        phosphorGlow = true,
        fontFamily = FontFamily.Monospace
    )

    val RetroWindows = RetroPalette(
        id = RetroThemeId.RETRO_WINDOWS,
        name = "Retro 95 System",
        description = "Classic 90s desktop computing with clean beveled accents.",
        background = Color(0xFF008080),
        surface = Color(0xFFC0C0C0),
        surfaceBorder = Color(0xFF808080),
        textPrimary = Color(0xFF000000),
        textSecondary = Color(0xFF404040),
        textAccent = Color(0xFF000080),
        buttonBackground = Color(0xFFC0C0C0),
        buttonBorder = Color(0xFF000000),
        buttonText = Color(0xFF000000),
        buttonActiveBackground = Color(0xFF000080),
        buttonActiveText = Color(0xFFFFFFFF),
        crtColor = Color(0xFF000000),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif,
        isBeveled = true
    )

    val PixelArcade = RetroPalette(
        id = RetroThemeId.PIXEL_ARCADE,
        name = "Pixel Arcade",
        description = "Monochromatic dot-matrix display with arcade crispness.",
        background = Color(0xFF0F0F14),
        surface = Color(0xFF1A1A24),
        surfaceBorder = Color(0xFF38384D),
        textPrimary = Color(0xFFECECF0),
        textSecondary = Color(0xFF9494A8),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF1A1A24),
        buttonBorder = Color(0xFFECECF0),
        buttonText = Color(0xFFECECF0),
        buttonActiveBackground = Color(0xFFECECF0),
        buttonActiveText = Color(0xFF0F0F14),
        crtColor = Color(0xFFECECF0),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif,
        hasDottedGrid = true
    )

    val MinimalEInk = RetroPalette(
        id = RetroThemeId.MINIMAL_EINK,
        name = "Minimal E-Ink",
        description = "Soft warm reflective paper texture with zero glare.",
        background = Color(0xFFF4F3EE),
        surface = Color(0xFFE8E6DF),
        surfaceBorder = Color(0xFFB8B5AA),
        textPrimary = Color(0xFF181816),
        textSecondary = Color(0xFF5E5C56),
        textAccent = Color(0xFF000000),
        buttonBackground = Color(0xFFE8E6DF),
        buttonBorder = Color(0xFF181816),
        buttonText = Color(0xFF181816),
        buttonActiveBackground = Color(0xFF181816),
        buttonActiveText = Color(0xFFF4F3EE),
        crtColor = Color(0xFF181816),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif
    )

    val DarkPaper = RetroPalette(
        id = RetroThemeId.DARK_PAPER,
        name = "Dark Paper",
        description = "Charcoal slate background with soft chalk typography.",
        background = Color(0xFF161618),
        surface = Color(0xFF222226),
        surfaceBorder = Color(0xFF3A3A40),
        textPrimary = Color(0xFFE6E6EB),
        textSecondary = Color(0xFF8E8E98),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF222226),
        buttonBorder = Color(0xFFE6E6EB),
        buttonText = Color(0xFFE6E6EB),
        buttonActiveBackground = Color(0xFFE6E6EB),
        buttonActiveText = Color(0xFF161618),
        crtColor = Color(0xFFE6E6EB),
        phosphorGlow = false,
        fontFamily = FontFamily.SansSerif
    )

    val Night = RetroPalette(
        id = RetroThemeId.NIGHT,
        name = "NIGHT OLED",
        description = "Dark OLED-friendly true black interface, minimal glow.",
        background = Color(0xFF000000),
        surface = Color(0xFF0D0D10),
        surfaceBorder = Color(0xFF222228),
        textPrimary = Color(0xFFE4E4E8),
        textSecondary = Color(0xFF72727D),
        textAccent = Color(0xFFFFFFFF),
        buttonBackground = Color(0xFF121216),
        buttonBorder = Color(0xFF2E2E36),
        buttonText = Color(0xFFE4E4E8),
        buttonActiveBackground = Color(0xFFE4E4E8),
        buttonActiveText = Color(0xFF000000),
        crtColor = Color(0xFFE4E4E8),
        phosphorGlow = false,
        fontFamily = FontFamily.Monospace
    )

    fun getPalette(id: RetroThemeId, customColors: CustomThemeColors? = null): RetroPalette {
        return when (id) {
            RetroThemeId.DEMICHROME -> DemichromeBlue
            RetroThemeId.DARK_TERMINAL -> Terminal
            RetroThemeId.PAPER -> Paper
            RetroThemeId.NIGHT -> Night
            RetroThemeId.CUSTOM -> {
                val c = customColors ?: CustomThemeColors()
                RetroPalette(
                    id = RetroThemeId.CUSTOM,
                    name = "CUSTOM",
                    description = "User customizable retro palette.",
                    background = c.bg,
                    surface = c.surface,
                    surfaceBorder = c.border,
                    textPrimary = c.textPrimary,
                    textSecondary = c.textSecondary,
                    textAccent = c.textAccent,
                    buttonBackground = c.surface,
                    buttonBorder = c.border,
                    buttonText = c.textPrimary,
                    buttonActiveBackground = c.textAccent,
                    buttonActiveText = c.bg,
                    crtColor = c.textAccent,
                    phosphorGlow = false,
                    fontFamily = FontFamily.Monospace,
                    surfaceElevated = c.surface,
                    accentSoft = c.border.copy(alpha = 0.35f),
                    icon = c.textAccent,
                    divider = c.border.copy(alpha = 0.4f)
                )
            }
            RetroThemeId.CRT -> Crt
            RetroThemeId.TERMINAL -> Terminal
            RetroThemeId.INDUSTRIAL -> Industrial
            RetroThemeId.PURE_MONOCHROME -> PureMonochrome
            RetroThemeId.CRT_GREEN -> CrtGreen
            RetroThemeId.CRT_AMBER -> CrtAmber
            RetroThemeId.TERMINAL_MATRIX -> TerminalMatrix
            RetroThemeId.RETRO_WINDOWS -> RetroWindows
            RetroThemeId.PIXEL_ARCADE -> PixelArcade
            RetroThemeId.MINIMAL_EINK -> MinimalEInk
            RetroThemeId.DARK_PAPER -> DarkPaper
        }
    }
}

data class CustomThemeColors(
    val bg: Color = Color(0xFF101014),
    val surface: Color = Color(0xFF191A20),
    val textPrimary: Color = Color(0xFFF0F0F5),
    val textSecondary: Color = Color(0xFF8A8C99),
    val textAccent: Color = Color(0xFFFFFFFF),
    val border: Color = Color(0xFF383A44)
)

val LocalRetroPalette = staticCompositionLocalOf { RetroPalettes.DemichromeBlue }

object RetroTheme {
    val palette: RetroPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalRetroPalette.current
}
