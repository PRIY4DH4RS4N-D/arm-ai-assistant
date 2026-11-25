package com.example.adapt_ai_ui.data

import com.example.adapt_ai_ui.inference.LocalTutor
import com.example.adapt_ai_ui.inference.TutorEvent

internal const val AURORA_RUNTIME = "Llama 3.2 1B · ExecuTorch 1.0.0"

class ExecuTorchTutorEngine(private val runtime: LocalTutor) : TutorEngine {
    override suspend fun generateResponse(
        request: TutorRequest,
        onToken: (String) -> Unit
    ): TutorResponse {
        val text = StringBuilder()
        var elapsed = 0L
        runtime.generate(buildLlamaPrompt(request)).collect { event ->
            when (event) {
                is TutorEvent.Token -> {
                    text.append(event.text)
                    onToken(event.text)
                }
                is TutorEvent.Complete -> elapsed = event.elapsedMs
            }
        }
        val result = text.toString().substringBefore("<|eot_id|>").substringBefore("<|end_of_text|>").trim()
        check(result.isNotBlank())
        val code = Regex("(?s)\u0060\u0060\u0060([^\\n]*)\\n(.*?)\u0060\u0060\u0060").find(result)
        return TutorResponse(
            text = result,
            codeSnippet = code?.groupValues?.get(2)?.trim(),
            codeLanguage = code?.groupValues?.get(1)?.trim(),
            followUpPrompts = getSuggestedPrompts(request.domainId, request.topicTitle, request.mode),
            executionModelLabel = AURORA_RUNTIME,
            inferenceTimeMs = elapsed,
            sourceLabel = request.curriculumExcerpt?.let { SampleData.sampleLesson.title }
        )
    }
}

internal fun buildLlamaPrompt(request: TutorRequest): String {
    val instruction = when (request.mode) {
        TutorMode.TEACH_ME -> "Explain intuition, detail and an example. Recap and ask one check question."
        TutorMode.SOCRATIC -> "Ask one guiding question. Give progressive hints and assess the learner's reasoning."
        TutorMode.DEBUG -> "Identify likely issues and explain a fix. Never claim you ran the code."
        TutorMode.PRACTICE -> "Give one exercise and wait for an answer. Explain the rubric after an attempt."
        TutorMode.REVIEW -> "Recap the topic and use one retrieval question, focusing on a relevant mistake."
    }
    fun String.bounded(maxBytes: Int): String {
        var result = replace("<|", "‹|").take(maxBytes)
        while (result.toByteArray(Charsets.UTF_8).size > maxBytes) result = result.dropLast(1)
        return if (result.lastOrNull()?.isHighSurrogate() == true) result.dropLast(1) else result
    }
    fun turn(role: String, text: String) =
        "<|start_header_id|>$role<|end_header_id|>\n\n$text<|eot_id|>"

    val context = buildString {
        append("Subject ${request.domainTitle.bounded(65)}. Lesson ${request.topicTitle.bounded(65)}. ")
        append("${request.skillLevel.label}. Goal ${request.goal.bounded(55)}. ")
        append("Style ${request.learningStyle.bounded(35)}. ")
        request.recentMistake?.let { append("Gap ${it.bounded(65)}. ") }
        request.curriculumExcerpt?.let { append("Local lesson excerpt ${it.bounded(145)}.") }
    }
    return buildString {
        append(turn("system", "You are Aurora, an offline learning tutor. $instruction $context"))
        request.conversationHistory.takeLast(2).forEach {
            append(turn(if (it.isTutor) "assistant" else "user", it.content.bounded(110)))
        }
        append(turn("user", request.query.bounded(460)))
        append("<|start_header_id|>assistant<|end_header_id|>\n\n")
    }
}

internal fun retrieveLessonExcerpt(request: TutorRequest): String? {
    if (request.domainId != "ai_ml") return null
    val lesson = SampleData.sampleLesson
    val words = Regex("[\\p{L}\\p{N}]+").findAll(request.query.lowercase())
        .map { it.value }.filter { it.length >= 4 }.toSet()
    val ranked = lesson.conceptSections.map { section ->
        val search = "${section.heading} ${section.body}".lowercase()
        section to words.count { it in search }
    }.maxByOrNull { it.second }
    return when {
        ranked != null && ranked.second > 0 -> "${lesson.title} / ${ranked.first.heading}. ${ranked.first.body}"
        request.topicTitle.contains("quantization", ignoreCase = true) -> "${lesson.title}. ${lesson.overview}"
        else -> null
    }
}
