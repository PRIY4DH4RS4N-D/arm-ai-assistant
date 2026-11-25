package com.example.adapt_ai_ui.data.platform

import android.content.Context
import androidx.room.Room
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.persistence.AdaptJsonSerializers
import com.example.adapt_ai_ui.data.persistence.AdaptPersistenceRepository
import com.example.adapt_ai_ui.data.persistence.FileStorageBackend
import com.example.adapt_ai_ui.data.persistence.StorageBackend
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocalLearner(val id: String, val name: String, val guest: Boolean)

data class LearningSession(
    val profile: LocalLearner,
    val preferences: LearningPreferences,
    val topicStates: Map<String, TopicState>,
    val legacy: AdaptPersistenceRepository,
    val legacyDomains: Map<String, String>
) {
    val catalog get() = LearningCatalog.topics + preferences.customTopics
    val topic get() = catalog.firstOrNull { it.id == preferences.activeTopic }
    val topicState get() = topicStates[preferences.activeTopic] ?: TopicState()
}

class PlatformRepository(private val context: Context) {
    private val db = Room.databaseBuilder(context.applicationContext, PlatformDatabase::class.java, "aurora-learning.db").build()
    private val dao = db.store()
    private val device = context.getSharedPreferences("aurora_device", Context.MODE_PRIVATE)

    suspend fun initialize(): Pair<List<LocalLearner>, String?> = withContext(Dispatchers.IO) {
        db.runInTransaction {
            if (!device.getBoolean("legacy_imported", false) && dao.profile("legacy") == null) {
                val directory = File(context.filesDir, "adapt_ai_data")
                val keys = directory.listFiles().orEmpty().mapNotNull {
                    when {
                        it.name.endsWith(".json") -> it.name.removeSuffix(".json")
                        it.name.endsWith(".json.bak") -> it.name.removeSuffix(".json.bak")
                        else -> null
                    }
                }.toSet()
                if (keys.isNotEmpty()) {
                    val files = FileStorageBackend(directory)
                    val name = files.read("learner_profile")?.let(AdaptJsonSerializers::deserializeLearnerProfile)
                        ?.name?.takeUnless { it == "learner" } ?: "My learning"
                    insert("legacy", name, false)
                    val backend = ScopedStorage(dao, "legacy", null)
                    keys.forEach { key -> files.read(key)?.let { backend.write(key, it) } }
                }
            }
        }
        val learners = profiles()
        val active = device.getString("active_profile", null)?.takeIf { id -> learners.any { it.id == id } }
            ?: learners.firstOrNull { it.id == "legacy" && !device.getBoolean("legacy_imported", false) }?.id
        check(device.edit().putBoolean("legacy_imported", true).putString("active_profile", active).commit())
        learners to active
    }

    suspend fun list(): List<LocalLearner> = withContext(Dispatchers.IO) { profiles() }

    suspend fun create(name: String, guest: Boolean): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        db.runInTransaction { insert(id, name.trim(), guest) }
        id
    }

    suspend fun rename(id: String, name: String): Unit = withContext(Dispatchers.IO) {
        require(name.isNotBlank() && name.length <= 60)
        db.runInTransaction {
            dao.updateProfile(id, name, false)
            val backend = ScopedStorage(dao, id, null)
            val saved = backend.read("learner_profile")?.let(AdaptJsonSerializers::deserializeLearnerProfile)
            if (saved != null) backend.write("learner_profile", AdaptJsonSerializers.serializeLearnerProfile(saved.copy(name = name)))
        }
    }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.IO) {
        dao.deleteProfile(id)
        if (device.getString("active_profile", null) == id) device.edit().remove("active_profile").apply()
    }

    suspend fun rememberActive(id: String?) = withContext(Dispatchers.IO) {
        check(device.edit().putString("active_profile", id).commit())
    }

    suspend fun open(id: String): LearningSession = withContext(Dispatchers.IO) {
        val profile = requireNotNull(dao.profile(id)).learner()
        val preferences = readPreferences(id)
        val backend = ScopedStorage(dao, id, preferences.activeTopic)
        val legacy = AdaptPersistenceRepository(backend)
        val domains = com.example.adapt_ai_ui.data.DomainRepository.domains.mapNotNull { domain ->
            legacy.getRawCurriculumForDomain(domain.id)?.let { domain.id to it }
        }.toMap()
        val states = (preferences.interests + preferences.customTopics.map { it.id }).mapNotNull { topic ->
            dao.read(id, "topic/$topic")?.let { payload -> topic to platformJson.fromJson(payload, TopicState::class.java) }
        }.toMap()
        LearningSession(profile, preferences, states, legacy, domains)
    }

    suspend fun savePreferences(id: String, preferences: LearningPreferences): Unit = withContext(Dispatchers.IO) {
        require(preferences.minutes in 5..180 && preferences.studyDays.isNotEmpty())
        require(preferences.activeTopic == null || preferences.activeTopic in preferences.interests)
        put(id, "platform/preferences", platformJson.toJson(StoredPreferences(
            preferences.interests, preferences.customTopics, preferences.activeTopic, preferences.goal,
            preferences.level, preferences.minutes, preferences.studyDays, preferences.deadline?.toString(),
            preferences.language, preferences.style, preferences.assistant, preferences.recentTopics
        )))
    }

    suspend fun updateTopic(profile: String, topic: String, change: (TopicState) -> TopicState): TopicState =
        withContext(Dispatchers.IO) {
            db.runInTransaction(java.util.concurrent.Callable {
                val previous = dao.read(profile, "topic/$topic")?.let { platformJson.fromJson(it, TopicState::class.java) } ?: TopicState()
                change(previous).also { put(profile, "topic/$topic", platformJson.toJson(it)) }
            })
        }

    suspend fun loadScan(profile: String, topic: String?) = withContext(Dispatchers.IO) {
        dao.read(profile, scanKey(topic))?.let { platformJson.fromJson(it, SavedScan::class.java) }
    }

    suspend fun storeScan(profile: String, topic: String?, scan: SavedScan?) = withContext(Dispatchers.IO) {
        if (scan == null) dao.delete(profile, scanKey(topic))
        else put(profile, scanKey(topic), platformJson.toJson(scan))
        Unit
    }

    private fun scanKey(topic: String?) = "scan/${topic ?: "_profile"}"

    suspend fun codingProgress(profile: String): CodingProgress = withContext(Dispatchers.IO) {
        dao.read(profile, "platform/coding")?.let { platformJson.fromJson(it, CodingProgress::class.java) }
            ?: CodingProgress()
    }

    suspend fun saveCodingProgress(profile: String, progress: CodingProgress) = withContext(Dispatchers.IO) {
        put(profile, "platform/coding", platformJson.toJson(progress))
    }

    private fun readPreferences(id: String): LearningPreferences {
        val stored = dao.read(id, "platform/preferences")
            ?.let { platformJson.fromJson(it, StoredPreferences::class.java) } ?: return LearningPreferences()
        return LearningPreferences(stored.interests, stored.customTopics, stored.activeTopic, stored.goal,
            stored.level, stored.minutes, stored.studyDays, stored.deadline?.let(LocalDate::parse),
            stored.language, stored.style, stored.assistant, stored.recentTopics)
    }

    private fun insert(id: String, name: String, guest: Boolean) {
        require(name.isNotBlank() && name.length <= 60)
        dao.insertProfile(PlatformDatabase.Profile().also {
            it.id = id
            it.name = name
            it.guest = guest
            it.createdAt = System.currentTimeMillis()
        })
    }

    private fun put(id: String, key: String, payload: String) {
        dao.write(PlatformDatabase.Record().also { it.profileId = id; it.recordKey = key; it.payload = payload })
    }

    private fun profiles() = dao.profiles().map { it.learner() }
    private fun PlatformDatabase.Profile.learner() = LocalLearner(id, name, guest)

    private data class StoredPreferences(
        val interests: Set<String>, val customTopics: List<LearningTopic>, val activeTopic: String?,
        val goal: String, val level: DifficultyLevel?, val minutes: Int, val studyDays: Set<DayOfWeek>,
        val deadline: String?, val language: String, val style: String, val assistant: String, val recentTopics: List<String>
    )
}

internal class ScopedStorage(
    private val dao: PlatformDatabase.Store, private val profile: String, private val topic: String?
) : StorageBackend {
    private fun scope(key: String) = if (key == "tutor_session" && topic != null) "tutor/$topic" else "legacy/$key"
    override fun read(key: String): String? = dao.read(profile, scope(key))
    override fun write(key: String, content: String): Boolean {
        dao.write(PlatformDatabase.Record().also { it.profileId = profile; it.recordKey = scope(key); it.payload = content })
        return true
    }
    override fun delete(key: String): Boolean = dao.delete(profile, scope(key)) != 0
    override fun clearAll(): Boolean {
        dao.clear(profile)
        return true
    }
}
