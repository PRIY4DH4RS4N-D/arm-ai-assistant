package com.example.adapt_ai_ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.adapt_ai_ui.data.LearnerProfile
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.TutorPersonalization

class TutorViewModel(application: Application) : AndroidViewModel(application) {
    init {
        TutorRepository.initializeRuntime(application)
    }

    val messages get() = TutorRepository.conversationMessages
    val mode get() = TutorRepository.activeMode
    val runtime get() = TutorRepository.runtime
    val isGenerating get() = TutorRepository.isGenerating
    val draft get() = TutorRepository.draftAnswer

    fun send(text: String, learner: LearnerProfile, topic: String, personalization: TutorPersonalization = TutorPersonalization()) {
        TutorRepository.sendUserQuery(
            text, learner.activeDomainId, learner.activeDomainTitle, topic,
            learner.activeGoal, learner.skillLevel, learner.preferredLearningStyle, personalization
        )
    }

    fun stop() = TutorRepository.stopGeneration()
    fun retry(learner: LearnerProfile, topic: String, personalization: TutorPersonalization) =
        TutorRepository.retryLast(learner, topic, personalization)

    override fun onCleared() {
        stop()
        super.onCleared()
    }
}
