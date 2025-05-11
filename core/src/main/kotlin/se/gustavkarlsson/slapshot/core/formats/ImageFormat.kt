package se.gustavkarlsson.slapshot.core.formats

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.gustavkarlsson.slapshot.core.SnapshotFormat
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicLong
import javax.imageio.ImageIO
import kotlin.math.abs

// TODO Add tests

/**
 * A snapshot format for handling images.
 *
 * When comparing snapshots, the value of every pixel is compared to produce a total difference,
 * and if the total difference is greater than [tolerance], it's considered a mismatch.
 *
 * The comparison is straightforward and compares every pixel's respective color values.
 */
public data class ImageFormat(
    /**
     * How different the images may be, from 0.0 to 1.0, where 0.0 means every pixel must be an exact match,
     * and 1.0 means the images are 100% different.
     */
    val tolerance: Double = 0.0,
    override val fileExtension: String = "bmp",
    /**
     * Specifies the file format for encoding the images. See [ImageIO] for reference.
     */
    val fileFormat: String = fileExtension,
) : SnapshotFormat<BufferedImage> {
    init {
        require(tolerance in 0.0..1.0) {
            "tolerance must be 0..1: <$tolerance>"
        }
    }

    override fun test(
        actual: BufferedImage,
        expected: BufferedImage,
    ): String? {
        validate(actual, expected)?.let { error -> return error }
        val difference = getDifference(actual, expected)
        return if (difference > tolerance) {
            "Images differ by $difference"
        } else {
            null
        }
    }

    private fun validate(
        actual: BufferedImage,
        expected: BufferedImage,
    ): String? =
        when {
            actual.width != expected.width || actual.height != expected.height -> {
                "Images have different dimensions!"
            }

            actual.colorModel.transferType != expected.colorModel.transferType -> {
                "Images have different transfer type"
            }

            actual.colorModel.colorSpace != expected.colorModel.colorSpace -> {
                "Images have different color space!"
            }

            actual.colorModel.transparency != expected.colorModel.transparency -> {
                "Images have different transparency type"
            }

            actual.colorModel.isAlphaPremultiplied != expected.colorModel.isAlphaPremultiplied -> {
                "Images have different values for isAlphaPremultiplied"
            }

            !actual.colorModel.componentSize.contentEquals(expected.colorModel.componentSize) -> {
                "Images have different component sizes! Alpha missing?"
            }

            !actual.colorModel.componentSize.all { it == 8 } -> {
                "Component size is not 8 bits"
            }

            else -> null
        }

    private fun getDifference(
        actual: BufferedImage,
        expected: BufferedImage,
    ): Double {
        val hasAlpha = expected.colorModel.hasAlpha()
        val pixelDeltas = AtomicLong()
        // Launch a coroutine per line, for parallelism
        runBlocking(Dispatchers.Default) {
            repeat(actual.height) { y ->
                launch {
                    repeat(actual.width) { x ->
                        val actualColor = Color(actual.getRGB(x, y), hasAlpha)
                        val expectedColor = Color(expected.getRGB(x, y), hasAlpha)
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

    override fun deserialize(bytes: ByteArray): BufferedImage {
        return bytes.inputStream().use { stream ->
            ImageIO.read(stream)
        }
    }

    override fun serialize(value: BufferedImage): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            check(ImageIO.write(value, fileFormat, stream)) {
                "Failed to write image to ByteArrayOutputStream"
            }
            stream.toByteArray()
        }
    }
}
