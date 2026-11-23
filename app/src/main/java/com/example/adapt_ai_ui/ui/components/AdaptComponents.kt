package com.example.adapt_ai_ui.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

// ============================================================================
// 1. AdaptSurface — Standard Solid Content Container
// ============================================================================

@Composable
fun AdaptSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AdaptTheme.radius.large),
    backgroundColor: Color = AdaptTheme.colors.surface,
    borderColor: Color = AdaptTheme.colors.border,
    borderWidth: Dp = AdaptTheme.stroke.hairline,
    contentPadding: PaddingValues = PaddingValues(AdaptTheme.spacing.large),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

// ============================================================================
// 2. AdaptGlassSurface — Selective Liquid Glass Material
// ============================================================================

/**
 * Selective liquid glass container.
 * Uses restrained translucency and specular gradient borders with an immediate solid
 * fallback. Does not use expensive hardware blur on scrolling content to guarantee
 * smooth 120Hz performance on SoC targets.
 */
@Composable
fun AdaptGlassSurface(
    modifier: Modifier = Modifier,
    isGlassEnabled: Boolean = true,
    shape: Shape = RoundedCornerShape(AdaptTheme.radius.extraLarge),
    contentPadding: PaddingValues = PaddingValues(AdaptTheme.spacing.large),
    content: @Composable () -> Unit
) {
    val bgColor = if (isGlassEnabled) AdaptTheme.colors.glassSurface else AdaptTheme.colors.surface
    val borderBrush = if (isGlassEnabled) {
        Brush.verticalGradient(
            listOf(
                AdaptTheme.colors.glassHighlight,
                AdaptTheme.colors.glassBorder,
                AdaptTheme.colors.borderSubtle
            )
        )
    } else {
        Brush.verticalGradient(listOf(AdaptTheme.colors.border, AdaptTheme.colors.border))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(width = AdaptTheme.stroke.hairline, brush = borderBrush, shape = shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

// ============================================================================
// 3. AdaptPrimaryButton — High-Contrast Tactical Primary Action
// ============================================================================

@Composable
fun AdaptPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .semantics {
                contentDescription = text
            },
        shape = RoundedCornerShape(AdaptTheme.radius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = AdaptTheme.colors.primaryAccent,
            contentColor = if (AdaptTheme.isDark) Color(0xFF0F172A) else Color.White,
            disabledContainerColor = AdaptTheme.colors.raisedSurface,
            disabledContentColor = AdaptTheme.colors.textMuted
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AdaptTheme.colors.textPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))
                }
                Text(
                    text = text,
                    style = AdaptTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. AdaptSecondaryButton — Supporting Action Button
// ============================================================================

@Composable
fun AdaptSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics { contentDescription = text },
        shape = RoundedCornerShape(AdaptTheme.radius.medium),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AdaptTheme.colors.raisedSurface,
            contentColor = AdaptTheme.colors.textPrimary,
            disabledContainerColor = AdaptTheme.colors.surface,
            disabledContentColor = AdaptTheme.colors.textMuted
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.verticalGradient(
                listOf(AdaptTheme.colors.border, AdaptTheme.colors.borderSubtle)
            )
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AdaptTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))
            }
            Text(
                text = text,
                style = AdaptTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// ============================================================================
// 5. AdaptPill — Domain, Status & Filter Chip
// ============================================================================

@Composable
fun AdaptPill(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    icon: ImageVector? = null,
    accentColor: Color = AdaptTheme.colors.primaryAccent,
    onClick: (() -> Unit)? = null
) {
    val containerColor = when {
        isSelected -> accentColor.copy(alpha = if (AdaptTheme.isDark) 0.22f else 0.15f)
        else -> AdaptTheme.colors.raisedSurface
    }
    val contentColor = when {
        isSelected -> accentColor
        else -> AdaptTheme.colors.textSecondary
    }
    val borderColor = when {
        isSelected -> accentColor.copy(alpha = 0.5f)
        else -> AdaptTheme.colors.borderSubtle
    }

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            role = Role.Checkbox,
            onClick = onClick
        ).semantics {
            selected = isSelected
            contentDescription = "$text pill"
        }
    } else Modifier

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AdaptTheme.radius.full))
            .background(containerColor)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = borderColor,
                shape = RoundedCornerShape(AdaptTheme.radius.full)
            )
            .then(clickableMod)
            .padding(horizontal = AdaptTheme.spacing.medium, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(AdaptTheme.spacing.extraSmall))
            }
            Text(
                text = text,
                style = AdaptTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

// ============================================================================
// 6. AdaptSectionHeader — Editorial Section Title
// ============================================================================

@Composable
fun AdaptSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (overline != null) {
            Text(
                text = overline.uppercase(),
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AdaptTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )

            if (actionText != null && onActionClick != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .clickable(role = Role.Button, onClick = onActionClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .semantics { contentDescription = actionText }
                ) {
                    Text(
                        text = actionText,
                        style = AdaptTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AdaptTheme.colors.primaryAccent
                    )
                }
            }
        }
    }
}

// ============================================================================
// 7. AdaptProgressIndicator — Accessible Bar with Smooth Animation
// ============================================================================

@Composable
fun AdaptProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AdaptTheme.colors.secondaryAccent,
    trackColor: Color = AdaptTheme.colors.raisedSurface,
    height: Dp = 6.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "AdaptProgressAnimation"
    )

    val percent = (animatedProgress * 100).toInt()

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(AdaptTheme.radius.full))
            .semantics {
                contentDescription = "Progress: $percent percent"
            },
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

// ============================================================================
// 8. AdaptStatItem — Clean Stat Callout
// ============================================================================

@Composable
fun AdaptStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AdaptTheme.colors.primaryAccent,
    icon: ImageVector? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(AdaptTheme.spacing.extraSmall))
            }
            Text(
                text = value,
                style = AdaptTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = AdaptTheme.typography.bodySmall,
            color = AdaptTheme.colors.textSecondary
        )
    }
}

// ============================================================================
// 9. AdaptCodeBlock — High-Contrast Monospace Snippet with Copy Action
// ============================================================================

@Composable
fun AdaptCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String? = null
) {
    val clipboard = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
            .background(AdaptTheme.colors.codeBackground)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = AdaptTheme.colors.borderSubtle,
                shape = RoundedCornerShape(AdaptTheme.radius.medium)
            )
            .padding(AdaptTheme.spacing.medium)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (language ?: "CODE").uppercase(),
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AdaptTheme.colors.primaryAccent
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .clickable(role = Role.Button) {
                            clipboard.setText(AnnotatedString(code))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Copy code to clipboard" }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AdaptIcons.Copy,
                            contentDescription = null,
                            tint = AdaptTheme.colors.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = AdaptTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            Text(
                text = code,
                style = AdaptTheme.typographyExt.codeSnippet,
                color = AdaptTheme.colors.textPrimary
            )
        }
    }
}

// ============================================================================
// 10. AdaptTimelineNode — Visual Connected Milestone Stepper
// ============================================================================

enum class TimelineNodeState {
    COMPLETED,
    IN_PROGRESS,
    LOCKED
}

@Composable
fun AdaptTimelineNode(
    stepNumber: Int,
    title: String,
    subtitle: String,
    state: TimelineNodeState,
    modifier: Modifier = Modifier,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val nodeColor = when (state) {
        TimelineNodeState.COMPLETED -> AdaptTheme.colors.secondaryAccent
        TimelineNodeState.IN_PROGRESS -> AdaptTheme.colors.primaryAccent
        TimelineNodeState.LOCKED -> AdaptTheme.colors.borderSubtle
    }

    val clickMod = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod)
            .padding(vertical = AdaptTheme.spacing.small),
        verticalAlignment = Alignment.Top
    ) {
        // Spine Node Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            TimelineNodeState.COMPLETED -> nodeColor
                            TimelineNodeState.IN_PROGRESS -> nodeColor.copy(alpha = 0.2f)
                            TimelineNodeState.LOCKED -> AdaptTheme.colors.raisedSurface
                        }
                    )
                    .border(
                        width = if (state == TimelineNodeState.IN_PROGRESS) 2.dp else 1.dp,
                        color = nodeColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    TimelineNodeState.COMPLETED -> Icon(
                        imageVector = AdaptIcons.Check,
                        contentDescription = "Completed",
                        tint = if (AdaptTheme.isDark) Color(0xFF042F2E) else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    TimelineNodeState.IN_PROGRESS -> Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(nodeColor)
                    )
                    TimelineNodeState.LOCKED -> Icon(
                        imageVector = AdaptIcons.Lock,
                        contentDescription = "Locked",
                        tint = AdaptTheme.colors.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            if (state == TimelineNodeState.COMPLETED)
                                AdaptTheme.colors.secondaryAccent.copy(alpha = 0.6f)
                            else
                                AdaptTheme.colors.borderSubtle
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(AdaptTheme.spacing.medium))

        // Text & Detail Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "STEP $stepNumber",
                style = AdaptTheme.typographyExt.overline,
                color = when (state) {
                    TimelineNodeState.IN_PROGRESS -> AdaptTheme.colors.primaryAccent
                    TimelineNodeState.COMPLETED -> AdaptTheme.colors.secondaryAccent
                    TimelineNodeState.LOCKED -> AdaptTheme.colors.textMuted
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = AdaptTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (state == TimelineNodeState.LOCKED)
                    AdaptTheme.colors.textMuted
                else
                    AdaptTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }
    }
}

// ============================================================================
// Previews for Editorial Obsidian Liquid Glass Design Components
// ============================================================================

@Composable
private fun AdaptComponentCatalog() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdaptTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdaptSectionHeader(
            title = "Design System Components",
            overline = "Phase 1 Foundation",
            actionText = "See All",
            onActionClick = {}
        )

        AdaptGlassSurface {
            Column {
                Text(
                    text = "AdaptGlassSurface (Selective Liquid Glass)",
                    style = AdaptTheme.typography.titleMedium,
                    color = AdaptTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Restrained translucency with specular highlights and zero runtime blur cost.",
                    style = AdaptTheme.typography.bodySmall,
                    color = AdaptTheme.colors.textSecondary
                )
            }
        }

        AdaptSurface {
            Column {
                Text(
                    text = "AdaptSurface (Solid Obsidian/Pearl Container)",
                    style = AdaptTheme.typography.titleMedium,
                    color = AdaptTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdaptPill(text = "ARM Cortex-M55", isSelected = true)
                    AdaptPill(text = "INT8 Quantization", isSelected = false)
                }
            }
        }

        AdaptPrimaryButton(
            text = "Continue to Practice Set",
            onClick = {},
            leadingIcon = AdaptIcons.Play
        )

        AdaptSecondaryButton(
            text = "Ask AI Tutor (Socratic Mode)",
            onClick = {},
            leadingIcon = AdaptIcons.Sparkle
        )

        AdaptProgressIndicator(progress = 0.75f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AdaptStatItem(value = "82%", label = "Concept Mastery")
            AdaptStatItem(value = "25m", label = "Daily Study Time")
            AdaptStatItem(value = "6 Days", label = "Current Streak", icon = AdaptIcons.Target)
        }

        AdaptCodeBlock(
            code = "// Affine quantization:\nq = round(r / S) + Z",
            language = "C++"
        )

        AdaptTimelineNode(
            stepNumber = 2,
            title = "INT8 Weight Quantization",
            subtitle = "Active Milestone • Step 3 of 4",
            state = TimelineNodeState.IN_PROGRESS
        )
    }
}

@Preview(name = "Components - Dark Mode", showBackground = true)
@Composable
fun AdaptComponentsDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        AdaptComponentCatalog()
    }
}

@Preview(name = "Components - Light Mode", showBackground = true)
@Composable
fun AdaptComponentsLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        AdaptComponentCatalog()
    }
}
