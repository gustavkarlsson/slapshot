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
public data class ImageFormat(
    val tolerance: Double = 0.0,
    override val fileExtension: String = "bmp",
    val fileFormat: String = fileExtension,
) : SnapshotFormat<BufferedImage> {
    init {
        require(tolerance >= 0.0 && !tolerance.isNaN()) {
            "tolerance must be non-negative but was: <$tolerance>"
        }
    }

    override fun test(
        actual: BufferedImage,
        expected: BufferedImage,
    ): String? {
        validate(expected, actual)?.let { error -> return error }
        val difference = getDifference(expected, actual)
        return if (difference > tolerance) {
            "Images differ by $difference"
        } else {
            null
        }
    }

    private fun validate(
        expected: BufferedImage,
        actual: BufferedImage,
    ): String? =
        when {
            expected.width != actual.width || expected.height != actual.height -> {
                "Images have different dimensions!"
            }

            expected.colorModel.transferType != actual.colorModel.transferType -> {
                "Images have different transfer type"
            }

            expected.colorModel.colorSpace != actual.colorModel.colorSpace -> {
                "Images have different color space!"
            }

            expected.colorModel.transparency != actual.colorModel.transparency -> {
                "Images have different transparency type"
            }

            expected.colorModel.isAlphaPremultiplied != actual.colorModel.isAlphaPremultiplied -> {
                "Images have different values for isAlphaPremultiplied"
            }

            !expected.colorModel.componentSize.contentEquals(actual.colorModel.componentSize) -> {
                "Images have different component sizes! Alpha missing?"
            }

            !expected.colorModel.componentSize.all { it == 8 } -> {
                "Component size is not 8 bits"
            }

            else -> null
        }

    private fun getDifference(
        expected: BufferedImage,
        actual: BufferedImage,
    ): Double {
        val hasAlpha = expected.colorModel.hasAlpha()
        val pixelDeltas = AtomicLong()
        // Launch a coroutine per line, for parallelism
        runBlocking(Dispatchers.Default) {
            repeat(expected.height) { y ->
                launch {
                    repeat(expected.width) { x ->
                        val expectedColor = Color(expected.getRGB(x, y), hasAlpha)
                        val actualColor = Color(actual.getRGB(x, y), hasAlpha)
                        val pixelDelta = getPixelDelta(expectedColor, actualColor)
                        if (pixelDelta > 0) {
                            pixelDeltas.addAndGet(pixelDelta.toLong())
                        }
                    }
                }
            }
        }
        val pixelCount = expected.width * expected.height
        val componentsPerPixel = expected.colorModel.numComponents
        val maxPossibleDelta = pixelCount.toLong() * componentsPerPixel * 255
        return pixelDeltas.get().toDouble() / maxPossibleDelta
    }

    private fun getPixelDelta(
        expectedColor: Color,
        actualColor: Color,
    ): Int {
        val redDelta = abs(expectedColor.red - actualColor.red)
        val greenDelta = abs(expectedColor.green - actualColor.green)
        val blueDelta = abs(expectedColor.blue - actualColor.blue)
        val alphaDelta = abs(expectedColor.alpha - actualColor.alpha)
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
