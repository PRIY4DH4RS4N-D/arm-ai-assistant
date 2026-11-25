package com.example.adapt_ai_ui.data.platform

import com.example.adapt_ai_ui.data.DifficultyLevel
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class LearningAssessmentTest {
    private val topic = LearningCatalog.topics.single { it.name == "Algebra" }
    private val question = """{"p":"Solve x + 1 = 5.","o":["3","4","5"],"a":1,"e":"Subtract one.","c":"Linear equations"}"""
    private fun json(value: String = question) = """{"v":1,"topic":"Algebra","q":[$value]}"""
    private fun quiz(diagnostic: Boolean = false, difficulty: DifficultyLevel = DifficultyLevel.BEGINNER) =
        TopicQuiz("q$diagnostic${difficulty.name}", QuizValidator.parse(json(), topic, 1), difficulty, diagnostic, "model")

    @Test fun searchIncludesSubjectsAndNestedConcepts() {
        assertTrue(LearningCatalog.search("mathematics linear equations", null, emptyList()).any { it.name == "Algebra" })
        assertTrue(LearningCatalog.search("hIrAgAnA", "Languages", emptyList()).any { it.subject == "Japanese" })
        assertEquals(LearningCatalog.topics.size, LearningCatalog.topics.map { it.id }.toSet().size)
    }

    @Test fun strictValidationRejectsMissingWrongAndDuplicateFields() {
        assertEquals(1, QuizValidator.parse(json(), topic, 1).single().correct)
        listOf(json().dropLast(1), json().replace("\"a\":1", "\"a\":7"),
            json().replace("\"a\":1", "\"a\":\"1\""), json().replace("Linear equations", "VLSI"),
            json().replace("\"3\",\"4\",\"5\"", "\"4\",\"4\",\"5\""),
            json().replace("Solve x + 1 = 5.", "Correct answer is 4."), json() + " stray text").forEach {
            assertTrue(it, runCatching { QuizValidator.parse(it, topic, 1) }.isFailure)
        }
        assertTrue(runCatching { QuizValidator.parse(json("$question,$question"), topic, 2) }.isFailure)
    }

    @Test fun scoringAndInvalidationUseStoredAnswers() {
        val start = TopicState(draft = DraftQuiz(quiz()))
        assertNull(start.mastery())
        val result = start.answer(1, 1)
        assertEquals(1, result.attempts.single().correctCount())
        assertEquals(AssessedMastery(100, 1), result.mastery(now = 1))
        assertTrue(runCatching { result.answer(1, 1) }.isFailure)
        assertNull(result.copy(reported = setOf(questionKey(quiz(), 0))).mastery())
        assertNull(TopicState(draft = DraftQuiz(quiz(true))).answer(1, 1).mastery())
    }

    @Test fun generatedEquationIsConvertedAndCheckedIndependently() {
        val raw = "**Question:** Solve for x: 2x + 5 = 11\n**Answer:** x = 7"
        val normalized = requireNotNull(QuizValidator.normalizeAlgebra(raw, topic))
        val item = QuizValidator.parse(normalized, topic, 1).single()
        assertEquals("3", item.options[item.correct])
        assertNull(QuizValidator.normalizeAlgebra("no equation", topic))
    }

    @Test fun incompleteQuizIsNotIncludedAndDifficultyWeightsApply() {
        val q1 = quiz()
        val q2 = quiz(difficulty = DifficultyLevel.ADVANCED)
        val draft = TopicState(draft = DraftQuiz(q1.copy(questions = q1.questions + q1.questions)))
        assertNull(draft.answer(1, 100).mastery())
        val yesterday = 86_400_000L
        val result = TopicState(attempts = listOf(QuizEvidence(q1, listOf(0), 0), QuizEvidence(q2, listOf(1), yesterday)))
        assertTrue(result.mastery(now = yesterday)!!.percentage in 66..68)
        assertTrue(result.mastery(now = yesterday)!!.lowConfidence)
        assertEquals(yesterday * 3, result.revisionAt())
    }

    @Test fun scheduleRespectsDeadlineDaysAndBudget() {
        val start = LocalDate.of(2026, 11, 23)
        val prefs = LearningPreferences(minutes = 15, studyDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            deadline = start.plusDays(3))
        val blocks = studyWeek(topic, prefs, start.plusDays(1), start)
        assertEquals(listOf(start, start.plusDays(2)), blocks.map { it.date })
        assertEquals(30, blocks.sumOf { it.minutes })
        assertTrue(blocks.last().title.startsWith("Review"))
    }
}
