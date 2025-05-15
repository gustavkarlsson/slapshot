package se.gustavkarlsson.slapshot.core.testers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.gustavkarlsson.slapshot.core.Tester
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

/**
 * Tests image snapshots. Compares pixel by pixel and accumulates the difference to a total value (0..1),
 * If the total difference is greater than [tolerance], it's considered a mismatch.
 */
public data class ImageTester(
    /**
     * How different the images may be, from 0.0 to 1.0, where 0.0 means every pixel must be an exact match,
     * and 1.0 means the images are 100% different.
     */
    val tolerance: Double = 0.0,
) : Tester<BufferedImage> {
    init {
        require(tolerance in 0.0..1.0) {
            "tolerance must be 0..1: <$tolerance>"
        }
    }

    override fun test(
        actual: BufferedImage,
        expected: BufferedImage,
    ): String? {
        testMetadata(actual, expected)?.let { error -> return error }
        val difference = getDifference(actual, expected)
        return if (difference > tolerance) {
            "Images differ by $difference"
        } else {
            null
        }
    }

    private fun testMetadata(
        actual: BufferedImage,
        expected: BufferedImage,
    ): String? =
        when {
            actual.width != expected.width || actual.height != expected.height -> {
                "Images have different dimensions." +
                    " Expected: ${expected.width}x${expected.height}, actual: ${actual.width}x${actual.height}"
            }

            actual.colorModel.colorSpace != expected.colorModel.colorSpace -> {
                "Images have different color space"
            }

            actual.colorModel.transparency != expected.colorModel.transparency -> {
                "Images have different transparency type"
            }

            actual.colorModel.numComponents != (expected.colorModel.numComponents) -> {
                "Images have different numbers of components. Alpha missing?" +
                        " Expected: ${expected.colorModel.numComponents}, actual: ${actual.colorModel.numComponents}"
            }

            !actual.colorModel.componentSize.all { it == 8 } -> {
                "Component size is not 8 bits: ${actual.colorModel.componentSize}"
            }

            else -> null
        }

    private fun getDifference(
        actual: BufferedImage,
        expected: BufferedImage,
    ): Double {
        val actualHasAlpha = actual.colorModel.hasAlpha()
        val expectedHasAlpha = expected.colorModel.hasAlpha()
        val pixelDeltas = AtomicLong()
        // Launch a coroutine per line, for parallelism
        runBlocking(Dispatchers.Default) {
            repeat(actual.height) { y ->
                launch {
                    repeat(actual.width) { x ->
                        val actualColor = Color(actual.getRGB(x, y), actualHasAlpha)
                        val expectedColor = Color(expected.getRGB(x, y), expectedHasAlpha)
                        val pixelDelta = getPixelDelta(actualColor, expectedColor)
                        if (pixelDelta > 0) {
                            pixelDeltas.addAndGet(pixelDelta.toLong())
                        }
                    }
                }
            }
        }
        val pixelCount = actual.width * actual.height
        val componentsPerPixel = actual.colorModel.numComponents
        val maxPossibleDelta = pixelCount.toLong() * componentsPerPixel * 255
        return pixelDeltas.get().toDouble() / maxPossibleDelta
    }

    private fun getPixelDelta(
        actualColor: Color,
        expectedColor: Color,
    ): Int {
        val redDelta = abs(actualColor.red - expectedColor.red)
        val greenDelta = abs(actualColor.green - expectedColor.green)
        val blueDelta = abs(actualColor.blue - expectedColor.blue)
        val alphaDelta = abs(actualColor.alpha - expectedColor.alpha)
        return redDelta + greenDelta + blueDelta + alphaDelta
    }
}
