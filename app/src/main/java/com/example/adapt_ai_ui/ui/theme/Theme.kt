package com.example.adapt_ai_ui.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// Semantic Color Palette for Editorial Obsidian Liquid Glass
// ============================================================================

@Immutable
data class AdaptSemanticColors(
    val background: Color,
    val surface: Color,
    val raisedSurface: Color,
    val elevatedSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val primaryAccentHover: Color,
    val primaryAccentContainer: Color,
    val onPrimaryAccentContainer: Color,
    val secondaryAccent: Color,
    val secondaryAccentContainer: Color,
    val onSecondaryAccentContainer: Color,
    val border: Color,
    val borderSubtle: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val codeBackground: Color,
    val isDark: Boolean
)

val DarkSemanticColors = AdaptSemanticColors(
    background = ObsidianBackground,
    surface = ObsidianSurface,
    raisedSurface = ObsidianRaisedSurface,
    elevatedSurface = ObsidianElevated,
    textPrimary = ObsidianTextPrimary,
    textSecondary = ObsidianTextSecondary,
    textMuted = ObsidianTextMuted,
    primaryAccent = VioletPrimaryDark,
    primaryAccentHover = Color(0xFF8B7CF6),
    primaryAccentContainer = VioletContainerDark,
    onPrimaryAccentContainer = VioletOnContainerDark,
    secondaryAccent = TealSecondaryDark,
    secondaryAccentContainer = TealContainerDark,
    onSecondaryAccentContainer = TealOnContainerDark,
    border = ObsidianBorder,
    borderSubtle = ObsidianBorderSubtle,
    glassSurface = GlassSurfaceDark,
    glassBorder = GlassBorderDark,
    glassHighlight = GlassHighlightDark,
    success = StatusSuccessDark,
    warning = StatusWarningDark,
    error = StatusErrorDark,
    codeBackground = Color(0xFF0F131C),
    isDark = true
)

val LightSemanticColors = AdaptSemanticColors(
    background = PearlBackground,
    surface = PearlSurface,
    raisedSurface = PearlRaisedSurface,
    elevatedSurface = PearlElevated,
    textPrimary = PearlTextPrimary,
    textSecondary = PearlTextSecondary,
    textMuted = PearlTextMuted,
    primaryAccent = VioletPrimaryLight,
    primaryAccentHover = Color(0xFF5B47CC),
    primaryAccentContainer = VioletContainerLight,
    onPrimaryAccentContainer = VioletOnContainerLight,
    secondaryAccent = TealSecondaryLight,
    secondaryAccentContainer = TealContainerLight,
    onSecondaryAccentContainer = TealOnContainerLight,
    border = PearlBorder,
    borderSubtle = PearlBorderSubtle,
    glassSurface = GlassSurfaceLight,
    glassBorder = GlassBorderLight,
    glassHighlight = GlassHighlightLight,
    success = StatusSuccessLight,
    warning = StatusWarningLight,
    error = StatusErrorLight,
    codeBackground = Color(0xFFE9EDF4),
    isDark = false
)

val LocalAdaptColors = staticCompositionLocalOf { DarkSemanticColors }

// Material 3 Color Schemes aligned with Semantic Tokens
private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = VioletOnContainerDark,

    secondary = TealSecondaryDark,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = TealOnContainerDark,

    tertiary = StatusWarningDark,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),

    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,

    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianRaisedSurface,
    onSurfaceVariant = ObsidianTextSecondary,
    surfaceContainerHighest = ObsidianElevated,

    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderSubtle,

    error = StatusErrorDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = VioletOnContainerLight,

    secondary = TealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = TealContainerLight,
    onSecondaryContainer = TealOnContainerLight,

    tertiary = StatusWarningLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),

    background = PearlBackground,
    onBackground = PearlTextPrimary,

    surface = PearlSurface,
    onSurface = PearlTextPrimary,
    surfaceVariant = PearlRaisedSurface,
    onSurfaceVariant = PearlTextSecondary,
    surfaceContainerHighest = PearlElevated,

    outline = PearlBorder,
    outlineVariant = PearlBorderSubtle,

    error = StatusErrorLight,
    onError = Color.White
)

/**
 * Access point for ADAPT-AI design tokens.
 */
object AdaptTheme {
    val colors: AdaptSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val typographyExt: AdaptTypographyExt
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptTypographyExt.current

    val spacing: AdaptSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptSpacing.current

    val radius: AdaptCornerRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptCornerRadius.current

    val elevation: AdaptElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptElevation.current

    val stroke: AdaptStroke
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptStroke.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptColors.current.isDark
}

@Composable
fun ADAPTAIUITheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val targetColors = if (darkTheme) DarkColorScheme else LightColorScheme
    val targetSemanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAdaptColors provides targetSemanticColors,
        LocalAdaptSpacing provides AdaptSpacing(),
        LocalAdaptCornerRadius provides AdaptCornerRadius(),
        LocalAdaptElevation provides AdaptElevation(),
        LocalAdaptStroke provides AdaptStroke(),
        LocalAdaptTypographyExt provides AdaptTypographyExt()
    ) {
        MaterialTheme(
            colorScheme = targetColors,
            typography = Typography,
            content = content
        )
    }
}
