package com.example.adapt_ai_ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.MistakeRecord
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.PracticeQuestionItem
import com.example.adapt_ai_ui.data.PracticeSessionSummary
import com.example.adapt_ai_ui.data.QuestionSubmission
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TopicCompetency
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.ui.components.AdaptGlassSurface
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPill
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptProgressIndicator
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

/**
 * Editorial Obsidian Liquid Glass Practice & Revision Screen for ADAPT-AI.
 *
 * Visual & Functional Architecture (Phase 5):
 * 1. Editorial Practice Top Bar:
 *    - Circular back navigation (>= 48dp touch target)
 *    - Contextual overline and title
 *    - Adaptive Engine runtime badge
 *    - PracticeMode switch tabs: "Practice Session" vs "Mistake Notebook"
 * 2. Practice Overview / Hub (SESSION mode when session is not active):
 *    - Real Performance Metrics Matrix (Questions completed, authentic accuracy, pending mistakes)
 *    - Hero Action Card to start or resume practice
 *    - Spaced Repetition Leitner summary
 *    - Real Topic Competency Progress Bars
 * 3. Interactive Practice Session (SESSION mode when session is active):
 *    - Question header: Topic, Counter, Progress bar, Difficulty badge
 *    - Accessible, comfortable options (min 48dp) with distinct letter badges
 *    - Accidental double-submit protection
 *    - Immediate educational feedback with learning insights
 * 4. Session Completion View (SESSION mode when session completes):
 *    - Authentic score, accuracy percentage, and mastery XP derived from actual submissions
 *    - Topic-level competency breakdown
 *    - Actions: Retry incorrect questions, review in Mistake Notebook, return to Journey
 * 5. Mistake Notebook & Spaced Repetition Workspace (REVISION mode):
 *    - 2-Box Leitner Spaced Repetition distribution (Level 1: 1-day due now vs Level 2: 3-day reinforced)
 *    - Mistake Cards detailing learner's original incorrect attempt vs correct answer
 *    - Actions to retry question or Ask Socratic AI Tutor with pre-filled question context
 *    - Authentic empty state when no mistakes exist
 */
@Composable
fun PracticeScreen(
    onNavigateBack: () -> Unit,
    onSessionCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    initialMode: PracticeMode = PracticeMode.SESSION,
    customQuestions: List<PracticeQuestionItem>? = null,
    onAskTutor: ((TutorQuickPrompt) -> Unit)? = null
) {
    var activeMode by remember { mutableStateOf(initialMode) }

    // Session State
    val questions = remember(customQuestions) {
        customQuestions ?: AdaptiveLearningEngine.questionBank
    }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    val submissions = remember { mutableStateMapOf<String, QuestionSubmission>() }
    var isSessionActive by remember { mutableStateOf(customQuestions != null) }
    var isSessionFinished by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val currentSubmission = currentQuestion?.let { submissions[it.id] }

    fun startNewSession(targetQuestionId: String? = null) {
        submissions.clear()
        selectedOptionIndex = null
        isSessionFinished = false
        if (targetQuestionId != null) {
            val idx = questions.indexOfFirst { it.id == targetQuestionId }
            currentQuestionIndex = if (idx >= 0) idx else 0
        } else {
            currentQuestionIndex = 0
        }
        isSessionActive = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background)
            .statusBarsPadding()
    ) {
        // Top Navigation & Engine Mode Header
        PracticeEditorialTopBar(
            activeMode = activeMode,
            isSessionActive = isSessionActive && !isSessionFinished,
            onModeChange = { mode ->
                activeMode = mode
                if (mode == PracticeMode.SESSION && isSessionFinished) {
                    // Reset session on tab change
                    currentQuestionIndex = 0
                    selectedOptionIndex = null
                    isSessionFinished = false
                    isSessionActive = false
                    submissions.clear()
                }
            },
            onNavigateBack = {
                if (activeMode == PracticeMode.SESSION && isSessionActive && !isSessionFinished) {
                    // If in middle of a session, back button returns to Practice Overview
                    isSessionActive = false
                } else {
                    onNavigateBack()
                }
            }
        )

        if (activeMode == PracticeMode.SESSION) {
            when {
                // State 1: Active Interactive MCQ Session
                isSessionActive && !isSessionFinished && currentQuestion != null -> {
                    PracticeActiveQuestionContent(
                        question = currentQuestion,
                        questionNumber = currentQuestionIndex + 1,
                        totalQuestions = questions.size,
                        selectedOption = selectedOptionIndex,
                        submission = currentSubmission,
                        canGoPrevious = currentQuestionIndex > 0,
                        onSelectOption = { index ->
                            if (currentSubmission == null) {
                                selectedOptionIndex = index
                            }
                        },
                        onSubmitAnswer = {
                            selectedOptionIndex?.takeUnless { currentQuestion.id in submissions }?.let { optionIndex ->
                                val sub = AdaptiveLearningEngine.submitAnswer(currentQuestion, optionIndex)
                                submissions[currentQuestion.id] = sub
                            }
                        },
                        onPreviousClick = {
                            if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                                selectedOptionIndex = submissions[questions[currentQuestionIndex].id]?.selectedOptionIndex
                            }
                        },
                        onNextClick = {
                            if (currentQuestionIndex < questions.lastIndex) {
                                currentQuestionIndex++
                                selectedOptionIndex = submissions[questions[currentQuestionIndex].id]?.selectedOptionIndex
                            } else {
                                isSessionFinished = true
                            }
                        },
                        onExitSession = {
                            isSessionActive = false
                        }
                    )
                }

                // State 2: Session Completion Summary
                isSessionFinished -> {
                    val summary = remember(submissions.size) {
                        AdaptiveLearningEngine.generateSummary(questions, submissions)
                    }
                    PracticeSessionSummaryContent(
                        summary = summary,
                        onRetryIncorrect = {
                            if (summary.incorrectQuestions.isNotEmpty()) {
                                submissions.clear()
                                currentQuestionIndex = 0
                                selectedOptionIndex = null
                                isSessionFinished = false
                                isSessionActive = true
                            }
                        },
                        onReviewMistakes = {
                            activeMode = PracticeMode.REVISION
                            isSessionFinished = false
                            isSessionActive = false
                        },
                        onReturnToOverview = {
                            isSessionFinished = false
                            isSessionActive = false
                            submissions.clear()
                        },
                        onFinishSession = onSessionCompleted
                    )
                }

                // State 3: Practice Overview / Hub Landing View
                else -> {
                    PracticeOverviewContent(
                        questionsCount = questions.size,
                        submissions = AdaptiveLearningEngine.sessionSubmissions,
                        topicStats = AdaptiveLearningEngine.topicStats,
                        mistakes = AdaptiveLearningEngine.mistakeNotebook,
                        onStartSession = {
                            startNewSession()
                        },
                        onOpenRevision = {
                            activeMode = PracticeMode.REVISION
                        }
                    )
                }
            }
        } else {
            // Mode 2: Mistake Notebook & Spaced Repetition Workspace
            PracticeMistakeNotebookContent(
                mistakes = AdaptiveLearningEngine.mistakeNotebook,
                onRetryMistake = { mistakeItem ->
                    activeMode = PracticeMode.SESSION
                    startNewSession(targetQuestionId = mistakeItem.question.id)
                },
                onAskTutor = { mistakeItem ->
                    if (onAskTutor != null) {
                        val incorrectText = mistakeItem.question.options.getOrNull(mistakeItem.lastSelectedOptionIndex)
                            ?: "my previous choice"
                        val correctText = mistakeItem.question.options.getOrNull(mistakeItem.question.correctIndex)
                            ?: "the verified answer"

                        val prompt = TutorQuickPrompt(
                            id = "mistake_${mistakeItem.question.id}",
                            prompt = "Can you explain why \"$incorrectText\" is incorrect and why \"$correctText\" is the correct answer for: \"${mistakeItem.question.question}\" in ${mistakeItem.question.topicTag}?",
                            tag = mistakeItem.question.topicTag
                        )
                        onAskTutor(prompt)
                    }
                },
                onStartPractice = {
                    activeMode = PracticeMode.SESSION
                    startNewSession()
                }
            )
        }
    }
}

// ============================================================================
// 1. Top Bar Component
// ============================================================================

@Composable
private fun PracticeEditorialTopBar(
    activeMode: PracticeMode,
    isSessionActive: Boolean,
    onModeChange: (PracticeMode) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AdaptTheme.spacing.large,
                end = AdaptTheme.spacing.large,
                top = AdaptTheme.spacing.small,
                bottom = AdaptTheme.spacing.small
            )
    ) {
        // Navigation & Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accessible Back Button (>= 48dp touch target)
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
                    .semantics {
                        contentDescription = if (isSessionActive) "Exit active session to practice overview" else "Return to previous screen"
                    },
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDGE AI SYSTEMS • MODEL OPTIMIZATION",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                    Text(
                        text = "ADAPTIVE ENGINE",
                        style = AdaptTheme.typographyExt.overline.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.secondaryAccent,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isSessionActive) "Interactive Practice" else "Practice & Revision",
                    style = AdaptTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

        // Mode Switcher Tabs (Practice Session vs Mistake Notebook)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                .background(AdaptTheme.colors.raisedSurface)
                .border(
                    width = AdaptTheme.stroke.hairline,
                    color = AdaptTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(AdaptTheme.radius.medium)
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PracticeMode.entries.forEach { mode ->
                val isSelected = activeMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(
                            if (isSelected) AdaptTheme.colors.primaryAccentContainer else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) AdaptTheme.stroke.hairline else 0.dp,
                            color = if (isSelected) AdaptTheme.colors.primaryAccent.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(AdaptTheme.radius.small)
                        )
                        .clickable(
                            role = Role.Tab,
                            onClick = { onModeChange(mode) }
                        )
                        .semantics {
                            selected = isSelected
                            contentDescription = mode.title
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        style = AdaptTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

// ============================================================================
// 2. Practice Overview / Landing Hub View
// ============================================================================

@Composable
private fun PracticeOverviewContent(
    questionsCount: Int,
    submissions: List<QuestionSubmission>,
    topicStats: Map<String, TopicCompetency>,
    mistakes: List<MistakeRecord>,
    onStartSession: () -> Unit,
    onOpenRevision: () -> Unit
) {
    val totalAnswered = submissions.size
    val correctCount = submissions.count { it.isCorrect }
    val accuracyPercent = if (totalAnswered > 0) (correctCount * 100 / totalAnswered) else 0

    val unresolvedMistakesCount = mistakes.count { !it.isResolved }
    val resolvedMistakesCount = mistakes.count { it.isResolved }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.large,
            end = AdaptTheme.spacing.large,
            top = AdaptTheme.spacing.small,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.large)
    ) {
        // A. Performance Metrics Matrix
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.large))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(AdaptTheme.radius.large)
                    )
                    .padding(AdaptTheme.spacing.large)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AUTHENTIC ENGINE TELEMETRY",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Completed questions
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$totalAnswered",
                                style = AdaptTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                            Text(
                                text = "Answers Logged",
                                style = AdaptTheme.typography.labelSmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        }

                        // Accuracy
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (totalAnswered > 0) "$accuracyPercent%" else "—",
                                style = AdaptTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (accuracyPercent >= 70) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                            )
                            Text(
                                text = "Engine Accuracy",
                                style = AdaptTheme.typography.labelSmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        }

                        // Pending Mistakes
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$unresolvedMistakesCount",
                                style = AdaptTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (unresolvedMistakesCount > 0) AdaptTheme.colors.warning else AdaptTheme.colors.secondaryAccent
                            )
                            Text(
                                text = "Due for Review",
                                style = AdaptTheme.typography.labelSmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // B. Hero Action Card — Start Practice Session
        item {
            AdaptGlassSurface(
                isGlassEnabled = true,
                shape = RoundedCornerShape(AdaptTheme.radius.extraLarge),
                contentPadding = PaddingValues(AdaptTheme.spacing.large)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AdaptTheme.colors.primaryAccentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AdaptIcons.Target,
                                contentDescription = null,
                                tint = AdaptTheme.colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                        Column {
                            Text(
                                text = "TECHNICAL PRACTICE SESSION",
                                style = AdaptTheme.typographyExt.overline,
                                color = AdaptTheme.colors.primaryAccent
                            )
                            Text(
                                text = "INT8 Weight Quantization",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

                    Text(
                        text = "Reinforce linear mapping (r = S × (q - Z)), symmetric vs affine zero-point alignment, and ARM NEON SIMD vector register saturation with real adaptive questions.",
                        style = AdaptTheme.typography.bodyMedium,
                        color = AdaptTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

                    AdaptPrimaryButton(
                        text = "Start Practice Session ($questionsCount Questions) →",
                        onClick = onStartSession,
                        leadingIcon = AdaptIcons.Play
                    )
                }
            }
        }

        // C. Spaced Repetition Quick Summary Hub
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.large))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(AdaptTheme.radius.large)
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AdaptTheme.colors.secondaryAccentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AdaptIcons.Notebook,
                                    contentDescription = null,
                                    tint = AdaptTheme.colors.secondaryAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))
                            Text(
                                text = "Spaced Revision Queue",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }

                        Text(
                            text = if (unresolvedMistakesCount > 0) "$unresolvedMistakesCount due" else "All clear",
                            style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (unresolvedMistakesCount > 0) AdaptTheme.colors.warning else AdaptTheme.colors.secondaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

                    Text(
                        text = "Level 1: $unresolvedMistakesCount concepts due now • Level 2: $resolvedMistakesCount concepts verified & scheduled for 3-day retention.",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    AdaptSecondaryButton(
                        text = "Review Mistake Notebook →",
                        onClick = onOpenRevision,
                        leadingIcon = AdaptIcons.Notebook
                    )
                }
            }
        }

        // D. Topic Competency Matrix
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TOPIC COMPETENCY MATRIX",
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

                Text(
                    text = "Mastery by Architecture Domain",
                    style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AdaptTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.large))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.large)
                        )
                        .padding(AdaptTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.large)
                ) {
                    topicStats.values.forEach { competency ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = competency.topic,
                                    style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = AdaptTheme.colors.textPrimary
                                )

                                Text(
                                    text = "${competency.masteryPercentage}% (${competency.correctAttempts}/${competency.totalAttempts})",
                                    style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (competency.masteryPercentage >= 70) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            AdaptProgressIndicator(
                                progress = if (competency.totalAttempts > 0)
                                    (competency.correctAttempts.toFloat() / competency.totalAttempts)
                                else 0f,
                                color = if (competency.masteryPercentage >= 70) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent,
                                height = 4.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. Interactive Active MCQ Session View
// ============================================================================

@Composable
private fun PracticeActiveQuestionContent(
    question: PracticeQuestionItem,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    submission: QuestionSubmission?,
    canGoPrevious: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onExitSession: () -> Unit
) {
    val progressFraction = questionNumber.toFloat() / totalQuestions

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.large,
            end = AdaptTheme.spacing.large,
            top = AdaptTheme.spacing.small,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.medium)
    ) {
        // Question Header & Progress Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.small))
                            .background(AdaptTheme.colors.primaryAccentContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = question.topicTag.uppercase(),
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                    Text(
                        text = "QUESTION $questionNumber OF $totalQuestions",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.textMuted
                    )
                }

                // Difficulty badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.small)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = question.difficulty.name,
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        softWrap = false,
                        maxLines = 1,
                        color = when (question.difficulty) {
                            DifficultyLevel.BEGINNER -> AdaptTheme.colors.secondaryAccent
                            DifficultyLevel.INTERMEDIATE -> AdaptTheme.colors.primaryAccent
                            DifficultyLevel.ADVANCED -> AdaptTheme.colors.warning
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Accessible Progress Bar
            AdaptProgressIndicator(
                progress = progressFraction,
                color = AdaptTheme.colors.secondaryAccent,
                height = 4.dp
            )
        }

        // Question Prompt Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.large))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(AdaptTheme.radius.large)
                    )
                    .padding(AdaptTheme.spacing.large)
            ) {
                Text(
                    text = question.question,
                    style = AdaptTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = AdaptTheme.colors.textPrimary
                )
            }
        }

        // Multiple Choice Options (Touch targets >= 48dp)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isAnswerSubmitted = submission != null
                    val isCorrectAnswer = index == question.correctIndex

                    val borderColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> AdaptTheme.colors.secondaryAccent
                        isAnswerSubmitted && isSelected && !submission.isCorrect -> AdaptTheme.colors.error
                        isSelected -> AdaptTheme.colors.primaryAccent
                        else -> AdaptTheme.colors.borderSubtle
                    }

                    val backgroundColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> AdaptTheme.colors.secondaryAccentContainer
                        isAnswerSubmitted && isSelected && !submission.isCorrect -> AdaptTheme.colors.error.copy(alpha = 0.15f)
                        isSelected -> AdaptTheme.colors.primaryAccentContainer
                        else -> AdaptTheme.colors.raisedSurface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(backgroundColor)
                            .border(
                                width = if (isSelected || (isAnswerSubmitted && isCorrectAnswer)) AdaptTheme.stroke.focus else AdaptTheme.stroke.hairline,
                                color = borderColor,
                                shape = RoundedCornerShape(AdaptTheme.radius.medium)
                            )
                            .clickable(
                                role = Role.RadioButton,
                                enabled = submission == null,
                                onClick = { onSelectOption(index) }
                            )
                            .semantics {
                                selected = isSelected
                                contentDescription = "Option ${('A' + index)}: $option"
                            }
                            .padding(AdaptTheme.spacing.medium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Indicator (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isAnswerSubmitted && isCorrectAnswer -> AdaptTheme.colors.secondaryAccent
                                            isAnswerSubmitted && isSelected && !submission.isCorrect -> AdaptTheme.colors.error
                                            isSelected -> AdaptTheme.colors.primaryAccent
                                            else -> AdaptTheme.colors.surface
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnswerSubmitted && isCorrectAnswer) {
                                    Icon(
                                        imageVector = AdaptIcons.Check,
                                        contentDescription = "Correct",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isAnswerSubmitted && isSelected && !submission.isCorrect) {
                                    Icon(
                                        imageVector = AdaptIcons.Cross,
                                        contentDescription = "Incorrect",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else {
                                    Text(
                                        text = ('A' + index).toString(),
                                        style = AdaptTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) Color.White else AdaptTheme.colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(AdaptTheme.spacing.medium))

                            Text(
                                text = option,
                                style = AdaptTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = AdaptTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Immediate Feedback & Educational Explanation
        item {
            AnimatedVisibility(
                visible = submission != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (submission != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(
                                if (submission.isCorrect)
                                    AdaptTheme.colors.secondaryAccentContainer
                                else
                                    AdaptTheme.colors.raisedSurface
                            )
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = if (submission.isCorrect) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent,
                                shape = RoundedCornerShape(AdaptTheme.radius.medium)
                            )
                            .padding(AdaptTheme.spacing.large)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (submission.isCorrect) "🎉 Correct! Concept Verified." else "💡 Learning Insight",
                                    style = AdaptTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (submission.isCorrect) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))
                            Text(
                                text = question.explanation,
                                style = AdaptTheme.typography.bodySmall.copy(
                                    lineHeight = 20.sp
                                ),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Navigation Controls (Previous, Submit, Next, Exit)
        item {
            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
            ) {
                if (submission == null) {
                    AdaptPrimaryButton(
                        text = "Submit Answer",
                        onClick = onSubmitAnswer,
                        enabled = selectedOption != null,
                        leadingIcon = AdaptIcons.Check
                    )
                } else {
                    AdaptPrimaryButton(
                        text = if (questionNumber == totalQuestions) "Complete Session →" else "Next Question →",
                        onClick = onNextClick,
                        trailingIcon = AdaptIcons.ArrowForward
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
                ) {
                    if (canGoPrevious) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdaptSecondaryButton(
                                text = "← Previous",
                                onClick = onPreviousClick
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdaptSecondaryButton(
                            text = "Exit Session",
                            onClick = onExitSession
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. Session Completion Summary View
// ============================================================================

@Composable
private fun PracticeSessionSummaryContent(
    summary: PracticeSessionSummary,
    onRetryIncorrect: () -> Unit,
    onReviewMistakes: () -> Unit,
    onReturnToOverview: () -> Unit,
    onFinishSession: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.large,
            end = AdaptTheme.spacing.large,
            top = AdaptTheme.spacing.small,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.large)
    ) {
        // Celebration & Scorecard
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.extraLarge))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(AdaptTheme.radius.extraLarge)
                    )
                    .padding(AdaptTheme.spacing.large)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.secondaryAccentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AdaptIcons.Target,
                            contentDescription = null,
                            tint = AdaptTheme.colors.secondaryAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    Text(
                        text = "Session Complete!",
                        style = AdaptTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "You scored ${summary.correctCount} out of ${summary.totalQuestions} questions correctly",
                        style = AdaptTheme.typography.bodyMedium,
                        color = AdaptTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AdaptTheme.radius.large))
                            .background(AdaptTheme.colors.surface)
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = AdaptTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(AdaptTheme.radius.large)
                            )
                            .padding(AdaptTheme.spacing.large),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.scorePercentage}%",
                                style = AdaptTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (summary.scorePercentage >= 70) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                            )
                            Text(
                                text = "Session Accuracy",
                                style = AdaptTheme.typography.labelSmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "+${summary.xpEarned} XP",
                                style = AdaptTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AdaptTheme.colors.primaryAccent
                            )
                            Text(
                                text = "Engine XP Earned",
                                style = AdaptTheme.typography.labelSmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Topic Competency Breakdown
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SESSION COMPETENCY PERFORMANCE",
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.large))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.large)
                        )
                        .padding(AdaptTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.medium)
                ) {
                    summary.topicBreakdown.forEach { comp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.topic,
                                    style = AdaptTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = AdaptTheme.colors.textPrimary
                                )
                                Text(
                                    text = "${comp.correctAttempts}/${comp.totalAttempts} total attempts",
                                    style = AdaptTheme.typography.bodySmall,
                                    color = AdaptTheme.colors.textSecondary
                                )
                            }

                            Text(
                                text = "${comp.masteryPercentage}%",
                                style = AdaptTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (comp.masteryPercentage >= 70) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
            ) {
                if (summary.incorrectQuestions.isNotEmpty()) {
                    AdaptPrimaryButton(
                        text = "Retry Incorrect Questions (${summary.incorrectQuestions.size})",
                        onClick = onRetryIncorrect,
                        leadingIcon = AdaptIcons.Retry
                    )

                    AdaptSecondaryButton(
                        text = "Review Mistakes in Notebook",
                        onClick = onReviewMistakes,
                        leadingIcon = AdaptIcons.Notebook
                    )
                }

                AdaptSecondaryButton(
                    text = "Return to Practice Overview",
                    onClick = onReturnToOverview
                )

                AdaptSecondaryButton(
                    text = "Return to Learning Journey",
                    onClick = onFinishSession
                )
            }
        }
    }
}

// ============================================================================
// 5. Mistake Notebook & Spaced Repetition Workspace View
// ============================================================================

@Composable
private fun PracticeMistakeNotebookContent(
    mistakes: List<MistakeRecord>,
    onRetryMistake: (MistakeRecord) -> Unit,
    onAskTutor: (MistakeRecord) -> Unit,
    onStartPractice: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val level1DueNow = mistakes.filter { !it.isResolved }
    val level2Reinforced = mistakes.filter { it.isResolved }

    val filteredMistakes = when (selectedFilter) {
        "DUE" -> level1DueNow
        "RESOLVED" -> level2Reinforced
        else -> mistakes
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.large,
            end = AdaptTheme.spacing.large,
            top = AdaptTheme.spacing.small,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.medium)
    ) {
        // Leitner Spaced Repetition Distribution Card
        item {
            LeitnerSpacedRepetitionCard(
                dueNowCount = level1DueNow.size,
                reinforcedCount = level2Reinforced.size
            )
        }

        // Filter Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdaptPill(
                    text = "All (${mistakes.size})",
                    isSelected = selectedFilter == "ALL",
                    accentColor = AdaptTheme.colors.primaryAccent,
                    onClick = { selectedFilter = "ALL" }
                )
                AdaptPill(
                    text = "Due Now (${level1DueNow.size})",
                    isSelected = selectedFilter == "DUE",
                    accentColor = AdaptTheme.colors.warning,
                    onClick = { selectedFilter = "DUE" }
                )
                AdaptPill(
                    text = "Resolved ✨ (${level2Reinforced.size})",
                    isSelected = selectedFilter == "RESOLVED",
                    accentColor = AdaptTheme.colors.secondaryAccent,
                    onClick = { selectedFilter = "RESOLVED" }
                )
            }
        }

        // Mistake Cards List or Empty State
        if (filteredMistakes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.large))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.large)
                        )
                        .padding(AdaptTheme.spacing.extraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AdaptTheme.colors.secondaryAccentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AdaptIcons.Check,
                                contentDescription = null,
                                tint = AdaptTheme.colors.secondaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "No Concepts in this Category",
                            style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )

                        Text(
                            text = "All practiced concepts are currently verified or matching criteria.",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

                        AdaptPrimaryButton(
                            text = "Start Practice Session →",
                            onClick = onStartPractice
                        )
                    }
                }
            }
        } else {
            items(filteredMistakes, key = { it.question.id }) { mistake ->
                EditorialMistakeCard(
                    mistake = mistake,
                    onRetry = { onRetryMistake(mistake) },
                    onAskTutor = { onAskTutor(mistake) }
                )
            }
        }
    }
}

// ============================================================================
// 6. Leitner Spaced Repetition Distribution Component
// ============================================================================

@Composable
private fun LeitnerSpacedRepetitionCard(
    dueNowCount: Int,
    reinforcedCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(AdaptTheme.colors.raisedSurface)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = AdaptTheme.colors.borderSubtle,
                shape = RoundedCornerShape(AdaptTheme.radius.large)
            )
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEITNER SPACED REPETITION SCHEDULE",
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )

                Text(
                    text = "2-Stage Interval",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            // Two Leitner Box Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.medium)
            ) {
                // Box 1: Level 1 (Due Now / 1 Day)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.surface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = if (dueNowCount > 0) AdaptTheme.colors.warning.copy(alpha = 0.5f) else AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(AdaptTheme.spacing.medium)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BOX 1 (LEVEL 1)",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AdaptTheme.colors.warning
                            )
                            Text(
                                text = "1d Interval",
                                style = AdaptTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = AdaptTheme.colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$dueNowCount",
                            style = AdaptTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )

                        Text(
                            text = "Due for reinforcement",
                            style = AdaptTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AdaptTheme.colors.textSecondary
                        )
                    }
                }

                // Box 2: Level 2 (Reinforced / 3 Days)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.surface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = if (reinforcedCount > 0) AdaptTheme.colors.secondaryAccent.copy(alpha = 0.5f) else AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(AdaptTheme.spacing.medium)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BOX 2 (LEVEL 2)",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AdaptTheme.colors.secondaryAccent
                            )
                            Text(
                                text = "3d Interval",
                                style = AdaptTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = AdaptTheme.colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$reinforcedCount",
                            style = AdaptTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )

                        Text(
                            text = "Verified concepts",
                            style = AdaptTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AdaptTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7. Editorial Mistake Card Component
// ============================================================================

@Composable
private fun EditorialMistakeCard(
    mistake: MistakeRecord,
    onRetry: () -> Unit,
    onAskTutor: () -> Unit
) {
    val incorrectAttemptText = mistake.question.options.getOrNull(mistake.lastSelectedOptionIndex)
        ?: "Option ${mistake.lastSelectedOptionIndex + 1}"
    val correctAnswerText = mistake.question.options.getOrNull(mistake.question.correctIndex)
        ?: "Option ${mistake.question.correctIndex + 1}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(AdaptTheme.colors.raisedSurface)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = if (mistake.isResolved) AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f) else AdaptTheme.colors.borderSubtle,
                shape = RoundedCornerShape(AdaptTheme.radius.large)
            )
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Topic + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(AdaptTheme.colors.primaryAccentContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = mistake.question.topicTag.uppercase(),
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.primaryAccent
                    )
                }

                Text(
                    text = if (mistake.isResolved) "✓ Resolved • in ${mistake.nextRevisionDays}d" else "⚡ Due for review • in ${mistake.nextRevisionDays}d",
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    softWrap = false,
                    maxLines = 1,
                    color = if (mistake.isResolved) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.warning
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Question Text
            Text(
                text = mistake.question.question,
                style = AdaptTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            // Attempt Comparison Matrix
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                    .background(AdaptTheme.colors.surface)
                    .border(
                        width = AdaptTheme.stroke.hairline,
                        color = AdaptTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(AdaptTheme.radius.medium)
                    )
                    .padding(AdaptTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Your Attempt
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AdaptIcons.Cross,
                        contentDescription = null,
                        tint = AdaptTheme.colors.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your Attempt: ",
                        style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.error
                    )
                    Text(
                        text = incorrectAttemptText,
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary,
                        maxLines = 1
                    )
                }

                // Correct Answer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AdaptIcons.Check,
                        contentDescription = null,
                        tint = AdaptTheme.colors.secondaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Correct Answer: ",
                        style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                    Text(
                        text = correctAnswerText,
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Explanation
            Text(
                text = "Key Insight: ${mistake.question.explanation}",
                style = AdaptTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                ),
                color = AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            // Actions: Retry Question & Ask AI Tutor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AdaptSecondaryButton(
                        text = "Retry Question",
                        onClick = onRetry,
                        leadingIcon = AdaptIcons.Retry
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

// ============================================================================
// Previews
// ============================================================================

@Preview(name = "Practice Screen - Dark Mode", showBackground = true)
@Composable
fun PracticeScreenDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        PracticeScreen(
            onNavigateBack = {},
            onSessionCompleted = {}
        )
    }
}

@Preview(name = "Practice Screen - Light Mode", showBackground = true)
@Composable
fun PracticeScreenLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        PracticeScreen(
            onNavigateBack = {},
            onSessionCompleted = {}
        )
    }
}

@Preview(name = "Practice Question - Dark Mode", showBackground = true)
@Composable
fun PracticeQuestionDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        PracticeActiveQuestionContent(
            question = AdaptiveLearningEngine.questionBank.first(),
            questionNumber = 1,
            totalQuestions = 3,
            selectedOption = 1,
            submission = null,
            canGoPrevious = false,
            onSelectOption = {},
            onSubmitAnswer = {},
            onPreviousClick = {},
            onNextClick = {},
            onExitSession = {}
        )
    }
}

@Preview(name = "Practice Mistake Notebook - Dark Mode", showBackground = true)
@Composable
fun PracticeMistakeNotebookDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        PracticeMistakeNotebookContent(
            mistakes = AdaptiveLearningEngine.mistakeNotebook,
            onRetryMistake = {},
            onAskTutor = {},
            onStartPractice = {}
        )
    }
}
