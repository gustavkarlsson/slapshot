package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat
import kotlin.math.abs

public data class DoubleFormat(
    val tolerance: Double = 0.0,
    override val fileExtension: String = "txt",
) : SnapshotFormat<Double> {
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

    override fun deserialize(bytes: ByteArray): Double {
        return bytes.decodeToString()
            .trim()
            .toDouble()
    }

    override fun serialize(value: Double): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
