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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.LessonContent
import com.example.adapt_ai_ui.data.MetricPair
import com.example.adapt_ai_ui.data.PracticeQuestion
import com.example.adapt_ai_ui.data.RealWorldExample
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Editorial Obsidian Liquid Glass Interactive Lesson Screen for ADAPT-AI.
 *
 * Visual & Functional Architecture (Phase 4):
 * 1. Lesson Masthead: Circular back navigation, bookmark persistence action, curriculum overline,
 *    lesson title, estimated duration, and 4-step progress indicator.
 * 2. Step 1 (Foundations): Hardware bottleneck breakdown, 32-bit vs 8-bit memory analysis with
 *    copyable code blocks, and key architectural takeaways.
 * 3. Step 2 (Mathematics): Linear quantization formulation (r = S * (q - Z)), affine vs symmetric
 *    comparative breakdown, and calibration deep dive.
 * 4. Step 3 (Interactive Laboratory & Benchmarks): Real-time INT8 Quantization Calculator
 *    (symmetric vs affine, slider input, binary/hex conversion, quantization error) + Real-World Edge AI
 *    Smart Security Camera case study with before/after metrics.
 * 5. Step 4 (Concept Verification & Completion): Knowledge check quiz with immediate feedback and
 *    AdaptiveLearningEngine synchronization, Socratic AI Tutor action hub, and genuine milestone completion.
 */
@Composable
fun LessonScreen(
    onNavigateBack: () -> Unit,
    onAskTutor: (TutorQuickPrompt) -> Unit,
    onContinueToPractice: () -> Unit,
    onCompleteLesson: () -> Unit,
    modifier: Modifier = Modifier,
    lesson: LessonContent = remember { SampleData.sampleLesson }
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 4-step pedagogical structure
    var currentStepIndex by remember { mutableIntStateOf(1) }
    val totalSteps = 4

    var isDeepDiveExpanded by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isLessonCompleted by remember { mutableStateOf(SampleData.isCurrentLessonCompleted) }
    var isCompleting by remember { mutableStateOf(false) }

    var isBookmarked by remember { mutableStateOf(SampleData.isLessonBookmarked(lesson.id)) }

    fun scrollToTop() {
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background),
        contentPadding = PaddingValues(
            start = AdaptTheme.spacing.extraLarge,
            end = AdaptTheme.spacing.extraLarge,
            top = AdaptTheme.spacing.large,
            bottom = AdaptTheme.spacing.giant
        ),
        verticalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.large)
    ) {
        // --------------------------------------------------------------------
        // 1. Lesson Masthead with Navigation, Bookmark & Step Indicator
        // --------------------------------------------------------------------
        item {
            LessonEditorialMasthead(
                lesson = lesson,
                currentStep = currentStepIndex,
                totalSteps = totalSteps,
                isBookmarked = isBookmarked,
                onToggleBookmark = {
                    isBookmarked = SampleData.toggleBookmark(lesson.id)
                },
                onStepSelect = { step ->
                    currentStepIndex = step
                    scrollToTop()
                },
                onNavigateBack = onNavigateBack
            )
        }

        // --------------------------------------------------------------------
        // 2. Dynamic Step-Based Content
        // --------------------------------------------------------------------
        when (currentStepIndex) {
            1 -> {
                // STEP 1: Hardware Foundations & The Memory Bottleneck
                item {
                    LessonStepFoundations(
                        overview = lesson.overview,
                        conceptSection = lesson.conceptSections.firstOrNull(),
                        takeaways = lesson.keyTakeaways,
                        onNextStep = {
                            currentStepIndex = 2
                            scrollToTop()
                        }
                    )
                }
            }

            2 -> {
                // STEP 2: Mathematical Theory & Calibration
                item {
                    LessonStepMathematics(
                        conceptSection = lesson.conceptSections.getOrNull(1),
                        deepDive = lesson.deepDive,
                        isExpanded = isDeepDiveExpanded,
                        onToggleExpand = { isDeepDiveExpanded = !isDeepDiveExpanded },
                        onPreviousStep = {
                            currentStepIndex = 1
                            scrollToTop()
                        },
                        onNextStep = {
                            currentStepIndex = 3
                            scrollToTop()
                        }
                    )
                }
            }

            3 -> {
                // STEP 3: Interactive Quantization Laboratory & Case Study
                item {
                    LessonStepInteractiveLab(
                        realWorldExample = lesson.realWorldExample,
                        onPreviousStep = {
                            currentStepIndex = 2
                            scrollToTop()
                        },
                        onNextStep = {
                            currentStepIndex = 4
                            scrollToTop()
                        }
                    )
                }
            }

            4 -> {
                // STEP 4: Verification, Socratic Tutor & Completion Flow
                item {
                    LessonStepVerification(
                        lessonTitle = lesson.title,
                        practiceQuestion = lesson.practiceQuestion,
                        selectedOption = selectedOptionIndex,
                        isCompleted = isLessonCompleted || isCompleting,
                        isCompleting = isCompleting,
                        onSelectOption = { index ->
                            selectedOptionIndex = index
                            // Synchronize with AdaptiveLearningEngine question bank
                            val matchingQ = AdaptiveLearningEngine.questionBank.firstOrNull()
                            if (matchingQ != null) {
                                AdaptiveLearningEngine.submitAnswer(matchingQ, index)
                            }
                        },
                        onAskTutor = {
                            onAskTutor(
                                TutorQuickPrompt(
                                    id = "prompt_lesson_int8",
                                    prompt = "Can you give me an intuitive explanation of the INT8 scale factor, zero-point offset, and ARM NEON registers?",
                                    tag = "INT8 Lesson"
                                )
                            )
                        },
                        onContinueToPractice = onContinueToPractice,
                        onPreviousStep = {
                            currentStepIndex = 3
                            scrollToTop()
                        },
                        onCompleteClick = {
                            if (!isCompleting) {
                                isCompleting = true
                                isLessonCompleted = true
                                onCompleteLesson()
                            }
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Subcomponents for Clean Architecture & Reusability
// ============================================================================

/**
 * Editorial Lesson Masthead with Navigation, Bookmark Action, and Step Selector.
 */
@Composable
private fun LessonEditorialMasthead(
    lesson: LessonContent,
    currentStep: Int,
    totalSteps: Int,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onStepSelect: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Navigation & Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    .semantics { contentDescription = "Return to previous screen" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AdaptIcons.ArrowBack,
                    contentDescription = null,
                    tint = AdaptTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Bookmark Action Button (>= 48dp touch target)
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
                            onClick = onToggleBookmark
                        )
                        .semantics {
                            contentDescription = if (isBookmarked) "Remove lesson bookmark" else "Save lesson bookmark"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBookmarked) AdaptIcons.Bookmark else AdaptIcons.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isBookmarked) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                // ARM SoC Educational Study Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.small)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "ARM ARCHITECTURE STUDY",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Track and Step Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${lesson.trackName.uppercase()} • STEP $currentStep OF $totalSteps",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )
            Text(
                text = "~${lesson.estimatedMinutes} mins estimated",
                style = AdaptTheme.typography.labelSmall,
                color = AdaptTheme.colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

        // Topic Title
        Text(
            text = lesson.title,
            style = AdaptTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AdaptTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle
        Text(
            text = lesson.subtitle,
            style = AdaptTheme.typography.bodyMedium,
            color = AdaptTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Interactive Step Navigation Bar
        val stepLabels = listOf("1. Foundations", "2. Math Theory", "3. Interactive Lab", "4. Verification")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            stepLabels.forEachIndexed { index, label ->
                val stepNum = index + 1
                val isSelected = currentStep == stepNum
                val isDone = currentStep > stepNum

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .background(
                            when {
                                isSelected -> AdaptTheme.colors.primaryAccentContainer
                                isDone -> AdaptTheme.colors.raisedSurface
                                else -> AdaptTheme.colors.raisedSurface.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = when {
                                isSelected -> AdaptTheme.colors.primaryAccent
                                isDone -> AdaptTheme.colors.secondaryAccent.copy(alpha = 0.5f)
                                else -> AdaptTheme.colors.borderSubtle
                            },
                            shape = RoundedCornerShape(AdaptTheme.radius.small)
                        )
                        .clickable(role = Role.Tab) { onStepSelect(stepNum) }
                        .semantics {
                            selected = isSelected
                            contentDescription = label
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDone) "✓ $stepNum" else "$stepNum",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = when {
                            isSelected -> AdaptTheme.colors.primaryAccent
                            isDone -> AdaptTheme.colors.secondaryAccent
                            else -> AdaptTheme.colors.textMuted
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

        // Continuous Progress Indicator
        AdaptProgressIndicator(
            progress = currentStep.toFloat() / totalSteps,
            color = AdaptTheme.colors.primaryAccent,
            height = 3.dp
        )
    }
}

/**
 * Step 1: Foundations — Edge AI Memory Bottleneck & Key Takeaways.
 */
@Composable
private fun LessonStepFoundations(
    overview: String,
    conceptSection: com.example.adapt_ai_ui.data.ConceptSection?,
    takeaways: List<String>,
    onNextStep: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "STEP 1: HARDWARE BOTTLENECK",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.primaryAccent
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

        Text(
            text = "The Memory Bottleneck in Edge AI",
            style = AdaptTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AdaptTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

        Text(
            text = overview,
            style = AdaptTheme.typography.bodyLarge,
            color = AdaptTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Concept Explanation Body
        conceptSection?.let { section ->
            Text(
                text = section.body,
                style = AdaptTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = AdaptTheme.colors.textSecondary
            )

            section.codeOrFormula?.let { code ->
                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))
                EditorialCodeBlock(
                    label = "MEMORY FOOTPRINT COMPARISON",
                    code = code
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Key Architectural Takeaways
        KeyTakeawaysRibbon(takeaways = takeaways)

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraLarge))

        // Step Navigation
        AdaptPrimaryButton(
            text = "Next: Mathematical Theory →",
            onClick = onNextStep,
            trailingIcon = AdaptIcons.ArrowForward
        )
    }
}

/**
 * Step 2: Mathematical Theory — Linear Quantization Formula & Calibration Deep Dive.
 */
@Composable
private fun LessonStepMathematics(
    conceptSection: com.example.adapt_ai_ui.data.ConceptSection?,
    deepDive: com.example.adapt_ai_ui.data.DeepDiveSection,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "STEP 2: NUMERICAL FORMULATION",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.primaryAccent
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

        Text(
            text = "The Linear Quantization Formula",
            style = AdaptTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AdaptTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

        conceptSection?.let { section ->
            Text(
                text = section.body,
                style = AdaptTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = AdaptTheme.colors.textSecondary
            )

            section.codeOrFormula?.let { formula ->
                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))
                EditorialCodeBlock(
                    label = "LINEAR QUANTIZATION MAPPING",
                    code = formula
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Symmetric vs Affine Quantization Comparison Cards
        Text(
            text = "AFFINE VS SYMMETRIC QUANTIZATION",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.medium))
                    .padding(AdaptTheme.spacing.medium)
            ) {
                Column {
                    Text(
                        text = "Affine (Asymmetric)",
                        style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uses non-zero ZeroPoint Z. Preserves exact 0.0 mapping for ReLU activations with asymmetric ranges.",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(AdaptTheme.radius.medium))
                    .padding(AdaptTheme.spacing.medium)
            ) {
                Column {
                    Text(
                        text = "Symmetric (Z = 0)",
                        style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.primaryAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Locks Z = 0. Omits subtraction, accelerating hardware MAC loops on ARM NEON SIMD vector engines.",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Deep Dive Accordion
        EditorialDeepDiveAccordion(
            deepDive = deepDive,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraLarge))

        // Step Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AdaptSecondaryButton(
                    text = "← Foundations",
                    onClick = onPreviousStep
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AdaptPrimaryButton(
                    text = "Next: Interactive Lab →",
                    onClick = onNextStep
                )
            }
        }
    }
}

/**
 * Step 3: Interactive Technical Visualization (INT8 Calculator) & Real-World Case Study.
 */
@Composable
private fun LessonStepInteractiveLab(
    realWorldExample: RealWorldExample,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "STEP 3: INTERACTIVE LAB & BENCHMARKS",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.primaryAccent
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

        Text(
            text = "INT8 Quantization Calculator",
            style = AdaptTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AdaptTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Adjust the floating-point weight below to observe real-time conversion to 8-bit integer format and calculate quantization error.",
            style = AdaptTheme.typography.bodySmall,
            color = AdaptTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Authentic Interactive INT8 Quantization Lab
        Int8QuantizationLaboratory()

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraLarge))

        // Real-World Edge AI Benchmark Case Study
        RealWorldBenchmarkCard(example = realWorldExample)

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraLarge))

        // Step Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AdaptSecondaryButton(
                    text = "← Math Theory",
                    onClick = onPreviousStep
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AdaptPrimaryButton(
                    text = "Next: Verification →",
                    onClick = onNextStep
                )
            }
        }
    }
}

/**
 * Authentic, mathematically exact INT8 Quantization Interactive Laboratory.
 */
@Composable
private fun Int8QuantizationLaboratory() {
    var inputFloat by remember { mutableFloatStateOf(1.42f) }
    var isSymmetric by remember { mutableStateOf(true) }

    // Dynamic mathematical calculations
    val scale = 0.0235f // Derived from [-3.0, +3.0] dynamic range
    val zeroPoint = if (isSymmetric) 0 else -12

    // Quantize: q = round(r / S) + Z, clamped to [-128, 127]
    val unconstrainedQ = (inputFloat / scale).roundToInt() + zeroPoint
    val quantizedInt = unconstrainedQ.coerceIn(-128, 127)

    // Dequantize: r_recon = S * (q - Z)
    val reconstructedFloat = scale * (quantizedInt - zeroPoint)
    val quantizationError = abs(inputFloat - reconstructedFloat)

    // 8-bit two's complement binary representation
    val byteVal = quantizedInt.toByte().toInt() and 0xFF
    val binaryString = "0b" + byteVal.toString(2).padStart(8, '0')
    val hexString = "0x" + byteVal.toString(16).uppercase().padStart(2, '0')

    val labShape = RoundedCornerShape(AdaptTheme.radius.extraLarge)

    val ambientGlow = Brush.radialGradient(
        colors = listOf(
            AdaptTheme.colors.primaryAccent.copy(alpha = 0.14f),
            Color.Transparent
        ),
        center = Offset(800f, 0f),
        radius = 600f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(labShape)
            .background(AdaptTheme.colors.glassSurface)
            .background(ambientGlow)
            .border(
                width = AdaptTheme.stroke.hairline,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x80FFFFFF),
                        AdaptTheme.colors.primaryAccent.copy(alpha = 0.40f),
                        AdaptTheme.colors.borderSubtle
                    )
                ),
                shape = labShape
            )
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Lab Header & Mode Selector
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
                            .background(AdaptTheme.colors.secondaryAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KOTLIN ARITHMETIC SIMULATION",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }

                // Mode toggle pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AdaptPill(
                        text = "Symmetric (Z=0)",
                        isSelected = isSymmetric,
                        accentColor = AdaptTheme.colors.primaryAccent,
                        onClick = { isSymmetric = true }
                    )
                    AdaptPill(
                        text = "Affine (Z=-12)*",
                        isSelected = !isSymmetric,
                        accentColor = AdaptTheme.colors.primaryAccent,
                        onClick = { isSymmetric = false }
                    )
                }
            }

            if (!isSymmetric) {
                Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))
                Text(
                    text = "* Illustrative zero-point: Z = -12 is an educational example demonstrating asymmetric activation mapping (where real 0.0 maps to integer -12 on the [-128, 127] grid), rather than an empirical value derived from a calibrated dataset.",
                    style = AdaptTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = AdaptTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            // Float Slider Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Input Float (r):",
                    style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AdaptTheme.colors.textPrimary
                )
                Text(
                    text = String.format(Locale.US, "%.2f", inputFloat),
                    style = AdaptTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AdaptTheme.colors.secondaryAccent
                )
            }

            Slider(
                value = inputFloat,
                onValueChange = { inputFloat = it },
                valueRange = -3.0f..3.0f,
                steps = 60,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AdaptTheme.colors.primaryAccent,
                    activeTrackColor = AdaptTheme.colors.primaryAccent,
                    inactiveTrackColor = AdaptTheme.colors.raisedSurface
                )
            )

            // Preset value quick chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(-2.15f, -0.80f, 0.00f, 1.42f, 2.80f).forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.small))
                            .background(
                                if (abs(inputFloat - preset) < 0.05f)
                                    AdaptTheme.colors.primaryAccentContainer
                                else
                                    AdaptTheme.colors.raisedSurface
                            )
                            .clickable(role = Role.Button) { inputFloat = preset }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%+.2f", preset),
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = if (abs(inputFloat - preset) < 0.05f)
                                AdaptTheme.colors.primaryAccent
                            else
                                AdaptTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

            // Mathematical Conversion Data Matrix
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                    .background(AdaptTheme.colors.raisedSurface)
                    .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.medium))
                    .padding(AdaptTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Quantized Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantized INT8 (q):",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                    Text(
                        text = "$quantizedInt ($binaryString / $hexString)",
                        style = AdaptTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.primaryAccent
                    )
                }

                // Row 2: Reconstructed Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reconstructed (r̂):",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f", reconstructedFloat),
                        style = AdaptTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.textPrimary
                    )
                }

                // Row 3: Quantization Error
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantization Error (ε):",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f", quantizationError),
                        style = AdaptTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (quantizationError < 0.015f) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.warning
                    )
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            // Hardware Efficiency Note
            Text(
                text = "⚡ Hardware Concept (Simulated in Kotlin): On actual ARM processors with NEON SIMD, 16 packed 8-bit integer weights fit inside a 128-bit vector register (int8x16_t), cutting memory bandwidth by 75% compared to FP32.",
                style = AdaptTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = AdaptTheme.colors.secondaryAccent
            )
        }
    }
}

/**
 * Real-World Edge AI Benchmark Case Study Component.
 */
@Composable
private fun RealWorldBenchmarkCard(
    example: RealWorldExample
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(AdaptTheme.colors.raisedSurface)
            .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.large))
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ILLUSTRATIVE CASE STUDY • EDGE AI ARCHITECTURE",
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.primaryAccent
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

            Text(
                text = example.title,
                style = AdaptTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AdaptTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = example.scenario,
                style = AdaptTheme.typography.bodyMedium,
                color = AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            // Metrics Comparison Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                    .background(AdaptTheme.colors.surface)
                    .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.medium))
                    .padding(AdaptTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                example.comparisonMetrics.forEach { metric ->
                    MetricComparisonRow(metric = metric)
                }
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            Text(
                text = "💡 ${example.impactSummary}",
                style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = AdaptTheme.colors.secondaryAccent
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            Text(
                text = "* Illustrative architectural scenario based on typical Cortex-M55 / Ethos-U micro-NPU edge deployment estimates, demonstrating on-chip SRAM retention vs external DRAM bus transfers.",
                style = AdaptTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = AdaptTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun MetricComparisonRow(metric: MetricPair) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.label,
                style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "FP32: ${metric.beforeValue} → INT8: ${metric.afterValue}",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AdaptTheme.radius.small))
                .background(AdaptTheme.colors.secondaryAccentContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = metric.improvementLabel,
                style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.secondaryAccent
            )
        }
    }
}

/**
 * Step 4: Verification, Socratic Tutor Hub & Milestone Completion.
 */
@Composable
private fun LessonStepVerification(
    lessonTitle: String,
    practiceQuestion: PracticeQuestion,
    selectedOption: Int?,
    isCompleted: Boolean,
    isCompleting: Boolean,
    onSelectOption: (Int) -> Unit,
    onAskTutor: () -> Unit,
    onContinueToPractice: () -> Unit,
    onPreviousStep: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "STEP 4: CONCEPT VERIFICATION",
            style = AdaptTheme.typographyExt.overline,
            color = AdaptTheme.colors.primaryAccent
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.extraSmall))

        Text(
            text = "Knowledge Check & Verification",
            style = AdaptTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AdaptTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

        // Quiz Question Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AdaptTheme.radius.large))
                .background(AdaptTheme.colors.raisedSurface)
                .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.large))
                .padding(AdaptTheme.spacing.large)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = practiceQuestion.question,
                    style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AdaptTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                practiceQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrect = index == practiceQuestion.correctIndex

                    val borderColor = when {
                        isSelected && isCorrect -> AdaptTheme.colors.secondaryAccent
                        isSelected && !isCorrect -> AdaptTheme.colors.error
                        else -> AdaptTheme.colors.borderSubtle
                    }

                    val backgroundColor = when {
                        isSelected && isCorrect -> AdaptTheme.colors.secondaryAccentContainer
                        isSelected && !isCorrect -> AdaptTheme.colors.error.copy(alpha = 0.15f)
                        else -> AdaptTheme.colors.surface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(backgroundColor)
                            .border(AdaptTheme.stroke.hairline, borderColor, RoundedCornerShape(AdaptTheme.radius.medium))
                            .clickable(
                                role = Role.RadioButton,
                                onClick = { onSelectOption(index) }
                            )
                            .semantics {
                                selected = isSelected
                                contentDescription = "Option ${index + 1}: $option"
                            }
                            .padding(AdaptTheme.spacing.medium)
                    ) {
                        Text(
                            text = option,
                            style = AdaptTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = AdaptTheme.colors.textPrimary
                        )
                    }
                }

                // Immediate Feedback Box
                if (selectedOption != null) {
                    val isCorrect = selectedOption == practiceQuestion.correctIndex
                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(
                                if (isCorrect)
                                    AdaptTheme.colors.secondaryAccentContainer
                                else
                                    AdaptTheme.colors.raisedSurface
                            )
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = if (isCorrect) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent,
                                shape = RoundedCornerShape(AdaptTheme.radius.medium)
                            )
                            .padding(AdaptTheme.spacing.medium)
                    ) {
                        Column {
                            Text(
                                text = if (isCorrect) "🎉 Correct! Concept Verified." else "💡 Hint & Insight",
                                style = AdaptTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCorrect) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.primaryAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = practiceQuestion.explanation,
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Socratic AI Tutor & Practice Hub Actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AdaptTheme.radius.large))
                .background(AdaptTheme.colors.raisedSurface)
                .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.large))
                .padding(AdaptTheme.spacing.large)
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
                            imageVector = AdaptIcons.Sparkle,
                            contentDescription = null,
                            tint = AdaptTheme.colors.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                    Column {
                        Text(
                            text = "Interactive Learning Steps",
                            style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )
                        Text(
                            text = "Consult the Socratic Tutor about $lessonTitle or take a multi-question quiz",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AdaptTheme.spacing.small)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdaptSecondaryButton(
                            text = "Ask AI Tutor",
                            onClick = onAskTutor,
                            leadingIcon = AdaptIcons.Sparkle
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdaptSecondaryButton(
                            text = "Practice Hub",
                            onClick = onContinueToPractice,
                            leadingIcon = AdaptIcons.Target
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptTheme.spacing.large))

        // Lesson Completion Action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AdaptPrimaryButton(
                text = if (isCompleted) "Completed! Returning to Journey..." else "Complete Lesson & Return",
                onClick = onCompleteClick,
                enabled = !isCompleting,
                leadingIcon = AdaptIcons.Check
            )

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.small))

            AdaptSecondaryButton(
                text = "← Previous: Interactive Lab",
                onClick = onPreviousStep
            )
        }
    }
}

/**
 * Editorial Monospace Code / Formula Block with Copy Action.
 */
@Composable
private fun EditorialCodeBlock(
    label: String,
    code: String
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
            .background(AdaptTheme.colors.codeBackground)
            .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.medium))
            .padding(AdaptTheme.spacing.medium)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = AdaptTheme.typographyExt.overline,
                    color = AdaptTheme.colors.primaryAccent
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .clickable(role = Role.Button) {
                            clipboardManager.setText(AnnotatedString(code))
                            isCopied = true
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Copy code to clipboard" }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCopied) AdaptIcons.Check else AdaptIcons.Copy,
                            contentDescription = null,
                            tint = if (isCopied) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isCopied) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = code,
                style = AdaptTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                color = AdaptTheme.colors.textPrimary
            )
        }
    }
}

/**
 * Key Architectural Takeaways Ribbon.
 */
@Composable
private fun KeyTakeawaysRibbon(
    takeaways: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(AdaptTheme.colors.raisedSurface)
            .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f), RoundedCornerShape(AdaptTheme.radius.large))
            .padding(AdaptTheme.spacing.large)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(AdaptTheme.colors.secondaryAccentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AdaptIcons.Lightbulb,
                        contentDescription = null,
                        tint = AdaptTheme.colors.secondaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                Text(
                    text = "Key Architectural Takeaways",
                    style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AdaptTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

            takeaways.forEach { takeaway ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.secondaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AdaptIcons.Check,
                            contentDescription = null,
                            tint = if (AdaptTheme.isDark) Color(0xFF042F2E) else Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                    Text(
                        text = takeaway,
                        style = AdaptTheme.typography.bodyMedium,
                        color = AdaptTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Expandable Deep Dive Accordion.
 */
@Composable
private fun EditorialDeepDiveAccordion(
    deepDive: com.example.adapt_ai_ui.data.DeepDiveSection,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(AdaptTheme.colors.raisedSurface)
            .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, RoundedCornerShape(AdaptTheme.radius.large))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClick = onToggleExpand
                    )
                    .semantics { contentDescription = "Expand deep dive on ${deepDive.title}" }
                    .padding(AdaptTheme.spacing.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deepDive.title,
                        style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = deepDive.teaser,
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(AdaptTheme.spacing.small))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AdaptTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) AdaptIcons.ChevronUp else AdaptIcons.ChevronDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = AdaptTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = AdaptTheme.spacing.large, end = AdaptTheme.spacing.large, bottom = AdaptTheme.spacing.large)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AdaptTheme.stroke.hairline)
                            .background(AdaptTheme.colors.borderSubtle)
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    Text(
                        text = deepDive.detailedExplanation,
                        style = AdaptTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = AdaptTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AdaptTheme.spacing.medium))

                    EditorialCodeBlock(
                        label = "CALIBRATION FORMULA",
                        code = deepDive.formulaExplanation
                    )
                }
            }
        }
    }
}

// ============================================================================
// Previews for Editorial Obsidian Liquid Glass Lesson Screen
// ============================================================================

@Preview(name = "Lesson Screen - Dark Mode", showBackground = true)
@Composable
fun LessonEditorialDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        LessonScreen(
            onNavigateBack = {},
            onAskTutor = {},
            onContinueToPractice = {},
            onCompleteLesson = {}
        )
    }
}

@Preview(name = "Lesson Screen - Light Mode", showBackground = true)
@Composable
fun LessonEditorialLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        LessonScreen(
            onNavigateBack = {},
            onAskTutor = {},
            onContinueToPractice = {},
            onCompleteLesson = {}
        )
    }
}

@Preview(name = "Lesson Step 3 - Interactive Lab", showBackground = true)
@Composable
fun LessonInteractiveLabDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.padding(16.dp)) {
            Int8QuantizationLaboratory()
        }
    }
}
