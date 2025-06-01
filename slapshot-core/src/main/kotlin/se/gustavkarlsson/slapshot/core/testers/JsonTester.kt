package se.gustavkarlsson.slapshot.core.testers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import se.gustavkarlsson.slapshot.core.Tester

/**
 * A test utility for comparing JSON structures.
 *
 * It reports differences when the JSON structures do not match, including mismatches in types, values, keys,
 * or array lengths.
 *
 * Comparison errors are presented in a JSONPath format to make them easy to find in large snapshots.
 */
public data object JsonTester : Tester<String> {
    private val json = Json

    override fun test(
        actual: String,
        expected: String,
    ): String? {
        val actualJson = json.decodeFromString<JsonElement>(actual)
        val expectedJson = json.decodeFromString<JsonElement>(expected)
        val diffs = diffElement(JsonPath.ROOT, actualJson, expectedJson)
        return if (diffs.isNotEmpty()) {
            val diffLines = diffs.joinToString(separator = "\n") { "- $it" }
            "Found ${diffs.size} differences:\n$diffLines"
        } else {
            null
        }
    }

    private fun diffElement(
        path: JsonPath,
        actual: JsonElement,
        expected: JsonElement,
    ): List<String> {
        diffType(path, actual, expected)?.let { typeDiff ->
            return listOf(typeDiff)
        }
        return when (actual) {
            is JsonPrimitive -> diffPrimitive(path, actual, expected as JsonPrimitive)
            is JsonObject -> diffObject(path, actual, expected as JsonObject)
            is JsonArray -> diffArray(path, actual, expected as JsonArray)
        }
    }

    private fun diffType(
        path: JsonPath,
        actual: JsonElement,
        expected: JsonElement,
    ): String? {
        val actualType = actual.typeName()
        val expectedType = expected.typeName()
        return if (actualType != expectedType) {
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
                    contentOrNull == null -> "null"
                    isString -> "string"
                    booleanOrNull != null -> "boolean"
                    longOrNull != null -> "number"
                    doubleOrNull != null -> "number"
                    else -> throw IllegalArgumentException("Unsupported json primitive: $this")
                }
            }
        }
    }

    private fun diffPrimitive(
        path: JsonPath,
        actual: JsonPrimitive,
        expected: JsonPrimitive,
    ): List<String> {
        return if (actual != expected) {
            listOf("Expected $path to be <$expected> but it was <$actual>")
        } else {
            emptyList()
        }
    }

    private fun diffObject(
        path: JsonPath,
        actual: JsonObject,
        expected: JsonObject,
    ): List<String> {
        return buildList {
            addAll(diffMissingKeys(path, actual, expected))
            addAll(diffAddedKeys(path, actual, expected))
            addAll(diffIntersectingKeys(path, actual, expected))
        }
    }

    private fun diffMissingKeys(
        path: JsonPath,
        actual: JsonObject,
        expected: JsonObject,
    ): List<String> {
        val missingKeys = expected.keys - actual.keys
        return missingKeys.map { key ->
            val keyPath = path.addKey(key)
            "Expected an element at $keyPath"
        }
    }

    private fun diffAddedKeys(
        path: JsonPath,
        actual: JsonObject,
        expected: JsonObject,
    ): List<String> {
        val addedKeys = actual.keys - expected.keys
        return addedKeys.map { key ->
            val keyPath = path.addKey(key)
            "Unexpected element at $keyPath"
        }
    }

    private fun diffIntersectingKeys(
        path: JsonPath,
        actual: JsonObject,
        expected: JsonObject,
    ): List<String> {
        val intersectingKeys = actual.keys intersect expected.keys
        return intersectingKeys.flatMap { key ->
            val keyPath = path.addKey(key)
            diffElement(keyPath, actual.getValue(key), expected.getValue(key))
        }
    }

    private fun diffArray(
        path: JsonPath,
        actual: JsonArray,
        expected: JsonArray,
    ): List<String> {
        val contentDiffs =
            actual.zip(expected).flatMapIndexed { index, (actualElement, expectedElement) ->
                val indexPath = path.addIndex(index)
                diffElement(indexPath, actualElement, expectedElement)
            }
        val sizeDiff =
            buildList {
                if (actual.size != expected.size) {
                    add("Expected $path to have a length of <${expected.size}> but it had a length of <${actual.size}>")
                }
            }
        return contentDiffs + sizeDiff
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
