package com.example.adapt_ai_ui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Editorial Obsidian Liquid Glass Spacing, Radii, and Elevation Design Tokens.
 * Promotes consistent layout rhythm across all screens and reusable components.
 */
@Immutable
data class AdaptSpacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 20.dp,
    val huge: Dp = 24.dp,
    val giant: Dp = 32.dp,
    val colossal: Dp = 40.dp
)

@Immutable
data class AdaptCornerRadius(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 14.dp,
    val large: Dp = 18.dp,
    val extraLarge: Dp = 24.dp,
    val full: Dp = 999.dp
)

@Immutable
data class AdaptElevation(
    val none: Dp = 0.dp,
    val card: Dp = 2.dp,
    val floating: Dp = 6.dp,
    val modal: Dp = 12.dp
)

@Immutable
data class AdaptStroke(
    val hairline: Dp = 1.dp,
    val accent: Dp = 1.5.dp,
    val focus: Dp = 2.dp
)

val LocalAdaptSpacing = staticCompositionLocalOf { AdaptSpacing() }
val LocalAdaptCornerRadius = staticCompositionLocalOf { AdaptCornerRadius() }
val LocalAdaptElevation = staticCompositionLocalOf { AdaptElevation() }
val LocalAdaptStroke = staticCompositionLocalOf { AdaptStroke() }
