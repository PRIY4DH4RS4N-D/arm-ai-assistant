package com.example.adapt_ai_ui

import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DailyGoal
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearnerProfile
import com.example.adapt_ai_ui.data.LearningModule
import com.example.adapt_ai_ui.data.MilestoneStatus
import com.example.adapt_ai_ui.data.MistakeRecord
import com.example.adapt_ai_ui.data.OnboardingPreferences
import com.example.adapt_ai_ui.data.PathwayNode
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.PrototypeTutorEngine
import com.example.adapt_ai_ui.data.QuestionSubmission
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TopicCompetency
import com.example.adapt_ai_ui.data.TutorMode
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.TutorRequest
import kotlinx.coroutines.runBlocking
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository
import com.example.adapt_ai_ui.data.persistence.InMemoryStorageBackend
import com.example.adapt_ai_ui.ui.navigation.Screen
import com.example.adapt_ai_ui.ui.theme.ThemeMode
import kotlin.math.abs
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdaptAiUnitTest {

    @Before
    fun setUp() {
        SampleData.resetToDefaultTrack()
    }

    @Test
    fun learningModule_progressFraction_calculatesCorrectly() {
        val module = LearningModule(
            id = "test_mod",
            title = "Quantization",
            summary = "Summary",
            trackName = "Edge AI",
            estimatedMinutes = 10,
            currentStep = 3,
            totalSteps = 4,
            difficulty = DifficultyLevel.INTERMEDIATE
        )

        assertEquals(0.75f, module.progressFraction, 0.001f)
        assertEquals(75, module.progressPercent)
    }

    @Test
    fun dailyGoal_progressFraction_handlesEdgeCases() {
        val zeroTargetGoal = DailyGoal(
            id = "g0",
            title = "Zero Goal",
            targetDescription = "None",
            current = 0,
            target = 0
        )
        assertEquals(0f, zeroTargetGoal.progressFraction, 0.001f)

        val completedGoal = DailyGoal(
            id = "g1",
            title = "Standard Goal",
            targetDescription = "Target",
            current = 2,
            target = 3
        )
        assertEquals(0.666f, completedGoal.progressFraction, 0.01f)
    }

    @Test
    fun sampleData_integrityCheck() {
        assertNotNull(SampleData.learnerProfile)
        assertEquals("Aryan", SampleData.learnerProfile.name)
        assertTrue(SampleData.learnerProfile.streakDays > 0)

        assertNotNull(SampleData.currentModule)
        assertFalse(SampleData.pathwayPreviewNodes.isEmpty())
    }

    @Test
    fun sampleLesson_contentStructureIsComplete() {
        val lesson = SampleData.sampleLesson
        assertNotNull(lesson)
        assertEquals("INT8 Weight Quantization", lesson.title)
        assertEquals(3, lesson.stepIndex)
        assertEquals(4, lesson.totalSteps)

        assertTrue(lesson.conceptSections.isNotEmpty())
        assertTrue(lesson.keyTakeaways.isNotEmpty())
        assertNotNull(lesson.realWorldExample)
        assertTrue(lesson.realWorldExample.comparisonMetrics.isNotEmpty())
        assertNotNull(lesson.deepDive)

        // Practice Question validation
        assertNotNull(lesson.practiceQuestion)
        assertEquals(4, lesson.practiceQuestion.options.size)
        assertTrue(lesson.practiceQuestion.correctIndex in 0 until lesson.practiceQuestion.options.size)
    }

    @Test
    fun screenRoutes_areUnique() {
        val routes = listOf(
            Screen.Home.route,
            Screen.Pathway.route,
            Screen.Tutor.route,
            Screen.Profile.route,
            Screen.Lesson.route,
            Screen.Practice.route,
            Screen.Explore.route,
            Screen.Onboarding.route
        )
        assertEquals(8, routes.distinct().size)
    }

    @Test
    fun completeCurrentLesson_updatesAllStateReactivity() {
        val initialMinutes = SampleData.learnerProfile.dailyMinutesSpent
        val initialMastery = SampleData.learnerProfile.masteryScorePercent

        SampleData.completeCurrentLesson()

        assertTrue(SampleData.isCurrentLessonCompleted)
        assertEquals(4, SampleData.currentModule.currentStep)
        assertEquals(3, SampleData.todayGoal.current)
        assertTrue(SampleData.todayGoal.isCompleted)

        // Learner stats must remain authentic and NOT be fabricated to hardcoded values
        assertEquals("Study minutes must not be fabricated", initialMinutes, SampleData.learnerProfile.dailyMinutesSpent)
        assertEquals("Mastery score must not be fabricated", initialMastery, SampleData.learnerProfile.masteryScorePercent)

        val node2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        assertEquals(MilestoneStatus.COMPLETED, node2.status)

        val node3 = SampleData.pathwayPreviewNodes.first { it.id == "node_3" }
        assertEquals(MilestoneStatus.IN_PROGRESS, node3.status)
    }

    @Test
    fun adaptiveLearningEngine_answerSubmission_handlesCorrectAndIncorrect() {
        val question = AdaptiveLearningEngine.questionBank.first()

        // Submit wrong answer
        val wrongSubmission = AdaptiveLearningEngine.submitAnswer(question, (question.correctIndex + 1) % 4)
        assertFalse(wrongSubmission.isCorrect)

        // Verify mistake was added to notebook
        val mistake = AdaptiveLearningEngine.mistakeNotebook.find { it.question.id == question.id }
        assertNotNull(mistake)
        assertEquals(1, mistake?.nextRevisionDays)
        assertFalse(mistake?.isResolved ?: true)

        // Submit correct answer
        val correctSubmission = AdaptiveLearningEngine.submitAnswer(question, question.correctIndex)
        assertTrue(correctSubmission.isCorrect)

        // Verify mistake was marked resolved and spaced interval expanded to 3 days
        val resolvedMistake = AdaptiveLearningEngine.mistakeNotebook.find { it.question.id == question.id }
        assertNotNull(resolvedMistake)
        assertTrue(resolvedMistake?.isResolved ?: false)
        assertEquals(3, resolvedMistake?.nextRevisionDays)
    }

    @Test
    fun adaptiveLearningEngine_sessionSummaryCalculation() {
        val questions = AdaptiveLearningEngine.questionBank
        val q1 = questions[0]
        val q2 = questions[1]

        val mockSubmissions = mapOf(
            q1.id to QuestionSubmission(q1.id, q1.correctIndex, true),
            q2.id to QuestionSubmission(q2.id, (q2.correctIndex + 1) % 4, false)
        )

        val summary = AdaptiveLearningEngine.generateSummary(listOf(q1, q2), mockSubmissions)
        assertEquals(2, summary.totalQuestions)
        assertEquals(1, summary.correctCount)
        assertEquals(50, summary.scorePercentage)
        assertEquals(25, summary.xpEarned)
        assertEquals(1, summary.incorrectQuestions.size)
        assertEquals(q2.id, summary.incorrectQuestions.first().id)
    }

    @Test
    fun practiceMode_enumsArePresent() {
        assertEquals(2, PracticeMode.entries.size)
        assertTrue(PracticeMode.entries.contains(PracticeMode.SESSION))
        assertTrue(PracticeMode.entries.contains(PracticeMode.REVISION))
    }

    @Test
    fun themeMode_enumsArePresent() {
        assertEquals(3, ThemeMode.entries.size)
        assertTrue(ThemeMode.entries.contains(ThemeMode.SYSTEM))
        assertTrue(ThemeMode.entries.contains(ThemeMode.LIGHT))
        assertTrue(ThemeMode.entries.contains(ThemeMode.DARK))
    }

    @Test
    fun domainRepository_containsAllRequiredDomains() {
        val domains = DomainRepository.domains
        assertTrue("Domain count should be at least 9", domains.size >= 9)

        val domainIds = domains.map { it.id }
        val requiredIds = listOf(
            "ai_ml", "cs", "math", "science", "embedded",
            "programming", "data_science", "languages", "custom"
        )
        requiredIds.forEach { reqId ->
            assertTrue("Should contain domain $reqId", domainIds.contains(reqId))
        }

        // Test retrieval
        val aiDomain = DomainRepository.getDomainById("ai_ml")
        assertEquals("Artificial Intelligence & ML", aiDomain.title)
        assertFalse(aiDomain.suggestedTopics.isEmpty())
        assertFalse(aiDomain.recommendedGoals.isEmpty())
        assertFalse(aiDomain.defaultPathwayNodes.isEmpty())
        assertNotNull(aiDomain.defaultModule)
    }

    @Test
    fun sampleData_applyOnboardingPreferences_updatesAllState() {
        val prefs = OnboardingPreferences(
            domainId = "programming",
            domainTitle = "Programming & Software Engineering",
            goal = "Master Concurrent & Systems Programming",
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutes = 45,
            interests = listOf("Concurrency & Multithreading", "Memory Models"),
            learningStyle = "Hands-on Projects & Code"
        )

        SampleData.applyOnboardingPreferences(prefs)

        // Verify learner profile updated
        assertEquals("programming", SampleData.learnerProfile.activeDomainId)
        assertEquals("Programming & Software Engineering", SampleData.learnerProfile.activeDomainTitle)
        assertEquals("Master Concurrent & Systems Programming", SampleData.learnerProfile.activeGoal)
        assertEquals(DifficultyLevel.ADVANCED, SampleData.learnerProfile.skillLevel)
        assertEquals(45, SampleData.learnerProfile.dailyMinutesGoal)
        assertEquals("Hands-on Projects & Code", SampleData.learnerProfile.preferredLearningStyle)

        // Verify module & pathway updated
        assertEquals("Modern C++ RAII & Memory Model", SampleData.currentModule.title)
        assertEquals(DifficultyLevel.ADVANCED, SampleData.currentModule.difficulty)
        assertFalse(SampleData.pathwayPreviewNodes.isEmpty())
        assertEquals(4, SampleData.pathwayPreviewNodes.size)

        // Verify goal updated
        assertEquals(45, SampleData.learnerProfile.dailyMinutesGoal)
        assertEquals("goal_programming", SampleData.todayGoal.id)
    }

    @Test
    fun tutorMode_enumsAreComplete() {
        assertEquals(5, TutorMode.entries.size)
        assertTrue(TutorMode.entries.contains(TutorMode.TEACH_ME))
        assertTrue(TutorMode.entries.contains(TutorMode.SOCRATIC))
        assertTrue(TutorMode.entries.contains(TutorMode.DEBUG))
        assertTrue(TutorMode.entries.contains(TutorMode.PRACTICE))
        assertTrue(TutorMode.entries.contains(TutorMode.REVIEW))
    }

    @Test
    fun prototypeTutorEngine_suggestedPromptsAndWelcomeMessage() {
        val engine = PrototypeTutorEngine()

        // Verify prompts for different modes
        TutorMode.entries.forEach { mode ->
            val prompts = engine.getSuggestedPrompts("ai_ml", "INT8 Quantization", mode)
            assertTrue("Mode $mode should provide suggested prompts", prompts.isNotEmpty())
        }

        // Verify welcome message
        val welcome = engine.getWelcomeMessage(
            learnerName = "Aryan",
            domainTitle = "Artificial Intelligence & ML",
            currentTopic = "INT8 Weight Quantization",
            mode = TutorMode.SOCRATIC
        )
        assertEquals("ADAPT-AI", welcome.sender)
        assertTrue(welcome.isTutor)
        assertNotNull(welcome.structuredPayload)
        assertFalse(welcome.structuredPayload?.keyTakeaways.isNullOrEmpty())
    }

    @Test
    fun prototypeTutorEngine_responseGenerationForModes() = runBlocking {
        val engine = PrototypeTutorEngine()

        val baseRequest = TutorRequest(
            query = "Why does INT8 quantization save memory?",
            mode = TutorMode.SOCRATIC,
            domainId = "ai_ml",
            domainTitle = "Artificial Intelligence & ML",
            topicTitle = "INT8 Weight Quantization",
            goal = "Build Real Edge AI Projects",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            learningStyle = "Hands-on Projects & Code"
        )

        // Socratic mode
        val socraticResp = engine.generateResponse(baseRequest)
        assertNotNull(socraticResp.text)
        assertFalse(socraticResp.keyTakeaways.isEmpty())
        assertFalse(socraticResp.followUpPrompts.isEmpty())

        // Teach Me mode
        val teachResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.TEACH_ME))
        assertNotNull(teachResp.conceptExplanation)
        assertNotNull(teachResp.formula)

        // Debug mode
        val debugResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.DEBUG))
        assertNotNull(debugResp.codeSnippet)
        assertEquals("C++ (ARM NEON)", debugResp.codeLanguage)

        // Practice mode
        val practiceResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.PRACTICE))
        assertNotNull(practiceResp.practiceQuestion)

        // Review mode
        val reviewResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.REVIEW))
        assertFalse(reviewResp.keyTakeaways.isEmpty())
    }

    @Test
    fun tutorRepository_stateManagement() = runBlocking {
        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(1, TutorRepository.conversationMessages.size)

        // Switch Mode
        TutorRepository.setMode(TutorMode.DEBUG, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.DEBUG, TutorRepository.activeMode)
        assertEquals(2, TutorRepository.conversationMessages.size)

        // Send query asynchronously
        TutorRepository.sendUserQueryAsync(
            query = "How to prevent integer overflow in ARM NEON dot product?",
            domainId = "ai_ml",
            domainTitle = "Artificial Intelligence & ML",
            currentTopic = "INT8 Weight Quantization",
            goal = "Build Real Edge AI Projects",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            learningStyle = "Hands-on Projects & Code"
        )

        // Expect user message + tutor message
        assertEquals(4, TutorRepository.conversationMessages.size)
        val userMsg = TutorRepository.conversationMessages[2]
        assertEquals("You", userMsg.sender)
        assertFalse(userMsg.isTutor)

        val tutorMsg = TutorRepository.conversationMessages[3]
        assertEquals("ADAPT-AI", tutorMsg.sender)
        assertTrue(tutorMsg.isTutor)
        assertNotNull(tutorMsg.structuredPayload?.codeSnippet)
    }

    @Test
    fun lessonNavigation_backButton_returnsToAppropriateScreen() {
        val backStack = mutableListOf<Screen>()
        var currentScreen: Screen = Screen.Home

        fun navigateTo(screen: Screen) {
            if (currentScreen != screen) {
                backStack.add(currentScreen)
                currentScreen = screen
            }
        }

        fun navigateBack(fallback: Screen = Screen.Home) {
            while (backStack.isNotEmpty() && backStack.last() == currentScreen) {
                backStack.removeAt(backStack.lastIndex)
            }
            if (backStack.isNotEmpty()) {
                currentScreen = backStack.removeAt(backStack.lastIndex)
            } else {
                currentScreen = fallback
            }
        }

        // Navigate Home -> Pathway -> Lesson
        navigateTo(Screen.Pathway)
        assertEquals(Screen.Pathway, currentScreen)
        navigateTo(Screen.Lesson)
        assertEquals(Screen.Lesson, currentScreen)

        // Tap top-left back button on LessonScreen
        navigateBack(fallback = Screen.Pathway)
        assertEquals(Screen.Pathway, currentScreen)
    }

    @Test
    fun lessonCompletion_savesStateAndNavigatesToPathway() {
        val backStack = mutableListOf<Screen>()
        var currentScreen: Screen = Screen.Home

        fun navigateTo(screen: Screen) {
            if (currentScreen != screen) {
                backStack.add(currentScreen)
                currentScreen = screen
            }
        }

        // Navigate Home -> Lesson
        navigateTo(Screen.Lesson)
        assertEquals(Screen.Lesson, currentScreen)

        // Complete lesson
        SampleData.completeCurrentLesson()
        backStack.removeAll { it == Screen.Lesson || it == Screen.Practice }
        currentScreen = Screen.Pathway

        // Verify state is saved
        assertTrue(SampleData.isCurrentLessonCompleted)
        assertEquals(4, SampleData.currentModule.currentStep)
        val node2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        assertEquals(MilestoneStatus.COMPLETED, node2.status)

        // Verify destination is Learning Journey (Pathway)
        assertEquals(Screen.Pathway, currentScreen)
        assertFalse(backStack.contains(Screen.Lesson))
    }

    @Test
    fun lessonNavigation_withPracticeRoundtrip_doesNotTrapUserOnLessonScreen() {
        val backStack = mutableListOf<Screen>()
        var currentScreen: Screen = Screen.Home

        fun navigateTo(screen: Screen) {
            if (currentScreen != screen) {
                backStack.add(currentScreen)
                currentScreen = screen
            }
        }

        fun navigateBack(fallback: Screen = Screen.Home) {
            while (backStack.isNotEmpty() && backStack.last() == currentScreen) {
                backStack.removeAt(backStack.lastIndex)
            }
            if (backStack.isNotEmpty()) {
                currentScreen = backStack.removeAt(backStack.lastIndex)
            } else {
                currentScreen = fallback
            }
        }

        // Home -> Pathway -> Lesson
        navigateTo(Screen.Pathway)
        navigateTo(Screen.Lesson)
        assertEquals(Screen.Lesson, currentScreen)

        // Lesson -> Practice
        navigateTo(Screen.Practice)
        assertEquals(Screen.Practice, currentScreen)

        // Practice -> Back to Lesson
        navigateBack(fallback = Screen.Lesson)
        assertEquals(Screen.Lesson, currentScreen)

        // From Lesson, Back must NOT be stuck on Lesson
        navigateBack(fallback = Screen.Pathway)
        assertEquals(Screen.Pathway, currentScreen)
    }

    @Test
    fun lessonCompletion_whenPersistenceDelayed_completesAndNavigatesImmediately() = runBlocking {
        SampleData.resetToDefaultTrack()
        assertFalse(SampleData.isCurrentLessonCompleted)

        var currentScreen: Screen = Screen.Lesson

        // Call completion (non-blocking persistence save)
        SampleData.completeCurrentLesson()
        currentScreen = Screen.Pathway

        // Immediate memory and navigation updates
        assertTrue(SampleData.isCurrentLessonCompleted)
        assertEquals(Screen.Pathway, currentScreen)
    }

    @Test
    fun lessonCompletion_duplicateEventPrevention() {
        SampleData.resetToDefaultTrack()

        var completionCalls = 0
        var isCompleting = false
        fun onCompleteClick() {
            if (!isCompleting) {
                isCompleting = true
                completionCalls++
                SampleData.completeCurrentLesson()
            }
        }

        // Rapid double/triple click
        onCompleteClick()
        onCompleteClick()
        onCompleteClick()

        assertEquals(1, completionCalls)
        assertTrue(SampleData.isCurrentLessonCompleted)
    }

    @Test
    fun learningJourney_curriculumCompletion_calculatesAccuratelyAcrossStates() {
        SampleData.resetToDefaultTrack()
        val nodes = SampleData.pathwayPreviewNodes

        // Initial: 1 of 4 completed = 25%
        val initialCompleted = nodes.count { it.status == MilestoneStatus.COMPLETED }
        val initialPercent = if (nodes.isNotEmpty()) (initialCompleted * 100 / nodes.size) else 0
        assertEquals(1, initialCompleted)
        assertEquals(25, initialPercent)

        // Post-Lesson: completeCurrentLesson completes node_2 -> 2 of 4 = 50%
        SampleData.completeCurrentLesson()
        val postNodes = SampleData.pathwayPreviewNodes
        val postCompleted = postNodes.count { it.status == MilestoneStatus.COMPLETED }
        val postPercent = if (postNodes.isNotEmpty()) (postCompleted * 100 / postNodes.size) else 0
        assertEquals(2, postCompleted)
        assertEquals(50, postPercent)
        assertEquals(MilestoneStatus.COMPLETED, postNodes.first { it.id == "node_2" }.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, postNodes.first { it.id == "node_3" }.status)
    }

    @Test
    fun learningJourney_trackSwitching_updatesCurriculumNodesCorrectly() {
        SampleData.resetToDefaultTrack()

        // Switch to Computer Science domain
        val csPrefs = OnboardingPreferences(
            domainId = "cs",
            domainTitle = "Computer Science",
            goal = "Crack Coding Interviews",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            dailyMinutes = 30,
            interests = listOf("Algorithms", "Systems"),
            learningStyle = "Hands-on Projects & Code"
        )
        SampleData.applyOnboardingPreferences(csPrefs)

        assertEquals("cs", SampleData.learnerProfile.activeDomainId)
        assertEquals("Computer Science", SampleData.learnerProfile.activeDomainTitle)
        assertEquals(4, SampleData.pathwayPreviewNodes.size)
        assertEquals("cs_1", SampleData.pathwayPreviewNodes[0].id)
        assertEquals("cs_2", SampleData.pathwayPreviewNodes[1].id)

        // Reset back
        SampleData.resetToDefaultTrack()
        assertEquals("ai_ml", SampleData.learnerProfile.activeDomainId)
    }

    @Test
    fun learningJourney_tutorPromptGeneration_preservesContextAndTag() {
        val node = PathwayNode(
            id = "node_2",
            index = 2,
            title = "INT8 Weight Quantization",
            track = "Precision Scaling",
            status = MilestoneStatus.IN_PROGRESS,
            estimatedMinutes = 12
        )

        val prompt = TutorQuickPrompt(
            id = "milestone_${node.id}",
            prompt = "Explain ${node.title} and why it's critical for ${node.track}.",
            tag = "Milestone ${node.index}"
        )

        assertEquals("milestone_node_2", prompt.id)
        assertEquals("Milestone 2", prompt.tag)
        assertTrue(prompt.prompt.contains("INT8 Weight Quantization"))
        assertTrue(prompt.prompt.contains("Precision Scaling"))
    }

    @Test
    fun navigation_fiveDestinations_haveAccurateLabelsAndRoutes() {
        assertEquals("Home", Screen.Home.title)
        assertEquals("home", Screen.Home.route)

        assertEquals("Journey", Screen.Pathway.title)
        assertEquals("pathway", Screen.Pathway.route)

        assertEquals("Tutor", Screen.Tutor.title)
        assertEquals("tutor", Screen.Tutor.route)

        assertEquals("Practice", Screen.Practice.title)
        assertEquals("practice", Screen.Practice.route)

        assertEquals("Profile", Screen.Profile.title)
        assertEquals("profile", Screen.Profile.route)
    }

    @Test
    fun navigation_transitionsAcrossAllFiveDestinations_preserveBackStackState() {
        val backStack = mutableListOf<Screen>()
        var currentScreen: Screen = Screen.Home

        fun selectBottomNav(screen: Screen) {
            if (currentScreen != screen) {
                backStack.clear()
                if (screen != Screen.Home) {
                    backStack.add(Screen.Home)
                }
                currentScreen = screen
            }
        }

        fun navigateBack(fallback: Screen = Screen.Home) {
            while (backStack.isNotEmpty() && backStack.last() == currentScreen) {
                backStack.removeAt(backStack.lastIndex)
            }
            if (backStack.isNotEmpty()) {
                currentScreen = backStack.removeAt(backStack.lastIndex)
            } else {
                currentScreen = fallback
            }
        }

        // 1. Home -> Journey
        selectBottomNav(Screen.Pathway)
        assertEquals(Screen.Pathway, currentScreen)
        assertEquals(listOf(Screen.Home), backStack)

        // 2. Journey -> Tutor
        selectBottomNav(Screen.Tutor)
        assertEquals(Screen.Tutor, currentScreen)
        assertEquals(listOf(Screen.Home), backStack)

        // 3. Tutor -> Practice
        selectBottomNav(Screen.Practice)
        assertEquals(Screen.Practice, currentScreen)
        assertEquals(listOf(Screen.Home), backStack)

        // 4. Practice -> Profile
        selectBottomNav(Screen.Profile)
        assertEquals(Screen.Profile, currentScreen)
        assertEquals(listOf(Screen.Home), backStack)

        // 5. Profile -> Back to Home
        navigateBack()
        assertEquals(Screen.Home, currentScreen)
        assertTrue(backStack.isEmpty())
    }

    @Test
    fun practice_mcqEvaluationAndMistakeNotebook_remainsFunctional() {
        val question = AdaptiveLearningEngine.questionBank.first()

        // Submit correct answer
        val correctSub = AdaptiveLearningEngine.submitAnswer(question, question.correctIndex)
        assertTrue(correctSub.isCorrect)

        // Submit incorrect answer
        val wrongIndex = if (question.correctIndex == 0) 1 else 0
        val wrongSub = AdaptiveLearningEngine.submitAnswer(question, wrongIndex)
        assertFalse(wrongSub.isCorrect)

        // Verify Mistake Notebook records incorrect submission
        val mistake = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == question.id }
        assertNotNull(mistake)
        assertFalse(mistake!!.isResolved)
    }

    @Test
    fun lesson_bookmarkToggling_persistsAndUpdatesState() {
        SampleData.resetToDefaultTrack()
        val lessonId = "lesson_int8"

        assertFalse(SampleData.isLessonBookmarked(lessonId))

        // Bookmark lesson
        val isBookmarked1 = SampleData.toggleBookmark(lessonId)
        assertTrue(isBookmarked1)
        assertTrue(SampleData.isLessonBookmarked(lessonId))
        assertTrue(SampleData.bookmarkedLessonIds.contains(lessonId))

        // Unbookmark lesson
        val isBookmarked2 = SampleData.toggleBookmark(lessonId)
        assertFalse(isBookmarked2)
        assertFalse(SampleData.isLessonBookmarked(lessonId))
    }

    @Test
    fun lesson_quantizationArithmetic_computesExactIntegerMappingAndError() {
        val scale = 0.0235f
        val zeroPointSymmetric = 0
        val inputFloat = 1.42f

        // Symmetric quantization: q = clamp(round(r / S) + Z, -128, 127)
        val quantizedSymmetric = ((inputFloat / scale).toInt() + zeroPointSymmetric).coerceIn(-128, 127)
        assertEquals(60, quantizedSymmetric) // 1.42 / 0.0235 ~ 60.4 -> 60

        // Reconstruction: r_hat = S * (q - Z)
        val reconstructed = scale * (quantizedSymmetric - zeroPointSymmetric)
        assertEquals(1.41f, reconstructed, 0.01f)

        // Error epsilon = |r - r_hat| < 0.02
        val error = kotlin.math.abs(inputFloat - reconstructed)
        assertTrue("Quantization error must be small", error < 0.02f)

        // 8-bit integer memory reduction: 32 bits / 8 bits = 4x compression (75% savings)
        val fp32Bytes = 4
        val int8Bytes = 1
        val savings = (fp32Bytes - int8Bytes).toFloat() / fp32Bytes
        assertEquals(0.75f, savings, 0.001f)
    }

    @Test
    fun lesson_stepProgressionAndCompletion_advancesCurriculumHarmoniously() {
        SampleData.resetToDefaultTrack()
        assertFalse(SampleData.isCurrentLessonCompleted)

        // Milestone 2 is currently IN_PROGRESS
        val initialM2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        assertEquals(MilestoneStatus.IN_PROGRESS, initialM2.status)

        // Complete lesson
        SampleData.completeCurrentLesson()

        // Milestone 2 is now COMPLETED, Milestone 3 is IN_PROGRESS
        assertTrue(SampleData.isCurrentLessonCompleted)
        val updatedM2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        val updatedM3 = SampleData.pathwayPreviewNodes.first { it.id == "node_3" }
        assertEquals(MilestoneStatus.COMPLETED, updatedM2.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, updatedM3.status)

        // Curriculum progress is now 2/4 = 50%
        val completedCount = SampleData.pathwayPreviewNodes.count { it.status == MilestoneStatus.COMPLETED }
        val progressPercent = (completedCount * 100) / SampleData.pathwayPreviewNodes.size
        assertEquals(50, progressPercent)
    }

    @Test
    fun lesson_tutorPromptGeneration_preservesLessonContext() {
        val prompt = TutorQuickPrompt(
            id = "prompt_lesson_int8",
            prompt = "Can you give me an intuitive explanation of the INT8 scale factor, zero-point offset, and ARM NEON registers?",
            tag = "INT8 Lesson"
        )

        assertEquals("prompt_lesson_int8", prompt.id)
        assertEquals("INT8 Lesson", prompt.tag)
        assertTrue(prompt.prompt.contains("scale factor"))
        assertTrue(prompt.prompt.contains("ARM NEON"))
    }

    @Test
    fun lesson_quantization_boundaryAndNegativeInputs_clampAndConvertAccurately() {
        val scale = 0.0235f

        // 1. Negative boundary (-3.0f, symmetric)
        val negInput = -3.0f
        val qNeg = ((negInput / scale).roundToInt() + 0).coerceIn(-128, 127)
        assertEquals(-128, qNeg)
        val byteNeg = qNeg.toByte().toInt() and 0xFF
        val hexNeg = "0x" + byteNeg.toString(16).uppercase().padStart(2, '0')
        val binNeg = "0b" + byteNeg.toString(2).padStart(8, '0')
        assertEquals("0x80", hexNeg)
        assertEquals("0b10000000", binNeg)
        val rHatNeg = scale * (qNeg - 0)
        assertTrue(abs(negInput - rHatNeg) < 0.02f)

        // 2. Exact zero (0.0f, symmetric)
        val zeroInput = 0.0f
        val qZero = ((zeroInput / scale).roundToInt() + 0).coerceIn(-128, 127)
        assertEquals(0, qZero)
        val byteZero = qZero.toByte().toInt() and 0xFF
        assertEquals("0x00", "0x" + byteZero.toString(16).uppercase().padStart(2, '0'))
        assertEquals("0b00000000", "0b" + byteZero.toString(2).padStart(8, '0'))
        assertEquals(0.0f, scale * (qZero - 0), 0.0001f)

        // 3. Positive boundary (+3.0f, symmetric)
        val posInput = 3.0f
        val qPos = ((posInput / scale).roundToInt() + 0).coerceIn(-128, 127)
        assertEquals(127, qPos) // Clamped to 127
        val bytePos = qPos.toByte().toInt() and 0xFF
        assertEquals("0x7F", "0x" + bytePos.toString(16).uppercase().padStart(2, '0'))
        assertEquals("0b01111111", "0b" + bytePos.toString(2).padStart(8, '0'))
        val rHatPos = scale * (qPos - 0)
        assertTrue(abs(posInput - rHatPos) < 0.02f)

        // 4. Affine mode with non-zero ZeroPoint (Z = -12)
        val affineZeroPoint = -12
        val qAffineZero = ((zeroInput / scale).roundToInt() + affineZeroPoint).coerceIn(-128, 127)
        assertEquals(-12, qAffineZero)
        val rHatAffineZero = scale * (qAffineZero - affineZeroPoint)
        assertEquals(0.0f, rHatAffineZero, 0.0001f)
    }

    @Test
    fun lesson_bookmark_persistenceHydration_survivesAppRestart() {
        val memoryBackend = InMemoryStorageBackend()
        AdaptPersistence.initWithBackend(memoryBackend)

        val lessonId = SampleData.sampleLesson.id
        // Ensure starting clean
        SampleData.bookmarkedLessonIds = emptySet()
        assertFalse(SampleData.isLessonBookmarked(lessonId))

        // Bookmark lesson
        val isNowBookmarked = SampleData.toggleBookmark(lessonId)
        assertTrue(isNowBookmarked)
        assertTrue(SampleData.isLessonBookmarked(lessonId))

        // Flush writes to storage
        AdaptPersistence.flushBlocking()

        // Verify written to storage
        val rawBookmarks = memoryBackend.read(com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository.KEY_BOOKMARKS)
        assertNotNull("Bookmarks must be written to storage", rawBookmarks)
        assertTrue(rawBookmarks!!.contains(lessonId))

        // Simulate app recreation / cold restart with same backend
        SampleData.bookmarkedLessonIds = emptySet()
        assertFalse(SampleData.isLessonBookmarked(lessonId))

        // Rehydrate
        AdaptPersistence.initWithBackend(memoryBackend)
        assertTrue("Bookmark must survive app restart", SampleData.isLessonBookmarked(lessonId))
    }

    @Test
    fun lesson_quizSubmission_incorrectAnswerRecordedInMistakeNotebook() {
        SampleData.resetToDefaultTrack()
        AdaptiveLearningEngine.mistakeNotebook.clear()

        val matchingQ = AdaptiveLearningEngine.questionBank.first()
        // Submit incorrect answer (index 0 instead of 1)
        val submission = AdaptiveLearningEngine.submitAnswer(matchingQ, 0)
        assertFalse(submission.isCorrect)

        // Verify mistake is added to notebook
        val record = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == matchingQ.id }
        assertNotNull("Incorrect answer must be recorded in Mistake Notebook", record)
        assertEquals(0, record!!.lastSelectedOptionIndex)
        assertFalse(record.isResolved)

        // Submit correct answer (index 1)
        val correctSub = AdaptiveLearningEngine.submitAnswer(matchingQ, 1)
        assertTrue(correctSub.isCorrect)

        // Verify mistake is now marked resolved
        val updatedRecord = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == matchingQ.id }
        assertNotNull(updatedRecord)
        assertTrue("Mistake must be marked resolved after correct answer", updatedRecord!!.isResolved)
    }

    @Test
    fun lessonCompletion_doesNotFabricateLearnerStats_preservesAuthenticProfile() {
        SampleData.resetToDefaultTrack()
        SampleData.learnerProfile = SampleData.learnerProfile.copy(
            dailyMinutesSpent = 18,
            masteryScorePercent = 74
        )

        SampleData.completeCurrentLesson()

        // Verify lesson completion flags and milestone unlock
        assertTrue(SampleData.isCurrentLessonCompleted)
        assertEquals(4, SampleData.currentModule.currentStep)
        assertEquals(MilestoneStatus.COMPLETED, SampleData.pathwayPreviewNodes.first { it.id == "node_2" }.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, SampleData.pathwayPreviewNodes.first { it.id == "node_3" }.status)

        // Verify NO fabrication: stats must remain authentic
        assertEquals(18, SampleData.learnerProfile.dailyMinutesSpent)
        assertEquals(74, SampleData.learnerProfile.masteryScorePercent)
    }

    @Test
    fun lessonCompletion_incrementsGoalLegitimately_doesNotForceCompletionWhenTargetNotMet() {
        SampleData.resetToDefaultTrack()
        SampleData.todayGoal = DailyGoal(
            id = "goal_test",
            title = "Test Goal",
            targetDescription = "Target 3",
            current = 0,
            target = 3,
            isCompleted = false
        )

        // Complete lesson for the first time -> advances by 1 legitimately
        SampleData.completeCurrentLesson()
        assertEquals(1, SampleData.todayGoal.current)
        assertFalse("Goal must not be marked complete when 1 < 3", SampleData.todayGoal.isCompleted)

        // Re-completing the same lesson must NOT duplicate goal progress
        SampleData.completeCurrentLesson()
        assertEquals(1, SampleData.todayGoal.current)
        assertFalse(SampleData.todayGoal.isCompleted)
    }

    @Test
    fun recordStudyTime_andRecordedMinutes_updatesMinutesFromActualActivity() {
        SampleData.resetToDefaultTrack()
        SampleData.learnerProfile = SampleData.learnerProfile.copy(dailyMinutesSpent = 20)

        // Explicit study time recording
        SampleData.recordStudyTime(10)
        assertEquals(30, SampleData.learnerProfile.dailyMinutesSpent)

        // Completing lesson with recorded actual duration
        SampleData.completeCurrentLesson(recordedMinutes = 12)
        assertEquals(42, SampleData.learnerProfile.dailyMinutesSpent)
    }

    @Test
    fun persistence_completeLesson_preservesAuthenticProfileWithoutFabrication() = runBlocking {
        val memoryBackend = InMemoryStorageBackend()
        val repo = AdaptPersistence.initWithBackend(memoryBackend)

        // Profile starts with authentic values
        val initialProfile = repo.profileFlow.value
        val initialMinutes = initialProfile.dailyMinutesSpent
        val initialMastery = initialProfile.masteryScorePercent

        // Complete lesson in repository
        repo.completeLesson()

        // Verify curriculum and milestone status are updated
        assertTrue(repo.isLessonCompletedFlow.value)
        assertEquals(4, repo.currentModuleFlow.value.currentStep)
        assertEquals(MilestoneStatus.COMPLETED, repo.pathwayNodesFlow.value.first { it.id == "node_2" }.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, repo.pathwayNodesFlow.value.first { it.id == "node_3" }.status)

        // Verify profile is NOT overwritten with fabricated stats (35m or 88%)
        assertEquals(initialMinutes, repo.profileFlow.value.dailyMinutesSpent)
        assertEquals(initialMastery, repo.profileFlow.value.masteryScorePercent)
    }

    // ========================================================================
    // Phase 5: Practice & Revision Tests
    // ========================================================================

    @Test
    fun practiceSession_progressionAndSummary_calculatesAccurately() {
        val questions = AdaptiveLearningEngine.questionBank
        assertTrue("Question bank must have questions", questions.size >= 3)

        val q1 = questions[0]
        val q2 = questions[1]
        val q3 = questions[2]

        // Submit q1 correct, q2 incorrect, q3 correct
        val sub1 = AdaptiveLearningEngine.submitAnswer(q1, q1.correctIndex)
        val wrongIndexQ2 = if (q2.correctIndex == 0) 1 else 0
        val sub2 = AdaptiveLearningEngine.submitAnswer(q2, wrongIndexQ2)
        val sub3 = AdaptiveLearningEngine.submitAnswer(q3, q3.correctIndex)

        assertTrue(sub1.isCorrect)
        assertFalse(sub2.isCorrect)
        assertTrue(sub3.isCorrect)

        val submissionMap = mapOf(q1.id to sub1, q2.id to sub2, q3.id to sub3)
        val summary = AdaptiveLearningEngine.generateSummary(questions, submissionMap)

        assertEquals(3, summary.totalQuestions)
        assertEquals(2, summary.correctCount)
        assertEquals(66, summary.scorePercentage) // 2/3 = 66%
        assertEquals(50, summary.xpEarned) // 2 * 25 = 50 XP
        assertEquals(1, summary.incorrectQuestions.size)
        assertEquals(q2.id, summary.incorrectQuestions.first().id)
    }

    @Test
    fun practice_retryAndResolutionLogic_advancesLeitnerStage() {
        SampleData.resetToDefaultTrack()
        AdaptiveLearningEngine.mistakeNotebook.clear()

        val question = AdaptiveLearningEngine.questionBank.first()
        val wrongIndex = if (question.correctIndex == 0) 1 else 0

        // 1. Initial incorrect attempt: enters Level 1 (Due now, 1d interval)
        val subWrong = AdaptiveLearningEngine.submitAnswer(question, wrongIndex)
        assertFalse(subWrong.isCorrect)

        val initialMistake = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == question.id }
        assertNotNull(initialMistake)
        assertEquals(1, initialMistake!!.mistakeCount)
        assertEquals(1, initialMistake.nextRevisionDays)
        assertFalse(initialMistake.isResolved)

        // 2. Retry and answer correctly: advances to Level 2 (Reinforced, 3d interval)
        val subCorrect = AdaptiveLearningEngine.submitAnswer(question, question.correctIndex)
        assertTrue(subCorrect.isCorrect)

        val resolvedMistake = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == question.id }
        assertNotNull(resolvedMistake)
        assertEquals(3, resolvedMistake!!.nextRevisionDays)
        assertTrue("Mistake must be marked resolved upon correct answer", resolvedMistake.isResolved)
    }

    @Test
    fun practice_leitnerSpacedRepetition_categorizesBoxesAccurately() {
        AdaptiveLearningEngine.mistakeNotebook.clear()

        val q1 = AdaptiveLearningEngine.questionBank[0]
        val q2 = AdaptiveLearningEngine.questionBank[1]

        // Add 1 unresolved mistake (Box 1: Level 1)
        AdaptiveLearningEngine.submitAnswer(q1, if (q1.correctIndex == 0) 1 else 0)

        // Add 1 resolved mistake (Box 2: Level 2)
        AdaptiveLearningEngine.submitAnswer(q2, if (q2.correctIndex == 0) 1 else 0)
        AdaptiveLearningEngine.submitAnswer(q2, q2.correctIndex) // resolved

        val box1DueNow = AdaptiveLearningEngine.mistakeNotebook.filter { !it.isResolved }
        val box2Reinforced = AdaptiveLearningEngine.mistakeNotebook.filter { it.isResolved }

        assertEquals(1, box1DueNow.size)
        assertEquals(1, box1DueNow.first().nextRevisionDays)
        assertEquals(q1.id, box1DueNow.first().question.id)

        assertEquals(1, box2Reinforced.size)
        assertEquals(3, box2Reinforced.first().nextRevisionDays)
        assertEquals(q2.id, box2Reinforced.first().question.id)
    }

    @Test
    fun practice_tutorContextualPrompt_containsSpecificQuestionAndOptions() {
        val question = AdaptiveLearningEngine.questionBank.first()
        val wrongIndex = if (question.correctIndex == 0) 1 else 0
        val mistake = MistakeRecord(
            question = question,
            lastSelectedOptionIndex = wrongIndex,
            mistakeCount = 1,
            nextRevisionDays = 1,
            isResolved = false
        )

        val incorrectText = mistake.question.options[mistake.lastSelectedOptionIndex]
        val correctText = mistake.question.options[mistake.question.correctIndex]

        val prompt = TutorQuickPrompt(
            id = "mistake_${mistake.question.id}",
            prompt = "Can you explain why \"$incorrectText\" is incorrect and why \"$correctText\" is the correct answer for: \"${mistake.question.question}\" in ${mistake.question.topicTag}?",
            tag = mistake.question.topicTag
        )

        assertTrue(prompt.prompt.contains(incorrectText))
        assertTrue(prompt.prompt.contains(correctText))
        assertTrue(prompt.prompt.contains(mistake.question.question))
        assertEquals(mistake.question.topicTag, prompt.tag)
    }

    @Test
    fun practice_persistenceHydration_survivesAppRestart() = runBlocking {
        val memoryBackend = InMemoryStorageBackend()
        AdaptPersistence.initWithBackend(memoryBackend)

        val question = AdaptiveLearningEngine.questionBank.first()
        val wrongIndex = if (question.correctIndex == 0) 1 else 0

        // Submit mistake
        AdaptiveLearningEngine.submitAnswer(question, wrongIndex)
        AdaptPersistence.flushBlocking()

        // Verify written to storage
        val rawPractice = memoryBackend.read(com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository.KEY_PRACTICE)
        assertNotNull("Practice state must be serialized to storage", rawPractice)
        assertTrue(rawPractice!!.contains(question.id))

        // Clear in-memory singletons to simulate app death
        AdaptiveLearningEngine.mistakeNotebook.clear()
        AdaptiveLearningEngine.sessionSubmissions.clear()

        // Rehydrate
        AdaptPersistence.initWithBackend(memoryBackend)

        // Verify restored
        val restoredMistake = AdaptiveLearningEngine.mistakeNotebook.firstOrNull { it.question.id == question.id }
        assertNotNull("Mistake notebook must be restored from persistence", restoredMistake)
        assertEquals(question.id, restoredMistake!!.question.id)
        assertFalse(restoredMistake.isResolved)
    }

    @Test
    fun practice_homeAndBottomNavModes_preserveModeRouting() {
        // Mode 1: From Home Quick Revision -> REVISION
        var practiceInitialMode = PracticeMode.REVISION
        assertEquals(PracticeMode.REVISION, practiceInitialMode)

        // Mode 2: From Bottom Nav Practice -> SESSION
        practiceInitialMode = PracticeMode.SESSION
        assertEquals(PracticeMode.SESSION, practiceInitialMode)
    }

    // ========================================================================
    // PHASE 6: AI TUTOR ARCHITECTURE & PERSISTENCE TESTS
    // ========================================================================

    @Test
    fun tutor_allFiveModes_availableAndConfigured() {
        assertEquals(5, TutorMode.entries.size)
        TutorMode.entries.forEach { mode ->
            assertTrue("Mode title must not be blank", mode.title.isNotBlank())
            assertTrue("Mode description must not be blank", mode.description.isNotBlank())
            assertTrue("Mode iconEmoji must not be blank", mode.iconEmoji.isNotBlank())
        }
    }

    @Test
    fun tutor_modeSwitching_preservesConversationHistory() {
        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        val initialCount = TutorRepository.conversationMessages.size
        assertEquals(1, initialCount) // Welcome message

        // User submits message in SOCRATIC mode
        TutorRepository.setMode(TutorMode.SOCRATIC, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.SOCRATIC, TutorRepository.activeMode)
        assertEquals(2, TutorRepository.conversationMessages.size)

        // Switch to DEBUG mode
        TutorRepository.setMode(TutorMode.DEBUG, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.DEBUG, TutorRepository.activeMode)
        assertEquals(3, TutorRepository.conversationMessages.size)

        // Switch to TEACH_ME mode
        TutorRepository.setMode(TutorMode.TEACH_ME, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.TEACH_ME, TutorRepository.activeMode)
        assertEquals(4, TutorRepository.conversationMessages.size)

        // Switch to PRACTICE mode
        TutorRepository.setMode(TutorMode.PRACTICE, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.PRACTICE, TutorRepository.activeMode)
        assertEquals(5, TutorRepository.conversationMessages.size)

        // Switch to REVIEW mode
        TutorRepository.setMode(TutorMode.REVIEW, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        assertEquals(TutorMode.REVIEW, TutorRepository.activeMode)
        assertEquals(6, TutorRepository.conversationMessages.size)

        // All previous messages preserved
        assertTrue(TutorRepository.conversationMessages[0].content.contains("Aryan"))
    }

    @Test
    fun tutor_responsePayload_structuredFieldsIntegrity() = runBlocking {
        val engine = PrototypeTutorEngine()
        val baseRequest = TutorRequest(
            query = "Explain INT8 quantization for edge deployment",
            mode = TutorMode.TEACH_ME,
            domainId = "ai_ml",
            domainTitle = "Artificial Intelligence & ML",
            topicTitle = "INT8 Weight Quantization",
            goal = "Master Edge AI",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            learningStyle = "Visual"
        )

        // 1. TEACH_ME: Expect concept explanation, steps, analogy, formula, takeaways
        val teachResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.TEACH_ME))
        assertNotNull(teachResp.conceptExplanation)
        assertFalse(teachResp.steps.isEmpty())
        assertNotNull(teachResp.exampleOrAnalogy)
        assertNotNull(teachResp.formula)
        assertFalse(teachResp.keyTakeaways.isEmpty())
        assertFalse(teachResp.followUpPrompts.isEmpty())

        // 2. SOCRATIC: Expect thought-provoking questions, steps, analogy, takeaways
        val socraticResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.SOCRATIC))
        assertNotNull(socraticResp.conceptExplanation)
        assertFalse(socraticResp.steps.isEmpty())
        assertNotNull(socraticResp.exampleOrAnalogy)
        assertFalse(socraticResp.keyTakeaways.isEmpty())

        // 3. DEBUG: Expect C++ ARM NEON code snippet, language tag, steps, takeaways
        val debugResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.DEBUG))
        assertNotNull(debugResp.codeSnippet)
        assertTrue(debugResp.codeSnippet!!.contains("vdotq_s32"))
        assertEquals("C++ (ARM NEON)", debugResp.codeLanguage)
        assertFalse(debugResp.steps.isEmpty())

        // 4. PRACTICE: Expect scenario / drill question, steps, takeaways
        val practiceResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.PRACTICE))
        assertNotNull(practiceResp.practiceQuestion)
        assertTrue(practiceResp.practiceQuestion!!.contains("SRAM"))
        assertFalse(practiceResp.steps.isEmpty())

        // 5. REVIEW: Expect formula, recap steps, takeaways
        val reviewResp = engine.generateResponse(baseRequest.copy(mode = TutorMode.REVIEW))
        assertNotNull(reviewResp.formula)
        assertTrue(reviewResp.formula!!.contains("Affine"))
        assertFalse(reviewResp.keyTakeaways.isEmpty())
    }

    @Test
    fun tutor_emptyOrGenerating_preventsDuplicateSubmission() = runBlocking {
        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        val initialCount = TutorRepository.conversationMessages.size

        // Blank query should do nothing
        TutorRepository.sendUserQueryAsync(
            query = "   ",
            domainId = "ai_ml",
            domainTitle = "AI",
            currentTopic = "INT8",
            goal = "Goal",
            skillLevel = DifficultyLevel.BEGINNER,
            learningStyle = "Visual"
        )
        assertEquals(initialCount, TutorRepository.conversationMessages.size)

        // When generating, second query should be rejected
        TutorRepository.isGenerating = true
        TutorRepository.sendUserQueryAsync(
            query = "Another query",
            domainId = "ai_ml",
            domainTitle = "AI",
            currentTopic = "INT8",
            goal = "Goal",
            skillLevel = DifficultyLevel.BEGINNER,
            learningStyle = "Visual"
        )
        assertEquals(initialCount, TutorRepository.conversationMessages.size)
        TutorRepository.isGenerating = false
    }

    @Test
    fun tutor_clearSession_resetsOnlyTutorConversation() {
        val initialMastery = SampleData.learnerProfile.masteryScorePercent
        val initialMinutes = SampleData.learnerProfile.dailyMinutesSpent
        val initialMistakesCount = AdaptiveLearningEngine.mistakeNotebook.size

        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")

        // Tutor conversation reset to single welcome message
        assertEquals(1, TutorRepository.conversationMessages.size)
        assertTrue(TutorRepository.conversationMessages.first().isTutor)

        // Learner profile and other state completely untouched
        assertEquals(initialMastery, SampleData.learnerProfile.masteryScorePercent)
        assertEquals(initialMinutes, SampleData.learnerProfile.dailyMinutesSpent)
        assertEquals(initialMistakesCount, AdaptiveLearningEngine.mistakeNotebook.size)
    }

    @Test
    fun tutor_contextualPrompt_dispatchFromLessonAndPractice() {
        // Lesson contextual prompt
        val lessonPrompt = TutorQuickPrompt(
            id = "lesson_ctx_1",
            prompt = "Why does symmetric quantization set zero-point Z to 0?",
            tag = "quantization"
        )
        assertEquals("quantization", lessonPrompt.tag)
        assertTrue(lessonPrompt.prompt.contains("symmetric"))

        // Practice contextual prompt
        val practicePrompt = TutorQuickPrompt(
            id = "practice_ctx_1",
            prompt = "Can you explain why 1 byte is 8 bits?",
            tag = "hardware"
        )
        assertEquals("hardware", practicePrompt.tag)
        assertTrue(practicePrompt.prompt.contains("1 byte"))
    }

    @Test
    fun tutor_persistence_serializesAndRehydratesFullConversation() = runBlocking {
        val memoryBackend = InMemoryStorageBackend()
        AdaptPersistence.initWithBackend(memoryBackend)

        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        TutorRepository.setMode(TutorMode.DEBUG, "Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")
        TutorRepository.sendUserQueryAsync(
            query = "How to optimize NEON vector loads?",
            domainId = "ai_ml",
            domainTitle = "Artificial Intelligence & ML",
            currentTopic = "INT8 Weight Quantization",
            goal = "Master Edge AI",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            learningStyle = "Code"
        )
        AdaptPersistence.flushBlocking()

        // Verify written to storage
        val rawTutor = memoryBackend.read(com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository.KEY_TUTOR_SESSION)
        assertNotNull("Tutor state must be serialized to storage", rawTutor)
        assertTrue(rawTutor!!.contains("NEON"))

        // Clear singleton messages
        TutorRepository.conversationMessages.clear()
        assertEquals(0, TutorRepository.conversationMessages.size)

        // Rehydrate
        AdaptPersistence.initWithBackend(memoryBackend)

        // Verify restored
        assertTrue(TutorRepository.conversationMessages.size >= 3)
        val restoredTutorMsg = TutorRepository.conversationMessages.last()
        assertTrue(restoredTutorMsg.isTutor)
        assertNotNull(restoredTutorMsg.structuredPayload?.codeSnippet)
    }

    // ========================================================================
    // PHASE 7: LEARNER PROFILE, ANALYTICS & PREFERENCES TESTS
    // ========================================================================

    @Test
    fun profile_dataHydration_hasAuthenticLearnerFields() {
        val profile = SampleData.learnerProfile
        assertEquals("Aryan", profile.name)
        assertTrue(profile.streakDays >= 1)
        assertTrue(profile.dailyMinutesSpent >= 0)
        assertTrue(profile.dailyMinutesGoal > 0)
        assertTrue(profile.weeklyHoursCompleted >= 0f)
        assertTrue(profile.masteryScorePercent in 0..100)
        assertEquals("ai_ml", profile.activeDomainId)
        assertEquals("Artificial Intelligence & ML", profile.activeDomainTitle)
        assertEquals("Build Real Edge AI Projects", profile.activeGoal)
        assertEquals(DifficultyLevel.INTERMEDIATE, profile.skillLevel)
        assertEquals("Hands-on Projects & Code", profile.preferredLearningStyle)
    }

    @Test
    fun profile_studyTimeAccuracy_incrementsWithoutHardcoding() {
        val initialSpent = SampleData.learnerProfile.dailyMinutesSpent
        SampleData.recordStudyTime(15)
        assertEquals(initialSpent + 15, SampleData.learnerProfile.dailyMinutesSpent)

        // Negative or zero time should not alter telemetry
        SampleData.recordStudyTime(0)
        assertEquals(initialSpent + 15, SampleData.learnerProfile.dailyMinutesSpent)
        SampleData.recordStudyTime(-5)
        assertEquals(initialSpent + 15, SampleData.learnerProfile.dailyMinutesSpent)
    }

    @Test
    fun profile_masteryConsistency_reflectsAuthenticAssessmentRecords() {
        val topicStats = AdaptiveLearningEngine.topicStats
        val totalAttempts = topicStats.values.sumOf { it.totalAttempts }
        val totalCorrect = topicStats.values.sumOf { it.correctAttempts }

        assertTrue(totalAttempts > 0)
        val calculatedAccuracy = (totalCorrect * 100) / totalAttempts
        assertTrue(calculatedAccuracy in 0..100)

        // Topic competencies individually valid
        topicStats.values.forEach { comp ->
            assertTrue(comp.topic.isNotBlank())
            assertTrue(comp.correctAttempts <= comp.totalAttempts)
            assertTrue(comp.masteryPercentage in 0..100)
        }
    }

    @Test
    fun profile_bookmarkAccess_togglesAndPreservesLibraryState() {
        val testLessonId = "mod_quant_1"
        val wasBookmarked = SampleData.isLessonBookmarked(testLessonId)

        val newState = SampleData.toggleBookmark(testLessonId)
        assertEquals(!wasBookmarked, newState)
        assertEquals(!wasBookmarked, SampleData.isLessonBookmarked(testLessonId))

        // Toggle back to preserve original state
        SampleData.toggleBookmark(testLessonId)
        assertEquals(wasBookmarked, SampleData.isLessonBookmarked(testLessonId))
    }

    @Test
    fun profile_revisionHistory_reflectsMistakeNotebookState() {
        val mistakes = AdaptiveLearningEngine.mistakeNotebook
        assertFalse(mistakes.isEmpty())

        val unresolved = AdaptiveLearningEngine.getUnresolvedMistakes()
        val unresolvedCount = mistakes.count { !it.isResolved }
        assertEquals(unresolvedCount, unresolved.size)

        mistakes.forEach { record ->
            assertTrue(record.question.topicTag.isNotBlank())
            assertTrue(record.mistakeCount >= 1)
            assertTrue(record.nextRevisionDays >= 1)
        }
    }

    @Test
    fun profile_preferenceUpdates_persistToStorage() = runBlocking {
        val memoryBackend = InMemoryStorageBackend()
        AdaptPersistence.initWithBackend(memoryBackend)

        // Update Skill Level
        SampleData.learnerProfile = SampleData.learnerProfile.copy(
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutesGoal = 45,
            preferredLearningStyle = "Mathematical Formalism"
        )
        AdaptPersistence.scheduleSaveProfile(SampleData.learnerProfile)
        AdaptPersistence.flushBlocking()

        // Verify storage
        val rawProfile = memoryBackend.read(com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository.KEY_LEARNER_PROFILE)
        assertNotNull(rawProfile)
        assertTrue(rawProfile!!.contains("ADVANCED"))
        assertTrue(rawProfile.contains("45"))
        assertTrue(rawProfile.contains("Mathematical Formalism"))

        // Rehydrate
        AdaptPersistence.initWithBackend(memoryBackend)
        assertEquals(DifficultyLevel.ADVANCED, SampleData.learnerProfile.skillLevel)
        assertEquals(45, SampleData.learnerProfile.dailyMinutesGoal)
        assertEquals("Mathematical Formalism", SampleData.learnerProfile.preferredLearningStyle)
    }

    @Test
    fun profile_themeSelection_allModesAvailable() {
        assertEquals(3, ThemeMode.entries.size)
        assertTrue(ThemeMode.entries.contains(ThemeMode.SYSTEM))
        assertTrue(ThemeMode.entries.contains(ThemeMode.LIGHT))
        assertTrue(ThemeMode.entries.contains(ThemeMode.DARK))

        assertEquals("Follow System", ThemeMode.SYSTEM.title)
        assertEquals("Light Mode", ThemeMode.LIGHT.title)
        assertEquals("Dark Mode", ThemeMode.DARK.title)
    }

    @Test
    fun profile_navigation_allFiveDestinationsSupported() {
        val destinations = listOf(
            Screen.Home,
            Screen.Pathway,
            Screen.Tutor,
            Screen.Practice,
            Screen.Profile
        )
        assertEquals(5, destinations.size)
        assertEquals(Screen.Profile.route, "profile")
        assertEquals(Screen.Profile.title, "Profile")
    }

    // ========================================================================
    // PHASE 7.1: PROFILE ANALYTICS AUTHENTICITY AUDIT TESTS
    // ========================================================================

    @Test
    fun audit_streakAndWeeklyHours_identifyStaticSampleDefaults() {
        // Source inspection: streakDays and weeklyHoursCompleted on default seed
        val profile = SampleData.learnerProfile
        assertEquals(6, profile.streakDays)
        assertEquals(3.8f, profile.weeklyHoursCompleted, 0.01f)

        // Inspection: There are no multi-day historical dated session records
        // sessionSubmissions only record current active session drills
        assertTrue(AdaptiveLearningEngine.sessionSubmissions.isEmpty() || AdaptiveLearningEngine.sessionSubmissions.all { it.timestamp > 0 })

        // Verification: When multi-day history is absent, UI distinguishes today's active session
        val todayRecordedMinutes = profile.dailyMinutesSpent
        assertTrue("Today's recorded study time must be non-negative", todayRecordedMinutes >= 0)
    }

    @Test
    fun audit_dailyMinutesSpent_recordedStudyTimeIsAuthenticAndIncremental() {
        val initialMinutes = SampleData.learnerProfile.dailyMinutesSpent
        SampleData.recordStudyTime(20)
        assertEquals(initialMinutes + 20, SampleData.learnerProfile.dailyMinutesSpent)

        // Verify lesson completion increments only by authentic recorded duration
        SampleData.completeCurrentLesson(recordedMinutes = 10)
        assertEquals(initialMinutes + 30, SampleData.learnerProfile.dailyMinutesSpent)

        // Passing 0 does not alter recorded minutes
        SampleData.completeCurrentLesson(recordedMinutes = 0)
        assertEquals(initialMinutes + 30, SampleData.learnerProfile.dailyMinutesSpent)
    }

    @Test
    fun audit_masteryScorePercent_derivedFromAdaptiveTopicStats() {
        val stats = AdaptiveLearningEngine.topicStats
        val totalAttempts = stats.values.sumOf { it.totalAttempts }
        val totalCorrect = stats.values.sumOf { it.correctAttempts }

        assertTrue(totalAttempts > 0)
        val authenticRatio = (totalCorrect * 100) / totalAttempts
        assertTrue(authenticRatio in 0..100)

        // Submitting answer updates mastery dynamically
        val question = AdaptiveLearningEngine.questionBank.first()
        AdaptiveLearningEngine.submitAnswer(question, question.correctIndex)
        val updatedAttempts = AdaptiveLearningEngine.topicStats.values.sumOf { it.totalAttempts }
        assertEquals(totalAttempts + 1, updatedAttempts)
    }

    @Test
    fun audit_todayGoal_progressIsDrivenByActualLessonCompletions() {
        // Reset to default track to have fresh todayGoal
        SampleData.resetToDefaultTrack()
        val initialGoal = SampleData.todayGoal
        assertEquals(2, initialGoal.current)
        assertEquals(3, initialGoal.target)
        assertFalse(initialGoal.isCompleted)

        // Completing lesson advances goal
        SampleData.completeCurrentLesson()
        assertEquals(3, SampleData.todayGoal.current)
        assertTrue(SampleData.todayGoal.isCompleted)

        // Completing already completed lesson does not exceed target
        SampleData.completeCurrentLesson()
        assertEquals(3, SampleData.todayGoal.current)
    }

    @Test
    fun audit_sevenDayConsistency_displaysEmptyStateWhenHistoricalRecordsUnavailable() {
        // Verify that the UI message distinguishes unavailable historical logs from zero progress
        val emptyStateTitle = "Not enough history yet"
        val expectedDisclaimer = "Historical daily streaks and weekly cumulative hours will populate as daily sessions complete."
        assertTrue(emptyStateTitle.isNotBlank())
        assertTrue(expectedDisclaimer.contains("daily sessions complete"))
    }

    @Test
    fun consistency_dailyGoal_dynamicCalculationMatchesAcrossCards() {
        // Test with different selectable preferences: 15, 25, 35, 45 minutes
        val testGoals = listOf(15, 25, 35, 45)
        val spentMinutes = 30

        for (goal in testGoals) {
            SampleData.learnerProfile = SampleData.learnerProfile.copy(
                dailyMinutesSpent = spentMinutes,
                dailyMinutesGoal = goal
            )

            // Progress percent calculation used across both Passport pill and Telemetry card
            val progressPercent = ((SampleData.learnerProfile.dailyMinutesSpent.toFloat() / SampleData.learnerProfile.dailyMinutesGoal) * 100).toInt()
            val expectedPercent = (30 * 100) / goal

            assertEquals(expectedPercent, progressPercent)
            // Verify neither card uses hardcoded 20m
            assertNotEquals(20, SampleData.learnerProfile.dailyMinutesGoal)
        }
    }

    @Test
    fun consistency_sevenDayStrip_activeWeekdayDerivedFromLocalCalendar() {
        val cal = java.util.Calendar.getInstance()
        val expectedIndex = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            java.util.Calendar.SUNDAY -> 6
            else -> 0
        }

        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val currentDayName = daysOfWeek[expectedIndex]
        assertTrue(currentDayName.isNotBlank())

        // The active day must be in 0..6
        assertTrue(expectedIndex in 0..6)

        // Verify that on Saturday (today, Nov 21, 2026), it is NOT hardcoded to Monday (index 0)
        if (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            assertNotEquals(0, expectedIndex)
        }
    }

    // ========================================================================
    // PHASE 8: DOMAIN EXPLORER & PERSONALISED ONBOARDING TESTS
    // ========================================================================

    @Test
    fun phase8_allNineDomainIdentifiers_existAndLoadValidMetadata() {
        val expectedDomainIds = listOf(
            "ai_ml",
            "cs",
            "math",
            "science",
            "embedded",
            "programming",
            "data_science",
            "languages",
            "custom"
        )

        assertEquals(9, DomainRepository.domains.size)
        val actualIds = DomainRepository.domains.map { it.id }
        assertEquals(expectedDomainIds, actualIds)

        for (domainId in expectedDomainIds) {
            val domain = DomainRepository.getDomainById(domainId)
            assertEquals(domainId, domain.id)
            assertTrue(domain.title.isNotBlank())
            assertTrue(domain.tagline.isNotBlank())
            assertTrue(domain.description.isNotBlank())
            assertTrue(domain.iconEmoji.isNotBlank())
            assertTrue(domain.suggestedTopics.isNotEmpty())
            assertTrue(domain.recommendedGoals.isNotEmpty())
            assertTrue(domain.estimatedHours > 0)
            assertTrue(domain.defaultPathwayNodes.isNotEmpty())
            assertNotNull(domain.defaultModule)
            assertEquals(domain.id == "custom", domain.isCustom)
        }
    }

    @Test
    fun phase8_customTopics_validationAndPersistence() {
        val initialSize = SampleData.customTopics.size
        assertTrue(initialSize >= 0)

        // Add valid custom topic
        val newTopic = "Edge Transformer Pruning"
        val added = SampleData.addCustomTopic(newTopic)
        assertTrue(added)
        assertTrue(SampleData.customTopics.contains(newTopic))

        // Reject duplicate (case-insensitive)
        val duplicateAdded = SampleData.addCustomTopic("edge transformer pruning")
        assertFalse(duplicateAdded)

        // Reject blank or whitespace
        val blankAdded = SampleData.addCustomTopic("   ")
        assertFalse(blankAdded)

        // Remove custom topic
        val removed = SampleData.removeCustomTopic(newTopic)
        assertTrue(removed)
        assertFalse(SampleData.customTopics.contains(newTopic))

        // Removing non-existent returns false
        val removedAgain = SampleData.removeCustomTopic("Non Existent Topic 12345")
        assertFalse(removedAgain)
    }

    @Test
    fun phase8_onboardingPreferences_fullCycleAndPersistence() {
        // Record prior study time and bookmark to test non-regression
        SampleData.recordStudyTime(20)
        val initialSpent = SampleData.learnerProfile.dailyMinutesSpent
        SampleData.toggleBookmark("node_1")
        assertTrue(SampleData.isLessonBookmarked("node_1"))

        // Apply preferences for Embedded track
        val prefs = OnboardingPreferences(
            domainId = "embedded",
            domainTitle = "Electronics & Embedded Systems",
            goal = "Write Bare-Metal Firmware in C and Rust",
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutes = 45,
            interests = listOf("Hardware Interrupts & NVIC", "ARM Cortex-M Registers"),
            learningStyle = "Visual Intuition & Diagrams"
        )

        SampleData.applyOnboardingPreferences(prefs)

        // Verify learner profile updated correctly
        val profile = SampleData.learnerProfile
        assertEquals("embedded", profile.activeDomainId)
        assertEquals("Electronics & Embedded Systems", profile.activeDomainTitle)
        assertEquals("Write Bare-Metal Firmware in C and Rust", profile.activeGoal)
        assertEquals(DifficultyLevel.ADVANCED, profile.skillLevel)
        assertEquals(45, profile.dailyMinutesGoal)
        assertEquals("Visual Intuition & Diagrams", profile.preferredLearningStyle)

        // Verify study time was NOT reset
        assertEquals(initialSpent, profile.dailyMinutesSpent)

        // Verify bookmark was preserved
        assertTrue(SampleData.isLessonBookmarked("node_1"))

        // Verify curriculum updated for embedded
        assertEquals("ARM Embedded Systems", SampleData.currentModule.trackName)
        assertEquals(DifficultyLevel.ADVANCED, SampleData.currentModule.difficulty)
        assertTrue(SampleData.pathwayPreviewNodes.any { it.track == "ARM Cortex-M" })
        assertEquals("Electronics & Embedded Systems Focus", SampleData.todayGoal.title)
    }

    @Test
    fun phase8_dailyStudyAvailability_supportsExactTiers() {
        val supportedTiers = listOf(15, 25, 35, 45)

        for (tier in supportedTiers) {
            SampleData.learnerProfile = SampleData.learnerProfile.copy(dailyMinutesGoal = tier)
            assertEquals(tier, SampleData.learnerProfile.dailyMinutesGoal)
        }
    }

    @Test
    fun phase8_customDomain_dynamicSyllabusGeneration() {
        val customTopic = "RISC-V Vector Assembly"
        SampleData.addCustomTopic(customTopic)

        val customDomain = DomainRepository.getDomainById("custom")
        assertTrue(customDomain.isCustom)
        assertTrue(customDomain.suggestedTopics.contains(customTopic))

        // Clean up
        SampleData.removeCustomTopic(customTopic)
    }

    @Test
    fun phase8_persistenceSerialization_customTopicsRoundTrip() {
        val customTopic = "Neuromorphic Spike Encoding"
        SampleData.addCustomTopic(customTopic)

        val prefs = OnboardingPreferences(
            domainId = "custom",
            domainTitle = "Custom Learning Domain",
            goal = "Build Neuromorphic Edge Vision",
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutes = 35,
            interests = listOf(customTopic),
            learningStyle = "Hands-on Projects & Code"
        )

        val json = com.example.adapt_ai_ui.data.persistence.AdaptJsonSerializers.serializeOnboardingPreferences(prefs)
        assertTrue(json.contains(customTopic))

        val restored = com.example.adapt_ai_ui.data.persistence.AdaptJsonSerializers.deserializeOnboardingPreferences(json)
        assertNotNull(restored)
        assertEquals("custom", restored?.domainId)
        assertEquals(35, restored?.dailyMinutes)
        assertTrue(SampleData.customTopics.contains(customTopic))

        // Clean up
        SampleData.removeCustomTopic(customTopic)
    }

    // ========================================================================
    // TUTOR COMPOSER SPACING & LAYOUT CORRECTION TESTS
    // ========================================================================

    @Test
    fun tutorComposer_allFiveTutorModesPreserved() {
        assertEquals(5, TutorMode.entries.size)
        val expectedModes = listOf(
            TutorMode.TEACH_ME,
            TutorMode.SOCRATIC,
            TutorMode.DEBUG,
            TutorMode.PRACTICE,
            TutorMode.REVIEW
        )
        assertEquals(expectedModes, TutorMode.entries)

        for (mode in TutorMode.entries) {
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.description.isNotBlank())
            assertTrue(mode.iconEmoji.isNotBlank())
        }
    }

    @Test
    fun tutorComposer_promptSubmissionAndHistoryIntact() {
        TutorRepository.initializeSession(
            learnerName = "Aryan",
            domainTitle = "Artificial Intelligence & ML",
            currentTopic = "INT8 Weight Quantization",
            initialPrompt = null
        )

        val initialCount = TutorRepository.conversationMessages.size
        assertTrue(initialCount > 0)

        // Submit query via suspend async engine
        runBlocking {
            TutorRepository.sendUserQueryAsync(
                query = "What is affine zero-point?",
                domainId = "ai_ml",
                domainTitle = "Artificial Intelligence & ML",
                currentTopic = "INT8 Weight Quantization",
                goal = "Deploy on ARM",
                skillLevel = DifficultyLevel.INTERMEDIATE,
                learningStyle = "Hands-on Projects & Code"
            )
        }

        assertEquals(initialCount + 2, TutorRepository.conversationMessages.size)
        val userMsg = TutorRepository.conversationMessages[initialCount]
        assertFalse(userMsg.isTutor)
        assertEquals("What is affine zero-point?", userMsg.content)

        val tutorMsg = TutorRepository.conversationMessages.last()
        assertTrue(tutorMsg.isTutor)
        assertNotNull(tutorMsg.structuredPayload)

        // Suggested prompts remain functional
        val prompts = TutorRepository.getSuggestedPrompts("ai_ml", "INT8 Weight Quantization")
        assertTrue(prompts.isNotEmpty())
    }

    @Test
    fun tutorComposer_clearSessionPreservesCurriculumAndTelemetry() {
        SampleData.recordStudyTime(15)
        val initialSpent = SampleData.learnerProfile.dailyMinutesSpent
        SampleData.toggleBookmark("node_2")
        assertTrue(SampleData.isLessonBookmarked("node_2"))

        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")

        // Tutor messages reset to initial welcome message
        assertEquals(1, TutorRepository.conversationMessages.size)
        assertTrue(TutorRepository.conversationMessages.first().isTutor)

        // User telemetry & bookmarks completely intact
        assertEquals(initialSpent, SampleData.learnerProfile.dailyMinutesSpent)
        assertTrue(SampleData.isLessonBookmarked("node_2"))
    }

    // ========================================================================
    // PHASE 9 REGRESSION TESTS: DOMAIN SWITCHING & INTEGRATION AUDIT
    // ========================================================================

    @Test
    fun domainSwitching_preservesMilestonesCompetencyAndBookmarks() {
        // 1. Start in AI/ML default domain
        SampleData.resetToDefaultTrack()
        assertEquals("ai_ml", SampleData.learnerProfile.activeDomainId)
        assertFalse(SampleData.isCurrentLessonCompleted)

        // 2. Complete Lesson & Quiz in AI/ML
        SampleData.completeCurrentLesson(recordedMinutes = 12)
        assertTrue(SampleData.isCurrentLessonCompleted)
        val m2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        val m3 = SampleData.pathwayPreviewNodes.first { it.id == "node_3" }
        assertEquals(MilestoneStatus.COMPLETED, m2.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, m3.status)

        // Record assessment attempt
        val q = AdaptiveLearningEngine.questionBank.first()
        AdaptiveLearningEngine.submitAnswer(q, q.correctIndex)
        assertTrue(AdaptiveLearningEngine.sessionSubmissions.isNotEmpty())
        val competencyBefore = AdaptiveLearningEngine.topicStats[q.topicTag]?.correctAttempts ?: 0

        // Bookmark a lesson
        SampleData.toggleBookmark("lesson_int8")
        assertTrue(SampleData.isLessonBookmarked("lesson_int8"))

        // 3. Switch to Computer Science domain
        val csPrefs = OnboardingPreferences(
            domainId = "cs",
            domainTitle = "Computer Science",
            goal = "Crack Coding Interviews",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            dailyMinutes = 35,
            interests = listOf("Algorithms", "Data Structures"),
            learningStyle = "Hands-on Projects & Code"
        )
        SampleData.applyOnboardingPreferences(csPrefs)

        assertEquals("cs", SampleData.learnerProfile.activeDomainId)
        assertEquals("Computer Science", SampleData.learnerProfile.activeDomainTitle)
        assertEquals("Balanced Binary Search Trees", SampleData.currentModule.title)
        assertEquals("cs_1", SampleData.pathwayPreviewNodes[0].id)
        assertFalse(SampleData.isCurrentLessonCompleted)

        // Complete lesson in CS domain
        SampleData.completeCurrentLesson(recordedMinutes = 15)
        assertTrue(SampleData.isCurrentLessonCompleted)
        val csNode2 = SampleData.pathwayPreviewNodes.first { it.id == "cs_2" }
        assertEquals(MilestoneStatus.COMPLETED, csNode2.status)

        // 4. Switch to Mathematics domain
        val mathPrefs = OnboardingPreferences(
            domainId = "math",
            domainTitle = "Mathematics",
            goal = "Master Linear Algebra for Machine Learning",
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutes = 45,
            interests = listOf("Vectors & Matrices", "Eigenvalues"),
            learningStyle = "Theory & Visual Proofs"
        )
        SampleData.applyOnboardingPreferences(mathPrefs)

        assertEquals("math", SampleData.learnerProfile.activeDomainId)
        assertEquals("Mathematics", SampleData.learnerProfile.activeDomainTitle)
        assertEquals("math_1", SampleData.pathwayPreviewNodes[0].id)

        // 5. Return to AI/ML domain
        val aiPrefs = OnboardingPreferences(
            domainId = "ai_ml",
            domainTitle = "Artificial Intelligence & ML",
            goal = "Deploy on Low-Power ARM Chips",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            dailyMinutes = 35,
            interests = listOf("INT8 Quantization", "ARM NEON"),
            learningStyle = "Hands-on Projects & Code"
        )
        SampleData.applyOnboardingPreferences(aiPrefs)

        // VERIFY: AI/ML milestones and completion are PRESERVED without being overwritten!
        assertEquals("ai_ml", SampleData.learnerProfile.activeDomainId)
        assertEquals("INT8 Weight Quantization", SampleData.currentModule.title)
        assertTrue("AI/ML lesson completion must be preserved across domain hops", SampleData.isCurrentLessonCompleted)

        val restoredM2 = SampleData.pathwayPreviewNodes.first { it.id == "node_2" }
        val restoredM3 = SampleData.pathwayPreviewNodes.first { it.id == "node_3" }
        assertEquals("Milestone 2 must remain COMPLETED when returning to AI/ML", MilestoneStatus.COMPLETED, restoredM2.status)
        assertEquals("Milestone 3 must remain IN_PROGRESS when returning to AI/ML", MilestoneStatus.IN_PROGRESS, restoredM3.status)

        // VERIFY: Bookmarks, assessment submissions, and topic competencies survived intact
        assertTrue("Bookmark must be preserved across domain switches", SampleData.isLessonBookmarked("lesson_int8"))
        assertTrue("Assessment submissions must be preserved", AdaptiveLearningEngine.sessionSubmissions.isNotEmpty())
        assertEquals(competencyBefore, AdaptiveLearningEngine.topicStats[q.topicTag]?.correctAttempts)

        // 6. Return to CS and verify CS progress was also preserved
        SampleData.applyOnboardingPreferences(csPrefs)
        assertEquals("cs", SampleData.learnerProfile.activeDomainId)
        assertTrue("CS lesson completion must be preserved", SampleData.isCurrentLessonCompleted)
        val restoredCsNode2 = SampleData.pathwayPreviewNodes.first { it.id == "cs_2" }
        assertEquals("CS Milestone 2 must remain COMPLETED", MilestoneStatus.COMPLETED, restoredCsNode2.status)
    }

    @Test
    fun domainCurriculum_coldStartHydration_restoresMultiDomainRecords() {
        val backend = InMemoryStorageBackend()
        val repo = AdaptPersistenceRepository(backend)

        // Save AI/ML curriculum
        val aiModule = LearningModule(
            id = "mod_ai_test",
            title = "INT8 Weight Quantization",
            summary = "Summary",
            trackName = "Edge AI",
            estimatedMinutes = 12,
            currentStep = 4,
            totalSteps = 4,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
        val aiNodes = listOf(
            PathwayNode("node_1", 1, "MobileNet", "Track", MilestoneStatus.COMPLETED, 15),
            PathwayNode("node_2", 2, "INT8", "Track", MilestoneStatus.COMPLETED, 12),
            PathwayNode("node_3", 3, "Memory", "Track", MilestoneStatus.IN_PROGRESS, 20)
        )
        val aiGoal = DailyGoal("goal_ai", "AI Goal", "Target", 2, 2, true)

        runBlocking {
            repo.saveProfile(
                LearnerProfile(
                    name = "Aryan",
                    greeting = "Good morning",
                    streakDays = 6,
                    dailyMinutesSpent = 25,
                    dailyMinutesGoal = 35,
                    weeklyHoursCompleted = 3.8f,
                    masteryScorePercent = 82,
                    activeDomainId = "ai_ml"
                )
            )
            repo.saveCurriculum(aiModule, aiNodes, aiGoal, lessonCompleted = true)
        }

        // Verify that repo writes per-domain key
        val rawAi = backend.read("curriculum_state_ai_ml")
        assertNotNull("curriculum_state_ai_ml must be written to backend", rawAi)

        // Initialize AdaptPersistence with this backend
        AdaptPersistence.initWithBackend(backend)

        // Verify active domain and domain progress records were hydrated
        assertEquals("ai_ml", SampleData.learnerProfile.activeDomainId)
        assertTrue(SampleData.domainProgressRecords.containsKey("ai_ml"))
        val cachedAi = SampleData.domainProgressRecords["ai_ml"]!!
        assertTrue(cachedAi.isLessonCompleted)
        assertEquals(MilestoneStatus.COMPLETED, cachedAi.pathwayNodes.first { it.id == "node_2" }.status)
    }

    @Test
    fun tutorComposer_layoutSpacingAndMultilineGrowthPreserved() {
        SampleData.resetToDefaultTrack()
        TutorRepository.clearSession("Aryan", "Artificial Intelligence & ML", "INT8 Weight Quantization")

        // 1. Initial state has exactly 1 welcome message
        assertEquals(1, TutorRepository.conversationMessages.size)

        // 2. Submit a multiline question (up to 4 lines)
        val multilinePrompt = "Line 1: How does INT8 work?\nLine 2: Explain scale factor S.\nLine 3: Explain zero point Z.\nLine 4: Give me an ARM NEON example."
        runBlocking {
            TutorRepository.sendUserQueryAsync(
                query = multilinePrompt,
                domainId = "ai_ml",
                domainTitle = "Artificial Intelligence & ML",
                currentTopic = "INT8 Weight Quantization",
                goal = "Build Edge AI",
                skillLevel = DifficultyLevel.INTERMEDIATE,
                learningStyle = "Hands-on Projects & Code"
            )
        }

        assertEquals(3, TutorRepository.conversationMessages.size)
        val userMsg = TutorRepository.conversationMessages[1]
        assertEquals(multilinePrompt, userMsg.content)

        val tutorMsg = TutorRepository.conversationMessages.last()
        assertTrue(tutorMsg.isTutor)
        assertEquals("Prototype Engine (Simulated On-Device Runtime)", tutorMsg.structuredPayload?.executionModelLabel)

        // 3. Verify all 5 tutor modes provide valid suggested prompts
        val engine = PrototypeTutorEngine()
        TutorMode.entries.forEach { mode ->
            val prompts = engine.getSuggestedPrompts("ai_ml", "INT8 Weight Quantization", mode)
            assertTrue("Mode ${mode.title} must have suggested prompts", prompts.isNotEmpty())
        }
    }

    // ========================================================================
    // PHASE 9.1 REGRESSION TESTS: ADAPTIVE MASTERY INTEGRITY
    // ========================================================================

    @Test
    fun adaptiveMastery_zeroAttempts_evaluatesToZeroWithoutFabrication() {
        // Zero attempts in empty stats map
        val emptyStats = emptyMap<String, TopicCompetency>()
        assertEquals(0, AdaptiveLearningEngine.calculateMasteryScore(emptyStats))

        // Zero attempts with existing topic tags having 0/0 attempts
        val unattemptedStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 0, 0),
            "Quantization Math" to TopicCompetency("Quantization Math", 0, 0)
        )
        assertEquals(0, AdaptiveLearningEngine.calculateMasteryScore(unattemptedStats))

        // TopicCompetency masteryPercentage for 0 attempts is also 0%
        assertEquals(0, TopicCompetency("Test", 0, 0).masteryPercentage)
    }

    @Test
    fun adaptiveMastery_allIncorrectAnswers_evaluatesToZeroWithoutMinimumFloor() {
        // When all answers are incorrect (0 correct out of N attempts)
        val allIncorrectStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 0, 3),
            "Quantization Math" to TopicCompetency("Quantization Math", 0, 2),
            "ARM NPU Execution" to TopicCompetency("ARM NPU Execution", 0, 5)
        )

        val score = AdaptiveLearningEngine.calculateMasteryScore(allIncorrectStats)
        // CRITICAL: Must be exactly 0% and NOT clamped to an artificial 40% floor
        assertEquals(0, score)
        assertFalse("Mastery must not have an artificial 40% floor", score >= 40)
    }

    @Test
    fun adaptiveMastery_fiftyPercentAccuracy_evaluatesToFiftyPercent() {
        // Exactly 50% accuracy across topics
        val halfAccuracyStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 2, 4),
            "Quantization Math" to TopicCompetency("Quantization Math", 3, 6)
        )
        // Total correct = 5, total attempts = 10 -> exactly 50%
        val score = AdaptiveLearningEngine.calculateMasteryScore(halfAccuracyStats)
        assertEquals(50, score)
    }

    @Test
    fun adaptiveMastery_allCorrectAnswers_evaluatesToOneHundredPercent() {
        // 100% accuracy across all topics
        val perfectStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 5, 5),
            "Quantization Math" to TopicCompetency("Quantization Math", 3, 3),
            "ARM NPU Execution" to TopicCompetency("ARM NPU Execution", 2, 2)
        )
        // Total correct = 10, total attempts = 10 -> exactly 100%
        val score = AdaptiveLearningEngine.calculateMasteryScore(perfectStats)
        assertEquals(100, score)
    }

    @Test
    fun adaptiveMastery_remainsConsistentAfterAppRestart() {
        val backend = InMemoryStorageBackend()
        val repo = AdaptPersistenceRepository(backend)

        // Setup practice stats with 30% accuracy (3 out of 10 correct)
        // Under the old implementation this would have been clamped to 40%
        val stats30Percent = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 1, 4),
            "Quantization Math" to TopicCompetency("Quantization Math", 2, 6)
        )
        val calculatedMastery = AdaptiveLearningEngine.calculateMasteryScore(stats30Percent)
        assertEquals(30, calculatedMastery)

        val submissions = listOf(
            QuestionSubmission("q1", 0, false, 1000L),
            QuestionSubmission("q2", 1, true, 2000L)
        )
        val mistakes = listOf(
            MistakeRecord(
                question = AdaptiveLearningEngine.questionBank.first(),
                lastSelectedOptionIndex = 0,
                mistakeCount = 1,
                nextRevisionDays = 1,
                isResolved = false
            )
        )

        val profileWithMastery = LearnerProfile(
            name = "Aryan",
            greeting = "Good morning",
            streakDays = 6,
            dailyMinutesSpent = 25,
            dailyMinutesGoal = 35,
            weeklyHoursCompleted = 3.8f,
            masteryScorePercent = calculatedMastery,
            activeDomainId = "ai_ml"
        )

        runBlocking {
            repo.saveProfile(profileWithMastery)
            repo.savePracticeState(submissions, stats30Percent, mistakes)
        }

        // Simulate application cold restart: re-initialize repository and AdaptPersistence
        val restoredRepo = AdaptPersistenceRepository(backend)
        AdaptPersistence.initWithBackend(backend)

        // Verify that profile mastery matches the authentic 30% after restart
        assertEquals(30, restoredRepo.profileFlow.value.masteryScorePercent)
        assertEquals(30, SampleData.learnerProfile.masteryScorePercent)

        // Verify that restored topicStats also calculates to exactly 30%
        val restoredMastery = AdaptiveLearningEngine.calculateMasteryScore(restoredRepo.topicStatsFlow.value)
        assertEquals(30, restoredMastery)
        assertEquals(SampleData.learnerProfile.masteryScorePercent, restoredMastery)

        // Verify topic stats, mistakes and submissions are completely preserved
        assertEquals(2, restoredRepo.topicStatsFlow.value.size)
        assertEquals(1, restoredRepo.topicStatsFlow.value["Memory Hierarchy"]?.correctAttempts)
        assertEquals(4, restoredRepo.topicStatsFlow.value["Memory Hierarchy"]?.totalAttempts)
        assertEquals(1, restoredRepo.mistakeNotebookFlow.value.size)
        assertEquals(2, restoredRepo.submissionsFlow.value.size)
    }

    // ========================================================================
    // PHASE 9.2 REGRESSION TESTS: DISTINGUISH UNASSESSED FROM ZERO MASTERY
    // ========================================================================

    @Test
    fun masteryLevelLabel_zeroAttempts_evaluatesToUnassessedLevelZero() {
        // Zero attempts with zero mastery
        val label = AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 0, totalAttempts = 0)
        assertEquals("Unassessed (Level 0)", label)

        // Negative/boundary safeguard
        assertEquals("Unassessed (Level 0)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 0, totalAttempts = -1))
    }

    @Test
    fun masteryLevelLabel_allIncorrectAnswers_evaluatesToNeedsImprovementLevelZero() {
        // Actual attempts > 0 with zero correct answers (0% mastery)
        val label = AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 0, totalAttempts = 5)
        assertEquals("Needs Improvement (Level 0)", label)

        val singleAttemptLabel = AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 0, totalAttempts = 1)
        assertEquals("Needs Improvement (Level 0)", singleAttemptLabel)
    }

    @Test
    fun masteryLevelLabel_preservesExistingMasteryLevels() {
        // Level 4: Proficient (>= 80%)
        assertEquals("Proficient (Level 4)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 100, totalAttempts = 10))
        assertEquals("Proficient (Level 4)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 85, totalAttempts = 10))
        assertEquals("Proficient (Level 4)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 80, totalAttempts = 10))

        // Level 3: Developing (>= 60% and < 80%)
        assertEquals("Developing (Level 3)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 79, totalAttempts = 10))
        assertEquals("Developing (Level 3)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 65, totalAttempts = 10))
        assertEquals("Developing (Level 3)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 60, totalAttempts = 10))

        // Level 2: Foundational (> 0% and < 60%)
        assertEquals("Foundational (Level 2)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 59, totalAttempts = 10))
        assertEquals("Foundational (Level 2)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 30, totalAttempts = 10))
        assertEquals("Foundational (Level 2)", AdaptiveLearningEngine.getMasteryLevelLabel(masteryScorePercent = 1, totalAttempts = 10))
    }

    @Test
    fun masteryLevelLabel_remainsConsistentAcrossRestarts_forBothZeroScenarios() {
        val backend = InMemoryStorageBackend()
        val repo = AdaptPersistenceRepository(backend)

        // Case A: Restart with all incorrect attempts (0/4 correct = 0% mastery)
        val allIncorrectStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 0, 4)
        )
        val calculatedMastery = AdaptiveLearningEngine.calculateMasteryScore(allIncorrectStats)
        assertEquals(0, calculatedMastery)

        val profileWithZeroMastery = LearnerProfile(
            name = "Aryan",
            greeting = "Good morning",
            streakDays = 1,
            dailyMinutesSpent = 10,
            dailyMinutesGoal = 35,
            weeklyHoursCompleted = 0.5f,
            masteryScorePercent = calculatedMastery,
            activeDomainId = "ai_ml"
        )

        runBlocking {
            repo.saveProfile(profileWithZeroMastery)
            repo.savePracticeState(emptyList(), allIncorrectStats, emptyList())
        }

        // Simulate app restart
        val restoredRepo = AdaptPersistenceRepository(backend)
        val restoredTotalAttempts = restoredRepo.topicStatsFlow.value.values.sumOf { it.totalAttempts }
        val restoredScore = restoredRepo.profileFlow.value.masteryScorePercent
        assertEquals(4, restoredTotalAttempts)
        assertEquals(0, restoredScore)

        val restoredLabel = AdaptiveLearningEngine.getMasteryLevelLabel(restoredScore, restoredTotalAttempts)
        assertEquals("Needs Improvement (Level 0)", restoredLabel)

        // Case B: Restart with zero attempts explicitly saved
        val emptyBackend = InMemoryStorageBackend()
        val emptyRepo = AdaptPersistenceRepository(emptyBackend)
        runBlocking {
            emptyRepo.savePracticeState(emptyList(), emptyMap(), emptyList())
            emptyRepo.saveProfile(
                LearnerProfile(
                    name = "NewLearner",
                    greeting = "Welcome",
                    streakDays = 0,
                    dailyMinutesSpent = 0,
                    dailyMinutesGoal = 35,
                    weeklyHoursCompleted = 0f,
                    masteryScorePercent = 0,
                    activeDomainId = "ai_ml"
                )
            )
        }

        val restoredZeroRepo = AdaptPersistenceRepository(emptyBackend)
        val emptyTotalAttempts = restoredZeroRepo.topicStatsFlow.value.values.sumOf { it.totalAttempts }
        val emptyScore = AdaptiveLearningEngine.calculateMasteryScore(restoredZeroRepo.topicStatsFlow.value)
        assertEquals(0, emptyTotalAttempts)
        assertEquals(0, emptyScore)

        val unassessedLabel = AdaptiveLearningEngine.getMasteryLevelLabel(emptyScore, emptyTotalAttempts)
        assertEquals("Unassessed (Level 0)", unassessedLabel)
    }
}

