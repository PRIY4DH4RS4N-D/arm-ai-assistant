package com.example.adapt_ai_ui.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearningDomain
import com.example.adapt_ai_ui.data.OnboardingPreferences
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.ui.components.AdaptGlassSurface
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptProgressIndicator
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSurface
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

@Composable
fun OnboardingScreen(
    domain: LearningDomain,
    onNavigateBack: () -> Unit,
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current wizard step (1 to 4: 1=Goal & Level, 2=Time & Style, 3=Topic Priorities, 4=Diagnostic/Confirmation)
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = if (domain.diagnosticQuestions.isNotEmpty()) 4 else 3

    val learner = SampleData.learnerProfile

    // Pre-fill with existing learner selections to avoid resetting returning user's preferences
    var selectedGoal by remember {
        mutableStateOf(
            if (learner.activeDomainId == domain.id && learner.activeGoal.isNotBlank()) {
                learner.activeGoal
            } else {
                domain.recommendedGoals.firstOrNull() ?: "Master Core Technical Foundations"
            }
        )
    }
    var customGoalInput by remember { mutableStateOf("") }
    var isCustomGoalActive by remember { mutableStateOf(false) }

    var selectedSkillLevel by remember {
        mutableStateOf(learner.skillLevel)
    }

    // Supported daily study targets: 15, 25, 35, 45 minutes
    var selectedDailyMinutes by remember {
        mutableIntStateOf(if (learner.dailyMinutesGoal in listOf(15, 25, 35, 45)) learner.dailyMinutesGoal else 35)
    }

    var selectedStyle by remember {
        mutableStateOf(learner.preferredLearningStyle)
    }

    val selectedInterests = remember {
        mutableStateListOf<String>().apply {
            addAll(domain.suggestedTopics.take(3))
        }
    }
    var newTopicInput by remember { mutableStateOf("") }
    var topicInputError by remember { mutableStateOf<String?>(null) }

    // Diagnostic calibration state
    var diagnosticSelectedOption by remember { mutableStateOf<Int?>(null) }
    var isDiagnosticSubmitted by remember { mutableStateOf(false) }
    val activeDiagnostic = domain.diagnosticQuestions.firstOrNull()

    val finishOnboarding: () -> Unit = {
        val finalGoal = if (isCustomGoalActive && customGoalInput.trim().isNotBlank()) {
            customGoalInput.trim()
        } else {
            selectedGoal
        }

        val finalSkill = if (isDiagnosticSubmitted && diagnosticSelectedOption == activeDiagnostic?.correctIndex) {
            if (selectedSkillLevel == DifficultyLevel.BEGINNER) DifficultyLevel.INTERMEDIATE else selectedSkillLevel
        } else {
            selectedSkillLevel
        }

        val prefs = OnboardingPreferences(
            domainId = domain.id,
            domainTitle = domain.title,
            goal = finalGoal,
            skillLevel = finalSkill,
            dailyMinutes = selectedDailyMinutes,
            interests = selectedInterests.toList(),
            learningStyle = selectedStyle
        )
        SampleData.applyOnboardingPreferences(prefs)
        onOnboardingCompleted()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background)
            .statusBarsPadding()
    ) {
        // ====================================================================
        // 1. Wizard Top Bar & Stepper Progress
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, CircleShape)
                        .clickable(role = Role.Button) {
                            if (currentStep > 1) {
                                currentStep -= 1
                            } else {
                                onNavigateBack()
                            }
                        }
                        .semantics { contentDescription = "Go back" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AdaptIcons.ArrowBack,
                        contentDescription = null,
                        tint = AdaptTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "PERSONALIZED ONBOARDING",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent
                    )
                    Text(
                        text = "${domain.iconEmoji} ${domain.title}",
                        style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Step Indicator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AdaptTheme.radius.full))
                    .background(AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.25f))
                    .border(
                        AdaptTheme.stroke.hairline,
                        AdaptTheme.colors.primaryAccent.copy(alpha = 0.35f),
                        RoundedCornerShape(AdaptTheme.radius.full)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = AdaptTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.primaryAccent
                )
            }
        }

        // Stepper Progress Line
        AdaptProgressIndicator(
            progress = currentStep.toFloat() / totalSteps,
            height = 3.dp,
            color = AdaptTheme.colors.primaryAccent,
            trackColor = AdaptTheme.colors.borderSubtle
        )

        // ====================================================================
        // 2. Wizard Step Content (Animated Transition)
        // ====================================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "WizardStepAnimation"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        1 -> StepGoalAndSkillLevel(
                            domain = domain,
                            selectedGoal = selectedGoal,
                            onGoalSelected = {
                                selectedGoal = it
                                isCustomGoalActive = false
                            },
                            customGoal = customGoalInput,
                            onCustomGoalChange = {
                                customGoalInput = it
                                isCustomGoalActive = true
                            },
                            isCustomGoalActive = isCustomGoalActive,
                            onSelectCustomGoal = { isCustomGoalActive = true },
                            selectedSkillLevel = selectedSkillLevel,
                            onSkillLevelSelected = { selectedSkillLevel = it }
                        )

                        2 -> StepTimeAndStyle(
                            selectedMinutes = selectedDailyMinutes,
                            onMinutesSelected = { selectedDailyMinutes = it },
                            selectedStyle = selectedStyle,
                            onStyleSelected = { selectedStyle = it }
                        )

                        3 -> StepTopicInterests(
                            domain = domain,
                            selectedInterests = selectedInterests,
                            onToggleInterest = { topic ->
                                if (selectedInterests.contains(topic)) {
                                    if (selectedInterests.size > 1) selectedInterests.remove(topic)
                                } else {
                                    selectedInterests.add(topic)
                                }
                            },
                            newTopicInput = newTopicInput,
                            onNewTopicInputChange = {
                                newTopicInput = it
                                if (topicInputError != null) topicInputError = null
                            },
                            topicInputError = topicInputError,
                            onAddNewTopic = {
                                val trimmed = newTopicInput.trim()
                                when {
                                    trimmed.isBlank() -> {
                                        topicInputError = "Topic cannot be empty"
                                    }
                                    selectedInterests.any { it.equals(trimmed, ignoreCase = true) } -> {
                                        topicInputError = "Topic already added"
                                    }
                                    else -> {
                                        selectedInterests.add(trimmed)
                                        SampleData.addCustomTopic(trimmed)
                                        newTopicInput = ""
                                        topicInputError = null
                                    }
                                }
                            }
                        )

                        4 -> StepDiagnosticAndReview(
                            domain = domain,
                            selectedGoal = if (isCustomGoalActive && customGoalInput.trim().isNotBlank()) customGoalInput.trim() else selectedGoal,
                            selectedSkillLevel = selectedSkillLevel,
                            selectedDailyMinutes = selectedDailyMinutes,
                            selectedStyle = selectedStyle,
                            selectedTopicsCount = selectedInterests.size,
                            diagnostic = activeDiagnostic,
                            selectedOption = diagnosticSelectedOption,
                            isSubmitted = isDiagnosticSubmitted,
                            onOptionSelected = {
                                if (!isDiagnosticSubmitted) diagnosticSelectedOption = it
                            },
                            onSubmit = { isDiagnosticSubmitted = true },
                            onSkip = finishOnboarding
                        )
                    }
                }
            }
        }

        // ====================================================================
        // 3. Bottom Tactical Action Bar
        // ====================================================================
        AdaptSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = AdaptTheme.radius.large, topEnd = AdaptTheme.radius.large),
            backgroundColor = AdaptTheme.colors.surface,
            borderColor = AdaptTheme.colors.borderSubtle,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    AdaptSecondaryButton(
                        text = "← Back",
                        onClick = { currentStep -= 1 },
                        modifier = Modifier.weight(0.35f)
                    )
                }

                AdaptPrimaryButton(
                    text = if (currentStep == totalSteps) "Build My Personalized Pathway ✨" else "Continue →",
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep += 1
                        } else {
                            finishOnboarding()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ============================================================================
// STEP 1: Goal & Skill Level
// ============================================================================

@Composable
private fun StepGoalAndSkillLevel(
    domain: LearningDomain,
    selectedGoal: String,
    onGoalSelected: (String) -> Unit,
    customGoal: String,
    onCustomGoalChange: (String) -> Unit,
    isCustomGoalActive: Boolean,
    onSelectCustomGoal: () -> Unit,
    selectedSkillLevel: DifficultyLevel,
    onSkillLevelSelected: (DifficultyLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "PRIMARY LEARNING OBJECTIVE",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "What is your main goal for this track?",
                style = AdaptTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "ADAPT-AI dynamically tailors milestone sequencing and practical exercises around your goal.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            domain.recommendedGoals.forEach { goal ->
                val isSelected = !isCustomGoalActive && goal == selectedGoal

                AdaptSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton, onClick = { onGoalSelected(goal) })
                        .semantics {
                            selected = isSelected
                            contentDescription = "Select goal: $goal"
                        },
                    backgroundColor = if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f) else AdaptTheme.colors.raisedSurface,
                    borderColor = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                    borderWidth = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal,
                            style = AdaptTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textMuted,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AdaptTheme.colors.primaryAccent)
                                )
                            }
                        }
                    }
                }
            }

            // Custom Objective Field
            AdaptSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.RadioButton, onClick = onSelectCustomGoal),
                backgroundColor = if (isCustomGoalActive) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f) else AdaptTheme.colors.raisedSurface,
                borderColor = if (isCustomGoalActive) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                borderWidth = if (isCustomGoalActive) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Learning Objective",
                            style = AdaptTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCustomGoalActive) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCustomGoalActive) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (isCustomGoalActive) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textMuted,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCustomGoalActive) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AdaptTheme.colors.primaryAccent)
                                )
                            }
                        }
                    }

                    if (isCustomGoalActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customGoal,
                            onValueChange = onCustomGoalChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Build an autonomous edge object detector", style = AdaptTheme.typography.bodySmall) },
                            singleLine = true,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                    }
                }
            }
        }

        // Skill Level Section
        Column {
            Text(
                text = "STARTING PROFICIENCY LEVEL",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "What is your current familiarity?",
                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "Calibrates the pedagogical depth of Socratic tutor prompts and interactive lessons.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyLevel.entries.forEach { level ->
                val isSelected = level == selectedSkillLevel

                val description = when (level) {
                    DifficultyLevel.BEGINNER -> "🌱 Starting from fundamentals, intuitive explanations & guided drills"
                    DifficultyLevel.INTERMEDIATE -> "⚡ Working knowledge, practical implementation & architectural trade-offs"
                    DifficultyLevel.ADVANCED -> "🚀 Deep technical mastery, edge constraints & hardware optimization"
                }

                AdaptSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton, onClick = { onSkillLevelSelected(level) })
                        .semantics {
                            selected = isSelected
                            contentDescription = "Select ${level.label} level"
                        },
                    backgroundColor = if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f) else AdaptTheme.colors.raisedSurface,
                    borderColor = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                    borderWidth = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.label,
                                style = AdaptTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = AdaptIcons.Check,
                                contentDescription = null,
                                tint = AdaptTheme.colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 2: Daily Study Time & Learning Style
// ============================================================================

@Composable
private fun StepTimeAndStyle(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    selectedStyle: String,
    onStyleSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "DAILY STUDY AVAILABILITY",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Daily micro-learning commitment",
                style = AdaptTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "Consistent daily study prevents cognitive overload and maintains active memory retention.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        // Exact supported options: 15, 25, 35, 45 minutes
        val timeOptions = listOf(
            15 to "Bite-sized daily check-in",
            25 to "Focused study session",
            35 to "Deep technical practice",
            45 to "Accelerated mastery"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeOptions.forEach { (minutes, label) ->
                val isSelected = minutes == selectedMinutes

                AdaptSurface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.RadioButton, onClick = { onMinutesSelected(minutes) })
                        .semantics {
                            selected = isSelected
                            contentDescription = "$minutes minutes per day: $label"
                        },
                    backgroundColor = if (isSelected) AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.25f) else AdaptTheme.colors.raisedSurface,
                    borderColor = if (isSelected) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.borderSubtle,
                    borderWidth = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${minutes}m",
                            style = AdaptTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (isSelected) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily Target",
                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = AdaptTheme.colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            Text(
                text = "PEDAGOGICAL LEARNING STYLE",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Preferred internal mental model",
                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "How do you internalize complex technical concepts most naturally?",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        val styles = listOf(
            "Hands-on Projects & Code" to "Active code examples, live simulations & execution diagrams",
            "Visual Intuition & Diagrams" to "Hardware block layouts, dataflow charts & visual mental models",
            "Mathematical Formalism" to "Rigorous mathematical derivations, loss surfaces & quantization proofs",
            "Concept-First & Intuition" to "Deep conceptual breakdown before technical implementation",
            "Socratic Q&A Dialogue" to "Interactive question-driven guided discovery prompts"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.forEach { (styleTitle, styleDescription) ->
                val isSelected = styleTitle == selectedStyle

                AdaptSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton, onClick = { onStyleSelected(styleTitle) })
                        .semantics {
                            selected = isSelected
                            contentDescription = "Learning style: $styleTitle"
                        },
                    backgroundColor = if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f) else AdaptTheme.colors.raisedSurface,
                    borderColor = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                    borderWidth = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = styleTitle,
                                style = AdaptTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = styleDescription,
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = AdaptIcons.Check,
                                contentDescription = null,
                                tint = AdaptTheme.colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 3: Topic Priorities & Custom Topics
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTopicInterests(
    domain: LearningDomain,
    selectedInterests: List<String>,
    onToggleInterest: (String) -> Unit,
    newTopicInput: String,
    onNewTopicInputChange: (String) -> Unit,
    topicInputError: String?,
    onAddNewTopic: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "CURRICULUM FOCUS & TOPIC SELECTION",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Prioritize topics in your learning journey",
                style = AdaptTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "Select key concepts you want prioritized across interactive lessons and daily practice drills.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        // Available Topics Multi-Select
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            domain.suggestedTopics.forEach { topic ->
                val isSelected = selectedInterests.contains(topic)

                AdaptSurface(
                    modifier = Modifier
                        .clickable(role = Role.Checkbox, onClick = { onToggleInterest(topic) })
                        .semantics {
                            selected = isSelected
                            contentDescription = "Toggle topic $topic"
                        },
                    backgroundColor = if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f) else AdaptTheme.colors.raisedSurface,
                    borderColor = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                    borderWidth = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isSelected) "✓ $topic" else "+ $topic",
                            style = AdaptTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                        )
                    }
                }
            }
        }

        // Add Custom Topic Field
        Column {
            Text(
                text = "ADD YOUR OWN CUSTOM TOPIC",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTopicInput,
                    onValueChange = onNewTopicInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "e.g. Memory-Mapped Registers",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(AdaptTheme.radius.medium)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.primaryAccent)
                        .clickable(role = Role.Button, onClick = onAddNewTopic)
                        .semantics { contentDescription = "Add custom topic" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AdaptIcons.Plus,
                        contentDescription = null,
                        tint = if (AdaptTheme.isDark) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (topicInputError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topicInputError,
                    style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AdaptTheme.colors.error
                )
            }
        }
    }
}

// ============================================================================
// STEP 4: Diagnostic Calibration & Summary Review
// ============================================================================

@Composable
private fun StepDiagnosticAndReview(
    domain: LearningDomain,
    selectedGoal: String,
    selectedSkillLevel: DifficultyLevel,
    selectedDailyMinutes: Int,
    selectedStyle: String,
    selectedTopicsCount: Int,
    diagnostic: com.example.adapt_ai_ui.data.DiagnosticQuestion?,
    selectedOption: Int?,
    isSubmitted: Boolean,
    onOptionSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "CALIBRATION & VERIFICATION",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Curriculum Blueprint Review",
                style = AdaptTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "Review your configured personalized syllabus before activating your on-device learning pathway.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        // Summary Card
        AdaptGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            isGlassEnabled = true,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${domain.iconEmoji} ${domain.title}",
                        style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary
                    )
                    Text(
                        text = "Active Track",
                        style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Objective:", style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                    Text(selectedGoal, style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = AdaptTheme.colors.textPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Starting Level:", style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                    Text(selectedSkillLevel.label, style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = AdaptTheme.colors.primaryAccent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Target:", style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                    Text("${selectedDailyMinutes}m/day", style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AdaptTheme.colors.secondaryAccent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Learning Style:", style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                    Text(selectedStyle, style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = AdaptTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Prioritized Topics:", style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                    Text("$selectedTopicsCount concepts selected", style = AdaptTheme.typography.labelSmall, color = AdaptTheme.colors.textPrimary)
                }
            }
        }

        // Optional Diagnostic Check
        if (diagnostic != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPTIONAL DIAGNOSTIC CHECK",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.secondaryAccent
                    )
                    Text(
                        text = "Skip Diagnostic →",
                        style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.secondaryAccent,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onSkip)
                            .padding(4.dp)
                    )
                }

                AdaptSurface(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AdaptTheme.colors.surface,
                    borderColor = AdaptTheme.colors.borderSubtle,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = diagnostic.question,
                            style = AdaptTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AdaptTheme.colors.textPrimary
                        )

                        diagnostic.options.forEachIndexed { index, option ->
                            val isSelected = selectedOption == index
                            val isCorrect = isSubmitted && index == diagnostic.correctIndex
                            val isWrong = isSubmitted && isSelected && !isCorrect

                            val bg = when {
                                isCorrect -> AdaptTheme.colors.success.copy(alpha = 0.2f)
                                isWrong -> AdaptTheme.colors.error.copy(alpha = 0.2f)
                                isSelected -> AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.3f)
                                else -> AdaptTheme.colors.raisedSurface
                            }
                            val border = when {
                                isCorrect -> AdaptTheme.colors.success
                                isWrong -> AdaptTheme.colors.error
                                isSelected -> AdaptTheme.colors.primaryAccent
                                else -> AdaptTheme.colors.borderSubtle
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                    .background(bg)
                                    .border(AdaptTheme.stroke.hairline, border, RoundedCornerShape(AdaptTheme.radius.medium))
                                    .clickable(enabled = !isSubmitted, role = Role.RadioButton) { onOptionSelected(index) }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = option,
                                    style = AdaptTheme.typography.bodySmall,
                                    color = AdaptTheme.colors.textPrimary
                                )
                            }
                        }

                        if (selectedOption != null && !isSubmitted) {
                            AdaptPrimaryButton(
                                text = "Verify Diagnostic Answer",
                                onClick = onSubmit
                            )
                        }

                        if (isSubmitted) {
                            Text(
                                text = "💡 ${diagnostic.explanation}",
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Truthful disclaimer
        Text(
            text = "Personalized curriculum is generated from pre-calibrated verified track blueprints with on-device Socratic AI calibration. All study progress is saved strictly locally.",
            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = AdaptTheme.colors.textMuted
        )
    }
}

// -------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------
@Preview(name = "Onboarding - Dark", showBackground = true)
@Composable
private fun OnboardingDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        OnboardingScreen(
            domain = DomainRepository.domains.first(),
            onNavigateBack = {},
            onOnboardingCompleted = {}
        )
    }
}

@Preview(name = "Onboarding - Light", showBackground = true)
@Composable
private fun OnboardingLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        OnboardingScreen(
            domain = DomainRepository.domains.first(),
            onNavigateBack = {},
            onOnboardingCompleted = {}
        )
    }
}
