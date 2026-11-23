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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearnerProfile
import com.example.adapt_ai_ui.data.LearningModule
import com.example.adapt_ai_ui.data.MilestoneStatus
import com.example.adapt_ai_ui.data.PathwayNode
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPill
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptProgressIndicator
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSectionHeader
import com.example.adapt_ai_ui.ui.components.AdaptTimelineNode
import com.example.adapt_ai_ui.ui.components.TimelineNodeState
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

/**
 * Editorial Obsidian Liquid Glass Home Dashboard for ADAPT-AI.
 *
 * Visual & Structural Principles (Phase 2.1 Refinements):
 * 1. Top Identity Area: Minimal editorial masthead with brand overline, SoC pill, and streak touchpoint.
 * 2. Signature Liquid Glass Hero: True multi-layered translucent glass with specular highlights and radial ambient wash.
 *    Dynamically reflects completed vs. in-progress lesson states without confusion.
 * 3. Open Activity & Mastery Metric Strip: De-boxed horizontal metric strip directly on the obsidian canvas,
 *    clearly decoupling Study Time (35m spent vs 20m goal), Concept Mastery (88% quiz accuracy), and Curriculum (50% progress).
 * 4. Open Curriculum Roadmap: Seamless connected timeline spine on the obsidian canvas (no enclosing box).
 * 5. Asymmetric Spaced Revision Ribbon: Distinct urgent amber-bordered callout when revisions are due, minimal ribbon when caught up.
 * 6. Conversational AI Tutor Dock: Socratic query portal with topic pills and simulated on-device inference prompt.
 */
@Composable
fun HomeScreen(
    onNavigateToPathway: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToPractice: (PracticeMode) -> Unit,
    onNavigateToTutor: (TutorQuickPrompt?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val learner = SampleData.learnerProfile
    val currentModule = SampleData.currentModule
    val pathwayNodes = SampleData.pathwayPreviewNodes
    val isLessonCompleted = SampleData.isCurrentLessonCompleted
    val tutorPrompts = remember(learner.activeDomainId) {
        DomainRepository.getQuickPromptsForDomain(learner.activeDomainId)
    }
    val unresolvedCount = AdaptiveLearningEngine.mistakeNotebook.count { !it.isResolved }

    val completedMilestonesCount = pathwayNodes.count { it.status == MilestoneStatus.COMPLETED }
    val totalMilestonesCount = pathwayNodes.size
    val curriculumProgressPercent = if (totalMilestonesCount > 0) {
        (completedMilestonesCount * 100) / totalMilestonesCount
    } else 0

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
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.huge)
    ) {
        // --------------------------------------------------------------------
        // A. Top Identity Area
        // --------------------------------------------------------------------
        item {
            HomeTopIdentityBar(
                learner = learner,
                onProfileClick = onNavigateToProfile
            )
        }

        // --------------------------------------------------------------------
        // B. Main Learning-Focus Hero (Signature Selective Liquid Glass Surface)
        // --------------------------------------------------------------------
        item {
            HomeLearningFocusHero(
                module = currentModule,
                domainTitle = learner.activeDomainTitle,
                isCompleted = isLessonCompleted || currentModule.currentStep >= currentModule.totalSteps,
                onResumeLesson = onNavigateToLesson,
                onExploreTracks = onNavigateToExplore
            )
        }

        // --------------------------------------------------------------------
        // C. Open Editorial Activity & Mastery Metric Strip (De-boxed)
        // --------------------------------------------------------------------
        item {
            HomeActivityProgressStrip(
                learner = learner,
                completedMilestones = completedMilestonesCount,
                totalMilestones = totalMilestonesCount,
                curriculumPercent = curriculumProgressPercent
            )
        }

        // --------------------------------------------------------------------
        // D. Learning Journey Preview (Connected Roadmap Spine on Canvas)
        // --------------------------------------------------------------------
        item {
            HomeCurriculumRoadmapSection(
                nodes = pathwayNodes,
                onNodeClick = { node ->
                    if (node.status == MilestoneStatus.IN_PROGRESS) {
                        onNavigateToLesson()
                    } else {
                        onNavigateToPathway()
                    }
                },
                onViewFullJourney = onNavigateToPathway
            )
        }

        // --------------------------------------------------------------------
        // E. Quick Revision Section (Asymmetric Spaced Repetition Ribbon)
        // --------------------------------------------------------------------
        item {
            HomeQuickRevisionSection(
                unresolvedCount = unresolvedCount,
                onStartRevision = { onNavigateToPractice(PracticeMode.REVISION) },
                onPracticeSession = { onNavigateToPractice(PracticeMode.SESSION) }
            )
        }

        // --------------------------------------------------------------------
        // F. AI Tutor Entry Point (Socratic Assistant Conversational Dock)
        // --------------------------------------------------------------------
        item {
            HomeTutorAssistantSection(
                prompts = tutorPrompts,
                onOpenTutor = { onNavigateToTutor(null) },
                onSelectPrompt = { prompt -> onNavigateToTutor(prompt) }
            )
        }
    }
}

// ============================================================================
// Subcomponents for Clean Architecture & Reusability
// ============================================================================

/**
 * Top identity header: Minimal editorial masthead, contextual greeting, and streak touchpoint.
 */
@Composable
private fun HomeTopIdentityBar(
    learner: LearnerProfile,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ADAPT-AI",
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )
                Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.small)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ON-DEVICE SOC",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${learner.greeting}, ${learner.name}",
                style = AdaptTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )
        }

        // Profile & Streak Pill Touchpoint
        Row(
            verticalAlignment = Alignment.CenterVertically,
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
                    onClick = onProfileClick
                )
                .semantics {
                    contentDescription = "Profile and settings. Streak: ${learner.streakDays} days"
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🔥 ${learner.streakDays}d",
                style = AdaptTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.warning
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AdaptTheme.colors.primaryAccentContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = learner.name.take(1),
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
 * Signature Learning Hero: Multi-layered Liquid Glass container housing the current lesson focus.
 *
 * Glass Treatment:
 * - Specular multi-stop border gradient (white top reflection, violet mid, subtle border baseline).
 * - Translucent glass fill with top-right ambient violet radial wash.
 * - Specular top rim light fading out smoothly over the top 80dp.
 * - Dynamic state awareness: displays "Milestone Completed" vs "In Progress" with clear CTAs.
 */
@Composable
private fun HomeLearningFocusHero(
    module: LearningModule,
    domainTitle: String,
    isCompleted: Boolean,
    onResumeLesson: () -> Unit,
    onExploreTracks: () -> Unit
) {
    val heroShape = RoundedCornerShape(AdaptTheme.radius.extraLarge)

    val borderBrush = Brush.verticalGradient(
        listOf(
            Color(0x80FFFFFF),
            AdaptTheme.colors.primaryAccent.copy(alpha = 0.40f),
            AdaptTheme.colors.glassBorder,
            AdaptTheme.colors.borderSubtle
        )
    )

    val ambientGlow = Brush.radialGradient(
        colors = listOf(
            AdaptTheme.colors.primaryAccent.copy(alpha = 0.16f),
            Color.Transparent
        ),
        center = Offset(800f, 0f),
        radius = 700f
    )

    val specularHighlight = Brush.verticalGradient(
        colors = listOf(
            AdaptTheme.colors.glassHighlight,
            Color.Transparent
        ),
        startY = 0f,
        endY = 120f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(AdaptTheme.colors.glassSurface)
            .background(ambientGlow)
            .background(specularHighlight)
            .border(
                width = AdaptTheme.stroke.hairline,
                brush = borderBrush,
                shape = heroShape
            )
            .padding(AdaptTheme.spacing.extraLarge)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Track Subheading & Switch Track Affordance
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
                            .background(
                                if (isCompleted) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) {
                            "MILESTONE COMPLETED ✓"
                        } else {
                            "${domainTitle.uppercase()} • ACTIVE FOCUS"
                        },
                        style = AdaptTheme.typographyExt.overline,
                        color = if (isCompleted) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .clickable(role = Role.Button, onClick = onExploreTracks)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Explore other learning tracks" }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AdaptIcons.Compass,
                            contentDescription = null,
                            tint = AdaptTheme.colors.primaryAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tracks",
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Main Lesson Title
            Text(
                text = module.title,
                style = AdaptTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

            // Technical Concept Overview
            Text(
                text = module.summary,
                style = AdaptTheme.typography.bodyMedium,
                color = AdaptTheme.colors.textSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

            // Step Progress and Contextual Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCompleted) {
                        "All ${module.totalSteps} Steps Mastered (100%)"
                    } else {
                        "Step ${module.currentStep} of ${module.totalSteps}"
                    },
                    style = AdaptTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AdaptTheme.colors.textPrimary
                )

                Text(
                    text = if (isCompleted) {
                        "Completed Today"
                    } else {
                        "~${module.estimatedMinutes} mins remaining"
                    },
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isCompleted) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            AdaptProgressIndicator(
                progress = if (isCompleted) 1f else module.progressFraction,
                color = if (isCompleted) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

            // Primary Call to Action
            AdaptPrimaryButton(
                text = if (isCompleted) "Review Lesson" else "Continue Lesson",
                onClick = onResumeLesson,
                leadingIcon = if (isCompleted) AdaptIcons.Retry else AdaptIcons.Play
            )
        }
    }
}

/**
 * Open Editorial Activity & Mastery Metric Strip.
 *
 * Eliminates the enclosing card box. Sits directly on the obsidian canvas,
 * clearly decoupling Study Time (minutes vs goal), Concept Mastery (quiz accuracy),
 * and Curriculum Progress (completed milestones).
 */
@Composable
private fun HomeActivityProgressStrip(
    learner: LearnerProfile,
    completedMilestones: Int,
    totalMilestones: Int,
    curriculumPercent: Int
) {
    val isGoalAchieved = learner.dailyMinutesSpent >= learner.dailyMinutesGoal
    val dailyProgress = (learner.dailyMinutesSpent.toFloat() / learner.dailyMinutesGoal).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DAILY FOCUS & MASTERY",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Open Metric Row with Hairline Dividers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stat 1: Study Time
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AdaptTheme.spacing.small)
            ) {
                Text(
                    text = "${learner.dailyMinutesSpent}m",
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = AdaptTheme.colors.secondaryAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Study Time",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isGoalAchieved) {
                        "Goal: ${learner.dailyMinutesGoal}m ✓"
                    } else {
                        "Goal: ${learner.dailyMinutesGoal}m"
                    },
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isGoalAchieved) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isGoalAchieved) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textSecondary
                )
            }

            // Hairline Divider 1
            Box(
                modifier = Modifier
                    .width(AdaptTheme.stroke.hairline)
                    .fillMaxHeight()
                    .background(AdaptTheme.colors.borderSubtle)
            )

            // Stat 2: Concept Mastery (Quiz accuracy)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AdaptTheme.spacing.small)
            ) {
                Text(
                    text = "${learner.masteryScorePercent}%",
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = AdaptTheme.colors.primaryAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Concept Mastery",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Quiz Competency",
                    style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AdaptTheme.colors.textSecondary
                )
            }

            // Hairline Divider 2
            Box(
                modifier = Modifier
                    .width(AdaptTheme.stroke.hairline)
                    .fillMaxHeight()
                    .background(AdaptTheme.colors.borderSubtle)
            )

            // Stat 3: Curriculum Milestone Progress
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = AdaptTheme.spacing.small)
            ) {
                Text(
                    text = "$curriculumPercent%",
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = AdaptTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Curriculum",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedMilestones of $totalMilestones Done",
                    style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AdaptTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Slim Daily Target Progress Bar
        AdaptProgressIndicator(
            progress = dailyProgress,
            color = if (isGoalAchieved) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent,
            height = 3.dp
        )
    }
}

/**
 * Curriculum roadmap preview using Phase 1's AdaptTimelineNode on an open canvas.
 * Eliminates enclosing rectangular box so the connected spine breathes naturally.
 */
@Composable
private fun HomeCurriculumRoadmapSection(
    nodes: List<PathwayNode>,
    onNodeClick: (PathwayNode) -> Unit,
    onViewFullJourney: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AdaptSectionHeader(
            title = "Curriculum Roadmap",
            overline = "Learning Pathway",
            actionText = "Full Journey",
            onActionClick = onViewFullJourney
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Direct timeline rendering on the canvas (no enclosing card)
        val previewNodes = nodes.take(3)
        previewNodes.forEachIndexed { index, node ->
            val nodeState = when (node.status) {
                MilestoneStatus.COMPLETED -> TimelineNodeState.COMPLETED
                MilestoneStatus.IN_PROGRESS -> TimelineNodeState.IN_PROGRESS
                MilestoneStatus.LOCKED -> TimelineNodeState.LOCKED
            }

            AdaptTimelineNode(
                stepNumber = node.index,
                title = node.title,
                subtitle = "${node.track} • ${node.estimatedMinutes} mins",
                state = nodeState,
                isLast = index == previewNodes.lastIndex,
                onClick = { onNodeClick(node) }
            )
        }
    }
}

/**
 * Spaced revision status dock displaying actual pending mistakes.
 * Styled as an asymmetric status ribbon with amber accent when due.
 */
@Composable
private fun HomeQuickRevisionSection(
    unresolvedCount: Int,
    onStartRevision: () -> Unit,
    onPracticeSession: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AdaptSectionHeader(
            title = "Spaced Revision",
            overline = "Retention System",
            actionText = if (unresolvedCount > 0) "$unresolvedCount Due" else "Caught Up"
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        val ribbonShape = RoundedCornerShape(AdaptTheme.radius.large)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ribbonShape)
                .background(AdaptTheme.colors.raisedSurface)
                .border(
                    width = AdaptTheme.stroke.hairline,
                    color = if (unresolvedCount > 0)
                        AdaptTheme.colors.warning.copy(alpha = 0.35f)
                    else
                        AdaptTheme.colors.borderSubtle,
                    shape = ribbonShape
                )
                .padding(AdaptTheme.spacing.large)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (unresolvedCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mistake Notebook Needs Review",
                            style = AdaptTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AdaptTheme.colors.textPrimary
                        )

                        AdaptPill(
                            text = "$unresolvedCount Due Today",
                            isSelected = true,
                            accentColor = AdaptTheme.colors.warning
                        )
                    }

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

                    Text(
                        text = "Reinforce previously missed concepts on quantization arithmetic and hardware caches to establish permanent retention.",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    AdaptSecondaryButton(
                        text = "Revise Due Concepts ($unresolvedCount)",
                        onClick = onStartRevision,
                        leadingIcon = AdaptIcons.Retry
                    )
                } else {
                    // Honest, encouraging empty state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AdaptTheme.colors.secondaryAccentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AdaptIcons.Check,
                                contentDescription = null,
                                tint = AdaptTheme.colors.secondaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(AdaptTheme.spacing.medium))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Revisions Completed",
                                style = AdaptTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AdaptTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Zero weak spots pending in your mistake notebook. Great consistency!",
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    AdaptSecondaryButton(
                        text = "Start Practice Session",
                        onClick = onPracticeSession,
                        leadingIcon = AdaptIcons.Target
                    )
                }
            }
        }
    }
}

/**
 * Conversational AI Tutor Entry Point: Socratic Assistant Prompt Dock.
 */
@Composable
private fun HomeTutorAssistantSection(
    prompts: List<TutorQuickPrompt>,
    onOpenTutor: () -> Unit,
    onSelectPrompt: (TutorQuickPrompt) -> Unit
) {
    val dockShape = RoundedCornerShape(AdaptTheme.radius.large)

    val topBorderBrush = Brush.horizontalGradient(
        listOf(
            AdaptTheme.colors.borderSubtle,
            AdaptTheme.colors.primaryAccent.copy(alpha = 0.4f),
            AdaptTheme.colors.borderSubtle
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        AdaptSectionHeader(
            title = "AI Learning Assistant",
            overline = "On-Device Socratic Tutor",
            actionText = "Open Chat",
            onActionClick = onOpenTutor
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .background(AdaptTheme.colors.raisedSurface)
                .border(
                    width = AdaptTheme.stroke.hairline,
                    brush = topBorderBrush,
                    shape = dockShape
                )
                .padding(AdaptTheme.spacing.large)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Explore Concepts with Socratic Guidance",
                    style = AdaptTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AdaptTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

                Text(
                    text = "Tap a suggested topic prompt or ask custom technical queries with simulated on-device inference.",
                    style = AdaptTheme.typography.bodySmall,
                    color = AdaptTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                // Suggested Prompts Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small),
                    contentPadding = PaddingValues(bottom = AdaptTheme.spacing.extraSmall)
                ) {
                    items(prompts) { prompt ->
                        AdaptPill(
                            text = prompt.prompt,
                            icon = AdaptIcons.Sparkle,
                            isSelected = false,
                            accentColor = AdaptTheme.colors.primaryAccent,
                            onClick = { onSelectPrompt(prompt) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                AdaptSecondaryButton(
                    text = "Ask AI Tutor a Question",
                    onClick = onOpenTutor,
                    leadingIcon = AdaptIcons.Sparkle
                )
            }
        }
    }
}

// ============================================================================
// Previews for Editorial Obsidian Liquid Glass Home Screen
// ============================================================================

@Preview(name = "Home Screen - Dark Mode", showBackground = true)
@Composable
fun HomeEditorialDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            onNavigateToPathway = {},
            onNavigateToLesson = {},
            onNavigateToPractice = {},
            onNavigateToTutor = {},
            onNavigateToProfile = {},
            onNavigateToExplore = {}
        )
    }
}

@Preview(name = "Home Screen - Light Mode", showBackground = true)
@Composable
fun HomeEditorialLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        HomeScreen(
            onNavigateToPathway = {},
            onNavigateToLesson = {},
            onNavigateToPractice = {},
            onNavigateToTutor = {},
            onNavigateToProfile = {},
            onNavigateToExplore = {}
        )
    }
}
