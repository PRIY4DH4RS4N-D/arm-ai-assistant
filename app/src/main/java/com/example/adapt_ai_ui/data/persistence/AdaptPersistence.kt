package com.example.adapt_ai_ui.data.persistence

import android.content.Context
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.DailyGoal
import com.example.adapt_ai_ui.data.DomainCurriculumRecord
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearnerProfile
import com.example.adapt_ai_ui.data.LearningModule
import com.example.adapt_ai_ui.data.MilestoneStatus
import com.example.adapt_ai_ui.data.MistakeRecord
import com.example.adapt_ai_ui.data.OnboardingPreferences
import com.example.adapt_ai_ui.data.PathwayNode
import com.example.adapt_ai_ui.data.PracticeQuestionItem
import com.example.adapt_ai_ui.data.QuestionSubmission
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TopicCompetency
import com.example.adapt_ai_ui.data.TutorConversationMessage
import com.example.adapt_ai_ui.data.TutorMode
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.TutorResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

// ============================================================================
// Repository Interfaces (Clean Abstractions)
// ============================================================================

interface LearnerProfileRepository {
    val profileFlow: StateFlow<LearnerProfile>
    suspend fun saveProfile(profile: LearnerProfile)
    suspend fun saveOnboardingPreferences(prefs: OnboardingPreferences)
    suspend fun getOnboardingPreferences(): OnboardingPreferences?
}

interface CurriculumRepository {
    val currentModuleFlow: StateFlow<LearningModule>
    val pathwayNodesFlow: StateFlow<List<PathwayNode>>
    val todayGoalFlow: StateFlow<DailyGoal>
    val isLessonCompletedFlow: StateFlow<Boolean>
    val bookmarksFlow: StateFlow<Set<String>>
    suspend fun saveCurriculum(
        module: LearningModule,
        nodes: List<PathwayNode>,
        goal: DailyGoal,
        lessonCompleted: Boolean
    )
    suspend fun completeLesson()
    suspend fun saveBookmarks(bookmarks: Set<String>)
}

interface PracticeRepository {
    val submissionsFlow: StateFlow<List<QuestionSubmission>>
    val topicStatsFlow: StateFlow<Map<String, TopicCompetency>>
    val mistakeNotebookFlow: StateFlow<List<MistakeRecord>>
    suspend fun savePracticeState(
        submissions: List<QuestionSubmission>,
        topicStats: Map<String, TopicCompetency>,
        mistakes: List<MistakeRecord>
    )
    suspend fun recordSubmission(submission: QuestionSubmission)
}

interface TutorHistoryRepository {
    val activeModeFlow: StateFlow<TutorMode>
    val messagesFlow: StateFlow<List<TutorConversationMessage>>
    suspend fun saveTutorSession(mode: TutorMode, messages: List<TutorConversationMessage>)
    suspend fun clearTutorSession()
}

// ============================================================================
// Concrete Implementation backed by StorageBackend & Coroutines
// ============================================================================

class AdaptPersistenceRepository(
    private val storage: StorageBackend,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LearnerProfileRepository, CurriculumRepository, PracticeRepository, TutorHistoryRepository {

    // Storage keys
    companion object {
        const val KEY_LEARNER_PROFILE = "learner_profile"
        const val KEY_ONBOARDING_PREFS = "onboarding_prefs"
        const val KEY_CURRICULUM = "curriculum_state"
        const val KEY_PRACTICE = "practice_state"
        const val KEY_TUTOR_SESSION = "tutor_session"
        const val KEY_BOOKMARKS = "lesson_bookmarks"
    }

    // StateFlows
    private val _profileFlow = MutableStateFlow(loadInitialProfile())
    override val profileFlow: StateFlow<LearnerProfile> = _profileFlow.asStateFlow()

    private val _currentModuleFlow = MutableStateFlow(loadInitialModule())
    override val currentModuleFlow: StateFlow<LearningModule> = _currentModuleFlow.asStateFlow()

    private val _pathwayNodesFlow = MutableStateFlow(loadInitialPathwayNodes())
    override val pathwayNodesFlow: StateFlow<List<PathwayNode>> = _pathwayNodesFlow.asStateFlow()

    private val _todayGoalFlow = MutableStateFlow(loadInitialTodayGoal())
    override val todayGoalFlow: StateFlow<DailyGoal> = _todayGoalFlow.asStateFlow()

    private val _isLessonCompletedFlow = MutableStateFlow(loadInitialLessonCompleted())
    override val isLessonCompletedFlow: StateFlow<Boolean> = _isLessonCompletedFlow.asStateFlow()

    private val _bookmarksFlow = MutableStateFlow(loadInitialBookmarks())
    override val bookmarksFlow: StateFlow<Set<String>> = _bookmarksFlow.asStateFlow()

    private val _submissionsFlow = MutableStateFlow(loadInitialSubmissions())
    override val submissionsFlow: StateFlow<List<QuestionSubmission>> = _submissionsFlow.asStateFlow()

    private val _topicStatsFlow = MutableStateFlow(loadInitialTopicStats())
    override val topicStatsFlow: StateFlow<Map<String, TopicCompetency>> = _topicStatsFlow.asStateFlow()

    private val _mistakeNotebookFlow = MutableStateFlow(loadInitialMistakes())
    override val mistakeNotebookFlow: StateFlow<List<MistakeRecord>> = _mistakeNotebookFlow.asStateFlow()

    private val _activeModeFlow = MutableStateFlow(loadInitialTutorMode())
    override val activeModeFlow: StateFlow<TutorMode> = _activeModeFlow.asStateFlow()

    private val _messagesFlow = MutableStateFlow(loadInitialTutorMessages())
    override val messagesFlow: StateFlow<List<TutorConversationMessage>> = _messagesFlow.asStateFlow()

    // ------------------------------------------------------------------------
    // Learner Profile Implementation
    // ------------------------------------------------------------------------

    override suspend fun saveProfile(profile: LearnerProfile) {
        withContext(dispatcher) {
            _profileFlow.value = profile
            val json = AdaptJsonSerializers.serializeLearnerProfile(profile)
            storage.write(KEY_LEARNER_PROFILE, json)
        }
    }

    override suspend fun saveOnboardingPreferences(prefs: OnboardingPreferences) {
        withContext(dispatcher) {
            val json = AdaptJsonSerializers.serializeOnboardingPreferences(prefs)
            storage.write(KEY_ONBOARDING_PREFS, json)
        }
    }

    override suspend fun getOnboardingPreferences(): OnboardingPreferences? = withContext(dispatcher) {
        val raw = storage.read(KEY_ONBOARDING_PREFS) ?: return@withContext null
        AdaptJsonSerializers.deserializeOnboardingPreferences(raw)
    }

    // ------------------------------------------------------------------------
    // Curriculum Implementation
    // ------------------------------------------------------------------------

    override suspend fun saveCurriculum(
        module: LearningModule,
        nodes: List<PathwayNode>,
        goal: DailyGoal,
        lessonCompleted: Boolean
    ) {
        withContext(dispatcher) {
            _currentModuleFlow.value = module
            _pathwayNodesFlow.value = nodes
            _todayGoalFlow.value = goal
            _isLessonCompletedFlow.value = lessonCompleted

            val json = AdaptJsonSerializers.serializeCurriculumState(module, nodes, goal, lessonCompleted)
            storage.write(KEY_CURRICULUM, json)
            val domainId = _profileFlow.value.activeDomainId
            if (domainId.isNotBlank()) {
                storage.write("${KEY_CURRICULUM}_$domainId", json)
            }
        }
    }

    fun getRawCurriculumForDomain(domainId: String): String? = storage.read("${KEY_CURRICULUM}_$domainId")

    override suspend fun completeLesson() {
        withContext(dispatcher) {
            val wasCompleted = _isLessonCompletedFlow.value
            val updatedModule = _currentModuleFlow.value.copy(currentStep = 4)
            val currentGoal = _todayGoalFlow.value
            val updatedGoal = if (!wasCompleted) {
                val newCurrent = (currentGoal.current + 1).coerceAtMost(currentGoal.target)
                currentGoal.copy(current = newCurrent, isCompleted = newCurrent >= currentGoal.target)
            } else {
                currentGoal
            }
            val updatedNodes = _pathwayNodesFlow.value.map { node ->
                when (node.id) {
                    "node_2" -> node.copy(status = MilestoneStatus.COMPLETED)
                    "node_3" -> node.copy(status = MilestoneStatus.IN_PROGRESS)
                    else -> node
                }
            }
            // Preserve authentic learnerProfile without fabricated dailyMinutesSpent or masteryScorePercent
            saveCurriculum(updatedModule, updatedNodes, updatedGoal, true)
        }
    }

    override suspend fun saveBookmarks(bookmarks: Set<String>) {
        withContext(dispatcher) {
            _bookmarksFlow.value = bookmarks
            val json = AdaptJsonSerializers.serializeBookmarks(bookmarks)
            storage.write(KEY_BOOKMARKS, json)
        }
    }

    // ------------------------------------------------------------------------
    // Practice Implementation
    // ------------------------------------------------------------------------

    override suspend fun savePracticeState(
        submissions: List<QuestionSubmission>,
        topicStats: Map<String, TopicCompetency>,
        mistakes: List<MistakeRecord>
    ) {
        withContext(dispatcher) {
            _submissionsFlow.value = submissions
            _topicStatsFlow.value = topicStats
            _mistakeNotebookFlow.value = mistakes

            val json = AdaptJsonSerializers.serializePracticeState(submissions, topicStats, mistakes)
            storage.write(KEY_PRACTICE, json)
        }
    }

    override suspend fun recordSubmission(submission: QuestionSubmission) {
        withContext(dispatcher) {
            val currentSubmissions = _submissionsFlow.value.toMutableList().apply { add(submission) }
            savePracticeState(currentSubmissions, _topicStatsFlow.value, _mistakeNotebookFlow.value)
        }
    }

    // ------------------------------------------------------------------------
    // Tutor Implementation
    // ------------------------------------------------------------------------

    override suspend fun saveTutorSession(mode: TutorMode, messages: List<TutorConversationMessage>) {
        withContext(dispatcher) {
            _activeModeFlow.value = mode
            _messagesFlow.value = messages

            val json = AdaptJsonSerializers.serializeTutorSession(mode, messages)
            storage.write(KEY_TUTOR_SESSION, json)
        }
    }

    override suspend fun clearTutorSession() {
        withContext(dispatcher) {
            storage.delete(KEY_TUTOR_SESSION)
            _messagesFlow.value = emptyList()
        }
    }

    // ------------------------------------------------------------------------
    // Initial Loaders with safe defaults & corruption recovery
    // ------------------------------------------------------------------------

    private fun loadInitialProfile(): LearnerProfile {
        val raw = storage.read(KEY_LEARNER_PROFILE) ?: return defaultProfile()
        return AdaptJsonSerializers.deserializeLearnerProfile(raw) ?: defaultProfile()
    }

    private fun loadInitialModule(): LearningModule {
        val raw = storage.read(KEY_CURRICULUM) ?: return defaultModule()
        return AdaptJsonSerializers.deserializeCurriculumModule(raw) ?: defaultModule()
    }

    private fun loadInitialPathwayNodes(): List<PathwayNode> {
        val raw = storage.read(KEY_CURRICULUM) ?: return defaultPathwayNodes()
        return AdaptJsonSerializers.deserializeCurriculumNodes(raw) ?: defaultPathwayNodes()
    }

    private fun loadInitialTodayGoal(): DailyGoal {
        val raw = storage.read(KEY_CURRICULUM) ?: return defaultTodayGoal()
        return AdaptJsonSerializers.deserializeCurriculumGoal(raw) ?: defaultTodayGoal()
    }

    private fun loadInitialLessonCompleted(): Boolean {
        val raw = storage.read(KEY_CURRICULUM) ?: return false
        return AdaptJsonSerializers.deserializeCurriculumLessonCompleted(raw)
    }

    private fun loadInitialBookmarks(): Set<String> {
        val raw = storage.read(KEY_BOOKMARKS) ?: return emptySet()
        return AdaptJsonSerializers.deserializeBookmarks(raw) ?: emptySet()
    }

    private fun loadInitialSubmissions(): List<QuestionSubmission> {
        val raw = storage.read(KEY_PRACTICE) ?: return emptyList()
        return AdaptJsonSerializers.deserializeSubmissions(raw) ?: emptyList()
    }

    private fun loadInitialTopicStats(): Map<String, TopicCompetency> {
        val raw = storage.read(KEY_PRACTICE) ?: return emptyMap()
        return AdaptJsonSerializers.deserializeTopicStats(raw) ?: emptyMap()
    }

    private fun loadInitialMistakes(): List<MistakeRecord> {
        val raw = storage.read(KEY_PRACTICE) ?: return emptyList()
        return AdaptJsonSerializers.deserializeMistakeNotebook(raw) ?: emptyList()
    }

    private fun loadInitialTutorMode(): TutorMode {
        val raw = storage.read(KEY_TUTOR_SESSION) ?: return TutorMode.SOCRATIC
        return AdaptJsonSerializers.deserializeTutorMode(raw) ?: TutorMode.SOCRATIC
    }

    private fun loadInitialTutorMessages(): List<TutorConversationMessage> {
        val raw = storage.read(KEY_TUTOR_SESSION) ?: return emptyList()
        return AdaptJsonSerializers.deserializeTutorMessages(raw) ?: emptyList()
    }

    fun loadInitialOnboarding(): OnboardingPreferences? {
        val raw = storage.read(KEY_ONBOARDING_PREFS) ?: return null
        return AdaptJsonSerializers.deserializeOnboardingPreferences(raw)
    }

    // Defaults
    private fun defaultProfile() = LearnerProfile(
        name = "learner",
        greeting = "Good morning",
        streakDays = 0,
        dailyMinutesSpent = 0,
        dailyMinutesGoal = 35,
        weeklyHoursCompleted = 0f,
        masteryScorePercent = 0,
        activeDomainId = "ai_ml",
        activeDomainTitle = "Artificial Intelligence & ML",
        activeGoal = "Build Real Edge AI Projects",
        skillLevel = DifficultyLevel.INTERMEDIATE,
        preferredLearningStyle = "Hands-on Projects & Code"
    )

    private fun defaultModule() = LearningModule(
        id = "mod_quant_1",
        title = "INT8 Weight Quantization",
        summary = "Learn how converting 32-bit floating point weights into 8-bit integers accelerates edge neural networks while reducing memory bandwidth.",
        trackName = "Edge AI Systems",
        estimatedMinutes = 12,
        currentStep = 1,
        totalSteps = 4,
        difficulty = DifficultyLevel.INTERMEDIATE
    )

    private fun defaultPathwayNodes() = listOf(
        PathwayNode("node_1", 1, "MobileNet Architecture", "Model Compression", MilestoneStatus.COMPLETED, 15),
        PathwayNode("node_2", 2, "INT8 Weight Quantization", "Precision Scaling", MilestoneStatus.IN_PROGRESS, 12),
        PathwayNode("node_3", 3, "Tensor Memory Layouts", "Cache Optimization", MilestoneStatus.LOCKED, 20),
        PathwayNode("node_4", 4, "Edge Pipeline Benchmark", "Profiling & Latency", MilestoneStatus.LOCKED, 25)
    )

    private fun defaultTodayGoal() = DailyGoal(
        id = "goal_today",
        title = "Quantization Foundations",
        targetDescription = "Complete 2 practical lessons & 1 concept quiz",
        current = 0,
        target = 3,
        isCompleted = false
    )


}

// ============================================================================
// Specialized JSON Serializers / Deserializers
// ============================================================================

object AdaptJsonSerializers {

    // --- Learner Profile ---
    fun serializeLearnerProfile(p: LearnerProfile): String {
        val root = AdaptJson.buildObject {
            put("name", p.name)
            put("greeting", p.greeting)
            put("streakDays", p.streakDays)
            put("dailyMinutesSpent", p.dailyMinutesSpent)
            put("dailyMinutesGoal", p.dailyMinutesGoal)
            put("weeklyHoursCompleted", p.weeklyHoursCompleted)
            put("masteryScorePercent", p.masteryScorePercent)
            put("activeDomainId", p.activeDomainId)
            put("activeDomainTitle", p.activeDomainTitle)
            put("activeGoal", p.activeGoal)
            put("skillLevel", p.skillLevel.name)
            put("preferredLearningStyle", p.preferredLearningStyle)
        }
        return AdaptJson.serialize(root)
    }

    fun deserializeLearnerProfile(json: String): LearnerProfile? {
        val obj = AdaptJson.parseObject(json) ?: return null
        return try {
            LearnerProfile(
                name = obj.getString("name", "learner"),
                greeting = obj.getString("greeting", "Good morning"),
                streakDays = obj.getInt("streakDays", 0),
                dailyMinutesSpent = obj.getInt("dailyMinutesSpent", 0),
                dailyMinutesGoal = obj.getInt("dailyMinutesGoal", 35),
                weeklyHoursCompleted = obj.getFloat("weeklyHoursCompleted", 0f),
                masteryScorePercent = obj.getInt("masteryScorePercent", 0),
                activeDomainId = obj.getString("activeDomainId", "ai_ml"),
                activeDomainTitle = obj.getString("activeDomainTitle", "Artificial Intelligence & ML"),
                activeGoal = obj.getString("activeGoal", "Build Real Edge AI Projects"),
                skillLevel = try { DifficultyLevel.valueOf(obj.getString("skillLevel", DifficultyLevel.INTERMEDIATE.name)) } catch (_: Exception) { DifficultyLevel.INTERMEDIATE },
                preferredLearningStyle = obj.getString("preferredLearningStyle", "Hands-on Projects & Code")
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- Onboarding Preferences ---
    fun serializeOnboardingPreferences(prefs: OnboardingPreferences): String {
        val root = AdaptJson.buildObject {
            put("domainId", prefs.domainId)
            put("domainTitle", prefs.domainTitle)
            put("goal", prefs.goal)
            put("skillLevel", prefs.skillLevel.name)
            put("dailyMinutes", prefs.dailyMinutes)
            putStringList("interests", prefs.interests)
            put("learningStyle", prefs.learningStyle)
            putStringList("customTopics", SampleData.customTopics)
        }
        return AdaptJson.serialize(root)
    }

    fun deserializeOnboardingPreferences(json: String): OnboardingPreferences? {
        val obj = AdaptJson.parseObject(json) ?: return null
        return try {
            val custom = obj.getStringList("customTopics")
            if (custom.isNotEmpty()) {
                SampleData.customTopics = custom
            }
            OnboardingPreferences(
                domainId = obj.getString("domainId", "ai_ml"),
                domainTitle = obj.getString("domainTitle", "Artificial Intelligence & ML"),
                goal = obj.getString("goal", "Build Real Edge AI Projects"),
                skillLevel = try { DifficultyLevel.valueOf(obj.getString("skillLevel", DifficultyLevel.INTERMEDIATE.name)) } catch (_: Exception) { DifficultyLevel.INTERMEDIATE },
                dailyMinutes = obj.getInt("dailyMinutes", 35),
                interests = obj.getStringList("interests"),
                learningStyle = obj.getString("learningStyle", "Hands-on Projects & Code")
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- Curriculum State ---
    fun serializeCurriculumState(
        module: LearningModule,
        nodes: List<PathwayNode>,
        goal: DailyGoal,
        lessonCompleted: Boolean
    ): String {
        val moduleObj = AdaptJson.buildObject {
            put("id", module.id)
            put("title", module.title)
            put("summary", module.summary)
            put("trackName", module.trackName)
            put("estimatedMinutes", module.estimatedMinutes)
            put("currentStep", module.currentStep)
            put("totalSteps", module.totalSteps)
            put("difficulty", module.difficulty.name)
        }

        val nodesArray = JsonElement.JsonArray(nodes.map { n ->
            AdaptJson.buildObject {
                put("id", n.id)
                put("index", n.index)
                put("title", n.title)
                put("track", n.track)
                put("status", n.status.name)
                put("estimatedMinutes", n.estimatedMinutes)
            }
        })

        val goalObj = AdaptJson.buildObject {
            put("id", goal.id)
            put("title", goal.title)
            put("targetDescription", goal.targetDescription)
            put("current", goal.current)
            put("target", goal.target)
            put("isCompleted", goal.isCompleted)
        }

        val root = AdaptJson.buildObject {
            put("module", moduleObj)
            put("nodes", nodesArray)
            put("goal", goalObj)
            put("isLessonCompleted", lessonCompleted)
        }

        return AdaptJson.serialize(root)
    }

    fun deserializeCurriculumModule(json: String): LearningModule? {
        val root = AdaptJson.parseObject(json) ?: return null
        val m = root.getObject("module") ?: return null
        return try {
            LearningModule(
                id = m.getString("id"),
                title = m.getString("title"),
                summary = m.getString("summary"),
                trackName = m.getString("trackName"),
                estimatedMinutes = m.getInt("estimatedMinutes"),
                currentStep = m.getInt("currentStep"),
                totalSteps = m.getInt("totalSteps"),
                difficulty = try { DifficultyLevel.valueOf(m.getString("difficulty", DifficultyLevel.INTERMEDIATE.name)) } catch (_: Exception) { DifficultyLevel.INTERMEDIATE }
            )
        } catch (_: Exception) {
            null
        }
    }

    fun deserializeCurriculumNodes(json: String): List<PathwayNode>? {
        val root = AdaptJson.parseObject(json) ?: return null
        val arr = root.getArray("nodes") ?: return null
        return try {
            arr.elements.mapNotNull { el ->
                val o = el as? JsonElement.JsonObject ?: return@mapNotNull null
                PathwayNode(
                    id = o.getString("id"),
                    index = o.getInt("index"),
                    title = o.getString("title"),
                    track = o.getString("track"),
                    status = try { MilestoneStatus.valueOf(o.getString("status", MilestoneStatus.LOCKED.name)) } catch (_: Exception) { MilestoneStatus.LOCKED },
                    estimatedMinutes = o.getInt("estimatedMinutes")
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    fun deserializeCurriculumGoal(json: String): DailyGoal? {
        val root = AdaptJson.parseObject(json) ?: return null
        val g = root.getObject("goal") ?: return null
        return try {
            DailyGoal(
                id = g.getString("id"),
                title = g.getString("title"),
                targetDescription = g.getString("targetDescription"),
                current = g.getInt("current"),
                target = g.getInt("target"),
                isCompleted = g.getBoolean("isCompleted")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun deserializeCurriculumLessonCompleted(json: String): Boolean {
        val root = AdaptJson.parseObject(json) ?: return false
        return root.getBoolean("isLessonCompleted", false)
    }

    // --- Bookmarks State ---
    fun serializeBookmarks(bookmarks: Set<String>): String {
        val root = AdaptJson.buildObject {
            putStringList("bookmarks", bookmarks.toList())
        }
        return AdaptJson.serialize(root)
    }

    fun deserializeBookmarks(json: String): Set<String>? {
        val root = AdaptJson.parseObject(json) ?: return null
        return root.getStringList("bookmarks").toSet()
    }

    // --- Practice & Revision State ---
    fun serializePracticeState(
        submissions: List<QuestionSubmission>,
        topicStats: Map<String, TopicCompetency>,
        mistakes: List<MistakeRecord>
    ): String {
        val subArray = JsonElement.JsonArray(submissions.map { s ->
            AdaptJson.buildObject {
                put("questionId", s.questionId)
                put("selectedOptionIndex", s.selectedOptionIndex)
                put("isCorrect", s.isCorrect)
                put("timestamp", s.timestamp)
            }
        })

        val statsArray = JsonElement.JsonArray(topicStats.values.map { c ->
            AdaptJson.buildObject {
                put("topic", c.topic)
                put("correctAttempts", c.correctAttempts)
                put("totalAttempts", c.totalAttempts)
            }
        })

        val mistakeArray = JsonElement.JsonArray(mistakes.map { m ->
            val q = m.question
            val qObj = AdaptJson.buildObject {
                put("id", q.id)
                put("topicTag", q.topicTag)
                put("question", q.question)
                putStringList("options", q.options)
                put("correctIndex", q.correctIndex)
                put("explanation", q.explanation)
                put("difficulty", q.difficulty.name)
            }
            AdaptJson.buildObject {
                put("question", qObj)
                put("lastSelectedOptionIndex", m.lastSelectedOptionIndex)
                put("mistakeCount", m.mistakeCount)
                put("nextRevisionDays", m.nextRevisionDays)
                put("isResolved", m.isResolved)
            }
        })

        val root = AdaptJson.buildObject {
            put("submissions", subArray)
            put("topicStats", statsArray)
            put("mistakes", mistakeArray)
        }

        return AdaptJson.serialize(root)
    }

    fun deserializeSubmissions(json: String): List<QuestionSubmission>? {
        val root = AdaptJson.parseObject(json) ?: return null
        val arr = root.getArray("submissions") ?: return null
        return arr.elements.mapNotNull { el ->
            val o = el as? JsonElement.JsonObject ?: return@mapNotNull null
            QuestionSubmission(
                questionId = o.getString("questionId"),
                selectedOptionIndex = o.getInt("selectedOptionIndex"),
                isCorrect = o.getBoolean("isCorrect"),
                timestamp = o.getLong("timestamp")
            )
        }
    }

    fun deserializeTopicStats(json: String): Map<String, TopicCompetency>? {
        val root = AdaptJson.parseObject(json) ?: return null
        val arr = root.getArray("topicStats") ?: return null
        val map = mutableMapOf<String, TopicCompetency>()
        for (el in arr.elements) {
            val o = el as? JsonElement.JsonObject ?: continue
            val topic = o.getString("topic")
            map[topic] = TopicCompetency(
                topic = topic,
                correctAttempts = o.getInt("correctAttempts"),
                totalAttempts = o.getInt("totalAttempts")
            )
        }
        return map
    }

    fun deserializeMistakeNotebook(json: String): List<MistakeRecord>? {
        val root = AdaptJson.parseObject(json) ?: return null
        val arr = root.getArray("mistakes") ?: return null
        return arr.elements.mapNotNull { el ->
            val o = el as? JsonElement.JsonObject ?: return@mapNotNull null
            val qObj = o.getObject("question") ?: return@mapNotNull null
            val q = PracticeQuestionItem(
                id = qObj.getString("id"),
                topicTag = qObj.getString("topicTag"),
                question = qObj.getString("question"),
                options = qObj.getStringList("options"),
                correctIndex = qObj.getInt("correctIndex"),
                explanation = qObj.getString("explanation"),
                difficulty = try { DifficultyLevel.valueOf(qObj.getString("difficulty", DifficultyLevel.INTERMEDIATE.name)) } catch (_: Exception) { DifficultyLevel.INTERMEDIATE }
            )
            MistakeRecord(
                question = q,
                lastSelectedOptionIndex = o.getInt("lastSelectedOptionIndex"),
                mistakeCount = o.getInt("mistakeCount"),
                nextRevisionDays = o.getInt("nextRevisionDays", 1),
                isResolved = o.getBoolean("isResolved", false)
            )
        }
    }

    // --- Tutor Session State ---
    fun serializeTutorSession(mode: TutorMode, messages: List<TutorConversationMessage>): String {
        val msgArray = JsonElement.JsonArray(messages.map { msg ->
            val payload = msg.structuredPayload
            val payloadObj = if (payload != null) {
                AdaptJson.buildObject {
                    put("text", payload.text)
                    put("conceptExplanation", payload.conceptExplanation)
                    putStringList("steps", payload.steps)
                    put("exampleOrAnalogy", payload.exampleOrAnalogy)
                    put("codeSnippet", payload.codeSnippet)
                    put("codeLanguage", payload.codeLanguage)
                    put("formula", payload.formula)
                    putStringList("keyTakeaways", payload.keyTakeaways)
                    putStringList("followUpPrompts", payload.followUpPrompts)
                    put("practiceQuestion", payload.practiceQuestion)
                    put("executionModelLabel", payload.executionModelLabel)
                    put("inferenceTimeMs", payload.inferenceTimeMs)
                    payload.sourceLabel?.let { put("sourceLabel", it) }
                }
            } else null

            AdaptJson.buildObject {
                put("id", msg.id)
                put("sender", msg.sender)
                put("isTutor", msg.isTutor)
                put("content", msg.content)
                put("mode", msg.mode.name)
                put("timestamp", msg.timestamp)
                put("isError", msg.isError)
                put("payload", payloadObj)
            }
        })

        val root = AdaptJson.buildObject {
            put("mode", mode.name)
            put("messages", msgArray)
        }
        return AdaptJson.serialize(root)
    }

    fun deserializeTutorMode(json: String): TutorMode? {
        val root = AdaptJson.parseObject(json) ?: return null
        val modeStr = root.getString("mode", TutorMode.SOCRATIC.name)
        return try {
            TutorMode.valueOf(modeStr)
        } catch (_: Exception) {
            TutorMode.SOCRATIC
        }
    }

    fun deserializeTutorMessages(json: String): List<TutorConversationMessage>? {
        val root = AdaptJson.parseObject(json) ?: return null
        val arr = root.getArray("messages") ?: return null
        return arr.elements.mapNotNull { el ->
            val o = el as? JsonElement.JsonObject ?: return@mapNotNull null
            val payloadObj = o.getObject("payload")
            val payload = if (payloadObj != null) {
                TutorResponse(
                    text = payloadObj.getString("text"),
                    conceptExplanation = payloadObj.getString("conceptExplanation").ifBlank { null },
                    steps = payloadObj.getStringList("steps"),
                    exampleOrAnalogy = payloadObj.getString("exampleOrAnalogy").ifBlank { null },
                    codeSnippet = payloadObj.getString("codeSnippet").ifBlank { null },
                    codeLanguage = payloadObj.getString("codeLanguage").ifBlank { null },
                    formula = payloadObj.getString("formula").ifBlank { null },
                    keyTakeaways = payloadObj.getStringList("keyTakeaways"),
                    followUpPrompts = payloadObj.getStringList("followUpPrompts"),
                    practiceQuestion = payloadObj.getString("practiceQuestion").ifBlank { null },
                    executionModelLabel = payloadObj.getString("executionModelLabel", "Prototype Engine (Simulated On-Device Runtime)"),
                    inferenceTimeMs = payloadObj.getLong("inferenceTimeMs", 380L),
                    sourceLabel = payloadObj.getString("sourceLabel", "").ifBlank { null }
                )
            } else null

            TutorConversationMessage(
                id = o.getString("id"),
                sender = o.getString("sender"),
                isTutor = o.getBoolean("isTutor"),
                content = o.getString("content"),
                mode = try { TutorMode.valueOf(o.getString("mode", TutorMode.SOCRATIC.name)) } catch (_: Exception) { TutorMode.SOCRATIC },
                structuredPayload = payload,
                timestamp = o.getLong("timestamp"),
                isError = o.getBoolean("isError")
            )
        }
    }
}

// ============================================================================
// Managed Asynchronous Write Coordinator & Durability Engine
// ============================================================================

/**
 * Managed asynchronous write coordinator.
 * Guarantees that:
 * 1. Writes for the same storage key execute sequentially in strict FIFO order, preventing .tmp race conditions.
 * 2. Newer queued state cannot be overwritten by an older save (monotonic sequence tracking per key).
 * 3. Writes for different keys execute concurrently across Dispatchers.IO without contention.
 * 4. Suspendable [flush] and [flushBlocking] enable lifecycle-aware synchronization (e.g. Activity onStop).
 * 5. Exceptions during write or serialization are logged and contained without crashing UI or background scopes.
 */
class PersistenceWriteCoordinator(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val coordinatorScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val inFlightJobs = ConcurrentHashMap.newKeySet<Job>()
    private val keyTails = ConcurrentHashMap<String, Job>()
    private val keyVersions = ConcurrentHashMap<String, AtomicLong>()
    private val lastCommittedVersions = ConcurrentHashMap<String, Long>()

    private fun getVersionSeq(key: String): AtomicLong =
        keyVersions.computeIfAbsent(key) { AtomicLong(0L) }

    fun scheduleWrite(key: String, action: suspend () -> Unit): Job {
        val version = getVersionSeq(key).incrementAndGet()
        var createdJob: Job? = null

        keyTails.compute(key) { _, previousJob ->
            val job = coordinatorScope.launch(start = CoroutineStart.LAZY) {
                // Wait for any prior write to this key to finish before starting
                previousJob?.join()

                val lastCommitted = lastCommittedVersions[key] ?: 0L
                if (version < lastCommitted) {
                    // An equal or newer state has already been committed to disk; skip older version
                    return@launch
                }

                try {
                    action()
                    lastCommittedVersions[key] = version
                } catch (e: Exception) {
                    System.err.println("PersistenceWriteCoordinator: error writing key '$key': ${e.message}")
                }
            }
            createdJob = job
            job
        }

        val job = createdJob!!
        inFlightJobs.add(job)
        job.invokeOnCompletion {
            keyTails.remove(key, job)
            inFlightJobs.remove(job)
        }
        job.start()
        return job
    }

    suspend fun flush() {
        while (true) {
            // Avoid a size-based collection copy while completion callbacks remove jobs.
            val jobs = buildList { inFlightJobs.forEach { add(it) } }
            if (jobs.isEmpty()) break
            jobs.joinAll()
        }
    }

    fun flushBlocking(timeoutMillis: Long = 2000L) {
        runBlocking {
            withTimeoutOrNull(timeoutMillis) {
                flush()
            }
        }
    }

    val pendingCount: Int
        get() = inFlightJobs.size
}

// ============================================================================
// Global Persistence Coordinator
// ============================================================================

object AdaptPersistence {
    private var repository: AdaptPersistenceRepository? = null
    val coordinator: PersistenceWriteCoordinator = PersistenceWriteCoordinator()

    val isInitialized: Boolean
        get() = repository != null

    fun init(context: Context): AdaptPersistenceRepository {
        if (repository == null) {
            val dir = File(context.applicationContext.filesDir, "adapt_ai_data")
            val backend = FileStorageBackend(dir)
            repository = AdaptPersistenceRepository(backend)
            syncToSingletons(repository!!)
        }
        return repository!!
    }

    fun initWithBackend(backend: StorageBackend): AdaptPersistenceRepository {
        val repo = AdaptPersistenceRepository(backend)
        repository = repo
        syncToSingletons(repo)
        return repo
    }

    fun attach(repo: AdaptPersistenceRepository, domains: Map<String, String>) {
        repository = repo
        syncToSingletons(repo, domains)
    }

    fun getRepository(): AdaptPersistenceRepository {
        return repository ?: error("AdaptPersistence not initialized. Call AdaptPersistence.init(context) first.")
    }

    fun scheduleSaveProfile(profile: LearnerProfile): Job {
        val repo = repository ?: return Job().apply { complete() }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_LEARNER_PROFILE) {
            repo.saveProfile(profile)
        }
    }

    fun scheduleSaveCurriculum(
        module: LearningModule,
        nodes: List<PathwayNode>,
        goal: DailyGoal,
        completed: Boolean
    ): Job {
        val repo = repository ?: return Job().apply { complete() }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_CURRICULUM) {
            repo.saveCurriculum(module, nodes, goal, completed)
        }
    }

    fun scheduleSaveOnboarding(
        prefs: OnboardingPreferences,
        profile: LearnerProfile,
        module: LearningModule,
        nodes: List<PathwayNode>,
        goal: DailyGoal,
        completed: Boolean
    ): Job {
        val repo = repository ?: return Job().apply { complete() }
        coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_ONBOARDING_PREFS) {
            repo.saveOnboardingPreferences(prefs)
        }
        coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_LEARNER_PROFILE) {
            repo.saveProfile(profile)
        }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_CURRICULUM) {
            repo.saveCurriculum(module, nodes, goal, completed)
        }
    }

    fun scheduleSavePractice(
        submissions: List<QuestionSubmission>,
        topicStats: Map<String, TopicCompetency>,
        mistakes: List<MistakeRecord>
    ): Job {
        val repo = repository ?: return Job().apply { complete() }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_PRACTICE) {
            repo.savePracticeState(submissions, topicStats, mistakes)
        }
    }

    fun scheduleSaveTutor(
        mode: TutorMode,
        messages: List<TutorConversationMessage>
    ): Job {
        val repo = repository ?: return Job().apply { complete() }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_TUTOR_SESSION) {
            repo.saveTutorSession(mode, messages)
        }
    }

    fun scheduleSaveBookmarks(bookmarks: Set<String>): Job {
        val repo = repository ?: return Job().apply { complete() }
        return coordinator.scheduleWrite(AdaptPersistenceRepository.KEY_BOOKMARKS) {
            repo.saveBookmarks(bookmarks)
        }
    }

    suspend fun flush() {
        coordinator.flush()
    }

    fun flushBlocking(timeoutMillis: Long = 2000L) {
        coordinator.flushBlocking(timeoutMillis)
    }

    /**
     * Synchronizes persisted states into UI singletons on application startup.
     */
    private fun syncToSingletons(repo: AdaptPersistenceRepository, domains: Map<String, String>? = null) {
        SampleData.domainProgressRecords.clear()
        SampleData.customTopics = emptyList()
        // Sync Learner Profile
        SampleData.learnerProfile = repo.profileFlow.value

        // Sync Curriculum & Bookmarks
        SampleData.currentModule = repo.currentModuleFlow.value
        SampleData.pathwayPreviewNodes = repo.pathwayNodesFlow.value
        SampleData.todayGoal = repo.todayGoalFlow.value
        SampleData.isCurrentLessonCompleted = repo.isLessonCompletedFlow.value
        SampleData.bookmarkedLessonIds = repo.bookmarksFlow.value

        // Sync Practice & Mistakes
        AdaptiveLearningEngine.sessionSubmissions.clear()
        AdaptiveLearningEngine.sessionSubmissions.addAll(repo.submissionsFlow.value)
        AdaptiveLearningEngine.topicStats = repo.topicStatsFlow.value
        AdaptiveLearningEngine.mistakeNotebook.clear()
        AdaptiveLearningEngine.mistakeNotebook.addAll(repo.mistakeNotebookFlow.value)

        // Sync Tutor
        TutorRepository.activeMode = repo.activeModeFlow.value
        TutorRepository.conversationMessages.clear()
        TutorRepository.conversationMessages.addAll(repo.messagesFlow.value)

        // Hydrate per-domain curriculum progress
        val activeDomain = repo.profileFlow.value.activeDomainId
        SampleData.domainProgressRecords[activeDomain] = DomainCurriculumRecord(
            domainId = activeDomain,
            currentModule = repo.currentModuleFlow.value,
            pathwayNodes = repo.pathwayNodesFlow.value,
            todayGoal = repo.todayGoalFlow.value,
            isLessonCompleted = repo.isLessonCompletedFlow.value
        )
        DomainRepository.domains.forEach { d ->
            if (d.id != activeDomain) {
                val rawDomainCurriculum = if (domains != null) domains[d.id] else repo.getRawCurriculumForDomain(d.id)
                if (rawDomainCurriculum != null) {
                    val m = AdaptJsonSerializers.deserializeCurriculumModule(rawDomainCurriculum)
                    val n = AdaptJsonSerializers.deserializeCurriculumNodes(rawDomainCurriculum)
                    val g = AdaptJsonSerializers.deserializeCurriculumGoal(rawDomainCurriculum)
                    val comp = AdaptJsonSerializers.deserializeCurriculumLessonCompleted(rawDomainCurriculum)
                    if (m != null && n != null && g != null) {
                        SampleData.domainProgressRecords[d.id] = DomainCurriculumRecord(
                            domainId = d.id,
                            currentModule = m,
                            pathwayNodes = n,
                            todayGoal = g,
                            isLessonCompleted = comp
                        )
                    }
                }
            }
        }

    }
}
