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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.inference.ModelState
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AuroraOrb
import com.example.adapt_ai_ui.ui.components.OrbState
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import java.util.UUID

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
    var overview by rememberSaveable { mutableStateOf(false) }
    BackHandler(overview) { overview = false }
    if (overview) {
        Column {
            TextButton(
                onClick = { overview = false },
                modifier = Modifier.statusBarsPadding().padding(horizontal = 12.dp)
            ) { Text("Back to Aurora") }
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
    val keyboard = LocalSoftwareKeyboardController.current
    val dark = AdaptTheme.isDark
    val backdrop = if (dark) Color(0xFF02040A) else Color(0xFFF8FAFF)
    val ink = if (dark) Color(0xFFF5F7FB) else Color(0xFF15223C)
    val muted = if (dark) Color(0xFFA5AFBE) else Color(0xFF465572)
    val blue = if (dark) Color(0xFF75DFFF) else Color(0xFF254CC0)
    var text by rememberSaveable { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    val voice = rememberAuroraVoice { text = it }
    val activityState = when {
        voice.listening -> OrbState.LISTENING
        state == ModelState.GENERATING -> OrbState.THINKING
        state in setOf(ModelState.LOADING, ModelState.IMPORTING, ModelState.VERIFYING) -> OrbState.LOADING
        else -> OrbState.IDLE
    }
    val inputPadding by animateDpAsState(if (focused) 10.dp else 5.dp, label = "Input focus")
    val capsule = RoundedCornerShape(32.dp)
    fun send() {
        val query = text.trim()
        if (query.isEmpty()) return
        voice.stop()
        keyboard?.hide()
        text = ""
        onNavigateToTutor(TutorQuickPrompt(UUID.randomUUID().toString(), query, "Home"))
    }

    BoxWithConstraints(
        modifier.fillMaxSize().background(backdrop).statusBarsPadding()
    ) {
        val artworkSize = (maxHeight * 0.48f).coerceIn(170.dp, 340.dp)
        val compact = maxHeight < 425.dp
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(240.dp).background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF1353D6).copy(alpha = if (dark) 0.24f else 0.11f),
                    Color(0xFF36AFFF).copy(alpha = if (dark) 0.34f else 0.18f)))
            )
        )
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("A U R O R A", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    Modifier.clip(CircleShape).clickable(onClick = onNavigateToProfile).padding(9.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(6.dp).background(blue, CircleShape))
                    Text(state.label, color = muted, fontSize = 11.sp)
                }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!compact) AuroraOrb(activityState, { voice.amplitude }, Modifier.size(artworkSize))
                Text("Hello, ${learner.name}", color = blue, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(
                    "How can I help you learn today?",
                    modifier = Modifier.padding(top = 9.dp).widthIn(max = 390.dp),
                    color = ink, textAlign = TextAlign.Center, fontSize = if (compact) 24.sp else 32.sp,
                    lineHeight = if (compact) 29.sp else 38.sp, fontWeight = FontWeight.SemiBold
                )
            }
            voice.status?.let {
                Text(it, color = muted, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp)
            }
            Row(
                Modifier.fillMaxWidth().clip(capsule)
                    .background(if (dark) Color(0xC7152342) else Color(0xF5FFFFFF))
                    .border(1.dp, blue.copy(alpha = 0.36f), capsule)
                    .padding(start = 18.dp, end = 5.dp, top = inputPadding, bottom = inputPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused }
                        .semantics { contentDescription = "Ask anything" },
                    textStyle = AdaptTheme.typography.bodyLarge.copy(color = ink),
                    cursorBrush = SolidColor(blue), singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) Text("Ask anything...", color = muted, style = AdaptTheme.typography.bodyLarge)
                            inner()
                        }
                    }
                )
                IconButton(onClick = voice.toggle) {
                    Canvas(Modifier.size(23.dp).semantics {
                        contentDescription = if (voice.listening) "Stop listening" else "Start offline voice input"
                    }) {
                        listOf(0.23f, 0.55f, 0.86f, 0.45f, 0.28f).forEachIndexed { i, height ->
                            val x = size.width * (0.14f + i * 0.18f)
                            drawLine(blue, Offset(x, size.height * (1 - height) / 2),
                                Offset(x, size.height * (1 + height) / 2), 2.dp.toPx(), StrokeCap.Round)
                        }
                    }
                }
                IconButton(onClick = ::send, enabled = text.isNotBlank()) {
                    Icon(AdaptIcons.ArrowForward, "Send to Aurora", tint = if (text.isBlank()) muted else blue)
                }
            }
            TextButton(
                onClick = { overview = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Learning overview", color = muted, fontSize = 12.sp) }
        }
    }
}

private data class AuroraVoice(
    val listening: Boolean,
    val amplitude: Float,
    val status: String?,
    val toggle: () -> Unit,
    val stop: () -> Unit
)

@Composable
private fun rememberAuroraVoice(onTranscript: (String) -> Unit): AuroraVoice {
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
