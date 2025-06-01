package se.gustavkarlsson.slapshot.json

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
 * A test utility for comparing JSON snapshots. Supports printing errors in different styles.
 */
public data class JsonTester(
    /**
     * The style of error message to use when snapshots don't match.
     */
    val errorStyle: JsonErrorStyle = JsonErrorStyle.JsonPath,
) : Tester<String> {
    override fun test(
        actual: String,
        expected: String,
    ): String? {
        return if (actual.parseJsonElement() != expected.parseJsonElement()) {
            errorStyle.createMessage(actual.prettyPrint(), expected.prettyPrint())
        } else {
            null
        }
    }
}

/**
 * Creates error messages for differing json snapshots.
 */
public fun interface JsonErrorStyle {
    /**
     * Creates an error message explaining the mismatch between [actualPretty] and [expectedPretty].
     *
     * They are guaranteed to not match and be pretty printed.
     */
    public fun createMessage(
        actualPretty: String,
        expectedPretty: String,
    ): String

    /**
     * Locates differences between the two json structures
     * and returns an error message with a list of mismatches
     * and their [JSONPath](https://en.wikipedia.org/wiki/JSONPath) locations.
     */
    public data object JsonPath : JsonErrorStyle {
        override fun createMessage(
            actualPretty: String,
            expectedPretty: String,
        ): String {
            val actualJson = actualPretty.parseJsonElement()
            val expectedJson = expectedPretty.parseJsonElement()
            val diffs = diffElement(JsonPath.ROOT, actualJson, expectedJson)
            val diffLines = diffs.joinToString(separator = "\n") { "- $it" }
            return buildString {
                appendLine("Found ${diffs.size} differences:")
                appendLine(diffLines)
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
            val actualType = actual.typeName
            val expectedType = expected.typeName
            return if (actualType != expectedType) {
                "Expected $path to be a <$expectedType> but it was a <$actualType>"
            } else {
                null
            }
        }

        private val JsonElement.typeName: String
            get() =
                when (this) {
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

        @JvmInline
        private value class JsonPath private constructor(val path: String) {
            fun addKey(key: String) = JsonPath("$path.$key")

            fun addIndex(index: Int) = JsonPath("$path[$index]")

            override fun toString() = path

            companion object {
                val ROOT = JsonPath("$")
            }
        }
    }

    /**
     * Creates an error message in the style of JUnit.
     *
     * Example:
     * > expected: <$expected> but was: <$actual>
     *
     * This is useful if using IntelliJ, as it will show a convenient clickable diff view in the error log.
     *
     * Note that [JsonTester] does not care about the order of object keys, while the diff view does.
     * So you might see mismatches that are false positives.
     */
    public data object JUnitStyle : JsonErrorStyle {
        override fun createMessage(
            actualPretty: String,
            expectedPretty: String,
        ): String {
            return buildString {
                append("expected: <")
                append(expectedPretty)
                append("> but was: <")
                append(actualPretty)
                append(">")
            }
        }
    }
}
