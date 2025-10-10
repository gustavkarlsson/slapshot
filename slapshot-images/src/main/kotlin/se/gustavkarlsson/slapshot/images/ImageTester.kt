package se.gustavkarlsson.slapshot.images

import se.gustavkarlsson.slapshot.core.Tester
import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.IntStream
import javax.imageio.ImageIO
import kotlin.math.pow

// FIXME test

/**
 * Tests image snapshots. Compares pixel by pixel and accumulates the difference to a total value (0..1),
 * If the total difference is greater than [tolerance], it's considered a mismatch.
 */
public data class ImageTester(
    /**
     * How different the images may be, from 0.0 to 1.0, where 0.0 means every pixel must be an exact match,
     * and 1.0 means the images can be 100% different.
     */
    val tolerance: Double = 0.0,
    /**
     * Defines how two individual pixels are compared between the actual and expected images.
     */
    val pixelComparator: PixelComparator = PixelComparator.Scaled,
    /**
     * If set, will use the provided generator to generate a diff image and include its path in the error message.
     */
    val diffImageGenerator: DiffImageGenerator? = null,
) : Tester<BufferedImage> {
    // FIXME allow to configure where to save images, and format.
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
        val diffMatrix = getDiffMatrix(actual, expected)
        val differance = diffMatrix.sequence().average()
        return if (differance > tolerance) {
            val formattedDifference = "%.2f%%".format(differance * 100)
            if (diffImageGenerator != null) {
                val diffImage = diffImageGenerator.generateDiffImage(actual, expected, diffMatrix)
                // FIXME hard-code png?
                val diffImagePath = writeToTempFile(diffImage, "png").toAbsolutePath()
                "Images differ by $formattedDifference. Diff image written to: $diffImagePath"
            } else {
                "Images differ by $formattedDifference"
            }
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
                    " Expected: ${expected.width}x${expected.height}," +
                    " actual: ${actual.width}x${actual.height}"
            }

            actual.colorModel.colorSpace != expected.colorModel.colorSpace -> {
                "Images have different color space"
            }

            actual.colorModel.transparency != expected.colorModel.transparency -> {
                "Images have different transparency type." +
                    " Expected: ${transparencyTypeToString(expected.colorModel.transparency)}," +
                    " actual: ${transparencyTypeToString(actual.colorModel.transparency)}"
            }

            actual.colorModel.numComponents != (expected.colorModel.numComponents) -> {
                "Images have different numbers of components. Alpha missing?" +
                    " Expected: ${expected.colorModel.numComponents}," +
                    " actual: ${actual.colorModel.numComponents}"
            }

            !actual.colorModel.componentSize.contentEquals(expected.colorModel.componentSize) -> {
                "Component sizes do not match." +
                    " Expected: ${expected.colorModel.componentSize.contentToString()}," +
                    " actual: ${actual.colorModel.componentSize.contentToString()}"
            }

            else -> null
        }

    private fun transparencyTypeToString(type: Int): String =
        when (type) {
            BufferedImage.OPAQUE -> "OPAQUE"
            BufferedImage.BITMASK -> "BITMASK"
            BufferedImage.TRANSLUCENT -> "TRANSLUCENT"
            else -> "UNKNOWN($type)"
        }

    private fun getDiffMatrix(
        actual: BufferedImage,
        expected: BufferedImage,
    ): DiffMatrixImpl {
        val pixelCount = actual.width * actual.height
        val componentBitDepths = actual.componentBitDepths
        val diffData =
            IntStream.range(0, pixelCount)
                .parallel()
                .mapToDouble { pixelIndex ->
                    val x = pixelIndex % actual.width
                    val y = pixelIndex / actual.width
                    val actualPixel = actual.raster.getNormalizedPixel(x, y, componentBitDepths)
                    val expectedPixel = expected.raster.getNormalizedPixel(x, y, componentBitDepths)
                    pixelComparator.getDifference(actualPixel, expectedPixel)
                }
                .toArray()
        return DiffMatrixImpl(width = actual.width, height = actual.height, data = diffData)
    }
}

private fun Raster.getNormalizedPixel(
    x: Int,
    y: Int,
    componentBitDepths: List<Int>,
): List<Float> {
    val rawPixel = getPixel(x, y, null as FloatArray?)
    return rawPixel.apply {
        normalizeComponents(componentBitDepths)
    }.asList()
}

private fun FloatArray.normalizeComponents(componentBitDepths: List<Int>) {
    for (index in indices) {
        val bitDepth = componentBitDepths[index]
        val maxValue = 2f.pow(bitDepth)
        this[index] = this[index] / maxValue
    }
}

private val BufferedImage.componentBitDepths: List<Int>
    get() =
        List(colorModel.numComponents) { i ->
            colorModel.componentSize[i]
        }

private fun writeToTempFile(
    image: BufferedImage,
    extension: String,
): Path {
    val path = Files.createTempFile("snapshot-diff-", ".$extension")
    ImageIO.write(image, extension, path.toFile())
    return path
}
