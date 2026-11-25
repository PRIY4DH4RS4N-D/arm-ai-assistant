package com.example.adapt_ai_ui.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.platform.LocalLearningPlatform
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.data.TutorMode
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.inference.ModelState
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import java.util.UUID
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToPathway: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToPractice: (PracticeMode) -> Unit,
    onNavigateToTutor: (TutorQuickPrompt?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    modifier: Modifier = Modifier,
    onNavigateToProgress: () -> Unit = onNavigateToProfile
) {
    var overview by rememberSaveable { mutableStateOf(false) }
    BackHandler(overview) { overview = false }
    if (overview) {
        Column {
            TextButton(
                onClick = { overview = false },
                modifier = Modifier.statusBarsPadding().padding(horizontal = 12.dp)
            ) { Text("Back to Ember") }
            LearningOverviewScreen(
                onNavigateToPathway, onNavigateToLesson, onNavigateToPractice,
                onNavigateToTutor, onNavigateToProfile, onNavigateToExplore,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }
    val state = TutorRepository.runtime?.state?.collectAsState()?.value ?: ModelState.MISSING
    val learner = SampleData.learnerProfile
    val session = LocalLearningPlatform.current?.session
    val keyboard = LocalSoftwareKeyboardController.current
    var text by rememberSaveable { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    val voice = rememberAuroraVoice { text = it }
    val recentFinish = session?.topicState?.let { maxOf(it.completedAt ?: 0L, it.attempts.lastOrNull()?.finishedAt ?: 0L) }
    var celebrating by remember { mutableStateOf(false) }
    LaunchedEffect(recentFinish) {
        celebrating = recentFinish != null && System.currentTimeMillis() - recentFinish in 0..10_000
        if (celebrating) { delay(4_000); celebrating = false }
    }
    val activityState = when {
        voice.listening -> OrbState.LISTENING
        state == ModelState.GENERATING -> OrbState.GENERATING
        state in setOf(ModelState.LOADING, ModelState.IMPORTING, ModelState.VERIFYING) -> OrbState.LOADING
        state == ModelState.ERROR -> OrbState.ERROR
        celebrating -> OrbState.SUCCESS
        else -> OrbState.IDLE
    }
    fun send() {
        val query = text.trim()
        if (query.isEmpty()) return
        voice.stop()
        keyboard?.hide()
        text = ""
        onNavigateToTutor(TutorQuickPrompt(UUID.randomUUID().toString(), query, "Home"))
    }

    BoxWithConstraints(modifier.fillMaxSize().statusBarsPadding()) {
        val orbSize = (maxHeight * .32f).coerceIn(144.dp, 254.dp)
        val topic = session?.topic
        val target = session?.preferences?.minutes ?: learner.dailyMinutesGoal
        val studied = if (topic == null) learner.dailyMinutesSpent else 0
        AuroraScene(Modifier.fillMaxSize()) {
            Column(
                Modifier.align(Alignment.TopCenter).widthIn(max = 540.dp).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.clip(CircleShape).background(auroraPanel)
                            .clickable(onClick = onNavigateToProfile).padding(horizontal = 12.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ready = state == ModelState.INSTALLED || state == ModelState.READY
                        Box(Modifier.size(7.dp).background(if (ready) AuroraGreen else auroraAccent, CircleShape))
                        Text(if (ready) "Offline mode" else state.label, color = AdaptTheme.colors.textPrimary,
                            style = AdaptTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Box(contentAlignment = Alignment.Center) {
                            AuroraMedallion(AdaptIcons.Profile, AuroraViolet, Modifier.size(39.dp))
                            Spacer(Modifier.size(44.dp).semantics { contentDescription = "Open learner profile" })
                        }
                    }
                }
                AuroraOrb(activityState, { voice.amplitude }, Modifier.size(orbSize)
                    .clickable(onClick = { onNavigateToTutor(null) }).semantics { contentDescription = "Open Tutor" })
                Text("Hello, ${session?.profile?.name ?: learner.name}", color = AdaptTheme.colors.textPrimary,
                    fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text("How can I help you learn today?", Modifier.padding(top = 5.dp, bottom = 15.dp),
                    color = AdaptTheme.colors.textPrimary, textAlign = TextAlign.Center,
                    fontSize = 25.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold)
                AuroraComposer(text, { text = it }, "Ask anything...", voice, ::send,
                    Modifier.fillMaxWidth(), sendDescription = "Send to Aurora")
                Row(Modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    AuroraPill("Teach Me", {
                        if (!TutorRepository.isGenerating && TutorRepository.activeMode != TutorMode.TEACH_ME) {
                            TutorRepository.setMode(TutorMode.TEACH_ME, session?.profile?.name ?: learner.name,
                                topic?.subject ?: learner.activeDomainTitle, topic?.name ?: SampleData.currentModule.title)
                        }
                        onNavigateToTutor(null)
                    }, horizontalPadding = 9.dp)
                    AuroraPill("Practice", { onNavigateToPractice(PracticeMode.SESSION) }, horizontalPadding = 9.dp)
                    AuroraPill("My progress", onNavigateToProgress, horizontalPadding = 9.dp)
                    AuroraPill("Explore", onNavigateToExplore, horizontalPadding = 9.dp)
                }
                Spacer(Modifier.height(12.dp))
                AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue, onNavigateToPathway) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuroraMedallion(AdaptIcons.Target, AuroraCyan, Modifier.size(39.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Today's study target", style = AdaptTheme.typography.labelMedium, color = AdaptTheme.colors.textSecondary)
                            Text(if (studied > 0) "$studied of $target min" else "$target min planned",
                                style = AdaptTheme.typography.titleMedium, color = AdaptTheme.colors.textPrimary)
                        }
                        Icon(AdaptIcons.ArrowForward, null, tint = auroraAccent, modifier = Modifier.size(17.dp))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Continue learning", style = AdaptTheme.typography.titleMedium, color = AdaptTheme.colors.textPrimary)
                    TextButton(onClick = { overview = true }) { Text("Overview") }
                }
                AuroraPanel(Modifier.fillMaxWidth(), AuroraViolet, if (topic == null) onNavigateToExplore else onNavigateToLesson) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuroraMedallion(AdaptIcons.Notebook, AuroraViolet, Modifier.size(39.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(topic?.name ?: "Find a subject", color = AdaptTheme.colors.textPrimary,
                                style = AdaptTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(topic?.subject ?: "Choose what you want to learn", color = AdaptTheme.colors.textSecondary,
                                style = AdaptTheme.typography.bodySmall)
                        }
                        Icon(AdaptIcons.Play, null, tint = auroraAccent, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

}

@Composable
internal fun AuroraComposer(
    text: String, onText: (String) -> Unit, placeholder: String, voice: AuroraVoice, send: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, maxLines: Int = 1,
    sendDescription: String = "Send message to AI Tutor"
) {
    var focused by remember { mutableStateOf(false) }
    val padding by animateDpAsState(if (focused) 8.dp else 4.dp, label = "Composer focus")
    val accent = auroraAccent
    val shape = RoundedCornerShape(30.dp)
    Column(modifier) {
        voice.status?.let {
            Text(it, color = AdaptTheme.colors.textSecondary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = AdaptTheme.typography.bodySmall)
        }
        Row(
            Modifier.fillMaxWidth().clip(shape).background(Brush.horizontalGradient(listOf(auroraPanel, auroraRaised)))
                .border(1.3.dp, accent.copy(alpha = if (focused) .95f else .68f), shape)
                .padding(start = 17.dp, end = 3.dp, top = padding, bottom = padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(text, onText, Modifier.weight(1f).onFocusChanged { focused = it.isFocused }
                .semantics { contentDescription = placeholder },
                textStyle = AdaptTheme.typography.bodyMedium.copy(color = AdaptTheme.colors.textPrimary),
                cursorBrush = SolidColor(accent), singleLine = maxLines == 1, maxLines = maxLines,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (enabled) send() }),
                decorationBox = { content ->
                    Box {
                        if (text.isEmpty()) Text(placeholder, color = AdaptTheme.colors.textSecondary,
                            style = AdaptTheme.typography.bodyMedium)
                        content()
                    }
                }
            )
            IconButton(onClick = voice.toggle) {
                Canvas(Modifier.size(21.dp).semantics {
                    contentDescription = if (voice.listening) "Stop listening" else "Start offline voice input"
                }) {
                    listOf(.23f, .55f, .86f, .45f, .28f).forEachIndexed { i, height ->
                        val x = size.width * (.14f + i * .18f)
                        drawLine(accent, Offset(x, size.height * (1-height)/2),
                            Offset(x, size.height * (1+height)/2), 2.dp.toPx(), StrokeCap.Round)
                    }
                }
            }
            if (text.isNotBlank()) {
                IconButton(send, enabled = enabled) {
                    Box(Modifier.size(35.dp).background(Brush.linearGradient(listOf(AuroraBlue, Color(0xFF2349CF))), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(AdaptIcons.ArrowForward, sendDescription, tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
    }
}

internal data class AuroraVoice(
    val listening: Boolean,
    val amplitude: Float,
    val status: String?,
    val toggle: () -> Unit,
    val stop: () -> Unit
)

@Composable
internal fun rememberAuroraVoice(onTranscript: (String) -> Unit): AuroraVoice {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val transcript by rememberUpdatedState(onTranscript)
    var listening by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf<String?>(null) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    fun cancel() {
        recognizer?.cancel()
        listening = false
        amplitude = 0f
        if (status == "Listening on this device") status = null
    }
    fun start() {
        if (Build.VERSION.SDK_INT < 31 || !SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            status = "Offline speech recognition is unavailable on this device."
            return
        }
        try {
            val service = recognizer ?: SpeechRecognizer.createOnDeviceSpeechRecognizer(context).also {
                recognizer = it
                it.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { status = "Listening on this device" }
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) { amplitude = ((rmsdB + 2) / 12).coerceIn(0f, 1f) }
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() { amplitude = 0f }
                    override fun onError(error: Int) {
                        if (listening) status = "Offline transcription unavailable. Try again or type your question."
                        listening = false
                    }
                    override fun onResults(results: Bundle?) {
                        listening = false
                        status = null
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(transcript)
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        if (listening) partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.let(transcript)
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })
            }
            listening = true
            status = "Listening on this device"
            service.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            })
        } catch (_: RuntimeException) {
            listening = false
            status = "Offline speech recognition could not start."
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) start() else status = "Microphone permission denied. You can still type a question."
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) cancel() }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            recognizer?.destroy()
        }
    }
    return AuroraVoice(listening, amplitude, status, toggle = {
        when {
            listening -> recognizer?.stopListening()
            Build.VERSION.SDK_INT < 31 || !SpeechRecognizer.isOnDeviceRecognitionAvailable(context) ->
                status = "Offline speech recognition is unavailable on this device."
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ->
                permission.launch(Manifest.permission.RECORD_AUDIO)
            else -> start()
        }
    }, stop = ::cancel)
}
