package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test

class LongStringFormatTest {
    private val format = LongStringFormat()
    private val formatWithTrim = LongStringFormat(trim = true)

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                "" to "",
                "foo" to "foo",
                "foo  " to "foo  ",
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                "foo" to "bar",
                "foo" to " foo ",
            )

        tableTestValuesFailing(table, format)
    }

    @Test
    fun `test values passing with trim`() {
        val table =
            listOf(
                "foo \n\t" to "foo",
                " " to "",
            )

        tableTestValuesPassing(table, formatWithTrim)
    }

    @Test
    fun `test values failing with trim`() {
        val table =
            listOf(
                "foo" to "foob",
            )

        tableTestValuesFailing(table, formatWithTrim)
    }

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
            )

        tableTestSerialization(table, format)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
            )

        tableTestDeserialization(table, format)
    }
}
