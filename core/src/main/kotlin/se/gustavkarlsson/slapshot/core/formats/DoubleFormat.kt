package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat
import kotlin.math.abs

public data class DoubleFormat(
    val tolerance: Double = 0.0,
    override val fileExtension: String = "txt",
) : SnapshotFormat<Double> {
    init {
        require(tolerance >= 0.0 && !tolerance.isNaN()) {
            "tolerance must be non-negative but was: <$tolerance>"
        }
    }

    override fun test(actual: Double, expected: Double): String? {
        return when {
            expected == actual -> null
            tolerance == 0.0 -> "expected: <$expected> but was: <$actual>"
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
