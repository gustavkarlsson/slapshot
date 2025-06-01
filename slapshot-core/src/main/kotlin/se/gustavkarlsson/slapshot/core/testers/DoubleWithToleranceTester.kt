package se.gustavkarlsson.slapshot.core.testers

import se.gustavkarlsson.slapshot.core.Tester
import kotlin.math.abs

/**
 * Tests equality of double snapshots with an optional tolerance, allowing
 * two values to be considered equal if the absolute difference between them is within the
 * specified tolerance.
 */
public data class DoubleWithToleranceTester(
    /**
     * The non-negative tolerance within which two double values are considered equal.
     */
    val tolerance: Double = 0.0,
) : Tester<Double> {
    init {
        require(tolerance >= 0.0) {
            "tolerance must be non-negative but was: <$tolerance>"
        }
        require(!tolerance.isNaN()) {
            "tolerance may not be NaN"
        }
        require(tolerance != Double.POSITIVE_INFINITY) {
            "tolerance may not be positive infinity"
        }
        require(tolerance != Double.NEGATIVE_INFINITY) {
            "tolerance may not be negative infinity"
        }
    }

    override fun test(
        actual: Double,
        expected: Double,
    ): String? {
        return when {
            expected == actual -> null
            tolerance == 0.0 -> "expected: <$expected> but was: <$actual>"
            actual.isNaN() || expected.isNaN() -> "expected: <$expected> or actual: <$actual> was NaN"
            abs(expected - actual) > tolerance -> "expected: <$expected> within a tolerance of <$tolerance> but was: <$actual>"
            else -> null
        }
    }
}
