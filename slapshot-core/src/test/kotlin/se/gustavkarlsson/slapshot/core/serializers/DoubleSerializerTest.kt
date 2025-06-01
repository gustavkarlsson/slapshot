package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test

class DoubleSerializerTest {
    private val serializer = DoubleSerializer

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                0.0 to "0.0",
                1.0 to "1.0",
                -1.0 to "-1.0",
                123.456 to "123.456",
                -123.456 to "-123.456",
                Double.MAX_VALUE to Double.MAX_VALUE.toString(),
                Double.MIN_VALUE to Double.MIN_VALUE.toString(),
                Double.POSITIVE_INFINITY to "Infinity",
                Double.POSITIVE_INFINITY to "Infinity",
                Double.NEGATIVE_INFINITY to "-Infinity",
                Double.NaN to "NaN",
            )

        tableTestSerialization(table, serializer)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "0.0" to 0.0,
                "1.0" to 1.0,
                "-1.0" to -1.0,
                "123.456" to 123.456,
                "-123.456" to -123.456,
                Double.MAX_VALUE.toString() to Double.MAX_VALUE,
                Double.MIN_VALUE.toString() to Double.MIN_VALUE,
                "Infinity" to Double.POSITIVE_INFINITY,
                "-Infinity" to Double.NEGATIVE_INFINITY,
                "NaN" to Double.NaN,
                // Test with whitespace
                " 123.456 " to 123.456,
                "\n123.456\t" to 123.456,
            )

        tableTestDeserialization(table, serializer)
    }

    @Test
    fun `deserialize invalid values`() {
        val strings =
            listOf(
                "",
                "not a number",
                "123a",
                "a123",
                "1,234",
            )

        tableTestDeserializationFailure<IllegalArgumentException>(strings, serializer)
    }
}
