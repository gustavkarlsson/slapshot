package se.gustavkarlsson.slapshot.core.formats

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import se.gustavkarlsson.slapshot.core.SnapshotFormat

private val json by lazy {
    Json {
        prettyPrint = true
    }
}

// TODO Add tests
data class JsonFormat(
    val allowAddedKeys: Boolean = false,
    val explicitNulls: Boolean = true,
    override val fileExtension: String = "json",
) : SnapshotFormat<String> {
    override fun test(actual: String, expected: String): String? {
        val expectedJson = json.decodeFromString<JsonElement>(expected)
        val actualJson = json.decodeFromString<JsonElement>(actual)
        val diffs = diffElement(JsonPath.ROOT, expectedJson, actualJson)
        return if (diffs.isNotEmpty()) {
            val diffLines = diffs.joinToString(separator = "\n") { "- $it" }
            "Found ${diffs.size} differences:\n$diffLines"
        } else {
            null
        }
    }

    private fun diffElement(path: JsonPath, expected: JsonElement, actual: JsonElement): List<String> {
        diffType(path, expected, actual).let { typeDiff ->
            if (typeDiff != null) {
                return listOf(typeDiff)
            }
        }
        return when (expected) {
            is JsonPrimitive -> diffPrimitive(path, expected, actual as JsonPrimitive)
            is JsonObject -> diffObject(path, expected, actual as JsonObject)
            is JsonArray -> diffArray(path, expected, actual as JsonArray)
        }
    }

    private fun diffType(path: JsonPath, expected: JsonElement, actual: JsonElement): String? {
        val expectedType = expected.typeName()
        val actualType = actual.typeName()
        return if (expectedType != actualType) {
            "Expected $path to be a <$expectedType> but it was a <$actualType>"
        } else {
            null
        }
    }

    private fun JsonElement.typeName(): String {
        return when (this) {
            is JsonObject -> "object"
            is JsonArray -> "array"
            is JsonPrimitive -> {
                when {
                    isString -> "string"
                    booleanOrNull != null -> "boolean"
                    (longOrNull ?: doubleOrNull) != null -> "number"
                    else -> "null"
                }
            }
        }
    }

    private fun diffPrimitive(path: JsonPath, expected: JsonPrimitive, actual: JsonPrimitive): List<String> {
        return if (expected.content != actual.content) {
            listOf("Expected $path to be <${expected.content}> but it was <${actual.content}>")
        } else {
            emptyList()
        }
    }

    private fun diffObject(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
        return buildList {
            addAll(diffMissingKeys(path, expected, actual))
            if (!allowAddedKeys) {
                addAll(diffAddedKeys(path, expected, actual))
            }
            addAll(diffIntersectingKeys(path, expected, actual))
        }
    }

    private fun diffMissingKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
        val expectedKeys = if (explicitNulls) {
            expected.keys
        } else {
            // Only expect keys where the value is not null
            expected.filterValues { it !is JsonNull }.keys
        }
        val missingKeys = expectedKeys - actual.keys
        return missingKeys.map { key ->
            val keyPath = path.addKey(key)
            "Expected an element at $keyPath"
        }
    }

    private fun diffAddedKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
        val addedKeys = actual.keys - expected.keys
        return addedKeys.map { key ->
            val keyPath = path.addKey(key)
            "Unexpected element at $keyPath"
        }
    }

    private fun diffIntersectingKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
        val intersectingKeys = actual.keys intersect expected.keys
        return intersectingKeys.flatMap { key ->
            val keyPath = path.addKey(key)
            diffElement(keyPath, expected.getValue(key), actual.getValue(key))
        }
    }

    private fun diffArray(path: JsonPath, expected: JsonArray, actual: JsonArray): List<String> {
        val contentDiffs = expected.zip(actual).flatMapIndexed { index, (expectedElement, actualElement) ->
            val indexPath = path.addIndex(index)
            diffElement(indexPath, expectedElement, actualElement)
        }
        val sizeDiff = buildList {
            if (expected.size != actual.size) {
                add("Expected $path to have a length of <${expected.size}> but it had a length of <${actual.size}>")
            }
        }
        return contentDiffs + sizeDiff
    }

    override fun deserialize(bytes: ByteArray): String {
        return bytes.decodeToString()
    }

    override fun serialize(value: String): ByteArray {
        val formatted = json.format(value)
        return formatted.encodeToByteArray()
    }

    private fun Json.format(value: String): String {
        val jsonElement = decodeFromString<JsonElement>(value)
        return encodeToString(jsonElement)
    }
}

@JvmInline
private value class JsonPath private constructor(val path: String) {
    fun addKey(key: String) = JsonPath("$path.$key")
    fun addIndex(index: Int) = JsonPath("$path[$index]")
    override fun toString() = path

    companion object {
        val ROOT = JsonPath("$")
    }
}
