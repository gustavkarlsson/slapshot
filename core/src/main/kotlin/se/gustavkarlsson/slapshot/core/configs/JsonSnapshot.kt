package se.gustavkarlsson.slapshot.core.configs

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import se.gustavkarlsson.slapshot.core.SnapshotConfig

private val json by lazy {
    Json {
        prettyPrint = true
    }
}

class JsonSnapshot : SnapshotConfig<String> {
    override val fileExtension: String = ".json"

    // FIXME implement
    var ignoreUnknownKeys = false

    // FIXME implement
    var explicitNulls = true

    override fun test(expected: String, actual: String): String? {
        val expectedJson = json.decodeFromString<JsonElement>(expected)
        val actualJson = json.decodeFromString<JsonElement>(actual)
        val diffs = diffElement(JsonPath.ROOT, expectedJson, actualJson)
        return if (diffs.isNotEmpty()) {
            "Found ${diffs.size} snapshot differences:\n${diffs.joinToString(separator = "\n")}"
        } else {
            null
        }
    }

    override fun deserialize(bytes: ByteArray): String {
        return bytes.decodeToString()
    }

    override fun serialize(value: String): ByteArray {
        val formatted = json.format(value)
        return formatted.encodeToByteArray()
    }
}

private fun diffElement(path: JsonPath, expected: JsonElement, actual: JsonElement): List<String> {
    diffType(path, expected, actual).let { typeDiff ->
        if (typeDiff.isNotEmpty()) {
            return typeDiff
        }
    }
    return when (expected) {
        is JsonPrimitive -> diffPrimitive(path, expected, actual as JsonPrimitive)
        is JsonObject -> diffObject(path, expected, actual as JsonObject)
        is JsonArray -> diffArray(path, expected, actual as JsonArray)
    }
}

private fun diffType(path: JsonPath, expected: JsonElement, actual: JsonElement): List<String> {
    val expectedType = expected.typeString()
    val actualType = actual.typeString()
    return if (expectedType != actualType) {
        listOf("Expected $path to be a <$expectedType> but it was a <$actualType>")
    } else {
        emptyList()
    }
}

private fun JsonElement.typeString(): String {
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
    val diffs = mutableListOf<String>()
    diffs += diffMissingKeys(path, expected, actual)
    diffs += diffExtraKeys(path, expected, actual)
    diffs += diffIntersectingKeys(path, expected, actual)
    return diffs
}

private fun diffMissingKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
    val missingKeys = expected.keys - actual.keys
    return missingKeys.map { key ->
        val keyPath = path.addKey(key)
        "Expected an element at $keyPath"
    }
}

private fun diffExtraKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
    val extraKeys = actual.keys - expected.keys
    return extraKeys.map { key ->
        val keyPath = path.addKey(key)
        "Unexpected element at $keyPath"
    }
}

private fun diffIntersectingKeys(path: JsonPath, expected: JsonObject, actual: JsonObject): List<String> {
    val intersectingKeys = actual.keys intersect expected.keys
    return intersectingKeys.flatMap { key ->
        val keyPath = path.addKey(key)
        diffElement(keyPath, expected[key]!!, actual[key]!!)
    }
}

private fun diffArray(path: JsonPath, expected: JsonArray, actual: JsonArray): List<String> {
    val diffs = mutableListOf<String>()
    if (expected.size != actual.size) {
        diffs += "Expected $path to have a length of <${expected.size}> but it had a length of <${actual.size}>"
    }
    return expected.zip(actual).flatMapIndexed { index, (expectedElement, actualElement) ->
        val indexPath = path.addIndex(index)
        diffElement(indexPath, expectedElement, actualElement)
    }
}

private fun Json.format(value: String): String {
    val jsonElement = decodeFromString<JsonElement>(value)
    return encodeToString(jsonElement)
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