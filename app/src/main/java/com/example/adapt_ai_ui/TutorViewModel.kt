package com.example.adapt_ai_ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.adapt_ai_ui.data.LearnerProfile
import com.example.adapt_ai_ui.data.TutorRepository

class TutorViewModel(application: Application) : AndroidViewModel(application) {
    init {
        TutorRepository.initializeRuntime(application)
    }

    val messages get() = TutorRepository.conversationMessages
    val mode get() = TutorRepository.activeMode
    val runtime get() = TutorRepository.runtime
    val isGenerating get() = TutorRepository.isGenerating
    val draft get() = TutorRepository.draftAnswer

    fun send(text: String, learner: LearnerProfile, topic: String) {
        TutorRepository.sendUserQuery(
            text, learner.activeDomainId, learner.activeDomainTitle, topic,
            learner.activeGoal, learner.skillLevel, learner.preferredLearningStyle
        )
    }

    fun stop() = TutorRepository.stopGeneration()
    fun retry() = TutorRepository.retryLast()

    override fun onCleared() {
        stop()
        super.onCleared()
    }
}
