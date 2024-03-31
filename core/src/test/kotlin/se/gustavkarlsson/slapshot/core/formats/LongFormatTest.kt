package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test

class LongFormatTest {
    private val format = LongFormat()

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                0L to 0L,
                1L to 1L,
                -5L to -5L,
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                0L to 1L,
                -2L to 2L,
            )

        tableTestValuesFailing(table, format)
    }

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                0L to "0",
                5L to "5",
                -2L to "-2",
                Long.MIN_VALUE to "-9223372036854775808",
                Long.MAX_VALUE to "9223372036854775807",
            )

        tableTestSerialization(table, format)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "0" to 0L,
                "-2" to -2L,
                "13" to 13L,
                "-9223372036854775808" to Long.MIN_VALUE,
                "9223372036854775807" to Long.MAX_VALUE,
                "\n \t0" to 0L,
            )

        tableTestDeserialization(table, format)
    }

    @Test
    fun `deserialize invalid values`() {
        val table =
            listOf(
                "0.5",
                "five",
                "a5",
            )

        tableTestDeserializationFailure(table, format)
    }
}
