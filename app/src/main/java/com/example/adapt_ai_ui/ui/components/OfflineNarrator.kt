package com.example.adapt_ai_ui.ui.components

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import java.util.UUID

internal data class OfflineNarrator(
    val status: String?,
    val speaking: Boolean,
    val speak: (String) -> Unit,
    val stop: () -> Unit
)

@Composable
internal fun rememberOfflineNarrator(): OfflineNarrator {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var engine by remember(context) { mutableStateOf<TextToSpeech?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var utterance by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<String?>(null) }
    var unavailable by remember { mutableStateOf(false) }
    val main = remember { Handler(Looper.getMainLooper()) }
    fun stop() {
        pending = null
        utterance = null
        engine?.stop()
    }
    fun read(text: String) {
        val tts = engine ?: return
        val voices = tts.voices.orEmpty().filter { !it.isNetworkConnectionRequired }
        val voice = voices.firstOrNull { it.locale == Locale.getDefault() }
            ?: voices.firstOrNull { it.locale.language == Locale.getDefault().language }
            ?: voices.firstOrNull { it.locale.language == "en" }
        if (voice == null || tts.setVoice(voice) != TextToSpeech.SUCCESS) {
            status = "An offline text-to-speech voice is unavailable."
            return
        }
        val id = UUID.randomUUID().toString()
        status = null
        utterance = id
        if (tts.speak(text.take(TextToSpeech.getMaxSpeechInputLength()), TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) {
            utterance = null
            status = "Offline speech could not start."
        }
    }
    DisposableEffect(context, lifecycle) {
        var disposed = false
        var tts: TextToSpeech? = null
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) stop() }
        lifecycle.addObserver(observer)
        run {
                tts = TextToSpeech(context.applicationContext) { result ->
                    main.post {
                        if (!disposed) {
                            val created = tts
                            if (result == TextToSpeech.SUCCESS && created != null) {
                                engine = created
                                created.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                    override fun onStart(utteranceId: String?) = Unit
                                    override fun onDone(utteranceId: String?) {
                                        main.post { if (utterance == utteranceId) utterance = null }
                                    }
                                    @Deprecated("Required by UtteranceProgressListener")
                                    override fun onError(utteranceId: String?) {
                                        main.post {
                                            if (utterance == utteranceId) {
                                                utterance = null
                                                status = "Offline speech stopped unexpectedly."
                                            }
                                        }
                                    }
                                })
                                pending?.let(::read)
                            } else {
                                created?.shutdown()
                                tts = null
                                unavailable = true
                                if (pending != null) status = "No offline text-to-speech engine is available."
                            }
                            pending = null
                        }
                    }
                }
        }
        onDispose {
            disposed = true
            lifecycle.removeObserver(observer)
            stop()
            tts?.shutdown()
            engine = null
        }
    }
    return OfflineNarrator(status, utterance != null, speak = { text ->
        if (unavailable) status = "No offline text-to-speech engine is available."
        else if (engine == null) {
            status = null
            pending = text
        } else read(text)
    }, stop = ::stop)
}
