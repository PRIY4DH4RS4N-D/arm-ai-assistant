package com.example.adapt_ai_ui.ui.screens

import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.platform.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TopicWorkspace(
    platform: LearningPlatform, initialPane: String,
    onBack: () -> Unit, onExplore: () -> Unit, onPreferences: () -> Unit, onTutor: () -> Unit
) {
    val current = platform.session ?: return
    val topic = current.topic ?: return
    val state = current.topicState
    val scope = rememberCoroutineScope()
    val persistenceScope = LocalLifecycleOwner.current.lifecycleScope
    var pane by rememberSaveable(topic.id, initialPane) { mutableStateOf(initialPane) }
    var count by rememberSaveable { mutableIntStateOf(1) }
    var difficulty by rememberSaveable { mutableStateOf(current.preferences.level ?: DifficultyLevel.BEGINNER) }
    var option by rememberSaveable(state.draft?.quiz?.id, state.draft?.answers?.size) { mutableIntStateOf(-1) }
    var note by rememberSaveable(topic.id, state.note) { mutableStateOf(state.note) }
    var noteError by remember { mutableStateOf<String?>(null) }
    val resolver = LocalContext.current.contentResolver
    val importNote = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                note = withContext(Dispatchers.IO) {
                    val stream = requireNotNull(resolver.openInputStream(uri))
                    stream.bufferedReader(Charsets.UTF_8).use { reader ->
                        buildString {
                            val buffer = CharArray(512)
                            while (length <= 12_000) {
                                val lengthRead = reader.read(buffer, 0, minOf(buffer.size, 12_001 - length))
                                if (lengthRead < 0) break
                                append(buffer, 0, lengthRead)
                            }
                            require(length <= 12_000 && '\u0000' !in this)
                        }
                    }
                }
                noteError = null
            } catch (cancel: kotlinx.coroutines.CancellationException) {
                throw cancel
            } catch (_: Exception) {
                noteError = "Choose a UTF-8 text note up to 12,000 characters."
            }
        }
    }
    var replaceQuiz by remember { mutableStateOf<Boolean?>(null) }
    var showLesson by rememberSaveable(topic.id) { mutableStateOf(false) }
    var dropLesson by remember { mutableStateOf(false) }
    var generation by remember { mutableStateOf<Job?>(null) }
    val mastery = state.mastery()
    val due = state.revisionAt()
    val today = LocalDate.now()
    val dueDate = due?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
    val skills = state.attempts.flatMap { it.quiz.questions }.map { it.competency }.distinct()
    val weak = skills.mapNotNull { skill -> state.mastery(skill)?.let { skill to it.percentage } }
        .filter { it.second < 70 }.minByOrNull { it.second }
    val recommendation = when {
        due != null && due <= System.currentTimeMillis() -> "Revision is due. Reassess this topic."
        weak != null -> "Practise ${weak.first}. Recent valid answers show ${weak.second}% mastery."
        state.completedAt == null -> "Continue the lesson for ${topic.name}."
        mastery == null -> "Complete a practice quiz to measure mastery."
        else -> "Try another assessment to confirm your progress."
    }

    fun launchQuiz(diagnostic: Boolean) {
        generation = scope.launch { platform.generateQuiz(count, difficulty, diagnostic) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        PanelHeader(topic.name, onBack)
        Text(topic.subject, color = MaterialTheme.colorScheme.primary)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Learn", "Practice", "Study plan", "Progress", "Notes").forEach {
                FilterChip(pane == it, { pane = it }, { Text(it) })
            }
        }
        if (platform.busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            if (generation?.isActive == true) TextButton({ generation?.cancel() }) { Text("Cancel generation") }
        }
        platform.error?.let {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(it, Modifier.weight(1f).padding(top = 8.dp), color = MaterialTheme.colorScheme.error)
                TextButton(platform::dismissError) { Text("Dismiss") }
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            when (pane) {
                "Learn" -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(topic.objective.ifBlank { current.preferences.goal }.ifBlank { "Build confidence in ${topic.name}." })
                            if (topic.concepts.isNotEmpty()) Text(topic.concepts.joinToString(" · "))
                            Text(recommendation, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(onTutor, enabled = !platform.busy) { Text("Ask Tutor") }
                        OutlinedButton(onExplore, enabled = !platform.busy) { Text("Switch topic") }
                    }
                    if (current.preferences.goal.isBlank()) {
                        TextButton(onPreferences, enabled = !platform.busy) { Text("Set a learning goal") }
                    }
                    if (current.preferences.level == null && state.attempts.none { it.quiz.diagnostic }) {
                        TextButton({ pane = "Practice" }) { Text("Assess your starting level") }
                    }
                    if (state.lesson.isBlank()) {
                        Text("Generate a local AI lesson for this topic.")
                        Button({ generation = scope.launch { platform.generateLesson() } }, enabled = !platform.busy) {
                            Text("Generate lesson")
                        }
                    } else {
                        Text(if (state.completedAt == null) "Lesson ready" else "Lesson completed")
                        Button({ showLesson = !showLesson }, enabled = !platform.busy) {
                            Text(if (showLesson) "Close lesson" else "Open lesson")
                        }
                        if (showLesson) {
                            ActiveLessonTimer { seconds ->
                                persistenceScope.launch { platform.recordStudy(current.profile.id, topic.id, seconds) }
                            }
                            Text("Generated locally. Check key facts against your sources.", style = MaterialTheme.typography.labelMedium)
                            RenderLesson(state.lesson)
                            Button({
                                scope.launch { platform.updateTopic { it.copy(completedAt = System.currentTimeMillis()) } }
                            }, enabled = !platform.busy && state.completedAt == null) { Text("Mark complete") }
                            Row(Modifier.horizontalScroll(rememberScrollState())) {
                                TextButton({
                                    scope.launch { platform.updateTopic { it.copy(bookmarked = !it.bookmarked) } }
                                }, enabled = !platform.busy) { Text(if (state.bookmarked) "Remove bookmark" else "Bookmark") }
                                TextButton({ pane = "Practice" }) { Text("Practise this topic") }
                                TextButton({ dropLesson = true }, enabled = !platform.busy) { Text("Dispute lesson") }
                            }
                        }
                    }
                }
                "Practice" -> {
                    val draft = state.draft
                    val question = draft?.quiz?.questions?.getOrNull(draft.answers.size)
                    if (draft != null && question != null) {
                        Text(if (draft.quiz.diagnostic) "Diagnostic assessment" else "Practice session",
                            style = MaterialTheme.typography.titleLarge)
                        Text("Question ${draft.answers.size + 1} of ${draft.quiz.questions.size}")
                        LinearProgressIndicator(progress = { draft.answers.size.toFloat() / draft.quiz.questions.size }, Modifier.fillMaxWidth())
                        Card(Modifier.fillMaxWidth()) { Text(question.prompt, Modifier.padding(16.dp)) }
                        question.options.forEachIndexed { index, answer ->
                            OutlinedButton({ option = index }, Modifier.fillMaxWidth(), enabled = !platform.busy) {
                                RadioButton(selected = option == index, onClick = null)
                                Text(answer, Modifier.weight(1f))
                            }
                        }
                        Button({
                            val quizId = draft.quiz.id
                            val index = draft.answers.size
                            scope.launch {
                                platform.updateTopic {
                                    check(it.draft?.quiz?.id == quizId && it.draft.answers.size == index)
                                    it.answer(option, System.currentTimeMillis())
                                }
                            }
                        }, enabled = option in question.options.indices && !platform.busy) { Text("Submit answer") }
                        TextButton({ replaceQuiz = draft.quiz.diagnostic }, enabled = !platform.busy) { Text("Replace quiz") }
                    } else {
                        val last = state.attempts.lastOrNull()
                        if (last != null) AssessmentResult(last, state.reported) { index ->
                            scope.launch { platform.updateTopic { it.copy(reported = it.reported + questionKey(last.quiz, index)) } }
                        }
                        Text("Generate a practice or diagnostic quiz using the on-device model.")
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            (1..5).forEach { n -> FilterChip(count == n, { count = n }, { Text("$n") }) }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            DifficultyLevel.entries.forEach { item ->
                                FilterChip(difficulty == item, { difficulty = item }, { Text(item.label) })
                            }
                        }
                        Button({ launchQuiz(false) }, enabled = !platform.busy) { Text("Generate quiz") }
                        OutlinedButton({ launchQuiz(true) }, enabled = !platform.busy) { Text("Start diagnostic") }
                    }
                }
                "Study plan" -> {
                    Text(current.preferences.goal.ifBlank { "Your weekly plan" }, style = MaterialTheme.typography.titleLarge)
                    Text(recommendation)
                    val week = studyWeek(topic, current.preferences, dueDate, today)
                    if (week.isEmpty()) Text("No study days fit this week's settings.")
                    week.forEach { block ->
                        ListItem(headlineContent = { Text(block.title) },
                            supportingContent = { Text("${block.date} · ${block.minutes} min") })
                    }
                    OutlinedButton(onPreferences, enabled = !platform.busy) { Text("Edit study schedule") }
                }
                "Progress" -> {
                    Text(mastery?.let { "Mastery ${it.percentage}%" } ?: "Unassessed", style = MaterialTheme.typography.headlineSmall)
                    mastery?.let {
                        LinearProgressIndicator(progress = { it.percentage / 100f }, Modifier.fillMaxWidth())
                        Text("${it.responses} valid answers${if (it.lowConfidence) " · Low confidence" else ""}")
                    }
                    Text("${state.attempts.count { !it.quiz.diagnostic }} completed practice quizzes")
                    Text(if (state.completedAt != null) "Lesson complete" else "Lesson incomplete")
                    Text("${state.studySeconds / 60} minutes of active lesson time")
                    state.attempts.lastOrNull { it.quiz.diagnostic }?.let {
                        Text("Diagnostic ${it.correctCount(state.reported)}/${it.quiz.questions.indices.count { i -> questionKey(it.quiz, i) !in state.reported }}")
                    }
                    skills.forEach { skill ->
                        state.mastery(skill)?.let { Text("$skill · ${it.percentage}%") }
                    }
                    Text(dueDate?.let { "Next revision $it" } ?: "Revision begins after a completed quiz.")
                    Text(recommendation)
                    val mistakes = state.attempts.asReversed().flatMap { evidence ->
                        evidence.quiz.questions.mapIndexedNotNull { i, q ->
                            if (q.correct != evidence.answers[i] && questionKey(evidence.quiz, i) !in state.reported) {
                                Triple(q, evidence, i)
                            } else null
                        }
                    }
                    if (mistakes.isNotEmpty()) Text("Mistake notebook", style = MaterialTheme.typography.titleLarge)
                    mistakes.forEach { (q, evidence, i) ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(q.prompt)
                                Text("Your answer: ${q.options[evidence.answers[i]]}")
                                Text("Expected answer: ${q.options[q.correct]}")
                                Text(q.explanation)
                            }
                        }
                    }
                    OutlinedButton({ pane = "Practice" }) { Text("Reassess topic") }
                }
                "Notes" -> {
                    Text("Saved notes can ground tutor, lesson and quiz prompts.")
                    OutlinedButton({ importNote.launch(arrayOf("text/plain", "text/markdown")) }) { Text("Import text notes") }
                    noteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(note, { note = it.take(12_000) }, Modifier.fillMaxWidth(), label = { Text("Your notes") }, minLines = 6)
                    Button({ scope.launch { platform.updateTopic { it.copy(note = note.trim()) } } }, enabled = !platform.busy) { Text("Save notes") }
                }
            }
        }
    }
    replaceQuiz?.let { diagnostic ->
        AlertDialog(onDismissRequest = { replaceQuiz = null }, title = { Text("Replace this quiz?") },
            text = { Text("Saved responses in the unfinished quiz will be removed after a valid new quiz is generated.") },
            confirmButton = { TextButton({ replaceQuiz = null; launchQuiz(diagnostic) }) { Text("Replace") } },
            dismissButton = { TextButton({ replaceQuiz = null }) { Text("Cancel") } })
    }
    if (dropLesson) {
        AlertDialog(onDismissRequest = { dropLesson = false }, title = { Text("Remove this generated lesson?") },
            text = { Text("Remove the inaccurate lesson and its completion marker. Your quiz evidence stays available.") },
            confirmButton = { TextButton({
                dropLesson = false
                showLesson = false
                scope.launch { platform.updateTopic { it.copy(lesson = "", completedAt = null, bookmarked = false) } }
            }) { Text("Remove lesson") } },
            dismissButton = { TextButton({ dropLesson = false }) { Text("Cancel") } })
    }
}

@Composable
private fun AssessmentResult(evidence: QuizEvidence, reported: Set<String>, onReport: (Int) -> Unit) {
    val valid = evidence.quiz.questions.indices.count { questionKey(evidence.quiz, it) !in reported }
    Text(if (evidence.quiz.diagnostic) "Diagnostic results" else "Quiz results", style = MaterialTheme.typography.titleLarge)
    val correct = evidence.correctCount(reported)
    Text(if (valid == 0) "No valid questions" else "$correct / $valid correct · ${100 * correct / valid}%")
    evidence.quiz.questions.forEachIndexed { i, question ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(question.prompt)
                if (questionKey(evidence.quiz, i) in reported) Text("Reported · excluded from scores and mastery")
                else {
                    Text(if (question.correct == evidence.answers[i]) "Correct" else "Your answer: ${question.options[evidence.answers[i]]}")
                    Text("Answer: ${question.options[question.correct]}")
                    Text(question.explanation)
                    TextButton({ onReport(i) }) { Text("Report ambiguous or incorrect question") }
                }
            }
        }
    }
}

@Composable
private fun RenderLesson(text: String) {
    var code = false
    text.lineSequence().forEach { line ->
        when {
            line.trimStart().startsWith("\u0060\u0060\u0060") -> code = !code
            code -> Text(line, Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(6.dp),
                fontFamily = FontFamily.Monospace)
            line.startsWith("#") -> Text(line.trimStart('#', ' '), style = MaterialTheme.typography.titleMedium)
            line.isNotBlank() -> Text(line)
        }
    }
}

@Composable
private fun ActiveLessonTimer(record: (Long) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val callback by rememberUpdatedState(record)
    DisposableEffect(lifecycle) {
        var started = if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) SystemClock.elapsedRealtime() else null
        fun pause() {
            val seconds = started?.let { (SystemClock.elapsedRealtime() - it) / 1000 } ?: 0
            started = null
            if (seconds > 0) callback(seconds)
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (started == null) started = SystemClock.elapsedRealtime()
                Lifecycle.Event.ON_PAUSE -> pause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); pause() }
    }
}
