package com.example.adapt_ai_ui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.MilestoneStatus
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.PracticeMode
import androidx.compose.material3.TextButton
import com.example.adapt_ai_ui.data.PathwayNode
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPill
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptProgressIndicator
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

/**
 * Editorial Obsidian Liquid Glass Learning Journey Screen for ADAPT-AI.
 *
 * Visual & Functional Architecture (Phase 3):
 * 1. Editorial Header: Back navigation, track indicator, and track-switching affordance to Explore.
 * 2. Curriculum Overview Ribbon: Authoritative progress percentage, completed vs in-progress breakdown,
 *    and high-contrast progress indicator.
 * 3. Recommended Next Step Callout: Highlights active milestone with direct lesson continuation and Socratic inquiry.
 * 4. Open Connected Learning Timeline: Continuous vertical pathway breathing on the obsidian canvas,
 *    avoiding isolated bulky rectangular cards. Each milestone supports Review, Continue, and Socratic Inquiry.
 */
@Composable
fun PathwayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onAskTutor: (TutorQuickPrompt) -> Unit = {},
    modifier: Modifier = Modifier,
    onPractice: (PracticeMode) -> Unit = {}
) {
    val learner = SampleData.learnerProfile
    val nodes = SampleData.pathwayPreviewNodes
    val completedCount = nodes.count { it.status == MilestoneStatus.COMPLETED }
    val inProgressCount = nodes.count { it.status == MilestoneStatus.IN_PROGRESS }
    val remainingCount = (nodes.size - completedCount - inProgressCount).coerceAtLeast(0)
    val completionPercent = if (nodes.isNotEmpty()) (completedCount * 100 / nodes.size) else 0

    val activeNode = nodes.firstOrNull { it.status == MilestoneStatus.IN_PROGRESS }
    val weakest = AdaptiveLearningEngine.topicStats.values
        .filter { it.totalAttempts > 0 && it.correctAttempts < it.totalAttempts }
        .minByOrNull { it.masteryPercentage }
    val revisionCount = AdaptiveLearningEngine.getUnresolvedMistakes().size
    val plannedLessonMinutes = minOf(learner.dailyMinutesGoal, activeNode?.estimatedMinutes ?: 0)
    val practiceMinutes = (learner.dailyMinutesGoal - plannedLessonMinutes).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.extraLarge,
            end = AdaptTheme.spacing.extraLarge,
            top = AdaptTheme.spacing.large,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.extraLarge)
    ) {
        // --------------------------------------------------------------------
        // A. Editorial Header
        // --------------------------------------------------------------------
        item {
            JourneyEditorialHeader(
                domainTitle = learner.activeDomainTitle,
                onNavigateBack = onNavigateBack,
                onNavigateToExplore = onNavigateToExplore
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(learner.activeGoal, style = AdaptTheme.typography.titleMedium, color = AdaptTheme.colors.textPrimary)
                Text(
                    "${learner.skillLevel.label} ? ${learner.dailyMinutesGoal} min daily",
                    style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary
                )
                if (plannedLessonMinutes > 0) {
                    Text(
                        "Today ? ${plannedLessonMinutes} min on ${activeNode?.title}" +
                            if (practiceMinutes > 0) " ? ${practiceMinutes} min practice" else "",
                        style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary
                    )
                }
                if (weakest != null) {
                    Text(
                        "Review ${weakest.topic}. ${weakest.correctAttempts} of ${weakest.totalAttempts} attempts correct.",
                        style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary
                    )
                    TextButton(onClick = { onPractice(if (revisionCount > 0) PracticeMode.REVISION else PracticeMode.SESSION) }) {
                        Text(if (revisionCount > 0) "Open revision queue" else "Practise again")
                    }
                } else if (AdaptiveLearningEngine.topicStats.isEmpty()) {
                    TextButton(onClick = { onPractice(PracticeMode.SESSION) }) { Text("Start a baseline practice set") }
                }
            }
        }

        // --------------------------------------------------------------------
        // B. Curriculum Progress Overview Ribbon (Liquid Glass)
        // --------------------------------------------------------------------
        item {
            JourneyCurriculumOverviewRibbon(
                completionPercent = completionPercent,
                completedCount = completedCount,
                inProgressCount = inProgressCount,
                remainingCount = remainingCount,
                totalNodes = nodes.size
            )
        }

        // --------------------------------------------------------------------
        // C. Recommended Next Action Callout (if active milestone exists)
        // --------------------------------------------------------------------
        if (activeNode != null) {
            item {
                JourneyRecommendedActionCallout(
                    activeNode = activeNode,
                    onResumeLesson = onNavigateToLesson,
                    onAskTutor = {
                        onAskTutor(
                            TutorQuickPrompt(
                                id = "pathway_rec_${activeNode.id}",
                                prompt = "I'm ready to learn ${activeNode.title}. Can you explain its key principles and why it's vital for ${activeNode.track}?",
                                tag = "Next Step"
                            )
                        )
                    }
                )
            }
        }

        // --------------------------------------------------------------------
        // D. Connected Learning Timeline Section Header
        // --------------------------------------------------------------------
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = learner.activeDomainTitle.uppercase(),
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Curriculum Milestones",
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.textPrimary
                )
            }
        }

        // --------------------------------------------------------------------
        // E. Open Connected Learning Timeline (Items on Canvas)
        // --------------------------------------------------------------------
        itemsIndexed(nodes) { index, node ->
            JourneyTimelineMilestone(
                node = node,
                isLast = index == nodes.lastIndex,
                onContinueLesson = onNavigateToLesson,
                onAskTutor = {
                    onAskTutor(
                        TutorQuickPrompt(
                            id = "milestone_${node.id}",
                            prompt = "Explain ${node.title} and why it's critical for ${node.track}.",
                            tag = "Milestone ${node.index}"
                        )
                    )
                }
            )
        }
    }
}

// ============================================================================
// Subcomponents for Clean Architecture & Reusability
// ============================================================================

/**
 * Editorial top navigation header for the Learning Journey.
 */
@Composable
private fun JourneyEditorialHeader(
    domainTitle: String,
    onNavigateBack: () -> Unit,
    onNavigateToExplore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Accessible Back Button Touchpoint (>= 48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = CircleShape
                    )
                    .clickable(
                        role = Role.Button,
                        onClick = onNavigateBack
                    )
                    .semantics { contentDescription = "Back to previous screen" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AdaptIcons.ArrowBack,
                    contentDescription = null,
                    tint = AdaptTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(AdaptTheme.spacing.medium))

            Column {
                Text(
                    text = "LEARNING JOURNEY",
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = domainTitle,
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.textPrimary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

        // Switch Track Pill Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AdaptTheme.radius.full))
                .background(AdaptTheme.colors.raisedSurface)
                .border(
                    width = AdaptTheme.stroke.hairline,
                    color = AdaptTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(AdaptTheme.radius.full)
                )
                .clickable(
                    role = Role.Button,
                    onClick = onNavigateToExplore
                )
                .semantics { contentDescription = "Switch learning track in Domain Explorer" }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AdaptIcons.Compass,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AdaptTheme.colors.primaryAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tracks",
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.primaryAccent
                )
            }
        }
    }
}

/**
 * Translucent Liquid Glass Curriculum Overview Ribbon.
 */
@Composable
private fun JourneyCurriculumOverviewRibbon(
    completionPercent: Int,
    completedCount: Int,
    inProgressCount: Int,
    remainingCount: Int,
    totalNodes: Int
) {
    val ribbonShape = RoundedCornerShape(AdaptTheme.radius.large)

    val topBorderBrush = Brush.horizontalGradient(
        listOf(
            Color.Transparent,
            AdaptTheme.colors.glassBorder,
            AdaptTheme.colors.glassHighlight,
            AdaptTheme.colors.glassBorder,
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ribbonShape)
            .background(AdaptTheme.colors.glassSurface)
            .border(
                width = AdaptTheme.stroke.hairline,
                brush = topBorderBrush,
                shape = ribbonShape
            )
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRICULUM MILESTONE PROGRESS",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completionPercent% Completed",
                        style = AdaptTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedCount completed • $inProgressCount active • $remainingCount locked",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                }

                // Completion Pill Badge
                AdaptPill(
                    text = "$completedCount of $totalNodes Milestones",
                    isSelected = true,
                    accentColor = AdaptTheme.colors.secondaryAccent
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            AdaptProgressIndicator(
                progress = if (totalNodes > 0) completedCount.toFloat() / totalNodes else 0f,
                color = AdaptTheme.colors.secondaryAccent,
                height = 4.dp
            )
        }
    }
}

/**
 * Recommended Next Action Callout for the active milestone.
 */
@Composable
private fun JourneyRecommendedActionCallout(
    activeNode: PathwayNode,
    onResumeLesson: () -> Unit,
    onAskTutor: () -> Unit
) {
    val calloutShape = RoundedCornerShape(AdaptTheme.radius.large)

    val ambientGlow = Brush.radialGradient(
        colors = listOf(
            AdaptTheme.colors.primaryAccent.copy(alpha = 0.12f),
            Color.Transparent
        ),
        center = Offset(800f, 0f),
        radius = 600f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(calloutShape)
            .background(AdaptTheme.colors.raisedSurface)
            .background(ambientGlow)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = AdaptTheme.colors.primaryAccent.copy(alpha = 0.35f),
                shape = calloutShape
            )
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.primaryAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECOMMENDED NEXT STEP",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent
                    )
                }

                Text(
                    text = "~${activeNode.estimatedMinutes} mins",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            Text(
                text = activeNode.title,
                style = AdaptTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${activeNode.track} • Continue practical edge deployment steps",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AdaptPrimaryButton(
                        text = "Continue Lesson",
                        onClick = onResumeLesson,
                        leadingIcon = AdaptIcons.Play
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    AdaptSecondaryButton(
                        text = "Ask AI Tutor",
                        onClick = onAskTutor,
                        leadingIcon = AdaptIcons.Sparkle
                    )
                }
            }
        }
    }
}

/**
 * Open Connected Learning Timeline Milestone item.
 *
 * Sits directly on the canvas with a vertical connector spine,
 * clear state representation (Completed, In-Progress, Locked),
 * and rich interaction affordances (Review, Continue, Socratic Ask AI).
 */
@Composable
private fun JourneyTimelineMilestone(
    node: PathwayNode,
    isLast: Boolean,
    onContinueLesson: () -> Unit,
    onAskTutor: () -> Unit
) {
    val stateColor = when (node.status) {
        MilestoneStatus.COMPLETED -> AdaptTheme.colors.secondaryAccent
        MilestoneStatus.IN_PROGRESS -> AdaptTheme.colors.primaryAccent
        MilestoneStatus.LOCKED -> AdaptTheme.colors.borderSubtle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Milestone ${node.index}: ${node.title}, ${node.status.name}"
            },
        verticalAlignment = Alignment.Top
    ) {
        // Spine Column: Indicator Circle + Continuous Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (node.status) {
                            MilestoneStatus.COMPLETED -> stateColor
                            MilestoneStatus.IN_PROGRESS -> stateColor.copy(alpha = 0.2f)
                            MilestoneStatus.LOCKED -> AdaptTheme.colors.raisedSurface
                        }
                    )
                    .border(
                        width = if (node.status == MilestoneStatus.IN_PROGRESS) 2.dp else 1.dp,
                        color = stateColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (node.status) {
                    MilestoneStatus.COMPLETED -> {
                        Icon(
                            imageVector = AdaptIcons.Check,
                            contentDescription = "Completed",
                            tint = if (AdaptTheme.isDark) Color(0xFF042F2E) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    MilestoneStatus.IN_PROGRESS -> {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(stateColor)
                        )
                    }
                    MilestoneStatus.LOCKED -> {
                        Icon(
                            imageVector = AdaptIcons.Lock,
                            contentDescription = "Locked",
                            tint = AdaptTheme.colors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(84.dp)
                        .background(
                            if (node.status == MilestoneStatus.COMPLETED)
                                AdaptTheme.colors.secondaryAccent.copy(alpha = 0.5f)
                            else
                                AdaptTheme.colors.borderSubtle
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(AdaptTheme.spacing.medium))

        // Content Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) AdaptTheme.spacing.large else AdaptTheme.spacing.extraSmall)
        ) {
            // Milestone Subheading & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MILESTONE ${node.index}",
                    style = AdaptTheme.typographyExt.overline,
                    color = if (node.status == MilestoneStatus.LOCKED)
                        AdaptTheme.colors.textMuted
                    else
                        AdaptTheme.colors.primaryAccent
                )

                Text(
                    text = when (node.status) {
                        MilestoneStatus.COMPLETED -> "Done ✓"
                        MilestoneStatus.IN_PROGRESS -> "Active"
                        MilestoneStatus.LOCKED -> "Locked"
                    },
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = when (node.status) {
                        MilestoneStatus.COMPLETED -> AdaptTheme.colors.secondaryAccent
                        MilestoneStatus.IN_PROGRESS -> AdaptTheme.colors.primaryAccent
                        MilestoneStatus.LOCKED -> AdaptTheme.colors.textMuted
                    }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Title
            Text(
                text = node.title,
                style = AdaptTheme.typography.titleMedium.copy(
                    fontWeight = if (node.status == MilestoneStatus.IN_PROGRESS) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (node.status == MilestoneStatus.LOCKED)
                    AdaptTheme.colors.textMuted
                else
                    AdaptTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Track & Estimated Time
            Text(
                text = "${node.track} • ~${node.estimatedMinutes} mins",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )

            // Contextual Helper Text
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (node.status) {
                    MilestoneStatus.COMPLETED -> "Milestone completed. Revisit concepts anytime to retain mastery."
                    MilestoneStatus.IN_PROGRESS -> "Current focus. Deep dive into practical implementation steps."
                    MilestoneStatus.LOCKED -> "Prerequisite: Master Milestone ${node.index - 1} to unlock this content."
                },
                style = AdaptTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (node.status == MilestoneStatus.LOCKED)
                    AdaptTheme.colors.textMuted
                else
                    AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Interaction Affordances Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (node.status) {
                    MilestoneStatus.COMPLETED -> {
                        AdaptPill(
                            text = "Review",
                            icon = AdaptIcons.Retry,
                            isSelected = false,
                            accentColor = AdaptTheme.colors.secondaryAccent,
                            onClick = onContinueLesson
                        )
                        AdaptPill(
                            text = "Ask AI",
                            icon = AdaptIcons.Sparkle,
                            isSelected = false,
                            accentColor = AdaptTheme.colors.primaryAccent,
                            onClick = onAskTutor
                        )
                    }

                    MilestoneStatus.IN_PROGRESS -> {
                        AdaptPill(
                            text = "Continue",
                            icon = AdaptIcons.Play,
                            isSelected = true,
                            accentColor = AdaptTheme.colors.primaryAccent,
                            onClick = onContinueLesson
                        )
                        AdaptPill(
                            text = "Ask AI",
                            icon = AdaptIcons.Sparkle,
                            isSelected = false,
                            accentColor = AdaptTheme.colors.primaryAccent,
                            onClick = onAskTutor
                        )
                    }

                    MilestoneStatus.LOCKED -> {
                        AdaptPill(
                            text = "Ask Prerequisites",
                            icon = AdaptIcons.Sparkle,
                            isSelected = false,
                            accentColor = AdaptTheme.colors.textMuted,
                            onClick = onAskTutor
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Previews for Editorial Obsidian Liquid Glass Learning Journey
// ============================================================================

@Preview(name = "Pathway Screen - Dark Mode", showBackground = true)
@Composable
fun PathwayEditorialDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        PathwayScreen(
            onNavigateBack = {},
            onNavigateToLesson = {},
            onNavigateToExplore = {},
            onAskTutor = {}
        )
    }
}

@Preview(name = "Pathway Screen - Light Mode", showBackground = true)
@Composable
fun PathwayEditorialLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        PathwayScreen(
            onNavigateBack = {},
            onNavigateToLesson = {},
            onNavigateToExplore = {},
            onAskTutor = {}
        )
    }
}
