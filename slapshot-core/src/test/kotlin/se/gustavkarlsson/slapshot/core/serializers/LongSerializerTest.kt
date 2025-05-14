package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test

class LongSerializerTest {
    private val serializer = LongSerializer

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                0L to "0",
                1L to "1",
                -1L to "-1",
                123456789L to "123456789",
                -123456789L to "-123456789",
                Long.MAX_VALUE to Long.MAX_VALUE.toString(),
                Long.MIN_VALUE to Long.MIN_VALUE.toString(),
            )

        tableTestSerialization(table, serializer)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "0" to 0L,
                "1" to 1L,
                "-1" to -1L,
                "123456789" to 123456789L,
                "-123456789" to -123456789L,
                Long.MAX_VALUE.toString() to Long.MAX_VALUE,
                Long.MIN_VALUE.toString() to Long.MIN_VALUE,
                // Test with whitespace
                " 123456789 " to 123456789L,
                "\n123456789\t" to 123456789L,
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
                // Decimal not allowed for Long
                "123.456",
                // Greater than Long.MAX_VALUE
                "9223372036854775808",
                // Less than Long.MIN_VALUE
                "-9223372036854775809",
            )

        tableTestDeserializationFailure<IllegalArgumentException>(strings, serializer)
    }
}
