package se.gustavkarlsson.slapshot.core.testers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DoubleWithToleranceTesterTest {
    private val defaultTester = DoubleWithToleranceTester()
    private val toleranceTester = DoubleWithToleranceTester(tolerance = 0.1)

    @Test
    fun `test exact equality with default tolerance`() {
        val table =
            listOf(
                0.0 to 0.0,
                1.0 to 1.0,
                -1.0 to -1.0,
                Double.MAX_VALUE to Double.MAX_VALUE,
                Double.MIN_VALUE to Double.MIN_VALUE,
            )

        tableTestValuesPassing(table, defaultTester)
    }

    @Test
    fun `test inequality with zero tolerance`() {
        val table =
            listOf(
                0.0 to 0.1,
                1.0 to 1.1,
                -1.0 to -0.9,
                0.0 to -0.0000001,
            )

        tableTestValuesFailing(table, defaultTester)
    }

    @Test
    fun `test values within tolerance`() {
        val table =
            listOf(
                0.0 to 0.05,
                0.05 to 0.0,
                1.0 to 1.09,
                1.09 to 1.0,
                -1.0 to -0.95,
                -0.95 to -1.0,
            )

        tableTestValuesPassing(table, toleranceTester)
    }

    @Test
    fun `test values outside tolerance`() {
        val table =
            listOf(
                0.0 to 0.11,
                0.11 to 0.0,
                1.0 to 1.11,
                1.11 to 1.0,
                -1.0 to -0.89,
                -0.89 to -1.0,
            )

        tableTestValuesFailing(table, toleranceTester)
    }

    @Test
    fun `test NaN values`() {
        val table =
            listOf(
                Double.NaN to 0.0,
                0.0 to Double.NaN,
                Double.NaN to Double.NaN,
            )

        tableTestValuesFailing(table, defaultTester)
        tableTestValuesFailing(table, toleranceTester)
    }

    @Test
    fun `test infinity values`() {
        val table =
            listOf(
                Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY to Double.NEGATIVE_INFINITY,
            )

        tableTestValuesPassing(table, defaultTester)
        tableTestValuesPassing(table, toleranceTester)
    }

    @Test
    fun `test mixed infinity values`() {
        val table =
            listOf(
                Double.POSITIVE_INFINITY to Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY to 0.0,
                0.0 to Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY to 0.0,
                0.0 to Double.NEGATIVE_INFINITY,
            )

        tableTestValuesFailing(table, defaultTester)
        tableTestValuesFailing(table, toleranceTester)
    }

    @Test
    fun `test constructor with negative tolerance`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                DoubleWithToleranceTester(tolerance = -0.1)
            }
        expectThat(exception.message).isEqualTo("tolerance must be non-negative but was: <-0.1>")
    }

    @Test
    fun `test constructor with NaN tolerance`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                DoubleWithToleranceTester(tolerance = Double.NaN)
            }
        expectThat(exception.message).isEqualTo("tolerance must be non-negative but was: <NaN>")
    }

    @Test
    fun `test constructor with positive infinity tolerance`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                DoubleWithToleranceTester(tolerance = Double.POSITIVE_INFINITY)
            }
        expectThat(exception.message).isEqualTo("tolerance may not be positive infinity")
    }

    @Test
    fun `test constructor with negative infinity tolerance`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                DoubleWithToleranceTester(tolerance = Double.NEGATIVE_INFINITY)
            }
        expectThat(exception.message).isEqualTo("tolerance must be non-negative but was: <-Infinity>")
    }
}
