package com.example.adapt_ai_ui.data.platform

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class ProfileIsolationTest {
    @Test fun scopedStorageSeparatesLearnersAndTopicChats() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, PlatformDatabase::class.java).build()
        try {
            val dao = db.store()
            listOf("sam", "robin").forEach { id ->
                dao.insertProfile(PlatformDatabase.Profile().also { it.id = id; it.name = id })
            }
            val first = ScopedStorage(dao, "sam", "algebra")
            val otherTopic = ScopedStorage(dao, "sam", "hiragana")
            val otherLearner = ScopedStorage(dao, "robin", "algebra")
            first.write("learner_profile", "private profile")
            first.write("tutor_session", "private algebra")
            first.write("practice_state", "private attempt")
            assertNull(otherTopic.read("tutor_session"))
            assertNull(otherLearner.read("learner_profile"))
            assertNull(otherLearner.read("practice_state"))
            otherLearner.write("tutor_session", "other algebra")
            dao.deleteProfile("sam")
            assertNull(first.read("tutor_session"))
            assertEquals("other algebra", otherLearner.read("tutor_session"))
        } finally { db.close() }
    }
}
