package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test

class BooleanFormatTest {
    private val format = BooleanFormat()

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                true to true,
                false to false,
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                true to false,
                false to true,
            )

        tableTestValuesFailing(table, format)
    }

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                true to "true",
                false to "false",
            )

        tableTestSerialization(table, format)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "true" to true,
                "false" to false,
                "TRUE" to true,
                "FALSE" to false,
                "TrUe" to true,
                "FaLsE" to false,
                "\n \ttrue" to true,
                "\n \tfalse" to false,
            )

        tableTestDeserialization(table, format)
    }

    @Test
    fun `deserialize invalid values`() {
        val table =
            listOf(
                "yes",
                "no",
                "bla",
                "1",
                "0",
                "",
            )

        tableTestDeserializationFailure(table, format)
    }
}
