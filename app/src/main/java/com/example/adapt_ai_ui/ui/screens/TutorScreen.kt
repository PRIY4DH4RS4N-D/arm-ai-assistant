package com.example.adapt_ai_ui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.adapt_ai_ui.ui.components.AuroraScene
import com.example.adapt_ai_ui.ui.components.AuroraPanel
import com.example.adapt_ai_ui.ui.components.AuroraPill
import com.example.adapt_ai_ui.ui.components.auroraPanel
import com.example.adapt_ai_ui.ui.components.auroraLine
import com.example.adapt_ai_ui.ui.components.auroraAccent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorConversationMessage
import com.example.adapt_ai_ui.data.TutorMode
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.TutorViewModel
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.TutorPersonalization
import com.example.adapt_ai_ui.data.platform.LocalLearningPlatform
import com.example.adapt_ai_ui.data.platform.mastery
import com.example.adapt_ai_ui.inference.ModelState
import com.example.adapt_ai_ui.ui.components.rememberOfflineNarrator
import com.example.adapt_ai_ui.ui.components.AuroraOrb
import com.example.adapt_ai_ui.ui.components.OrbState
import com.example.adapt_ai_ui.data.TutorResponse
import com.example.adapt_ai_ui.ui.components.AdaptGlassSurface
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSurface
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun TutorScreen(
    initialPrompt: TutorQuickPrompt? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onInitialPromptConsumed: () -> Unit = {},
    onOpenModelManager: () -> Unit = {},
    viewModel: TutorViewModel? = null
) {
    val session = LocalLearningPlatform.current?.session
    val topic = session?.topic
    val preferences = session?.preferences
    val base = SampleData.learnerProfile
    val learner = base.copy(
        name = session?.profile?.name ?: base.name,
        activeDomainId = topic?.id ?: base.activeDomainId,
        activeDomainTitle = topic?.subject ?: base.activeDomainTitle,
        activeGoal = preferences?.goal?.takeIf(String::isNotBlank) ?: topic?.objective?.ifBlank { "Learn ${topic.name}" } ?: base.activeGoal,
        skillLevel = preferences?.level ?: if (topic != null) com.example.adapt_ai_ui.data.DifficultyLevel.BEGINNER else base.skillLevel,
        preferredLearningStyle = if (topic != null) preferences?.style.orEmpty() else base.preferredLearningStyle
    )
    val topicTitle = topic?.name ?: SampleData.currentModule.title
    val topicState = session?.topicState
    val weakest = topicState?.attempts?.flatMap { it.quiz.questions }?.map { it.competency }?.distinct()
        ?.mapNotNull { skill -> topicState.mastery(skill)?.let { skill to it.percentage } }
        ?.filter { it.second < 70 }?.minByOrNull { it.second }?.first
    val personalization = TutorPersonalization(
        assistant = preferences?.assistant ?: "Aurora",
        language = preferences?.language ?: "English",
        note = topicState?.note?.takeIf(String::isNotBlank),
        assessedMastery = topicState?.mastery()?.percentage,
        weakness = weakest
    )
    val activeMode = viewModel?.mode ?: TutorRepository.activeMode
    val isGenerating = viewModel?.isGenerating ?: false
    val messages = viewModel?.messages ?: TutorRepository.conversationMessages
    val modelState = viewModel?.runtime?.state?.collectAsState()?.value ?: ModelState.MISSING
    val draft = viewModel?.draft.orEmpty()
    val narrator = rememberOfflineNarrator()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var textInput by remember { mutableStateOf("") }
    val voice = rememberAuroraVoice { textInput = it }
    val keyboard = LocalSoftwareKeyboardController.current
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialPrompt) {
        TutorRepository.initializeSession(learner.name, learner.activeDomainTitle, topicTitle)
        if (initialPrompt != null) {
            if (isGenerating) textInput = initialPrompt.prompt
            else viewModel?.send(initialPrompt.prompt, learner, topicTitle, personalization)
            onInitialPromptConsumed()
        }
    }

    // Scroll to latest message on update
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - if (isGenerating) 0 else 1)
        }
    }

    fun submitQuery(queryText: String) {
        if (queryText.isBlank() || isGenerating) return
        voice.stop()
        keyboard?.hide()
        textInput = ""
        viewModel?.send(queryText.trim(), learner, topicTitle, personalization)
    }

    AuroraScene(modifier) {
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (onNavigateBack != null) IconButton(onNavigateBack) {
                Icon(AdaptIcons.ArrowBack, "Back", tint = AdaptTheme.colors.textPrimary)
            }
            AuroraOrb(when {
                narrator.speaking -> OrbState.SPEAKING
                voice.listening -> OrbState.LISTENING
                modelState == ModelState.LOADING -> OrbState.LOADING
                modelState == ModelState.ERROR -> OrbState.ERROR
                isGenerating && draft.isNotBlank() -> OrbState.GENERATING
                isGenerating -> OrbState.THINKING
                else -> OrbState.IDLE
            }, { voice.amplitude }, Modifier.size(46.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("AI Tutor", style = AdaptTheme.typography.titleLarge, color = AdaptTheme.colors.textPrimary)
                Text("${modelState.label} | $topicTitle", color = auroraAccent,
                    style = AdaptTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton({ showClearDialog = true }) {
                Icon(AdaptIcons.Trash, "Clear conversation history", tint = AdaptTheme.colors.textSecondary)
            }
        }
        if (modelState == ModelState.MISSING || modelState == ModelState.ERROR) {
            TextButton(onClick = onOpenModelManager, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("Manage offline model")
            }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            TutorMode.entries.forEach { mode ->
                AuroraPill(mode.title, {
                    if (mode != activeMode) TutorRepository.setMode(mode, learner.name, learner.activeDomainTitle, topicTitle)
                }, Modifier.semantics { contentDescription = "${mode.title} mode: ${mode.description}" },
                    active = mode == activeMode, enabled = !isGenerating)
            }
        }
        // ====================================================================
        // 3. Conversation Message Stream
        // ====================================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isTutor) {
                        TutorAssistantCard(
                            message = msg,
                            onCopyText = { textToCopy ->
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Copied to clipboard")
                                }
                            },
                            onFollowUpClick = { prompt -> submitQuery(prompt) },
                            onRetryClick = {
                                if (msg == messages.lastOrNull()) viewModel?.retry(learner, topicTitle, personalization)
                            },
                            onSpeak = if (msg.structuredPayload?.executionModelLabel?.startsWith("Llama 3.2") == true) {
                                { narrator.speak(msg.content) }
                            } else null,
                            canRegenerate = !isGenerating && msg == messages.lastOrNull()
                        )
                    } else {
                        UserMessageBubble(message = msg)
                    }
                }

                if (isGenerating) {
                    item(key = "tutor_stream") {
                        Column {
                            if (draft.isBlank()) GeneratingIndicator()
                            else Text(
                                draft, color = AdaptTheme.colors.textPrimary,
                                style = AdaptTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            TextButton(onClick = { viewModel?.stop() }) { Text("Stop generation") }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        narrator.status?.let {
            Text(it, modifier = Modifier.padding(horizontal = 20.dp), color = AdaptTheme.colors.textSecondary)
        }
        if (narrator.speaking) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuroraOrb(OrbState.SPEAKING, { 0f }, Modifier.size(40.dp))
                TextButton(onClick = narrator.stop) { Text("Stop reading") }
            }
        }

        // ====================================================================
        // 4. Contextual Suggested Prompts Row
        // ====================================================================
        val suggestedPrompts = remember(activeMode, learner.activeDomainId, topicTitle) {
            TutorRepository.getSuggestedPrompts(learner.activeDomainId, topicTitle)
        }

        if (suggestedPrompts.isNotEmpty() && !isGenerating) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedPrompts.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.full))
                            .background(AdaptTheme.colors.raisedSurface)
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = AdaptTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(AdaptTheme.radius.full)
                            )
                            .clickable(role = Role.Button, onClick = { submitQuery(prompt) })
                            .semantics { contentDescription = "Ask prompt: $prompt" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AdaptIcons.Sparkle,
                                contentDescription = null,
                                tint = AdaptTheme.colors.primaryAccent,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AdaptTheme.colors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        AuroraComposer(textInput, { textInput = it }, "Type a message...", voice,
            { submitQuery(textInput) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            maxLines = 4, enabled = !isGenerating)
    }
    }

    // ========================================================================
    // Clear Confirmation Dialog (Editorial Obsidian)
    // ========================================================================
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = AdaptTheme.colors.surface,
            titleContentColor = AdaptTheme.colors.textPrimary,
            textContentColor = AdaptTheme.colors.textSecondary,
            title = {
                Text(
                    text = "Clear Conversation?",
                    style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will reset your current tutor session to the initial welcome state. Your learning journey, mastery scores, and mistake notebook will remain completely intact.",
                    style = AdaptTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        TutorRepository.clearSession(learner.name, learner.activeDomainTitle, topicTitle)
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = AdaptTheme.colors.error,
                        fontWeight = FontWeight.Bold,
                        style = AdaptTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = AdaptTheme.colors.textSecondary,
                        style = AdaptTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

// -------------------------------------------------------------
// USER MESSAGE BUBBLE (High-Contrast Subtle Violet Obsidian)
// -------------------------------------------------------------
@Composable
private fun UserMessageBubble(message: TutorConversationMessage) {
    val corners = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(message.content, Modifier.fillMaxWidth(.82f).clip(corners)
            .background(Brush.horizontalGradient(listOf(Color(0xFF103762), Color(0xFF222762))))
            .padding(horizontal = 15.dp, vertical = 11.dp),
            color = Color.White, style = AdaptTheme.typography.bodyMedium.copy(lineHeight = 20.sp))
    }
}

// -------------------------------------------------------------
// EDUCATIONAL ASSISTANT CARD (Rich components: Steps, Code, Math, Takeaways)
// -------------------------------------------------------------
@Composable
private fun TutorAssistantCard(
    message: TutorConversationMessage,
    onCopyText: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onSpeak: (() -> Unit)? = null,
    canRegenerate: Boolean = false
) {
    val payload = message.structuredPayload

    AuroraPanel(Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Avatar, Mode Badge, Copy action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AuroraOrb(OrbState.IDLE, { 0f }, Modifier.size(29.dp))
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "EMBER",
                        style = AdaptTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.full))
                            .background(
                                AdaptTheme.colors.primaryAccentContainer.copy(
                                    alpha = if (AdaptTheme.isDark) 0.5f else 0.35f
                                )
                            )
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = AdaptTheme.colors.primaryAccent.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(AdaptTheme.radius.full)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${message.mode.iconEmoji} ${message.mode.title}",
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }
                }

                // Copy Action Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) {
                            val fullCopyText = buildString {
                                append(message.content)
                                payload?.conceptExplanation?.let { append("\n\nConcept:\n$it") }
                                if (!payload?.steps.isNullOrEmpty()) {
                                    append("\n\nSteps:\n")
                                    payload.steps.forEachIndexed { i, s -> append("${i + 1}. $s\n") }
                                }
                                payload?.exampleOrAnalogy?.let { append("\n\nAnalogy:\n$it") }
                                payload?.codeSnippet?.let { append("\n\nCode:\n$it") }
                                payload?.formula?.let { append("\n\nFormula:\n$it") }
                                if (!payload?.keyTakeaways.isNullOrEmpty()) {
                                    append("\n\nKey Takeaways:\n")
                                    payload.keyTakeaways.forEach { append("• $it\n") }
                                }
                            }
                            onCopyText(fullCopyText)
                        }
                        .semantics { contentDescription = "Copy assistant response" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AdaptIcons.Copy,
                        contentDescription = null,
                        tint = AdaptTheme.colors.textMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Message Content
            Text(
                text = message.content,
                style = AdaptTheme.typography.bodyMedium,
                color = AdaptTheme.colors.textPrimary,
                lineHeight = 21.sp
            )

            // Optional Concept Explanation Card
            if (!payload?.conceptExplanation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = payload.conceptExplanation,
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textSecondary
                    )
                }
            }

            // Optional Ordered Steps
            if (!payload?.steps.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    payload.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(AdaptTheme.colors.primaryAccentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = AdaptTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AdaptTheme.colors.primaryAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Optional Real-World Analogy Callout
            if (!payload?.exampleOrAnalogy.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.15f))
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = AdaptIcons.Lightbulb,
                            contentDescription = null,
                            tint = AdaptTheme.colors.secondaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = payload.exampleOrAnalogy,
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textPrimary
                        )
                    }
                }
            }

            // Optional Code Block
            if (!payload?.codeSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                CodeBlockCard(
                    code = payload.codeSnippet,
                    language = payload.codeLanguage ?: "Code",
                    onCopyCode = onCopyText
                )
            }

            // Optional Mathematical Formula Box
            if (!payload?.formula.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📐 FORMULA SPECIFICATION",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.primaryAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = payload.formula,
                            style = AdaptTheme.typographyExt.mathFormula.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = AdaptTheme.colors.textPrimary
                        )
                    }
                }
            }

            // Optional Practice Drill Question
            if (!payload?.practiceQuestion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.secondaryAccent.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = AdaptIcons.Target,
                            contentDescription = null,
                            tint = AdaptTheme.colors.secondaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PRACTICE SCENARIO",
                                style = AdaptTheme.typographyExt.overline,
                                color = AdaptTheme.colors.secondaryAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = payload.practiceQuestion,
                                style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Optional Key Takeaways Checklist
            if (!payload?.keyTakeaways.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "KEY TAKEAWAYS",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        payload.keyTakeaways.forEach { takeaway ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = AdaptIcons.Check,
                                    contentDescription = null,
                                    tint = AdaptTheme.colors.secondaryAccent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = takeaway,
                                    style = AdaptTheme.typography.bodySmall,
                                    color = AdaptTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (payload != null && payload.executionModelLabel.startsWith("Llama 3.2")) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${payload.executionModelLabel} | ${payload.inferenceTimeMs} ms",
                    style = AdaptTheme.typography.labelSmall,
                    color = AdaptTheme.colors.textMuted
                )
                payload.sourceLabel?.let {
                    Text("Local source ? $it", style = AdaptTheme.typography.labelSmall, color = AdaptTheme.colors.textMuted)
                }
                Row {
                    onSpeak?.let { TextButton(onClick = it) { Text("Read aloud") } }
                    if (canRegenerate) TextButton(onClick = onRetryClick) { Text("Regenerate") }
                }
            } else if (payload?.executionModelLabel?.contains("Prototype") == true &&
                message.id.startsWith("tutor_")) {
                Text("Saved prototype response", style = AdaptTheme.typography.labelSmall, color = AdaptTheme.colors.textMuted)
            }

            // Error Retry Action
            if (message.isError && canRegenerate) {
                Spacer(modifier = Modifier.height(10.dp))
                AdaptSecondaryButton(
                    text = "Retry Generation",
                    onClick = onRetryClick,
                    leadingIcon = AdaptIcons.Retry
                )
            }

            // Clickable Follow-up Discovery Chips
            if (!payload?.followUpPrompts.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    payload.followUpPrompts.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AdaptTheme.radius.full))
                                .background(AdaptTheme.colors.raisedSurface)
                                .border(
                                    width = AdaptTheme.stroke.hairline,
                                    color = AdaptTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(AdaptTheme.radius.full)
                                )
                                .clickable(role = Role.Button, onClick = { onFollowUpClick(prompt) })
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "💬 $prompt",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AdaptTheme.colors.primaryAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CODE BLOCK COMPONENT (Monospace, Syntax Background, Copy Action)
// -------------------------------------------------------------
@Composable
private fun CodeBlockCard(
    code: String,
    language: String,
    onCopyCode: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
            .background(AdaptTheme.colors.codeBackground)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = AdaptTheme.colors.borderSubtle,
                shape = RoundedCornerShape(AdaptTheme.radius.medium)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AdaptIcons.Code,
                        contentDescription = null,
                        tint = AdaptTheme.colors.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.uppercase(),
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.small))
                        .clickable(role = Role.Button) { onCopyCode(code) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Copy code snippet" }
                ) {
                    Text(
                        text = "Copy Code",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = code,
                style = AdaptTheme.typographyExt.codeSnippet.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.5.sp
                ),
                color = AdaptTheme.colors.textPrimary
            )
        }
    }
}

// -------------------------------------------------------------
// THINKING / INFERENCE LOADING INDICATOR
// -------------------------------------------------------------
@Composable
private fun GeneratingIndicator() {
    AdaptSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AdaptTheme.radius.large),
        backgroundColor = AdaptTheme.colors.surface,
        borderColor = AdaptTheme.colors.borderSubtle,
        borderWidth = AdaptTheme.stroke.hairline,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = AdaptTheme.colors.primaryAccent
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Formulating response...",
                    style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AdaptTheme.colors.textPrimary
                )
                Text(
                    text = "Simulating on-device heuristic reasoning loop...",
                    style = AdaptTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = AdaptTheme.colors.textMuted
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------
@Preview(name = "Tutor - Dark Mode", showBackground = true)
@Composable
private fun TutorScreenDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        TutorScreen()
    }
}

@Preview(name = "Tutor - Light Mode", showBackground = true)
@Composable
private fun TutorScreenLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        TutorScreen()
    }
}

@Preview(name = "Tutor - Conversation Cards Dark", showBackground = true)
@Composable
private fun TutorConversationDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdaptTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserMessageBubble(
                message = TutorConversationMessage(
                    id = "u1",
                    sender = "You",
                    isTutor = false,
                    content = "How does INT8 quantization reduce DRAM bandwidth?"
                )
            )

            TutorAssistantCard(
                message = TutorConversationMessage(
                    id = "t1",
                    sender = "ADAPT-AI",
                    isTutor = true,
                    content = "Quantizing FP32 activations and weights to 8-bit integers reduces data movement over the DRAM bus by 75%.",
                    mode = TutorMode.DEBUG,
                    structuredPayload = TutorResponse(
                        text = "Quantizing FP32 activations and weights to 8-bit integers reduces data movement over the DRAM bus by 75%.",
                        conceptExplanation = "On-chip SRAM memory reads cost up to 100x less energy than fetching 32-bit floats across off-chip DRAM buses.",
                        steps = listOf(
                            "Calibrate dynamic activation range [r_min, r_max].",
                            "Calculate scale factor S = (r_max - r_min) / 255.",
                            "Map float activations to int8 via affine rounding."
                        ),
                        exampleOrAnalogy = "Analogy: Storing ingredients inside your kitchen counter rather than driving to the supermarket for every meal.",
                        codeSnippet = "// ARM NEON INT8 Vector Dot-Product\nacc = vdotq_s32(acc, va, vb);",
                        codeLanguage = "C++ (ARM NEON)",
                        formula = "r ≈ S × (q - Z)",
                        keyTakeaways = listOf(
                            "75% memory footprint reduction enables on-chip SRAM residency.",
                            "Eliminates thermal throttling on battery-constrained edge devices."
                        ),
                        followUpPrompts = listOf(
                            "How does zero-point Z eliminate precision skew?",
                            "What is per-channel vs per-tensor quantization?"
                        )
                    )
                ),
                onCopyText = {},
                onFollowUpClick = {},
                onRetryClick = {}
            )
        }
    }
}
