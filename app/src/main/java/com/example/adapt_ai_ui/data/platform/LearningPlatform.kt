package com.example.adapt_ai_ui.data.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import com.example.adapt_ai_ui.inference.ModelUnavailable
import com.example.adapt_ai_ui.inference.TutorEvent
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.UUID
import kotlinx.coroutines.CancellationException

val LocalLearningPlatform = staticCompositionLocalOf<LearningPlatform?> { null }

class LearningPlatform(private val repository: PlatformRepository) {
    var session by mutableStateOf<LearningSession?>(null)
        private set
    var profiles by mutableStateOf<List<LocalLearner>>(emptyList())
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    suspend fun initialize() = runAction("Could not open local learning data.") {
        val (learners, active) = repository.initialize()
        profiles = learners
        active?.let { attach(repository.open(it)) }
    }

    suspend fun create(name: String, guest: Boolean) = runAction("Could not create the profile.") {
        val id = repository.create(name, guest)
        profiles = repository.list()
        select(id)
    }

    suspend fun open(id: String) = runAction("Could not open the profile.") { select(id) }

    suspend fun closeProfile() = runAction("Could not close the profile.") {
        settleSession()
        repository.rememberActive(null)
        session = null
    }

    suspend fun rename(name: String) = runAction("Could not update the profile.") {
        val current = requireNotNull(session)
        settleSession()
        repository.rename(current.profile.id, name.trim())
        profiles = repository.list()
        attach(repository.open(current.profile.id))
    }

    suspend fun delete(id: String) = runAction("Could not delete the profile.") {
        if (session?.profile?.id == id) {
            settleSession()
            session = null
        }
        repository.delete(id)
        profiles = repository.list()
    }

    suspend fun savePreferences(preferences: LearningPreferences) = runAction("Could not save your learning preferences.") {
        val current = requireNotNull(session)
        val switchTopic = preferences.activeTopic != current.preferences.activeTopic
        if (switchTopic) settleSession()
        repository.savePreferences(current.profile.id, preferences)
        if (switchTopic) attach(repository.open(current.profile.id))
        else session = current.copy(preferences = preferences)
    }

    suspend fun selectTopic(topic: LearningTopic) {
        val current = session?.preferences ?: return
        savePreferences(current.copy(
            interests = current.interests + topic.id, activeTopic = topic.id,
            recentTopics = (listOf(topic.id) + current.recentTopics).distinct().take(12)
        ))
    }

    suspend fun updateTopic(change: (TopicState) -> TopicState) = runAction("Could not save topic progress.") {
        updateCurrentTopic(change)
    }

    suspend fun loadScan(): SavedScan? {
        val current = requireNotNull(session)
        return repository.loadScan(current.profile.id, current.topic?.id)
    }

    suspend fun storeScan(scan: SavedScan?) {
        val current = requireNotNull(session)
        repository.storeScan(current.profile.id, current.topic?.id, scan)
    }

    suspend fun codingProgress() = repository.codingProgress(requireNotNull(session).profile.id)

    suspend fun saveCodingProgress(progress: CodingProgress) =
        repository.saveCodingProgress(requireNotNull(session).profile.id, progress)

    suspend fun recordStudy(profile: String, topic: String, seconds: Long) {
        if (seconds <= 0) return
        try {
            val today = java.time.LocalDate.now().toString()
            val updated = repository.updateTopic(profile, topic) {
                val days = it.studyByDate.orEmpty()
                it.copy(studySeconds = it.studySeconds + seconds,
                    studyByDate = days + (today to (days.getOrDefault(today, 0L) + seconds)))
            }
            val current = session
            if (current?.profile?.id == profile && topic in current.preferences.interests) {
                session = current.copy(topicStates = current.topicStates + (topic to updated))
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (problem: Exception) {
            android.util.Log.w("AuroraPlatform", "Could not save study time", problem)
        }
    }

    suspend fun generateLesson() = runAction("Lesson generation failed. Check the model and try again.") {
        val current = requireNotNull(session)
        val topic = requireNotNull(current.topic)
        val input = context(current, topic)
        val (lesson, _) = generate(
            "You are an offline tutor. Write a concise, accurate lesson in ${current.preferences.language}. " +
                "Use headings, one example, a recap, and one understanding check. Never invent sources.",
            input
        )
        require(lesson.length >= 40)
        updateCurrentTopic { it.copy(lesson = lesson, completedAt = null) }
    }

    suspend fun generateQuiz(count: Int, difficulty: DifficultyLevel, diagnostic: Boolean) =
        runAction("Llama did not produce a valid quiz. Try again or request fewer questions.") {
            val current = requireNotNull(session)
            val topic = requireNotNull(current.topic)
            require(count in 1..5)
            val questions = mutableListOf<QuizQuestion>()
            val identities = hashSetOf<String>()
            val topicLabel = platformJson.toJson(topic.name)
            val prefix = "{\"v\":1,\"topic\":$topicLabel,\"q\":["
            var modelHash = ""
            repeat(count) {
                var question: QuizQuestion? = null
                repeat(2) tryQuestion@{ attempt ->
                    if (question != null) return@tryQuestion
                    val algebra = topic.id == "mathematics/algebra"
                    val (raw, hash) = if (algebra) generate(
                        "You teach Algebra. Write one integer linear equation in the form ax + b = c. " +
                            "Begin a new line with Question: Solve for x: followed by the equation.",
                        "Create one ${difficulty.label.lowercase()} Algebra question for solving a one-variable linear equation. " +
                            "Use integer coefficients, a nonzero constant and an integer solution. Check it by substitution. " +
                            if (questions.isNotEmpty()) "Avoid previous equation ${questions.last().prompt.take(90)}." else "",
                        limit = 384
                    ) else generate(
                        "You write quiz JSON. Continue the assistant prefix with ONE object: " +
                            "{\"p\":\"question\",\"o\":[\"option\",\"option\",\"option\"],\"a\":0,\"e\":\"reason\",\"c\":$topicLabel}. " +
                            "The integer a is the zero-based correct index (0,1,2). " +
                            "Use the exact requested topic or listed competency as c. Close with ]}. No Markdown.",
                        "Write one ${difficulty.label} question. ${context(current, topic)} " +
                            if (attempt == 1 && topic.name.equals("Algebra", true))
                                " Use a simple integer linear equation and verify the solution by substitution."
                            else if (questions.isNotEmpty()) " Avoid repeating: ${questions.last().prompt.take(100)}" else "",
                        prefix = prefix,
                        limit = 576
                    )
                    val candidate = runCatching {
                        val normalized = if (algebra) requireNotNull(QuizValidator.normalizeAlgebra(raw, topic)) else raw
                        QuizValidator.parse(normalized, topic, 1).single()
                    }
                        .onFailure { android.util.Log.i("AuroraPlatform", "Quiz validation rejected output (${it.javaClass.simpleName})") }
                        .getOrNull()
                    if (candidate != null && identities.add(candidate.prompt.trim().lowercase())) {
                        question = candidate
                        modelHash = hash
                    }
                }
                questions += requireNotNull(question)
            }
            updateCurrentTopic {
                it.copy(draft = DraftQuiz(TopicQuiz(UUID.randomUUID().toString(), questions, difficulty, diagnostic, modelHash)))
            }
        }

    private suspend fun generate(system: String, user: String, prefix: String = "", limit: Int = 896): Pair<String, String> {
        val runtime = TutorRepository.runtime ?: throw ModelUnavailable()
        fun String.safe(limit: Int): String {
            var text = replace("<|", "< |").take(limit)
            while (text.toByteArray(Charsets.UTF_8).size > limit) text = text.dropLast(1)
            return if (text.lastOrNull()?.isHighSurrogate() == true) text.dropLast(1) else text
        }
        val systemTurn = "<|start_header_id|>system<|end_header_id|>\n\n${system.safe(680)}<|eot_id|>"
        val userStart = "<|start_header_id|>user<|end_header_id|>\n\n"
        val assistantStart = "<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n$prefix"
        val budget = 1600 - (systemTurn + userStart + assistantStart).toByteArray(Charsets.UTF_8).size
        val prompt = systemTurn + userStart + user.safe(budget) + assistantStart
        val text = StringBuilder()
        var hash: String? = null
        runtime.generate(prompt, limit).collect { event ->
            when (event) {
                is TutorEvent.Token -> text.append(event.text)
                is TutorEvent.Complete -> { check(!event.textLimited); hash = event.modelHash }
            }
        }
        val output = text.toString().substringBefore("<|eot_id|>").substringBefore("<|end_of_text|>").trim()
        val result = if (prefix.isEmpty() || Regex("^\\s*\\{\\s*\\\"v\\\"").containsMatchIn(output))
            output else prefix + output
        return result to requireNotNull(hash)
    }

    private fun context(current: LearningSession, topic: LearningTopic): String = buildString {
        append("Subject: ${topic.subject}. Exact topic: ${topic.name}. Competencies: ${topic.concepts.joinToString()}. ")
        append("Level: ${current.preferences.level?.label ?: "unknown"}. Style: ${current.preferences.style}. ")
        val objective = topic.objective.ifBlank { current.preferences.goal }
        if (objective.isNotBlank()) append("Objective: $objective. ")
        current.topicState.mastery()?.let { append("Recent mastery: ${it.percentage}%. ") }
        val previous = current.topicState.attempts.lastOrNull()
        if (previous != null) {
            val gap = previous.quiz.questions.indices.firstOrNull {
                previous.quiz.questions[it].correct != previous.answers[it] &&
                    questionKey(previous.quiz, it) !in current.topicState.reported
            }
            if (gap != null) append("Recent mistake: ${previous.quiz.questions[gap].competency}. ")
        }
        val reference = current.topicState.note.ifBlank { current.topicState.lesson }
        if (reference.isNotBlank()) append("Local excerpt: ${reference.take(110)}")
    }

    private suspend fun updateCurrentTopic(change: (TopicState) -> TopicState) {
        val current = requireNotNull(session)
        val topic = requireNotNull(current.topic)
        val updated = repository.updateTopic(current.profile.id, topic.id, change)
        session = current.copy(topicStates = current.topicStates + (topic.id to updated))
    }

    fun dismissError() { error = null }

    private suspend fun select(id: String) {
        settleSession()
        val next = repository.open(id)
        repository.rememberActive(id)
        attach(next)
    }

    private fun attach(next: LearningSession) {
        AdaptPersistence.attach(next.legacy, next.legacyDomains)
        session = next
    }

    private suspend fun settleSession() {
        TutorRepository.detachSession()
        AdaptPersistence.flush()
    }

    private suspend fun runAction(failure: String, operation: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        try {
            operation()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: ModelUnavailable) {
            error = "Import a verified Llama model in Profile."
        } catch (problem: Exception) {
            android.util.Log.w("AuroraPlatform", failure, problem)
            error = failure
        } finally {
            busy = false
        }
    }
}
