package com.example.adapt_ai_ui.data

import android.content.Context
import com.example.adapt_ai_ui.inference.LocalTutor
import com.example.adapt_ai_ui.inference.ModelUnavailable
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface TutorEngine {
    suspend fun generateResponse(request: TutorRequest, onToken: (String) -> Unit = {}): TutorResponse
    fun getSuggestedPrompts(domainId: String, currentTopic: String, mode: TutorMode): List<String> = tutorSuggestions(domainId, currentTopic, mode)
}

private fun tutorSuggestions(domainId: String, currentTopic: String, mode: TutorMode): List<String> {
    return when (mode) {
        TutorMode.TEACH_ME -> listOf(
            "Can you explain this with a real-world analogy?",
            "Why is this concept foundational for beginners?",
            "What happens step-by-step during execution?"
        )
        TutorMode.SOCRATIC -> when (domainId) {
            "ai_ml" -> listOf(
                "Why does INT8 save 4x memory over FP32?",
                "What happens to activations if scale S is too large?",
                "How does symmetric quantization eliminate zero-point subtraction?"
            )
            "cs" -> listOf(
                "Why is binary search tree lookup O(log N)?",
                "How does a hash collision affect lookup latency?",
                "What is the difference between BFS and DFS traversal?"
            )
            "embedded" -> listOf(
                "Why should interrupt service routines (ISRs) be short?",
                "How does DMA transfer data without CPU intervention?",
                "What is the role of the NVIC in ARM Cortex-M?"
            )
            "programming" -> listOf(
                "Why does RAII eliminate manual delete calls in C++?",
                "How does std::atomic prevent data races?",
                "What causes pointer aliasing to hinder compiler optimization?"
            )
            else -> listOf(
                "What are the core trade-offs in $currentTopic?",
                "How does this connect to real-world hardware limits?",
                "Can you challenge my intuition on this topic?"
            )
        }
        TutorMode.DEBUG -> listOf(
            "Why am I getting accuracy loss or overflow here?",
            "Can you review this code snippet for edge device bottlenecks?",
            "How do I trace cache misses in this loop?"
        )
        TutorMode.PRACTICE -> listOf(
            "Give me a scenario-based question on $currentTopic",
            "Test my understanding of edge constraints",
            "Give me a quick calculation drill"
        )
        TutorMode.REVIEW -> listOf(
            "Summarize the key mathematical formulas",
            "What are the 3 most common learner misconceptions?",
            "Give me a cheat-sheet checklist for this milestone"
        )
    }
}

private fun welcomeMessage(
    learnerName: String,
    domainTitle: String,
    currentTopic: String,
    mode: TutorMode
) = TutorConversationMessage(
    id = "welcome_${UUID.randomUUID()}",
    sender = "Aurora",
    isTutor = true,
    content = "Hello, $learnerName. Ask me about $currentTopic in $domainTitle.",
    mode = mode
)

// -------------------------------------------------------------
// Tutor Session State & Repository
// -------------------------------------------------------------
object TutorRepository {
    private var engine: TutorEngine = object : TutorEngine {
        override suspend fun generateResponse(request: TutorRequest, onToken: (String) -> Unit): TutorResponse =
            throw ModelUnavailable()
    }
    var runtime by mutableStateOf<LocalTutor?>(null)
        private set
    private var generation: Job? = null
    private var lastRequest: TutorRequest? = null
    private var lastUserMessageId: String? = null
    var draftAnswer by mutableStateOf("")
        private set

    fun initializeRuntime(context: Context) {
        if (runtime != null) return
        val local = LocalTutor(context.applicationContext)
        runtime = local
        engine = ExecuTorchTutorEngine(local)
        CoroutineScope(Dispatchers.Main).launch { local.initialize() }
    }

    fun stopGeneration() {
        generation?.cancel()
    }

    internal fun setEngineForTest(testEngine: TutorEngine): TutorEngine {
        val previous = engine
        engine = testEngine
        return previous
    }

    var activeMode by mutableStateOf(TutorMode.SOCRATIC)
    var isGenerating by mutableStateOf(false)
    val conversationMessages = mutableStateListOf<TutorConversationMessage>()

    fun initializeSession(
        learnerName: String,
        domainTitle: String,
        currentTopic: String,
        initialPrompt: TutorQuickPrompt? = null
    ) {
        if (conversationMessages.isEmpty()) {
            val welcome = welcomeMessage(learnerName, domainTitle, currentTopic, activeMode)
            conversationMessages.add(welcome)

            if (initialPrompt != null) {
                // Auto-submit initial prompt if provided from Lesson or Home
                sendUserQuery(
                    query = initialPrompt.prompt,
                    domainId = "ai_ml",
                    domainTitle = domainTitle,
                    currentTopic = currentTopic,
                    goal = "Build Real Edge AI Projects",
                    skillLevel = DifficultyLevel.INTERMEDIATE,
                    learningStyle = "Hands-on Projects & Code"
                )
            }
        }
    }

    fun setMode(mode: TutorMode, learnerName: String, domainTitle: String, currentTopic: String) {
        activeMode = mode
        val switchMsg = TutorConversationMessage(
            id = "mode_switch_${UUID.randomUUID()}",
            sender = "ADAPT-AI",
            isTutor = true,
            content = "Switched to ${mode.title} Mode (${mode.iconEmoji}). ${mode.description}.",
            mode = mode,
            structuredPayload = TutorResponse(
                text = "Switched to ${mode.title} Mode.",
                conceptExplanation = mode.description,
                followUpPrompts = engine.getSuggestedPrompts("ai_ml", currentTopic, mode)
            )
        )
        conversationMessages.add(switchMsg)
        notifyPersistenceChanged()
    }

    suspend fun sendUserQueryAsync(
        query: String,
        domainId: String,
        domainTitle: String,
        currentTopic: String,
        goal: String,
        skillLevel: DifficultyLevel,
        learningStyle: String,
        retry: Boolean = false
    ) {
        if (query.isBlank() || isGenerating) return

        val mode = activeMode
        val history = conversationMessages.filter {
            !it.isError && (!it.isTutor || it.structuredPayload?.executionModelLabel == AURORA_RUNTIME) &&
                (!retry || it.id != lastUserMessageId)
        }.takeLast(2)
        val userMsg = TutorConversationMessage(
            id = "user_${UUID.randomUUID()}",
            sender = "You",
            isTutor = false,
            content = query.trim(),
            mode = activeMode
        )
        if (!retry) {
            conversationMessages.add(userMsg)
            lastUserMessageId = userMsg.id
            notifyPersistenceChanged()
        }
        isGenerating = true
        draftAnswer = ""

        try {
            val request = TutorRequest(
                query = query.trim(),
                mode = mode,
                domainId = domainId,
                domainTitle = domainTitle,
                topicTitle = currentTopic,
                goal = goal,
                skillLevel = skillLevel,
                learningStyle = learningStyle,
                conversationHistory = history,
                recentMistake = AdaptiveLearningEngine.mistakeNotebook.lastOrNull {
                    !it.isResolved && (it.question.topicTag.contains(currentTopic, ignoreCase = true) ||
                        currentTopic.contains(it.question.topicTag, ignoreCase = true))
                }?.question?.topicTag
            )

            lastRequest = request
            val response = engine.generateResponse(request.copy(curriculumExcerpt = retrieveLessonExcerpt(request))) {
                draftAnswer += it
            }
            val tutorMsg = TutorConversationMessage(
                id = "tutor_${UUID.randomUUID()}",
                sender = "ADAPT-AI",
                isTutor = true,
                content = response.text,
                mode = mode,
                structuredPayload = response
            )
            conversationMessages.add(tutorMsg)
            notifyPersistenceChanged()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMsg = TutorConversationMessage(
                id = "error_${UUID.randomUUID()}",
                sender = "ADAPT-AI",
                isTutor = true,
                content = if (e is ModelUnavailable) e.message.orEmpty()
                    else "Aurora could not complete this response. Check the model in Profile and retry.",
                mode = mode,
                isError = true
            )
            conversationMessages.add(errorMsg)
            notifyPersistenceChanged()
        } catch (_: LinkageError) {
            conversationMessages.add(TutorConversationMessage(
                id = "error_${UUID.randomUUID()}", sender = "Aurora", isTutor = true, mode = mode,
                content = "ExecuTorch could not start on this device.", isError = true
            ))
            notifyPersistenceChanged()
        } finally {
            draftAnswer = ""
            isGenerating = false
        }
    }

    fun sendUserQuery(
        query: String,
        domainId: String,
        domainTitle: String,
        currentTopic: String,
        goal: String,
        skillLevel: DifficultyLevel,
        learningStyle: String
    ) {
        if (generation?.isActive == true || isGenerating) return
        generation = CoroutineScope(Dispatchers.Main).launch {
            sendUserQueryAsync(query, domainId, domainTitle, currentTopic, goal, skillLevel, learningStyle)
        }
    }

    fun retryLast() {
        if (generation?.isActive == true || isGenerating) return
        val learner = SampleData.learnerProfile
        val user = conversationMessages.lastOrNull { !it.isTutor } ?: return
        val request = lastRequest ?: TutorRequest(
            user.content, user.mode, learner.activeDomainId, learner.activeDomainTitle,
            SampleData.currentModule.title, learner.activeGoal, learner.skillLevel, learner.preferredLearningStyle
        )
        lastUserMessageId = user.id
        activeMode = request.mode
        val previous = conversationMessages.lastOrNull()
        if (previous != null && (previous.isError || previous.structuredPayload?.executionModelLabel == AURORA_RUNTIME)) {
            conversationMessages.removeAt(conversationMessages.lastIndex)
            notifyPersistenceChanged()
        }
        generation = CoroutineScope(Dispatchers.Main).launch {
            sendUserQueryAsync(
                request.query, request.domainId, request.domainTitle, request.topicTitle,
                request.goal, request.skillLevel, request.learningStyle, retry = true
            )
        }
    }

    fun clearSession(learnerName: String, domainTitle: String, currentTopic: String) {
        stopGeneration()
        lastRequest = null
        lastUserMessageId = null
        conversationMessages.clear()
        val welcome = welcomeMessage(learnerName, domainTitle, currentTopic, activeMode)
        conversationMessages.add(welcome)
        notifyPersistenceChanged()
    }

    fun notifyPersistenceChanged() {
        if (AdaptPersistence.isInitialized) {
            AdaptPersistence.scheduleSaveTutor(activeMode, conversationMessages.toList())
        }
    }

    fun getSuggestedPrompts(domainId: String, currentTopic: String): List<String> {
        return engine.getSuggestedPrompts(domainId, currentTopic, activeMode)
    }
}
