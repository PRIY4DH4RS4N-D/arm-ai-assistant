package com.example.adapt_ai_ui

import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.DailyGoal
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
import com.example.adapt_ai_ui.data.persistence.AdaptJson
import com.example.adapt_ai_ui.data.persistence.AdaptJsonSerializers
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository
import com.example.adapt_ai_ui.data.persistence.FileStorageBackend
import com.example.adapt_ai_ui.data.persistence.InMemoryStorageBackend
import com.example.adapt_ai_ui.data.persistence.PersistenceWriteCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AdaptPersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var backend: InMemoryStorageBackend
    private lateinit var repo: AdaptPersistenceRepository

    @Before
    fun setup() {
        backend = InMemoryStorageBackend()
        repo = AdaptPersistenceRepository(backend, Dispatchers.Unconfined)
    }

    @Test
    fun learnerProfile_saveAndRestore_restoresAllFields() = runBlocking {
        val original = LearnerProfile(
            name = "Test Learner",
            greeting = "Good evening",
            streakDays = 12,
            dailyMinutesSpent = 45,
            dailyMinutesGoal = 60,
            weeklyHoursCompleted = 5.2f,
            masteryScorePercent = 94,
            activeDomainId = "embedded",
            activeDomainTitle = "Electronics & Embedded Systems",
            activeGoal = "Master Low-Power Microcontrollers",
            skillLevel = DifficultyLevel.ADVANCED,
            preferredLearningStyle = "Socratic Q&A Dialogue"
        )

        repo.saveProfile(original)

        // Verify JSON string written to storage
        val rawJson = backend.read(AdaptPersistenceRepository.KEY_LEARNER_PROFILE)
        assertNotNull(rawJson)

        // Re-read via serializer
        val restored = AdaptJsonSerializers.deserializeLearnerProfile(rawJson!!)
        assertNotNull(restored)
        assertEquals(original.name, restored?.name)
        assertEquals(original.streakDays, restored?.streakDays)
        assertEquals(original.dailyMinutesSpent, restored?.dailyMinutesSpent)
        assertEquals(original.dailyMinutesGoal, restored?.dailyMinutesGoal)
        assertEquals(original.weeklyHoursCompleted, restored?.weeklyHoursCompleted ?: 0f, 0.01f)
        assertEquals(original.masteryScorePercent, restored?.masteryScorePercent)
        assertEquals(original.activeDomainId, restored?.activeDomainId)
        assertEquals(original.activeDomainTitle, restored?.activeDomainTitle)
        assertEquals(original.activeGoal, restored?.activeGoal)
        assertEquals(original.skillLevel, restored?.skillLevel)
        assertEquals(original.preferredLearningStyle, restored?.preferredLearningStyle)
    }

    @Test
    fun onboardingPreferences_saveAndRestore_persistsCustomInterestsAndGoal() = runBlocking {
        val prefs = OnboardingPreferences(
            domainId = "programming",
            domainTitle = "Programming & Software Architecture",
            goal = "Master Concurrent & Systems Programming",
            skillLevel = DifficultyLevel.ADVANCED,
            dailyMinutes = 45,
            interests = listOf("Concurrency & Multithreading", "Memory Models", "ARM SIMD"),
            learningStyle = "Hands-on Projects & Code"
        )

        repo.saveOnboardingPreferences(prefs)

        val restored = repo.getOnboardingPreferences()
        assertNotNull(restored)
        assertEquals("programming", restored?.domainId)
        assertEquals("Master Concurrent & Systems Programming", restored?.goal)
        assertEquals(DifficultyLevel.ADVANCED, restored?.skillLevel)
        assertEquals(45, restored?.dailyMinutes)
        assertEquals(3, restored?.interests?.size)
        assertTrue(restored?.interests?.contains("ARM SIMD") == true)
        assertEquals("Hands-on Projects & Code", restored?.learningStyle)
    }

    @Test
    fun curriculumState_saveAndRestore_persistsMilestonesAndProgress() = runBlocking {
        val module = LearningModule(
            id = "mod_test_1",
            title = "Interrupt Latency Optimization",
            summary = "Measuring ISR overhead",
            trackName = "Embedded Systems",
            estimatedMinutes = 18,
            currentStep = 4,
            totalSteps = 4,
            difficulty = DifficultyLevel.ADVANCED
        )

        val nodes = listOf(
            PathwayNode("n1", 1, "NVIC Architecture", "Core", MilestoneStatus.COMPLETED, 15),
            PathwayNode("n2", 2, "Interrupt Latency", "Core", MilestoneStatus.COMPLETED, 18),
            PathwayNode("n3", 3, "DMA Channels", "IO", MilestoneStatus.IN_PROGRESS, 20)
        )

        val goal = DailyGoal(
            id = "goal_test",
            title = "Embedded Focus",
            targetDescription = "Complete 2 labs",
            current = 2,
            target = 2,
            isCompleted = true
        )

        repo.saveCurriculum(module, nodes, goal, lessonCompleted = true)

        val raw = backend.read(AdaptPersistenceRepository.KEY_CURRICULUM)
        assertNotNull(raw)

        val restoredModule = AdaptJsonSerializers.deserializeCurriculumModule(raw!!)
        val restoredNodes = AdaptJsonSerializers.deserializeCurriculumNodes(raw)
        val restoredGoal = AdaptJsonSerializers.deserializeCurriculumGoal(raw)
        val restoredLessonCompleted = AdaptJsonSerializers.deserializeCurriculumLessonCompleted(raw)

        assertEquals("mod_test_1", restoredModule?.id)
        assertEquals(4, restoredModule?.currentStep)
        assertEquals(3, restoredNodes?.size)
        assertEquals(MilestoneStatus.COMPLETED, restoredNodes?.get(1)?.status)
        assertEquals(MilestoneStatus.IN_PROGRESS, restoredNodes?.get(2)?.status)
        assertTrue(restoredGoal?.isCompleted == true)
        assertTrue(restoredLessonCompleted)
    }

    @Test
    fun practiceState_submitAnswers_persistsSubmissionsAndStats() = runBlocking {
        val submissions = listOf(
            QuestionSubmission("q1", 1, true, 1000L),
            QuestionSubmission("q2", 0, false, 2000L),
            QuestionSubmission("q3", 2, true, 3000L)
        )

        val topicStats = mapOf(
            "Memory Hierarchy" to TopicCompetency("Memory Hierarchy", 2, 2),
            "Quantization Math" to TopicCompetency("Quantization Math", 1, 2)
        )

        val mistakes = listOf(
            MistakeRecord(
                question = PracticeQuestionItem(
                    id = "q2",
                    topicTag = "Quantization Math",
                    question = "Zero-point offset test",
                    options = listOf("A", "B", "C", "D"),
                    correctIndex = 2,
                    explanation = "Test explanation",
                    difficulty = DifficultyLevel.INTERMEDIATE
                ),
                lastSelectedOptionIndex = 0,
                mistakeCount = 2,
                nextRevisionDays = 1,
                isResolved = false
            )
        )

        repo.savePracticeState(submissions, topicStats, mistakes)

        val raw = backend.read(AdaptPersistenceRepository.KEY_PRACTICE)
        assertNotNull(raw)

        val restoredSubs = AdaptJsonSerializers.deserializeSubmissions(raw!!)
        val restoredStats = AdaptJsonSerializers.deserializeTopicStats(raw)
        val restoredMistakes = AdaptJsonSerializers.deserializeMistakeNotebook(raw)

        assertEquals(3, restoredSubs?.size)
        assertTrue(restoredSubs?.first()?.isCorrect == true)
        assertFalse(restoredSubs?.get(1)?.isCorrect == true)

        assertEquals(2, restoredStats?.size)
        assertEquals(2, restoredStats?.get("Memory Hierarchy")?.correctAttempts)

        assertEquals(1, restoredMistakes?.size)
        assertEquals("q2", restoredMistakes?.first()?.question?.id)
        assertEquals(2, restoredMistakes?.first()?.mistakeCount)
        assertFalse(restoredMistakes?.first()?.isResolved ?: true)
    }

    @Test
    fun tutorSession_saveAndRestore_preservesModeAndRichStructuredPayload() = runBlocking {
        val response = TutorResponse(
            text = "Here is how INT8 dot product works on ARM NEON.",
            conceptExplanation = "NEON SIMD executes 16 integer MACs per cycle.",
            steps = listOf("Load 16 bytes", "Multiply vectors", "Accumulate to INT32"),
            exampleOrAnalogy = "Like a multi-lane highway vs a single lane.",
            codeSnippet = "acc = vdotq_s32(acc, va, vb);",
            codeLanguage = "C++ (ARM NEON)",
            formula = "r = S * (q - Z)",
            keyTakeaways = listOf("4x bandwidth reduction", "Prevents overflow"),
            followUpPrompts = listOf("What is symmetric quantization?", "How does zero-point work?"),
            practiceQuestion = "What register width is needed for accumulation?"
        )

        val messages = listOf(
            TutorConversationMessage(
                id = "m1",
                sender = "You",
                isTutor = false,
                content = "Explain ARM NEON INT8 dot products",
                mode = TutorMode.DEBUG
            ),
            TutorConversationMessage(
                id = "m2",
                sender = "ADAPT-AI",
                isTutor = true,
                content = response.text,
                mode = TutorMode.DEBUG,
                structuredPayload = response
            )
        )

        repo.saveTutorSession(TutorMode.DEBUG, messages)

        val raw = backend.read(AdaptPersistenceRepository.KEY_TUTOR_SESSION)
        assertNotNull(raw)

        val restoredMode = AdaptJsonSerializers.deserializeTutorMode(raw!!)
        val restoredMessages = AdaptJsonSerializers.deserializeTutorMessages(raw)

        assertEquals(TutorMode.DEBUG, restoredMode)
        assertEquals(2, restoredMessages?.size)

        val restoredAssistant = restoredMessages?.get(1)
        assertEquals("ADAPT-AI", restoredAssistant?.sender)
        assertTrue(restoredAssistant?.isTutor == true)
        assertEquals(response.text, restoredAssistant?.content)

        val payload = restoredAssistant?.structuredPayload
        assertNotNull(payload)
        assertEquals("C++ (ARM NEON)", payload?.codeLanguage)
        assertEquals("acc = vdotq_s32(acc, va, vb);", payload?.codeSnippet)
        assertEquals("r = S * (q - Z)", payload?.formula)
        assertEquals(3, payload?.steps?.size)
        assertEquals(2, payload?.keyTakeaways?.size)
        assertEquals(2, payload?.followUpPrompts?.size)
    }

    @Test
    fun tutorSession_clear_deletesHistorySafely() = runBlocking {
        repo.saveTutorSession(
            TutorMode.SOCRATIC,
            listOf(TutorConversationMessage("id1", "User", false, "Test", TutorMode.SOCRATIC))
        )
        assertNotNull(backend.read(AdaptPersistenceRepository.KEY_TUTOR_SESSION))

        repo.clearTutorSession()
        assertNull(backend.read(AdaptPersistenceRepository.KEY_TUTOR_SESSION))
        assertTrue(repo.messagesFlow.value.isEmpty())
    }

    @Test
    fun corruptedData_fallbackToSafeDefaults() = runBlocking {
        // Write invalid corrupted JSON
        backend.write(AdaptPersistenceRepository.KEY_LEARNER_PROFILE, "{ malformed json: not valid ]]]")

        val newRepo = AdaptPersistenceRepository(backend, Dispatchers.Unconfined)
        // Should safely fallback to default profile rather than crashing
        val profile = newRepo.profileFlow.value
        assertNotNull(profile)
        assertEquals("Aryan", profile.name)
        assertEquals(82, profile.masteryScorePercent)
    }

    @Test
    fun missingData_returnsSafeDefaults() {
        val emptyBackend = InMemoryStorageBackend()
        val emptyRepo = AdaptPersistenceRepository(emptyBackend, Dispatchers.Unconfined)

        assertEquals("Aryan", emptyRepo.profileFlow.value.name)
        assertEquals("INT8 Weight Quantization", emptyRepo.currentModuleFlow.value.title)
        assertEquals(4, emptyRepo.pathwayNodesFlow.value.size)
        assertEquals(TutorMode.SOCRATIC, emptyRepo.activeModeFlow.value)
        assertTrue(emptyRepo.submissionsFlow.value.isEmpty())
    }

    @Test
    fun fileStorageBackend_atomicWriteAndBackupRecovery() {
        val folder = tempFolder.newFolder("adapt_test_store")
        val fileBackend = FileStorageBackend(folder)

        // Write content
        val written = fileBackend.write("test_key", """{"status":"ok"}""")
        assertTrue(written)

        // Read content
        val content = fileBackend.read("test_key")
        assertEquals("""{"status":"ok"}""", content)

        // Update content (creates .bak)
        fileBackend.write("test_key", """{"status":"updated"}""")
        val updated = fileBackend.read("test_key")
        assertEquals("""{"status":"updated"}""", updated)

        // Delete content
        val deleted = fileBackend.delete("test_key")
        assertTrue(deleted)
        assertNull(fileBackend.read("test_key"))
    }

    @Test
    fun adaptPersistence_syncToSingletons_hydratesSampleDataAndEngines() = runBlocking {
        val customProfile = LearnerProfile(
            name = "Persisted User",
            greeting = "Welcome back",
            streakDays = 15,
            dailyMinutesSpent = 40,
            dailyMinutesGoal = 50,
            weeklyHoursCompleted = 4.5f,
            masteryScorePercent = 91,
            activeDomainId = "data_science",
            activeDomainTitle = "Data Science & Applied Statistics",
            activeGoal = "Master Feature Engineering",
            skillLevel = DifficultyLevel.ADVANCED,
            preferredLearningStyle = "Hands-on Projects & Code"
        )

        repo.saveProfile(customProfile)

        // Save custom practice & tutor state
        val submissions = listOf(QuestionSubmission("q100", 0, true, 5000L))
        val stats = mapOf("Data Science" to TopicCompetency("Data Science", 5, 5))
        repo.savePracticeState(submissions, stats, emptyList())

        val tutorMessages = listOf(
            TutorConversationMessage("t1", "User", false, "Hello", TutorMode.REVIEW),
            TutorConversationMessage("t2", "ADAPT-AI", true, "Review Summary", TutorMode.REVIEW)
        )
        repo.saveTutorSession(TutorMode.REVIEW, tutorMessages)

        // Initialize AdaptPersistence with our test repository backend
        AdaptPersistence.initWithBackend(backend)

        // Verify SampleData has been hydrated
        assertEquals("Persisted User", SampleData.learnerProfile.name)
        assertEquals("data_science", SampleData.learnerProfile.activeDomainId)
        assertEquals(15, SampleData.learnerProfile.streakDays)
        assertEquals(91, SampleData.learnerProfile.masteryScorePercent)

        // Verify AdaptiveLearningEngine has been hydrated
        assertEquals(1, AdaptiveLearningEngine.sessionSubmissions.size)
        assertEquals("q100", AdaptiveLearningEngine.sessionSubmissions.first().questionId)
        assertEquals(5, AdaptiveLearningEngine.topicStats["Data Science"]?.correctAttempts)

        // Verify TutorRepository has been hydrated
        assertEquals(TutorMode.REVIEW, TutorRepository.activeMode)
        assertEquals(2, TutorRepository.conversationMessages.size)
        assertEquals("Review Summary", TutorRepository.conversationMessages[1].content)
    }

    @Test
    fun concurrentWrites_toSameKey_allSucceedWithoutCorruption() = runBlocking {
        val folder = tempFolder.newFolder("adapt_concurrent_test")
        val fileBackend = FileStorageBackend(folder)
        val coordinator = PersistenceWriteCoordinator(Dispatchers.IO)

        // Launch 25 concurrent write operations to the same key
        (1..25).forEach { index ->
            coordinator.scheduleWrite("stress_key") {
                val json = """{"iteration": $index, "valid": true}"""
                fileBackend.write("stress_key", json)
            }
        }

        coordinator.flush()

        val finalContent = fileBackend.read("stress_key")
        assertNotNull(finalContent)
        val parsed = AdaptJson.parseObject(finalContent!!)
        assertNotNull(parsed)
        assertTrue(parsed!!.getBoolean("valid"))
    }

    @Test
    fun multipleUpdates_newestValueAlwaysWins() = runBlocking {
        val folder = tempFolder.newFolder("adapt_newest_test")
        val fileBackend = FileStorageBackend(folder)
        val coordinator = PersistenceWriteCoordinator(Dispatchers.IO)

        // Rapidly schedule sequential versions
        (1..10).forEach { i ->
            coordinator.scheduleWrite("version_key") {
                fileBackend.write("version_key", """{"version": $i}""")
            }
        }

        coordinator.flush()

        val content = fileBackend.read("version_key")
        assertNotNull(content)
        val parsed = AdaptJson.parseObject(content!!)
        assertNotNull(parsed)
        assertEquals(10L, parsed!!.getLong("version"))
    }

    @Test
    fun concurrentWrites_toDifferentKeys_executeIndependently() = runBlocking {
        val folder = tempFolder.newFolder("adapt_diff_keys_test")
        val fileBackend = FileStorageBackend(folder)
        val coordinator = PersistenceWriteCoordinator(Dispatchers.IO)

        val keys = listOf("key_alpha", "key_beta", "key_gamma", "key_delta", "key_epsilon")

        keys.forEach { key ->
            coordinator.scheduleWrite(key) {
                fileBackend.write(key, """{"keyName": "$key"}""")
            }
        }

        coordinator.flush()

        keys.forEach { key ->
            val content = fileBackend.read(key)
            assertNotNull("Content for $key should exist", content)
            val parsed = AdaptJson.parseObject(content!!)
            assertNotNull(parsed)
            assertEquals(key, parsed!!.getString("keyName"))
        }
    }

    @Test
    fun flush_waitsUntilAllPendingWritesFinish() = runBlocking {
        val coordinator = PersistenceWriteCoordinator(Dispatchers.IO)
        val memoryBackend = InMemoryStorageBackend()
        val executionLog = java.util.concurrent.CopyOnWriteArrayList<Int>()

        // Schedule tasks with slight simulated delay
        (1..5).forEach { index ->
            coordinator.scheduleWrite("key_$index") {
                delay(25)
                executionLog.add(index)
                memoryBackend.write("key_$index", "val_$index")
            }
        }

        assertEquals(5, coordinator.pendingCount)

        // Suspend until all tasks finish
        coordinator.flush()

        assertEquals(0, coordinator.pendingCount)
        assertEquals(5, executionLog.size)
    }

    @Test
    fun failedWrite_preservesPreviousDataAndBackup() {
        val folder = tempFolder.newFolder("adapt_failed_write_test")
        val fileBackend = FileStorageBackend(folder)

        // 1. Initial write
        fileBackend.write("critical_data", """{"state":"initial_good"}""")
        assertEquals("""{"state":"initial_good"}""", fileBackend.read("critical_data"))

        // 2. Second write to establish .bak
        fileBackend.write("critical_data", """{"state":"second_good"}""")
        val backupFile = File(folder, "critical_data.json.bak")
        assertTrue(backupFile.exists())
        assertEquals("""{"state":"initial_good"}""", backupFile.readText())

        // 3. Verify that if target file is corrupted or deleted, backup is recovered
        val targetFile = File(folder, "critical_data.json")
        targetFile.delete() // Simulate sudden file loss

        // read() seamlessly recovers from backupFile
        val recovered = fileBackend.read("critical_data")
        assertEquals("""{"state":"initial_good"}""", recovered)
    }

    @Test
    fun corruptedData_recovery_readsBackupOrReturnsDefault() = runBlocking {
        val folder = tempFolder.newFolder("adapt_corrupt_test")
        val fileBackend = FileStorageBackend(folder)

        // Write initial valid profile
        val profileJson = AdaptJsonSerializers.serializeLearnerProfile(SampleData.learnerProfile)
        fileBackend.write(AdaptPersistenceRepository.KEY_LEARNER_PROFILE, profileJson)

        // Now deliberately corrupt the target file
        val targetFile = File(folder, "${AdaptPersistenceRepository.KEY_LEARNER_PROFILE}.json")
        targetFile.writeText("Corrupted non-json bytes %&*@(!#")

        // Also check repository loading: does not crash, gracefully loads fallback/defaults
        val testRepo = AdaptPersistenceRepository(fileBackend, Dispatchers.Unconfined)
        assertNotNull(testRepo.profileFlow.value)
        assertEquals("Aryan", testRepo.profileFlow.value.name)
    }
}
