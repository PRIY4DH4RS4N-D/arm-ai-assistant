package com.example.adapt_ai_ui.data.platform

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class LearningActivity(
    val minutesByDate: Map<LocalDate, Long>,
    val qualifiedDays: Set<LocalDate>,
    val currentStreak: Int,
    val bestStreak: Int,
    val completedLessons: Int,
    val quizzes: Int,
    val perfectQuizzes: Int
)

internal fun LearningSession.activity(today: LocalDate, zone: ZoneId): LearningActivity {
    val seconds = mutableMapOf<LocalDate, Long>()
    val qualifying = mutableSetOf<LocalDate>()
    val attempts = topicStates.values.flatMap { it.attempts.filter { attempt ->
        attempt.quiz.questions.indices.any { index -> questionKey(attempt.quiz, index) !in it.reported }
    }.map { attempt -> attempt to it.reported } }
    fun date(epoch: Long) = Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()
    topicStates.values.forEach { state ->
        state.completedAt?.let { qualifying += date(it) }
        state.studyByDate.orEmpty().forEach { (key, value) ->
            val day = runCatching { LocalDate.parse(key) }.getOrNull()
            if (day != null && day <= today && value > 0) seconds.merge(day, value, Long::plus)
        }
    }
    qualifying += attempts.map { date(it.first.finishedAt) }
    qualifying += seconds.filterValues { it >= 60 }.keys
    val days = qualifying.filter { it <= today }.sorted()
    var best = 0
    var consecutive = 0
    var previous: LocalDate? = null
    for (day in days) {
        consecutive = if (previous?.plusDays(1) == day) consecutive + 1 else 1
        best = maxOf(best, consecutive)
        previous = day
    }
    return LearningActivity(
        seconds.mapValues { it.value / 60 }, days.toSet(),
        if (previous == today || previous == today.minusDays(1)) consecutive else 0, best,
        topicStates.values.count { it.completedAt != null }, attempts.size,
        attempts.count { (attempt, reported) ->
            val valid = attempt.quiz.questions.indices.count { questionKey(attempt.quiz, it) !in reported }
            attempt.correctCount(reported) == valid
        }
    )
}
