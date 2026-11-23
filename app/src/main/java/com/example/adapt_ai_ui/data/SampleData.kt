package com.example.adapt_ai_ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence

object SampleData {
    var learnerProfile by mutableStateOf(
        LearnerProfile(
            name = "Aryan",
            greeting = "Good morning",
            streakDays = 6,
            dailyMinutesSpent = 25,
            dailyMinutesGoal = 35,
            weeklyHoursCompleted = 3.8f,
            masteryScorePercent = 82
        )
    )

    var currentModule by mutableStateOf(
        LearningModule(
            id = "mod_quant_1",
            title = "INT8 Weight Quantization",
            summary = "Learn how converting 32-bit floating point weights into 8-bit integers accelerates edge neural networks while reducing memory bandwidth.",
            trackName = "Edge AI Systems",
            estimatedMinutes = 12,
            currentStep = 3,
            totalSteps = 4,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
    )

    var todayGoal by mutableStateOf(
        DailyGoal(
            id = "goal_today",
            title = "Quantization Foundations",
            targetDescription = "Complete 2 practical lessons & 1 concept quiz",
            current = 2,
            target = 3,
            isCompleted = false
        )
    )

    var pathwayPreviewNodes by mutableStateOf(
        listOf(
            PathwayNode(
                id = "node_1",
                index = 1,
                title = "MobileNet Architecture",
                track = "Model Compression",
                status = MilestoneStatus.COMPLETED,
                estimatedMinutes = 15
            ),
            PathwayNode(
                id = "node_2",
                index = 2,
                title = "INT8 Weight Quantization",
                track = "Precision Scaling",
                status = MilestoneStatus.IN_PROGRESS,
                estimatedMinutes = 12
            ),
            PathwayNode(
                id = "node_3",
                index = 3,
                title = "Tensor Memory Layouts",
                track = "Cache Optimization",
                status = MilestoneStatus.LOCKED,
                estimatedMinutes = 20
            ),
            PathwayNode(
                id = "node_4",
                index = 4,
                title = "Edge Pipeline Benchmark",
                track = "Profiling & Latency",
                status = MilestoneStatus.LOCKED,
                estimatedMinutes = 25
            )
        )
    )

    var isCurrentLessonCompleted by mutableStateOf(false)
    var bookmarkedLessonIds by mutableStateOf(setOf<String>())
    var customTopics by mutableStateOf(listOf("Quantum Computing", "Rust Async", "Embedded RTOS"))

    // Per-domain curriculum and milestone progress store
    val domainProgressRecords = mutableMapOf<String, DomainCurriculumRecord>()

    fun addCustomTopic(topic: String): Boolean {
        val trimmed = topic.trim()
        if (trimmed.isBlank() || customTopics.any { it.equals(trimmed, ignoreCase = true) }) {
            return false
        }
        customTopics = customTopics + trimmed
        notifyPersistenceChanged()
        return true
    }

    fun removeCustomTopic(topic: String): Boolean {
        val before = customTopics.size
        customTopics = customTopics.filterNot { it.equals(topic, ignoreCase = true) }
        val removed = customTopics.size < before
        if (removed) {
            notifyPersistenceChanged()
        }
        return removed
    }

    fun isLessonBookmarked(lessonId: String): Boolean = bookmarkedLessonIds.contains(lessonId)

    fun toggleBookmark(lessonId: String): Boolean {
        val willBeBookmarked = !bookmarkedLessonIds.contains(lessonId)
        bookmarkedLessonIds = if (willBeBookmarked) {
            bookmarkedLessonIds + lessonId
        } else {
            bookmarkedLessonIds - lessonId
        }
        notifyPersistenceChanged()
        return willBeBookmarked
    }

    fun recordStudyTime(minutes: Int) {
        if (minutes > 0) {
            learnerProfile = learnerProfile.copy(
                dailyMinutesSpent = learnerProfile.dailyMinutesSpent + minutes
            )
            notifyPersistenceChanged()
        }
    }

    fun completeCurrentLesson(recordedMinutes: Int = 0) {
        val wasAlreadyCompleted = isCurrentLessonCompleted
        isCurrentLessonCompleted = true

        // Complete current module
        currentModule = currentModule.copy(
            currentStep = 4
        )

        // Advance Today's Goal from legitimate tracked activity
        if (!wasAlreadyCompleted) {
            val newCurrent = (todayGoal.current + 1).coerceAtMost(todayGoal.target)
            todayGoal = todayGoal.copy(
                current = newCurrent,
                isCompleted = newCurrent >= todayGoal.target
            )
        }

        // Record actual study time if provided, without fabricating learner statistics
        if (recordedMinutes > 0) {
            learnerProfile = learnerProfile.copy(
                dailyMinutesSpent = learnerProfile.dailyMinutesSpent + recordedMinutes
            )
        }

        // Advance Pathway: Milestone 2 -> COMPLETED, Milestone 3 -> IN_PROGRESS across all domain schemas
        pathwayPreviewNodes = pathwayPreviewNodes.map { node ->
            when {
                node.id == "node_2" || node.index == 2 -> node.copy(status = MilestoneStatus.COMPLETED)
                node.id == "node_3" || node.index == 3 -> node.copy(status = MilestoneStatus.IN_PROGRESS)
                else -> node
            }
        }

        // Update active domain progress record
        domainProgressRecords[learnerProfile.activeDomainId] = DomainCurriculumRecord(
            domainId = learnerProfile.activeDomainId,
            currentModule = currentModule,
            pathwayNodes = pathwayPreviewNodes,
            todayGoal = todayGoal,
            isLessonCompleted = isCurrentLessonCompleted
        )

        notifyPersistenceChanged()
    }

    fun applyOnboardingPreferences(prefs: OnboardingPreferences) {
        // 1. Preserve outgoing domain's progress before switching
        val outgoingDomainId = learnerProfile.activeDomainId
        domainProgressRecords[outgoingDomainId] = DomainCurriculumRecord(
            domainId = outgoingDomainId,
            currentModule = currentModule,
            pathwayNodes = pathwayPreviewNodes,
            todayGoal = todayGoal,
            isLessonCompleted = isCurrentLessonCompleted
        )

        val domain = DomainRepository.getDomainById(prefs.domainId)

        learnerProfile = learnerProfile.copy(
            activeDomainId = domain.id,
            activeDomainTitle = domain.title,
            activeGoal = prefs.goal,
            skillLevel = prefs.skillLevel,
            dailyMinutesGoal = prefs.dailyMinutes,
            preferredLearningStyle = prefs.learningStyle
        )

        // 2. Restore previously recorded progress for this domain, or initialize defaults
        val savedProgress = domainProgressRecords[prefs.domainId]
        if (savedProgress != null) {
            currentModule = savedProgress.currentModule.copy(
                difficulty = prefs.skillLevel
            )
            pathwayPreviewNodes = savedProgress.pathwayNodes
            todayGoal = savedProgress.todayGoal
            isCurrentLessonCompleted = savedProgress.isLessonCompleted
        } else {
            currentModule = domain.defaultModule.copy(
                difficulty = prefs.skillLevel
            )
            pathwayPreviewNodes = domain.defaultPathwayNodes
            todayGoal = DailyGoal(
                id = "goal_${domain.id}",
                title = "${domain.title} Focus",
                targetDescription = "Complete 1 lesson & 1 practice set (${prefs.dailyMinutes}m daily target)",
                current = 0,
                target = 2,
                isCompleted = false
            )
            isCurrentLessonCompleted = false
            domainProgressRecords[prefs.domainId] = DomainCurriculumRecord(
                domainId = prefs.domainId,
                currentModule = currentModule,
                pathwayNodes = pathwayPreviewNodes,
                todayGoal = todayGoal,
                isLessonCompleted = isCurrentLessonCompleted
            )
        }

        if (AdaptPersistence.isInitialized) {
            AdaptPersistence.scheduleSaveOnboarding(
                prefs,
                learnerProfile,
                currentModule,
                pathwayPreviewNodes,
                todayGoal,
                isCurrentLessonCompleted
            )
        }
    }

    fun resetToDefaultTrack() {
        domainProgressRecords.clear()

        learnerProfile = LearnerProfile(
            name = "Aryan",
            greeting = "Good morning",
            streakDays = 6,
            dailyMinutesSpent = 25,
            dailyMinutesGoal = 35,
            weeklyHoursCompleted = 3.8f,
            masteryScorePercent = 82,
            activeDomainId = "ai_ml",
            activeDomainTitle = "Artificial Intelligence & ML",
            activeGoal = "Build Real Edge AI Projects",
            skillLevel = DifficultyLevel.INTERMEDIATE,
            preferredLearningStyle = "Hands-on Projects & Code"
        )
        currentModule = LearningModule(
            id = "mod_quant_1",
            title = "INT8 Weight Quantization",
            summary = "Learn how converting 32-bit floating point weights into 8-bit integers accelerates edge neural networks while reducing memory bandwidth.",
            trackName = "Edge AI Systems",
            estimatedMinutes = 12,
            currentStep = 3,
            totalSteps = 4,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
        todayGoal = DailyGoal(
            id = "goal_today",
            title = "Quantization Foundations",
            targetDescription = "Complete 2 practical lessons & 1 concept quiz",
            current = 2,
            target = 3,
            isCompleted = false
        )
        pathwayPreviewNodes = listOf(
            PathwayNode("node_1", 1, "MobileNet Architecture", "Model Compression", MilestoneStatus.COMPLETED, 15),
            PathwayNode("node_2", 2, "INT8 Weight Quantization", "Precision Scaling", MilestoneStatus.IN_PROGRESS, 12),
            PathwayNode("node_3", 3, "Tensor Memory Layouts", "Cache Optimization", MilestoneStatus.LOCKED, 20),
            PathwayNode("node_4", 4, "Edge Pipeline Benchmark", "Profiling & Latency", MilestoneStatus.LOCKED, 25)
        )
        isCurrentLessonCompleted = false
        bookmarkedLessonIds = emptySet()

        domainProgressRecords["ai_ml"] = DomainCurriculumRecord(
            domainId = "ai_ml",
            currentModule = currentModule,
            pathwayNodes = pathwayPreviewNodes,
            todayGoal = todayGoal,
            isLessonCompleted = isCurrentLessonCompleted
        )

        notifyPersistenceChanged()
    }

    fun notifyPersistenceChanged() {
        if (AdaptPersistence.isInitialized) {
            AdaptPersistence.scheduleSaveProfile(learnerProfile)
            AdaptPersistence.scheduleSaveCurriculum(
                currentModule,
                pathwayPreviewNodes,
                todayGoal,
                isCurrentLessonCompleted
            )
            AdaptPersistence.scheduleSaveBookmarks(bookmarkedLessonIds)
        }
    }

    val tutorQuickPrompts = listOf(
        TutorQuickPrompt(
            id = "p1",
            prompt = "Why does INT8 save 4x memory over FP32?",
            tag = "Concept"
        ),
        TutorQuickPrompt(
            id = "p2",
            prompt = "What is post-training quantization?",
            tag = "Quantization"
        ),
        TutorQuickPrompt(
            id = "p3",
            prompt = "How does zero-point offset work?",
            tag = "Math"
        )
    )

    val sampleLesson = LessonContent(
        id = "lesson_int8",
        title = "INT8 Weight Quantization",
        subtitle = "Module 2: Model Compression & Memory Optimization",
        trackName = "Edge AI Systems",
        stepIndex = 3,
        totalSteps = 4,
        estimatedMinutes = 12,
        overview = "Edge microcontrollers and mobile SoCs have strict limits on battery power and on-chip SRAM. Quantization allows neural networks to run locally on resource-constrained hardware by converting 32-bit floating point parameters into compact 8-bit integers.",
        conceptSections = listOf(
            ConceptSection(
                heading = "1. The Memory Bottleneck in Edge AI",
                body = "When running neural networks on mobile or embedded devices, computing math operations in the ALU is relatively cheap in terms of energy. The true bottleneck is reading weights from memory (DRAM). Moving 32 bits for every single weight burns significant battery and creates latency.",
                codeOrFormula = "// Memory footprint comparison per weight:\nFP32: 32 bits = 4 bytes per parameter\nINT8:  8 bits = 1 byte per parameter (75% savings!)"
            ),
            ConceptSection(
                heading = "2. The Linear Quantization Formula",
                body = "We map continuous floating-point weights [r_min, r_max] onto an 8-bit integer grid [-128, 127] or [0, 255] using a scale factor (S) and an integer zero-point offset (Z).",
                codeOrFormula = "Real Value = Scale × (Quantized Value - ZeroPoint)\nr = S × (q - Z)"
            )
        ),
        keyTakeaways = listOf(
            "4x Memory Footprint Reduction: A 16MB MobileNet model compresses down to just ~4MB, fitting inside on-chip SRAM.",
            "Lower Energy Consumption: Reducing DRAM read traffic by 75% saves up to 60-70% total system power.",
            "Accelerated Vector Math: ARM NEON vector registers and NPUs compute multiple 8-bit integer MACs per cycle.",
            "Minimal Accuracy Loss: Properly calibrated models typically retain 98-99%+ of original FP32 accuracy."
        ),
        realWorldExample = RealWorldExample(
            title = "Always-On Smart Security Camera",
            scenario = "An outdoor security sensor uses an ARM Cortex-M55 core to detect human presence. With FP32, the model exceeded the 8MB SRAM ceiling and caused thermal throttling.",
            comparisonMetrics = listOf(
                MetricPair("Model Size", "16.4 MB", "4.1 MB", "75% smaller"),
                MetricPair("SRAM Usage", "9.8 MB", "2.6 MB", "Fits in on-chip SRAM"),
                MetricPair("Inference Latency", "148 ms", "49 ms", "3x faster"),
                MetricPair("Battery Life", "4 months", "14 months", "3.5x longer")
            ),
            impactSummary = "By eliminating external DRAM bus traffic, the camera runs entirely in local static RAM, reducing heat dissipation and quadrupling battery longevity."
        ),
        deepDive = DeepDiveSection(
            title = "Deeper Dive: Calibration & Zero-Point",
            teaser = "How does the quantization algorithm determine the optimal scale without losing precision?",
            detailedExplanation = "Quantization requires a Calibration Dataset (typically 100-200 representative input samples). The system observes the minimum (r_min) and maximum (r_max) activation ranges across each layer.\n\nThe Scale (S) is calculated as:\nS = (r_max - r_min) / 255\n\nThe Zero-Point (Z) is calculated as:\nZ = round(-r_min / S)\n\nIn Symmetric Quantization, where the range is centered around zero, Z is locked to 0. This eliminates the zero-point subtraction step, speeding up hardware multiply-accumulate (MAC) loops on ARM NPUs!",
            formulaExplanation = "Affine formula: q = round(r / S) + Z\nSymmetric formula: q = round(r / S), where Z = 0"
        ),
        practiceQuestion = PracticeQuestion(
            question = "Why does INT8 quantization reduce energy consumption on edge chips even more than it improves raw clock cycle speed?",
            options = listOf(
                "A) Integer arithmetic units operate at a significantly lower clock voltage than floating-point units.",
                "B) Fetching weights across the external DRAM bus consumes up to 100x more energy than arithmetic compute; 4x smaller weights drastically reduce bus traffic.",
                "C) INT8 models bypass the activation functions like ReLU and GeLU entirely.",
                "D) Integer quantization automatically removes 50% of the network layers during compilation."
            ),
            correctIndex = 1,
            explanation = "Correct! On edge SoCs, DRAM bus access is by far the biggest energy drain. By shrinking model size by 75%, weights fit in local cache/SRAM, virtually eliminating high-energy DRAM transfers."
        )
    )
}
