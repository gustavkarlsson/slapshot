package se.gustavkarlsson.slapshot.core.testers

import org.junit.jupiter.api.Test

class EqualsTesterTest {
    private val tester = EqualsTester

    @Test
    fun `test boolean values passing`() {
        val table =
            listOf(
                true to true,
                false to false,
            )

        tableTestValuesPassing(table, tester)
    }

    @Test
    fun `test boolean values failing`() {
        val table =
            listOf(
                true to false,
                false to true,
            )

        tableTestValuesFailing(table, tester)
    }

    @Test
    fun `test string values passing`() {
        val table =
            listOf(
                "" to "",
                "foo" to "foo",
                "foo  " to "foo  ",
            )

        tableTestValuesPassing(table, tester)
    }

    @Test
    fun `test string values failing`() {
        val table =
            listOf(
                "foo" to "bar",
                "foo" to " foo ",
            )

        tableTestValuesFailing(table, tester)
    }
}
