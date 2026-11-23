package com.example.adapt_ai_ui.data

enum class MilestoneStatus {
    COMPLETED,
    IN_PROGRESS,
    LOCKED
}

enum class DifficultyLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class LearnerProfile(
    val name: String,
    val greeting: String,
    val streakDays: Int,
    val dailyMinutesSpent: Int,
    val dailyMinutesGoal: Int,
    val weeklyHoursCompleted: Float,
    val masteryScorePercent: Int,
    val activeDomainId: String = "ai_ml",
    val activeDomainTitle: String = "Artificial Intelligence & ML",
    val activeGoal: String = "Build Real Edge AI Projects",
    val skillLevel: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
    val preferredLearningStyle: String = "Hands-on Projects & Code"
)

data class LearningModule(
    val id: String,
    val title: String,
    val summary: String,
    val trackName: String,
    val estimatedMinutes: Int,
    val currentStep: Int,
    val totalSteps: Int,
    val difficulty: DifficultyLevel
) {
    val progressFraction: Float
        get() = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

data class DailyGoal(
    val id: String,
    val title: String,
    val targetDescription: String,
    val current: Int,
    val target: Int,
    val isCompleted: Boolean = false
) {
    val progressFraction: Float
        get() = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1f) else 0f
}

data class PathwayNode(
    val id: String,
    val index: Int,
    val title: String,
    val track: String,
    val status: MilestoneStatus,
    val estimatedMinutes: Int
)

data class TutorQuickPrompt(
    val id: String,
    val prompt: String,
    val tag: String
)

data class LessonContent(
    val id: String,
    val title: String,
    val subtitle: String,
    val trackName: String,
    val stepIndex: Int,
    val totalSteps: Int,
    val estimatedMinutes: Int,
    val overview: String,
    val conceptSections: List<ConceptSection>,
    val keyTakeaways: List<String>,
    val realWorldExample: RealWorldExample,
    val deepDive: DeepDiveSection,
    val practiceQuestion: PracticeQuestion
)

data class ConceptSection(
    val heading: String,
    val body: String,
    val codeOrFormula: String? = null
)

data class RealWorldExample(
    val title: String,
    val scenario: String,
    val comparisonMetrics: List<MetricPair>,
    val impactSummary: String
)

data class MetricPair(
    val label: String,
    val beforeValue: String,
    val afterValue: String,
    val improvementLabel: String
)

data class DeepDiveSection(
    val title: String,
    val teaser: String,
    val detailedExplanation: String,
    val formulaExplanation: String
)

data class PracticeQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

enum class PracticeMode(val title: String) {
    SESSION("Practice Session"),
    REVISION("Mistake Notebook")
}

data class PracticeQuestionItem(
    val id: String,
    val topicTag: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: DifficultyLevel
)

data class QuestionSubmission(
    val questionId: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class MistakeRecord(
    val question: PracticeQuestionItem,
    val lastSelectedOptionIndex: Int,
    val mistakeCount: Int,
    val nextRevisionDays: Int = 1,
    val isResolved: Boolean = false
)

data class TopicCompetency(
    val topic: String,
    val correctAttempts: Int,
    val totalAttempts: Int
) {
    val masteryPercentage: Int
        get() = if (totalAttempts > 0) (correctAttempts * 100 / totalAttempts).coerceIn(0, 100) else 0
}

data class PracticeSessionSummary(
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercentage: Int,
    val xpEarned: Int,
    val incorrectQuestions: List<PracticeQuestionItem>,
    val topicBreakdown: List<TopicCompetency>
)

data class LearningDomain(
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val iconEmoji: String,
    val suggestedTopics: List<String>,
    val recommendedGoals: List<String>,
    val estimatedHours: Int,
    val defaultPathwayNodes: List<PathwayNode>,
    val defaultModule: LearningModule,
    val diagnosticQuestions: List<DiagnosticQuestion> = emptyList(),
    val isCustom: Boolean = false
)

data class DiagnosticQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class OnboardingPreferences(
    val domainId: String,
    val domainTitle: String,
    val goal: String,
    val skillLevel: DifficultyLevel,
    val dailyMinutes: Int,
    val interests: List<String>,
    val learningStyle: String
)

enum class TutorMode(
    val title: String,
    val description: String,
    val iconEmoji: String
) {
    TEACH_ME(
        title = "Teach Me",
        description = "Explain concepts in clear, beginner-friendly language with analogies",
        iconEmoji = "🌱"
    ),
    SOCRATIC(
        title = "Socratic",
        description = "Guide understanding through progressive hints and probing questions",
        iconEmoji = "🤔"
    ),
    DEBUG(
        title = "Debug & Code",
        description = "Analyze code snippets, identify bugs, and trace execution logic",
        iconEmoji = "🐞"
    ),
    PRACTICE(
        title = "Practice Drill",
        description = "Generate quick contextual scenarios and challenge questions",
        iconEmoji = "🎯"
    ),
    REVIEW(
        title = "Review & Retain",
        description = "Summarize core takeaways, formulas, and identify knowledge gaps",
        iconEmoji = "📝"
    )
}

data class TutorRequest(
    val query: String,
    val mode: TutorMode,
    val domainId: String,
    val domainTitle: String,
    val topicTitle: String,
    val goal: String,
    val skillLevel: DifficultyLevel,
    val learningStyle: String,
    val conversationHistory: List<TutorConversationMessage> = emptyList()
)

data class TutorResponse(
    val text: String,
    val conceptExplanation: String? = null,
    val steps: List<String> = emptyList(),
    val exampleOrAnalogy: String? = null,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val formula: String? = null,
    val keyTakeaways: List<String> = emptyList(),
    val followUpPrompts: List<String> = emptyList(),
    val practiceQuestion: String? = null,
    val executionModelLabel: String = "Prototype Engine (Simulated On-Device Runtime)",
    val inferenceTimeMs: Long = 380L
)

data class TutorConversationMessage(
    val id: String,
    val sender: String,
    val isTutor: Boolean,
    val content: String,
    val mode: TutorMode = TutorMode.SOCRATIC,
    val structuredPayload: TutorResponse? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class DomainCurriculumRecord(
    val domainId: String,
    val currentModule: LearningModule,
    val pathwayNodes: List<PathwayNode>,
    val todayGoal: DailyGoal,
    val isLessonCompleted: Boolean
)
