package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test

class AnyToStringFormatTest {
    private val format = AnyToStringFormat()

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                "" to "",
                5 to 5,
                true to true,
                2.4 to 2.4,
                listOf("hello") to listOf("hello"),
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                "foo" to "bar",
                "five" to 5,
                2 to 8,
                2 to listOf(2),
            )

        tableTestValuesFailing(table, format)
    }

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
                5 to "5",
                true to "true",
                listOf("a", "b") to "[a, b]",
            )

        tableTestSerialization(table, format)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
                "5" to "5",
                "true" to "true",
                "[a, b]" to "[a, b]",
            )

        tableTestDeserialization(table, format)
    }
}
