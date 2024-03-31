package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test
import strikt.api.expectThrows

class DoubleFormatTest {
    private val format = DoubleFormat()
    private val formatWithTolerance = DoubleFormat(tolerance = 0.1)

    @Test
    fun `negative tolerance`() {
        expectThrows<IllegalArgumentException> { DoubleFormat(tolerance = -0.5) }
    }

    @Test
    fun `NaN tolerance`() {
        expectThrows<IllegalArgumentException> { DoubleFormat(tolerance = Double.NaN) }
    }

    @Test
    fun `test values passing`() {
        val table =
            listOf(
                0.0 to 0.0,
                2.4 to 2.4,
            )

        tableTestValuesPassing(table, format)
    }

    @Test
    fun `test values passing with tolerance`() {
        val table =
            listOf(
                0.0 to 0.05,
                0.0 to -0.05,
                0.0 to 0.1,
                0.0 to -0.1,
            )

        tableTestValuesPassing(table, formatWithTolerance)
    }

    @Test
    fun `test values failing`() {
        val table =
            listOf(
                0.0 to 0.05,
                0.0 to -0.05,
                0.0 to 0.1,
                0.0 to -0.1,
            )

        tableTestValuesFailing(table, format)
    }

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                0.0 to "0.0",
                -2.0 to "-2.0",
                13.3 to "13.3",
                Double.NaN to "NaN",
                Double.MIN_VALUE to "4.9E-324",
                Double.MAX_VALUE to "1.7976931348623157E308",
                Double.NEGATIVE_INFINITY to "-Infinity",
                Double.POSITIVE_INFINITY to "Infinity",
            )

        tableTestSerialization(table, format)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "0.0" to 0.0,
                "-2.0" to -2.0,
                "13.3" to 13.3,
                "NaN" to Double.NaN,
                "4.9E-324" to Double.MIN_VALUE,
                "1.7976931348623157E308" to Double.MAX_VALUE,
                "-Infinity" to Double.NEGATIVE_INFINITY,
                "Infinity" to Double.POSITIVE_INFINITY,
                "\n \t0.0" to 0.0,
                "0" to 0.0,
                "5" to 5.0,
                "-5" to -5.0,
            )

        tableTestDeserialization(table, format)
    }

    @Test
    fun `deserialize invalid values`() {
        val table =
            listOf(
                "0,3",
                "five",
                "a5.0",
            )

        tableTestDeserializationFailure(table, format)
    }
}
