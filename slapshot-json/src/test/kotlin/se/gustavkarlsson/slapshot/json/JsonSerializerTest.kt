package se.gustavkarlsson.slapshot.json

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class JsonSerializerTest {
    private val serializer = JsonSerializer

    @Test
    fun `serialize and deserialize JSON object`() {
        val json = """{"key":"value","number":123}"""
        val serialized = serializer.serialize(json)
        val deserialized = serializer.deserialize(serialized)

        // The serializer pretty-prints JSON, so we need to normalize it for comparison
        val expectedJson =
            """
            {
                "key": "value",
                "number": 123
            }
            """.trimIndent()

        expectThat(deserialized).isEqualTo(expectedJson)
    }

    @Test
    fun `serialize and deserialize JSON array`() {
        val json = """[1,2,3,"string"]"""
        val serialized = serializer.serialize(json)
        val deserialized = serializer.deserialize(serialized)

        // The serializer pretty-prints JSON, so we need to normalize it for comparison
        val expectedJson =
            """
            [
                1,
                2,
                3,
                "string"
            ]
            """.trimIndent()

        expectThat(deserialized).isEqualTo(expectedJson)
    }

    @Test
    fun `serialize and deserialize nested JSON`() {
        val json = """{"array":[1,2,3],"object":{"nested":"value"}}"""
        val serialized = serializer.serialize(json)
        val deserialized = serializer.deserialize(serialized)

        // The serializer pretty-prints JSON, so we need to normalize it for comparison
        val expectedJson =
            """
            {
                "array": [
                    1,
                    2,
                    3
                ],
                "object": {
                    "nested": "value"
                }
            }
            """.trimIndent()

        expectThat(deserialized).isEqualTo(expectedJson)
    }

    @Test
    fun `serialize and deserialize primitive JSON values`() {
        val testCases =
            listOf(
                "123",
                "true",
                "false",
                "null",
                """"string"""",
            )

        for (json in testCases) {
            val serialized = serializer.serialize(json)
            val deserialized = serializer.deserialize(serialized)

            expectThat(deserialized).isEqualTo(json)
        }
    }

    @Test
    fun `serialize and deserialize with whitespace and formatting variations`() {
        val jsonVariations =
            listOf(
                """{"key":"value"}""",
                """  {  "key"  :  "value"  }  """,
                """{"key"     :     "value"}""",
                """{
                "key": "value"
            }""",
            )

        // The serializer should normalize all these variations to the same pretty-printed format
        val expectedJson =
            """
            {
                "key": "value"
            }
            """.trimIndent()

        for (json in jsonVariations) {
            val serialized = serializer.serialize(json)
            val deserialized = serializer.deserialize(serialized)

            expectThat(deserialized).isEqualTo(expectedJson)
        }
    }
}
