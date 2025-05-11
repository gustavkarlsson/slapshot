package se.gustavkarlsson.slapshot.core.formats

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class JsonFormatTest {
    private val format = JsonFormat()

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                "5" to " 5 ",
                "null" to " null ",
                "\"hello\"" to " \"hello\" ",
                "[null, \"a\", 5]" to " [ null,\"a\", 5 ] ",
                "{}" to " { } ",
                """{ "keyA": true, "keyB": [1, 2] }""" to """{  "keyA" :  true ,  "keyB" : [ 1,  2 ]  }  """,
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                "\"foo\"" to "\"bar\"",
                "5" to "5.0",
                "true" to "5",
                "true" to "\"true\"",
                "5" to "\"5\"",
                "null" to "\"null\"",
                """{ "keyA": true }""" to """{ "keyA": false }""",
                """{ "keyA": true }""" to """{ "keyA": 5 }""",
                """{ "keyA": true }""" to """{  }""",
                """{ }""" to """{ "keyA": true }""",
                """{}""" to """{ "keyA": null }""",
                "[1, 2]" to "[1, 2, 3]",
                "[1]" to "1",
            )

        tableTestValuesFailing(table, format)
    }

    private val invalidJsonTable =
        listOf(
            "",
            "[",
            "]",
            "[}",
            "\"",
            ",",
            """{ test: true }""",
            """{ "test: true }""",
            """{ "test" true }""",
            """{ "test": }""",
            """{ "test": true, }""",
            "0.0.1",
            "yes",
            "no",
            "1a",
            "{[}]",
            "[,]",
            "[true,]",
            "[,true]",
            "-Infinity",
            "+Infinity",
            "Infinity",
            "NaN",
            "foo",
        )

    @Test
    fun `deserialization failures`() {
        tableTestDeserializationFailure(invalidJsonTable, format)
    }

    @Test
    fun `serialization failures`() {
        tableTestSerializationFailure(invalidJsonTable, format)
    }

    @Test
    fun `serialize and deserialize values`() {
        @Language("json")
        val jsonString =
            """
            {
              "keyA": 5,
              "keyB": true,
              "keyC": "foobar",
              "keyD": null,
              "keyE": [1, 2, 3],
              "keyF": { "keyG": true }
            }
            """.trimIndent()
        val inputJson = Json.decodeFromString<JsonElement>(jsonString)

        val serialized = format.serialize(Json.encodeToString(inputJson))
        val deserialized = format.deserialize(serialized)

        val outputJson = Json.decodeFromString<JsonElement>(deserialized)
        expectThat(outputJson).isEqualTo(inputJson)
    }

    @Test
    fun `test error message`() {
        @Language("json")
        val actualJsonString =
            """
            {
              "keyA": [
                0,
                {
                  "keyB": true
                }
              ]
            }
            """.trimIndent()

        @Language("json")
        val expectedJsonString =
            """
            {
              "keyA": [
                0,
                {
                  "keyB": 7
                }
              ]
            }
            """.trimIndent()
        val error = format.test(actualJsonString, expectedJsonString)
        expectThat(error)
            .isNotNull()
            .contains("$.keyA[1].keyB")
    }
}
