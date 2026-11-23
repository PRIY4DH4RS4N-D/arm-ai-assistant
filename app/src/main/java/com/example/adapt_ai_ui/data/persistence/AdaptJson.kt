package com.example.adapt_ai_ui.data.persistence

/**
 * Lightweight, zero-dependency pure Kotlin JSON serializer and parser.
 * Works seamlessly on Android device runtimes and pure JVM unit tests without unmocked stub errors.
 */
sealed class JsonElement {
    data class JsonObject(val fields: Map<String, JsonElement>) : JsonElement() {
        fun getString(key: String, default: String = ""): String =
            (fields[key] as? JsonPrimitive)?.asString() ?: default

        fun getInt(key: String, default: Int = 0): Int =
            (fields[key] as? JsonPrimitive)?.asInt() ?: default

        fun getLong(key: String, default: Long = 0L): Long =
            (fields[key] as? JsonPrimitive)?.asLong() ?: default

        fun getFloat(key: String, default: Float = 0f): Float =
            (fields[key] as? JsonPrimitive)?.asFloat() ?: default

        fun getBoolean(key: String, default: Boolean = false): Boolean =
            (fields[key] as? JsonPrimitive)?.asBoolean() ?: default

        fun getObject(key: String): JsonObject? = fields[key] as? JsonObject

        fun getArray(key: String): JsonArray? = fields[key] as? JsonArray

        fun getStringList(key: String): List<String> =
            getArray(key)?.elements?.mapNotNull { (it as? JsonPrimitive)?.asString() } ?: emptyList()
    }

    data class JsonArray(val elements: List<JsonElement>) : JsonElement()

    data class JsonPrimitive(val value: Any?) : JsonElement() {
        fun asString(): String = value?.toString() ?: ""
        fun asInt(): Int = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
        fun asLong(): Long = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
        fun asFloat(): Float = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0f
            else -> 0f
        }
        fun asBoolean(): Boolean = when (value) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            else -> false
        }
    }

    object JsonNull : JsonElement()
}

class JsonObjectBuilder {
    private val map = mutableMapOf<String, JsonElement>()

    fun put(key: String, value: String?) {
        map[key] = if (value != null) JsonElement.JsonPrimitive(value) else JsonElement.JsonNull
    }

    fun put(key: String, value: Number?) {
        map[key] = if (value != null) JsonElement.JsonPrimitive(value) else JsonElement.JsonNull
    }

    fun put(key: String, value: Boolean?) {
        map[key] = if (value != null) JsonElement.JsonPrimitive(value) else JsonElement.JsonNull
    }

    fun put(key: String, element: JsonElement?) {
        map[key] = element ?: JsonElement.JsonNull
    }

    fun putStringList(key: String, list: List<String>) {
        map[key] = JsonElement.JsonArray(list.map { JsonElement.JsonPrimitive(it) })
    }

    fun build(): JsonElement.JsonObject = JsonElement.JsonObject(map)
}

object AdaptJson {

    fun buildObject(init: JsonObjectBuilder.() -> Unit): JsonElement.JsonObject {
        val builder = JsonObjectBuilder()
        builder.init()
        return builder.build()
    }

    fun serialize(element: JsonElement): String = buildString {
        serializeElement(element, this)
    }

    private fun serializeElement(element: JsonElement, sb: StringBuilder) {
        when (element) {
            is JsonElement.JsonObject -> {
                sb.append('{')
                var first = true
                for ((key, value) in element.fields) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append('"').append(escapeString(key)).append("\":")
                    serializeElement(value, sb)
                }
                sb.append('}')
            }
            is JsonElement.JsonArray -> {
                sb.append('[')
                var first = true
                for (item in element.elements) {
                    if (!first) sb.append(',')
                    first = false
                    serializeElement(item, sb)
                }
                sb.append(']')
            }
            is JsonElement.JsonPrimitive -> {
                when (val v = element.value) {
                    null -> sb.append("null")
                    is Boolean -> sb.append(v)
                    is Number -> sb.append(v)
                    else -> sb.append('"').append(escapeString(v.toString())).append('"')
                }
            }
            JsonElement.JsonNull -> sb.append("null")
        }
    }

    private fun escapeString(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun parse(json: String): JsonElement {
        val parser = Parser(json.trim())
        return parser.parseValue()
    }

    fun parseObject(json: String): JsonElement.JsonObject? {
        return try {
            parse(json) as? JsonElement.JsonObject
        } catch (_: Exception) {
            null
        }
    }

    private class Parser(private val src: String) {
        private var pos = 0

        private fun skipWhitespace() {
            while (pos < src.length && src[pos].isWhitespace()) {
                pos++
            }
        }

        fun parseValue(): JsonElement {
            skipWhitespace()
            if (pos >= src.length) return JsonElement.JsonNull

            return when (val ch = src[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> if (ch == '-' || ch.isDigit()) parseNumber() else {
                    pos++
                    JsonElement.JsonNull
                }
            }
        }

        private fun parseObject(): JsonElement.JsonObject {
            pos++ // skip '{'
            val map = mutableMapOf<String, JsonElement>()
            skipWhitespace()
            if (pos < src.length && src[pos] == '}') {
                pos++
                return JsonElement.JsonObject(map)
            }

            while (pos < src.length) {
                skipWhitespace()
                val key = if (pos < src.length && src[pos] == '"') {
                    (parseString() as JsonElement.JsonPrimitive).asString()
                } else {
                    break
                }

                skipWhitespace()
                if (pos < src.length && src[pos] == ':') {
                    pos++
                }

                val value = parseValue()
                map[key] = value

                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                } else if (pos < src.length && src[pos] == '}') {
                    pos++
                    break
                }
            }
            return JsonElement.JsonObject(map)
        }

        private fun parseArray(): JsonElement.JsonArray {
            pos++ // skip '['
            val list = mutableListOf<JsonElement>()
            skipWhitespace()
            if (pos < src.length && src[pos] == ']') {
                pos++
                return JsonElement.JsonArray(list)
            }

            while (pos < src.length) {
                val value = parseValue()
                list.add(value)

                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                } else if (pos < src.length && src[pos] == ']') {
                    pos++
                    break
                }
            }
            return JsonElement.JsonArray(list)
        }

        private fun parseString(): JsonElement.JsonPrimitive {
            pos++ // skip opening quote
            val sb = StringBuilder()
            var escaped = false

            while (pos < src.length) {
                val c = src[pos++]
                if (escaped) {
                    when (c) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 <= src.length) {
                                val hex = src.substring(pos, pos + 4)
                                pos += 4
                                hex.toIntOrNull(16)?.let { sb.append(it.toChar()) }
                            }
                        }
                        else -> sb.append(c)
                    }
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == '"') {
                    break
                } else {
                    sb.append(c)
                }
            }
            return JsonElement.JsonPrimitive(sb.toString())
        }

        private fun parseNumber(): JsonElement.JsonPrimitive {
            val start = pos
            if (pos < src.length && src[pos] == '-') pos++
            while (pos < src.length && (src[pos].isDigit() || src[pos] == '.' || src[pos] == 'e' || src[pos] == 'E' || src[pos] == '+')) {
                pos++
            }
            val numStr = src.substring(start, pos)
            val num: Number = if (numStr.contains('.') || numStr.contains('e') || numStr.contains('E')) {
                numStr.toDoubleOrNull() ?: 0.0
            } else {
                numStr.toLongOrNull() ?: 0L
            }
            return JsonElement.JsonPrimitive(num)
        }

        private fun parseBoolean(): JsonElement.JsonPrimitive {
            return if (src.startsWith("true", pos)) {
                pos += 4
                JsonElement.JsonPrimitive(true)
            } else if (src.startsWith("false", pos)) {
                pos += 5
                JsonElement.JsonPrimitive(false)
            } else {
                pos++
                JsonElement.JsonPrimitive(false)
            }
        }

        private fun parseNull(): JsonElement {
            if (src.startsWith("null", pos)) {
                pos += 4
            } else {
                pos++
            }
            return JsonElement.JsonNull
        }
    }
}
