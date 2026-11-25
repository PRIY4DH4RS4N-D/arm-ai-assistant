package com.example.adapt_ai_ui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.adapt_ai_ui.data.platform.CodingProgress
import com.example.adapt_ai_ui.data.platform.LearningPlatform
import com.example.adapt_ai_ui.data.platform.codingExercises
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val codeTokens = Regex("(?m)#[^\n]*|//[^\n]*|\"[^\"\n]*\"|\\b(?:int|return|for|if|print|std|include|while|def|import|class|float|long)\\b|\\b[0-9]+\\b")

@Composable
internal fun CodingScreen(platform: LearningPlatform, onBack: () -> Unit, onTutor: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf<CodingProgress?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var error by remember { mutableStateOf("") }
    val exercise = codingExercises[selected]
    val stored = progress
    var source by rememberSaveable(exercise.id, stored?.drafts?.get(exercise.id)) {
        mutableStateOf(stored?.drafts?.get(exercise.id) ?: exercise.example)
    }
    var prediction by rememberSaveable(exercise.id) { mutableStateOf("") }
    var feedback by remember(exercise.id) { mutableStateOf("") }
    val syntax = if (AdaptTheme.isDark) Color(0xFFC3A6FF) else Color(0xFF6234A4)
    val highlight = remember(syntax) {
        VisualTransformation { input ->
            val annotated: AnnotatedString = buildAnnotatedString {
                append(input)
                codeTokens.findAll(input.text).forEach { match ->
                    addStyle(SpanStyle(if (match.value.first().isDigit()) syntax else syntax.copy(alpha = .92f)),
                        match.range.first, match.range.last + 1)
                }
            }
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }
    LaunchedEffect(platform) {
        try {
            progress = platform.codingProgress()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            error = "Could not load coding progress."
        } finally {
            loading = false
        }
    }
    fun save(next: CodingProgress, confirmation: String) {
        loading = true
        scope.launch {
            error = ""
            try {
                platform.saveCodingProgress(next)
                progress = next
                feedback = confirmation
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                error = "Could not save coding progress."
            } finally {
                loading = false
            }
        }
    }
    AuroraScene {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            AuroraHeader("Coding Practice", onBack)
            Text("Predict the output of the original snippet. Use the editor to sketch a correction or experiment on paper.",
                color = AdaptTheme.colors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                codingExercises.forEachIndexed { index, item ->
                    AuroraPill(item.language, { selected = index }, active = selected == index, enabled = !loading)
                }
            }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            if (stored != null) {
                Text(exercise.title, style = AdaptTheme.typography.titleLarge)
                Text("${stored.solved.size} of ${codingExercises.size} exercises solved", color = auroraAccent)
                OutlinedTextField(source, { source = it.take(3000) }, Modifier.fillMaxWidth(),
                    label = { Text("Editable code draft") }, textStyle = AdaptTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    visualTransformation = highlight, minLines = 6, maxLines = 13, enabled = !loading)
                OutlinedTextField(prediction, { prediction = it.take(100) }, Modifier.fillMaxWidth(),
                    label = { Text("Predicted output") }, singleLine = true, enabled = !loading)
                AuroraAction("Check original output", {
                    if (prediction.trim() == exercise.answer) {
                        save(stored.copy(solved = stored.solved + exercise.id), "Correct. ${exercise.explanation}")
                    } else {
                        feedback = "Try again. Trace the variables one operation at a time."
                    }
                }, Modifier.fillMaxWidth(), enabled = !loading && prediction.isNotBlank())
                if (feedback.isNotBlank()) Text(feedback, color = auroraAccent)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraPill("Save draft", { save(stored.copy(drafts = stored.drafts + (exercise.id to source)), "Draft saved.") },
                        enabled = !loading)
                    AuroraPill("Reset draft", { source = exercise.example }, enabled = !loading)
                    AuroraPill("Ask coding tutor", {
                        onTutor("Explain how to trace this ${exercise.language} code. Give a hint before the answer.\n${source.take(900)}")
                    }, enabled = !loading && source.isNotBlank())
                }
            }
        }
    }
}
