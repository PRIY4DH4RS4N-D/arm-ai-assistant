package com.example.adapt_ai_ui.data.platform

import com.example.adapt_ai_ui.data.DifficultyLevel
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.Strictness
import java.util.Locale

internal val platformJson = GsonBuilder().setStrictness(Strictness.STRICT).create()

data class QuizQuestion(val prompt: String, val options: List<String>, val correct: Int, val explanation: String, val competency: String)
data class TopicQuiz(val id: String, val questions: List<QuizQuestion>, val difficulty: DifficultyLevel, val diagnostic: Boolean, val modelHash: String)
data class QuizEvidence(val quiz: TopicQuiz, val answers: List<Int>, val finishedAt: Long)
data class DraftQuiz(val quiz: TopicQuiz, val answers: List<Int> = emptyList())
data class TopicState(
    val lesson: String = "", val completedAt: Long? = null, val bookmarked: Boolean = false,
    val note: String = "", val draft: DraftQuiz? = null, val attempts: List<QuizEvidence> = emptyList(),
    val reported: Set<String> = emptySet(), val studySeconds: Long = 0,
    val studyByDate: Map<String, Long>? = null
)
data class SavedScan(val jpeg: String, val text: String)
data class AssessedMastery(val percentage: Int, val responses: Int) {
    val lowConfidence get() = responses < 5
}

object QuizValidator {
    fun normalizeAlgebra(raw: String, topic: LearningTopic): String? {
        if (topic.id != "mathematics/algebra") return null
        val lines = raw.lineSequence().map { it.trim().replace("**", "") }
        val prompt = lines.map { it.replace(Regex("(?i)^question\\s*:\\s*"), "") }
            .firstOrNull { linearEquation.matches(it) } ?: return null
        val solution = solveLinearEquation(prompt) ?: return null
        require(solution.isFinite() && kotlin.math.abs(solution) < 1_000_000 && solution % 1.0 == 0.0)
        val answer = solution.toInt()
        val choices = listOf(answer - 1, answer, answer + 1).shuffled()
        return platformJson.toJson(mapOf(
            "v" to 1, "topic" to topic.name, "q" to listOf(mapOf(
                "p" to prompt, "o" to choices.map(Int::toString), "a" to choices.indexOf(answer),
                "e" to "Subtract the constant from both sides and divide by the coefficient of x. Substitution gives x = $answer.",
                "c" to "Linear equations"
            ))
        ))
    }

    fun parse(raw: String, topic: LearningTopic, count: Int): List<QuizQuestion> {
        require(count in 1..5 && raw.length <= 6000)
        val clean = raw.trim().removePrefix("\u0060\u0060\u0060json").removePrefix("\u0060\u0060\u0060")
            .removeSuffix("\u0060\u0060\u0060").trim()
        val root = platformJson.fromJson(clean, JsonObject::class.java) ?: error("Quiz JSON is empty")
        require(root.keySet() == setOf("v", "topic", "q"))
        require(root["v"]?.asJsonPrimitive?.toString() == "1" && root.string("topic", 90) == topic.name)
        val list = root.getAsJsonArray("q")
        require(list.size() == count)
        val prompts = hashSetOf<String>()
        return list.map { item ->
            val q = item.asJsonObject
            require(q.keySet() == setOf("p", "o", "a", "e", "c"))
            val prompt = q.string("p", 300)
            require(prompts.add(prompt.normalized()) && !Regex("(?i)\\b(correct answer|answer is)\\b").containsMatchIn(prompt))
            val options = q.getAsJsonArray("o").map { it.asJsonPrimitive.strictString(140) }
            require(options.size in 2..4 && options.map(String::normalized).toSet().size == options.size)
            require(options.size != 2 || options.map(String::normalized).toSet() == setOf("true", "false"))
            val correct = q["a"].asJsonPrimitive.toString().toIntOrNull() ?: error("Invalid answer index")
            require(correct in options.indices)
            verifyLinearEquation(prompt, options[correct])
            val competency = q.string("c", 120)
            require(competency.equals(topic.name, true) || topic.concepts.any { it.equals(competency, true) })
            QuizQuestion(prompt, options, correct, q.string("e", 500), competency)
        }
    }

    private val linearEquation = Regex("(?i)^(?:solve(?: for x)?\\s*:?\\s*)?([+-]?\\d*)\\s*x\\s*([+-])\\s*(\\d+)\\s*=\\s*([+-]?\\d+)[.?!]?$")

    private fun verifyLinearEquation(prompt: String, correctOption: String) {
        val expected = solveLinearEquation(prompt) ?: return
        val answer = correctOption.removePrefix("x").trim().removePrefix("=").trim()
        val parts = answer.split('/')
        val supplied = when (parts.size) {
            1 -> parts[0].toDoubleOrNull()
            2 -> parts[0].toDoubleOrNull()?.let { numerator -> parts[1].toDoubleOrNull()?.let { numerator / it } }
            else -> null
        }
        require(supplied != null && supplied.isFinite() && kotlin.math.abs(expected - supplied) < 1e-7)
    }

    private fun solveLinearEquation(prompt: String): Double? {
        val match = linearEquation.matchEntire(prompt) ?: return null
        val (coefficient, sign, magnitude, rhs) = match.destructured
        val factor = when (coefficient) { "", "+" -> 1.0; "-" -> -1.0; else -> coefficient.toDouble() }
        require(factor != 0.0 && factor.isFinite())
        return (rhs.toDouble() - (sign + magnitude).toDouble()) / factor
    }
}

fun TopicState.answer(index: Int, now: Long): TopicState {
    val current = requireNotNull(draft)
    val question = current.quiz.questions.getOrNull(current.answers.size) ?: error("Quiz already submitted")
    require(index in question.options.indices)
    val answers = current.answers + index
    return if (answers.size == current.quiz.questions.size) {
        check(attempts.none { it.quiz.id == current.quiz.id })
        copy(draft = null, attempts = attempts + QuizEvidence(current.quiz, answers, now))
    } else copy(draft = current.copy(answers = answers))
}

fun QuizEvidence.correctCount(excluded: Set<String> = emptySet()) = quiz.questions.indices.count {
    questionKey(quiz, it) !in excluded && quiz.questions[it].correct == answers[it]
}

fun questionKey(quiz: TopicQuiz, index: Int) = "${quiz.id}:$index"

fun TopicState.mastery(competency: String? = null, now: Long = System.currentTimeMillis()): AssessedMastery? {
    val evidence = attempts.asReversed().asSequence().filter { !it.quiz.diagnostic }.flatMap { attempt ->
        attempt.quiz.questions.asSequence().withIndex().filter { (index, question) ->
            questionKey(attempt.quiz, index) !in reported && (competency == null || question.competency == competency)
        }.map { (index, question) ->
            val difficulty = when (attempt.quiz.difficulty) {
                DifficultyLevel.BEGINNER -> 1.0
                DifficultyLevel.INTERMEDIATE -> 1.5
                DifficultyLevel.ADVANCED -> 2.0
            }
            val days = (now - attempt.finishedAt).coerceAtLeast(0L) / 86_400_000.0
            (question.correct == attempt.answers[index]) to difficulty / (1.0 + days / 30.0)
        }
    }.take(30).toList()
    if (evidence.isEmpty()) return null
    return AssessedMastery(kotlin.math.round(100 * evidence.filter { it.first }.sumOf { it.second } /
        evidence.sumOf { it.second }).toInt(), evidence.size)
}

fun TopicState.revisionAt(): Long? {
    var success = 0
    var due: Long? = null
    for (attempt in attempts.filterNot { it.quiz.diagnostic }) {
        val valid = attempt.quiz.questions.indices.filter { questionKey(attempt.quiz, it) !in reported }
        if (valid.isEmpty()) continue
        val correct = valid.all { attempt.quiz.questions[it].correct == attempt.answers[it] }
        success = if (correct) (success + 1).coerceAtMost(4) else 0
        due = attempt.finishedAt + (1L shl success) * 86_400_000L
    }
    return due
}

private fun JsonObject.string(key: String, max: Int) = get(key).asJsonPrimitive.strictString(max)
private fun JsonPrimitive.strictString(max: Int): String {
    require(isString)
    return asString.trim().also { require(it.isNotEmpty() && it.length <= max) }
}
private fun String.normalized(): String =
    trim().lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
