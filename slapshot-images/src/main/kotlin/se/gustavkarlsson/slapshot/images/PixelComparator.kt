package se.gustavkarlsson.slapshot.images

import kotlin.math.abs

/**
 * Calculates the difference between two pixels.
 */
public fun interface PixelComparator {
    /**
     * Calculates the difference between two pixels. A pixel is represented by a list of floats in the range 0..1,
     * where each value represents a "component" of the pixel (typically RGB or RGBA).
     *
     * @return the difference between the two pixels, in the range 0..1, where 0 means they are identical
     * and 1 means they are as different as they could possibly be.
     */
    public fun getDifference(
        actual: List<Float>,
        expected: List<Float>,
    ): Double

    /**
     * Calculates the difference between each component separately and returns an average.
     * This is suitable if you want small differences in color to have a small impact.
     */
    public object Scaled : PixelComparator {
        override fun getDifference(
            actual: List<Float>,
            expected: List<Float>,
        ): Double {
            var deltas = 0.0
            for (i in actual.indices) {
                val delta = abs(actual[i] - expected[i])
                deltas += delta
            }
            return deltas / actual.size
        }
    }

    /**
     * Calculates the difference between two pixels in a strict "all or nothing" manner.
     * Returns 0 if the pixels are identical, otherwise 1.
     */
    public object Strict : PixelComparator {
        override fun getDifference(
            actual: List<Float>,
            expected: List<Float>,
        ): Double {
            return if (actual == expected) 0.0 else 1.0
        }
    }
}
