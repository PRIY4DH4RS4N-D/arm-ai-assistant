package com.example.adapt_ai_ui.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// ADAPT-AI Editorial Obsidian Liquid Glass Design Tokens
// ============================================================================

// Dark Theme: Obsidian Canvas & Surfaces
val ObsidianBackground = Color(0xFF0B0D12)
val ObsidianSurface = Color(0xFF151923)
val ObsidianRaisedSurface = Color(0xFF1C222D)
val ObsidianElevated = Color(0xFF242B38)
val ObsidianBorder = Color(0xFF2B3342)
val ObsidianBorderSubtle = Color(0xFF1F2633)
val ObsidianTextPrimary = Color(0xFFF2F4F8)
val ObsidianTextSecondary = Color(0xFFA2AAB8)
val ObsidianTextMuted = Color(0xFF6B7280)

// Light Theme: Soft Pearl Canvas & Surfaces
val PearlBackground = Color(0xFFF4F5F8)
val PearlSurface = Color(0xFFFFFFFF)
val PearlRaisedSurface = Color(0xFFECEFF4)
val PearlElevated = Color(0xFFE2E6EE)
val PearlBorder = Color(0xFFD6DBE5)
val PearlBorderSubtle = Color(0xFFE5E9F0)
val PearlTextPrimary = Color(0xFF171B24)
val PearlTextSecondary = Color(0xFF626B7A)
val PearlTextMuted = Color(0xFF94A3B8)

// Brand Core Accents: Muted Violet (Primary) & Restrained Teal (Secondary)
val VioletPrimaryDark = Color(0xFFA99AFB)          // Dark theme primary accent
val VioletPrimaryLight = Color(0xFF6956D9)         // Light theme primary accent
val VioletContainerDark = Color(0xFF2E2459)
val VioletContainerLight = Color(0xFFEDE9FE)
val VioletOnContainerDark = Color(0xFFDDD6FE)
val VioletOnContainerLight = Color(0xFF2E1065)

val TealSecondaryDark = Color(0xFF72D6C2)          // Dark theme secondary accent
val TealSecondaryLight = Color(0xFF168D7A)         // Light theme secondary accent
val TealContainerDark = Color(0xFF13453D)
val TealContainerLight = Color(0xFFCCFBF1)
val TealOnContainerDark = Color(0xFFCCFBF1)
val TealOnContainerLight = Color(0xFF0F473D)

// Status & Semantic Feedback Colors
val StatusSuccessDark = Color(0xFF34D399)
val StatusSuccessLight = Color(0xFF059669)
val StatusWarningDark = Color(0xFFFBBF24)
val StatusWarningLight = Color(0xFFD97706)
val StatusErrorDark = Color(0xFFF87171)
val StatusErrorLight = Color(0xFFDC2626)

// Liquid Glass Translucency Tokens (Restrained, no expensive runtime blur required)
val GlassSurfaceDark = Color(0xD9151923)           // 85% opacity obsidian
val GlassBorderDark = Color(0x33A99AFB)            // 20% violet specular reflection
val GlassHighlightDark = Color(0x1AFFFFFF)         // 10% white top specular rim

val GlassSurfaceLight = Color(0xE6FFFFFF)          // 90% opacity soft pearl
val GlassBorderLight = Color(0x406956D9)           // 25% deep violet specular reflection
val GlassHighlightLight = Color(0x80FFFFFF)        // 50% white top specular rim

// Legacy / Backward Compatibility Aliases for Unmodified Screen References
val ElectricViolet = VioletPrimaryDark
val ElectricVioletDark = VioletPrimaryLight
val ElectricVioletLight = VioletPrimaryDark
val ElectricVioletContainerDark = VioletContainerDark
val ElectricVioletContainerLight = VioletContainerLight

val SubtleTeal = TealSecondaryDark
val SubtleTealDark = TealSecondaryLight
val SubtleTealLight = TealSecondaryDark
val SubtleTealContainerDark = TealContainerDark
val SubtleTealContainerLight = TealContainerLight

val AccentAmber = StatusWarningDark
val AccentCoral = StatusErrorDark

val MidnightBackground = ObsidianBackground
val MidnightSurface = ObsidianSurface
val MidnightSurfaceVariant = ObsidianRaisedSurface
val MidnightSurfaceContainer = ObsidianElevated
val MidnightOutline = ObsidianBorder
val MidnightOutlineVariant = ObsidianBorderSubtle
val MidnightOnSurface = ObsidianTextPrimary
val MidnightOnSurfaceVariant = ObsidianTextSecondary

val LightBackground = PearlBackground
val LightSurface = PearlSurface
val LightSurfaceVariant = PearlRaisedSurface
val LightSurfaceContainer = PearlElevated
val LightOutline = PearlBorder
val LightOutlineVariant = PearlBorderSubtle
val LightOnSurface = PearlTextPrimary
val LightOnSurfaceVariant = PearlTextSecondary
