package com.example.adapt_ai_ui.ui.screens

import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
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
    var feedbackQuiz by rememberSaveable(topic.id) { mutableStateOf<String?>(null) }
    var feedbackIndex by rememberSaveable(topic.id) { mutableIntStateOf(-1) }
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

    AuroraScene {
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp)) {
        val title = when (pane) {
            "Practice" -> if (state.draft != null || feedbackQuiz != null) "Practice Session" else "Practice"
            "Study plan" -> "Learning Journey"
            "Progress" -> "Your Progress"
            "Notes" -> "Topic Notes"
            else -> topic.name
        }
        AuroraHeader(title, onBack, if (pane == "Learn") topic.subject else topic.name)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Learn", "Practice", "Study plan", "Progress", "Notes").forEach { item ->
                AuroraPill(item, { pane = item }, active = pane == item, horizontalPadding = 9.dp)
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
                    val completed = state.attempts.lastOrNull()
                    val showFeedback = feedbackQuiz != null && feedbackIndex >= 0
                    val quiz = if (showFeedback) {
                        draft?.quiz?.takeIf { it.id == feedbackQuiz } ?: completed?.quiz?.takeIf { it.id == feedbackQuiz }
                    } else draft?.quiz
                    val questionIndex = if (showFeedback) feedbackIndex else draft?.answers?.size ?: 0
                    val question = quiz?.questions?.getOrNull(questionIndex)
                    val savedAnswer = if (showFeedback) {
                        draft?.takeIf { it.quiz.id == feedbackQuiz }?.answers?.getOrNull(questionIndex)
                            ?: completed?.takeIf { it.quiz.id == feedbackQuiz }?.answers?.getOrNull(questionIndex)
                    } else null
                    if (quiz != null && question != null) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LinearProgressIndicator(
                                progress = { (questionIndex + 1f) / quiz.questions.size },
                                modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                                color = auroraAccent, trackColor = auroraRaised
                            )
                            Text("${questionIndex + 1} / ${quiz.questions.size}", color = AdaptTheme.colors.textSecondary)
                        }
                        AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
                            Text(if (quiz.diagnostic) "Diagnostic assessment" else "Question ${questionIndex + 1}",
                                style = AdaptTheme.typography.labelMedium, color = auroraAccent)
                            Spacer(Modifier.height(8.dp))
                            Text(question.prompt, style = AdaptTheme.typography.titleLarge, color = AdaptTheme.colors.textPrimary)
                        }
                        question.options.forEachIndexed { index, answer ->
                            PracticeOption(index, answer, index == (savedAnswer ?: option),
                                if (savedAnswer == null) null else when {
                                    index == question.correct -> true
                                    index == savedAnswer -> false
                                    else -> null
                                }, savedAnswer == null && !platform.busy, { option = index })
                        }
                        if (savedAnswer != null) {
                            val correct = savedAnswer == question.correct
                            AuroraPanel(Modifier.fillMaxWidth(), if (correct) AuroraGreen else MaterialTheme.colorScheme.error) {
                                Text(if (correct) "Correct!" else "Review this answer", fontWeight = FontWeight.SemiBold,
                                    color = if (correct && AdaptTheme.isDark) AuroraGreen else MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(4.dp))
                                Text(question.explanation)
                            }
                            AuroraAction(if (questionIndex + 1 == quiz.questions.size) "View results" else "Next question",
                                { feedbackQuiz = null; feedbackIndex = -1 }, Modifier.fillMaxWidth(), icon = AdaptIcons.ArrowForward)
                        } else if (draft != null) {
                            AuroraAction("Submit answer", {
                                val chosen = option
                                scope.launch {
                                    platform.updateTopic {
                                        check(it.draft?.quiz?.id == quiz.id && it.draft.answers.size == questionIndex)
                                        it.answer(chosen, System.currentTimeMillis())
                                    }
                                    val updated = platform.session?.topicState
                                    if (platform.session?.topic?.id == topic.id &&
                                        (updated?.draft?.quiz?.id == quiz.id && updated.draft.answers.size > questionIndex ||
                                            updated?.attempts?.lastOrNull()?.quiz?.id == quiz.id)
                                    ) {
                                        feedbackIndex = questionIndex
                                        feedbackQuiz = quiz.id
                                    }
                                }
                            }, Modifier.fillMaxWidth(), enabled = option in question.options.indices && !platform.busy,
                                icon = AdaptIcons.ArrowForward)
                            TextButton({ replaceQuiz = draft.quiz.diagnostic }, enabled = !platform.busy) { Text("Replace quiz") }
                        }
                    } else {
                        if (completed != null) AssessmentResult(completed, state.reported) { index ->
                            scope.launch {
                                platform.updateTopic { it.copy(reported = it.reported + questionKey(completed.quiz, index)) }
                            }
                        }
                        AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
                            Text("Build a practice session", style = AdaptTheme.typography.titleLarge)
                            Text("Questions are generated on this device for ${topic.name}.",
                                color = AdaptTheme.colors.textSecondary, modifier = Modifier.padding(top = 5.dp, bottom = 10.dp))
                            TextButton(onExplore, enabled = !platform.busy) { Text("Switch topic") }
                            Text("Questions", style = AdaptTheme.typography.labelLarge)
                            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..5).forEach { n -> AuroraPill("$n", { count = n }, active = count == n) }
                            }
                            Text("Difficulty", style = AdaptTheme.typography.labelLarge)
                            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DifficultyLevel.entries.forEach { item ->
                                    AuroraPill(item.label, { difficulty = item }, active = difficulty == item)
                                }
                            }
                        }
                        AuroraAction("Generate quiz", { launchQuiz(false) }, Modifier.fillMaxWidth(),
                            enabled = !platform.busy, icon = AdaptIcons.Sparkle)
                        AuroraPill("Start diagnostic", { launchQuiz(true) }, enabled = !platform.busy)
                    }
                }
                "Study plan" -> {
                    AuroraPanel(Modifier.fillMaxWidth(), AuroraViolet) {
                        AuroraMedallion(AdaptIcons.Pathway, AuroraViolet, Modifier.size(45.dp))
                        Spacer(Modifier.height(9.dp))
                        Text(current.preferences.goal.ifBlank { "Your weekly plan" }, style = AdaptTheme.typography.headlineSmall)
                        Spacer(Modifier.height(5.dp))
                        Text(recommendation, color = AdaptTheme.colors.textSecondary)
                    }
                    Text("This week", style = AdaptTheme.typography.titleLarge)
                    val week = studyWeek(topic, current.preferences, dueDate, today)
                    if (week.isEmpty()) Text("No study days fit this week's settings.")
                    week.forEach { block ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(CircleShape).background(auroraRaised)
                                .border(1.dp, auroraLine, CircleShape), contentAlignment = Alignment.Center) {
                                Text(block.date.dayOfMonth.toString(), color = auroraAccent, fontWeight = FontWeight.Bold)
                            }
                            AuroraPanel(Modifier.weight(1f)) {
                                Text(block.title, style = AdaptTheme.typography.titleMedium)
                                Text("${block.date}  |  ${block.minutes} min", color = AdaptTheme.colors.textSecondary,
                                    style = AdaptTheme.typography.bodySmall)
                            }
                        }
                    }
                    AuroraAction("Edit study schedule", onPreferences, Modifier.fillMaxWidth(), enabled = !platform.busy)
                }
                "Progress" -> {
                    ProgressSummary(state, mastery?.percentage)
                    mastery?.let {
                        Text("${it.responses} valid answers${if (it.lowConfidence) " | Low confidence" else ""}",
                            color = AdaptTheme.colors.textSecondary)
                    }
                    state.attempts.lastOrNull { it.quiz.diagnostic }?.let {
                        Text("Diagnostic ${it.correctCount(state.reported)}/${it.quiz.questions.indices.count { i -> questionKey(it.quiz, i) !in state.reported }}")
                    }
                    skills.forEach { skill ->
                        state.mastery(skill)?.let {
                            AuroraPanel(Modifier.fillMaxWidth()) {
                                Text("$skill  |  ${it.percentage}%", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(progress = { it.percentage / 100f }, Modifier.fillMaxWidth(),
                                    color = auroraAccent, trackColor = auroraRaised)
                            }
                        }
                    }
                    AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
                        Text(dueDate?.let { "Next revision $it" } ?: "Revision begins after a completed quiz.",
                            style = AdaptTheme.typography.titleMedium)
                        Text(recommendation, color = AdaptTheme.colors.textSecondary)
                    }
                    val mistakes = state.attempts.asReversed().flatMap { evidence ->
                        evidence.quiz.questions.mapIndexedNotNull { i, q ->
                            if (q.correct != evidence.answers[i] && questionKey(evidence.quiz, i) !in state.reported) {
                                Triple(q, evidence, i)
                            } else null
                        }
                    }
                    if (mistakes.isNotEmpty()) Text("Mistake notebook", style = AdaptTheme.typography.titleLarge)
                    mistakes.forEach { (q, evidence, i) ->
                        AuroraPanel(Modifier.fillMaxWidth()) {
                            Text(q.prompt, fontWeight = FontWeight.SemiBold)
                            Text("Your answer: ${q.options[evidence.answers[i]]}")
                            Text("Expected answer: ${q.options[q.correct]}")
                            Text(q.explanation, color = AdaptTheme.colors.textSecondary)
                        }
                    }
                    AuroraAction("Reassess topic", { pane = "Practice" }, Modifier.fillMaxWidth())
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
    val correct = evidence.correctCount(reported)
    AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
        Text(if (evidence.quiz.diagnostic) "Diagnostic results" else "Quiz results",
            style = AdaptTheme.typography.titleLarge)
        Text(if (valid == 0) "No valid questions" else "$correct / $valid correct  |  ${100 * correct / valid}%",
            color = auroraAccent)
    }
    evidence.quiz.questions.forEachIndexed { i, question ->
        AuroraPanel(Modifier.fillMaxWidth()) {
            Text(question.prompt, fontWeight = FontWeight.SemiBold)
            if (questionKey(evidence.quiz, i) in reported) Text("Reported | Excluded from scores and mastery")
            else {
                Text(if (question.correct == evidence.answers[i]) "Correct" else "Your answer: ${question.options[evidence.answers[i]]}",
                    color = auroraAccent)
                Text("Answer: ${question.options[question.correct]}")
                Text(question.explanation, color = AdaptTheme.colors.textSecondary)
                TextButton({ onReport(i) }) { Text("Report ambiguous or incorrect question") }
            }
        }
    }
}

@Composable
private fun PracticeOption(index: Int, answer: String, selected: Boolean, correct: Boolean?,
                           enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val accent = when (correct) {
        true -> if (AdaptTheme.isDark) AuroraGreen else auroraAccent
        false -> MaterialTheme.colorScheme.error
        null -> auroraAccent
    }
    val highlighted = selected || correct != null
    val base = auroraPanel
    Row(Modifier.fillMaxWidth().heightIn(min = 49.dp).clip(shape)
        .background(if (highlighted) androidx.compose.ui.graphics.lerp(base, accent, .13f) else base)
        .border(1.dp, if (highlighted) accent else auroraLine, shape)
        .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(Modifier.size(29.dp).background(accent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
            Text(('A' + index).toString(), color = accent, fontWeight = FontWeight.Bold)
        }
        Text(answer, Modifier.weight(1f), color = AdaptTheme.colors.textPrimary)
        if (highlighted) Icon(if (correct == false) AdaptIcons.Cross else AdaptIcons.Check,
            if (correct == false) "Incorrect answer" else if (correct == true) "Correct answer" else null,
            tint = accent, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun ProgressSummary(state: TopicState, mastery: Int?) {
    val accent = auroraAccent
    val track = auroraRaised
    val scores = state.attempts.sortedBy { it.finishedAt }.mapNotNull { evidence ->
        val valid = evidence.quiz.questions.indices.count { questionKey(evidence.quiz, it) !in state.reported }
        if (valid > 0) evidence.correctCount(state.reported).toFloat() / valid else null
    }
    Box(Modifier.fillMaxWidth().height(172.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(146.dp)) {
            val stroke = 10.dp.toPx()
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .11f), Color.Transparent)))
            val top = Offset(stroke / 2f, stroke / 2f)
            val bounds = Size(size.width - stroke, size.height - stroke)
            drawArc(track, -90f, 360f, false, top, bounds, style = Stroke(stroke))
            if (mastery != null && mastery > 0) {
                drawArc(Brush.sweepGradient(listOf(AuroraCyan, AuroraBlue, AuroraCyan, AuroraCyan)),
                    -90f, 360f * mastery / 100f, false, top, bounds, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mastery?.let { "$it%" } ?: "Unassessed", style = AdaptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = AdaptTheme.colors.textPrimary)
            Text("Overall mastery", color = AdaptTheme.colors.textSecondary, style = AdaptTheme.typography.bodySmall)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple("${state.studySeconds / 60} min", "Study time", AdaptIcons.Pathway),
            Triple(if (state.completedAt == null) "0" else "1", "Lessons", AdaptIcons.Notebook),
            Triple(state.attempts.count { !it.quiz.diagnostic }.toString(), "Practice quizzes", AdaptIcons.Target)
        ).forEach { (value, label, icon) ->
            AuroraPanel(Modifier.weight(1f)) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
                Spacer(Modifier.height(6.dp))
                Text(value, style = AdaptTheme.typography.titleMedium, color = AdaptTheme.colors.textPrimary)
                Text(label, style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
            }
        }
    }
    Text("Skill Growth", style = AdaptTheme.typography.titleLarge)
    AuroraPanel(Modifier.fillMaxWidth()) {
        if (scores.size < 2) {
            Text("Complete two scored assessments to see your trend.", color = AdaptTheme.colors.textSecondary)
        } else {
            val plotted = scores.takeLast(12)
            Canvas(Modifier.fillMaxWidth().height(91.dp).padding(5.dp)) {
                (0..4).forEach { step ->
                    val y = size.height * step / 4f
                    drawLine(track, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
                val points = plotted.mapIndexed { i, score ->
                    Offset(size.width * i / (plotted.size - 1), size.height * (1f - score))
                }
                points.zipWithNext().forEach { (from, to) -> drawLine(accent, from, to, 3.dp.toPx(), StrokeCap.Round) }
                points.forEach { drawCircle(AuroraBlue, 4.dp.toPx(), it); drawCircle(accent, 2.dp.toPx(), it) }
            }
            Text("Accuracy across the last ${plotted.size} assessments. Latest: ${(plotted.last() * 100).toInt()}%.",
                style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
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
