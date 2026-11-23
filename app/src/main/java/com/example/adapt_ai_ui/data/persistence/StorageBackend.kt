package com.example.adapt_ai_ui.data.persistence

import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Storage backend abstraction to decouple persistence from Android Framework APIs.
 * Enables running pure JVM unit tests without needing device / Robolectric mocks.
 */
interface StorageBackend {
    fun read(key: String): String?
    fun write(key: String, content: String): Boolean
    fun delete(key: String): Boolean
    fun clearAll(): Boolean
}

/**
 * File-based atomic storage engine with per-key concurrency locking.
 * Writes to a unique temporary file before atomic renaming, preventing partial-write corruption
 * on process termination, battery depletion, or simultaneous multi-threaded access.
 */
class FileStorageBackend(private val directory: File) : StorageBackend {

    private val keyLocks = ConcurrentHashMap<String, Any>()
    private val tempFileCounter = AtomicLong(0L)

    private fun getLockForKey(key: String): Any = keyLocks.computeIfAbsent(key) { Any() }

    init {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    override fun read(key: String): String? = synchronized(getLockForKey(key)) {
        val file = File(directory, "$key.json")
        val backupFile = File(directory, "$key.json.bak")

        return try {
            if (file.exists()) {
                file.readText()
            } else if (backupFile.exists()) {
                backupFile.readText()
            } else {
                null
            }
        } catch (_: IOException) {
            if (backupFile.exists()) {
                try {
                    backupFile.readText()
                } catch (_: IOException) {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun write(key: String, content: String): Boolean = synchronized(getLockForKey(key)) {
        val targetFile = File(directory, "$key.json")
        val tempFile = File(directory, "$key.json.${System.nanoTime()}_${tempFileCounter.incrementAndGet()}.tmp")
        val backupFile = File(directory, "$key.json.bak")

        return try {
            tempFile.writeText(content)

            // If target exists, preserve as backup before replacing
            if (targetFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                targetFile.renameTo(backupFile)
            }

            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                // Fallback copy if rename fails across file boundaries
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            true
        } catch (_: Exception) {
            if (tempFile.exists()) tempFile.delete()
            false
        }
    }

    override fun delete(key: String): Boolean = synchronized(getLockForKey(key)) {
        val file = File(directory, "$key.json")
        val backupFile = File(directory, "$key.json.bak")
        var success = true
        if (file.exists()) success = file.delete()
        if (backupFile.exists()) success = backupFile.delete() && success
        return success
    }

    override fun clearAll(): Boolean = synchronized(this) {
        var success = true
        val files = directory.listFiles() ?: return true
        for (f in files) {
            if (!f.delete()) success = false
        }
        return success
    }
}

/**
 * In-memory storage backend for instantaneous, isolated, thread-safe unit testing.
 */
class InMemoryStorageBackend : StorageBackend {
    private val memoryStore = ConcurrentHashMap<String, String>()
    private val keyLocks = ConcurrentHashMap<String, Any>()

    private fun getLockForKey(key: String): Any = keyLocks.computeIfAbsent(key) { Any() }

    override fun read(key: String): String? = synchronized(getLockForKey(key)) {
        memoryStore[key]
    }

    override fun write(key: String, content: String): Boolean = synchronized(getLockForKey(key)) {
        memoryStore[key] = content
        true
    }

    override fun delete(key: String): Boolean = synchronized(getLockForKey(key)) {
        memoryStore.remove(key) != null
    }

    override fun clearAll(): Boolean = synchronized(this) {
        memoryStore.clear()
        true
    }
}
