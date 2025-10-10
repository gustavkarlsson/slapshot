package se.gustavkarlsson.slapshot.images.diffimagegenerators

import se.gustavkarlsson.slapshot.images.Color
import se.gustavkarlsson.slapshot.images.DiffImageGenerator
import se.gustavkarlsson.slapshot.images.DiffMatrix
import java.awt.image.BufferedImage
import kotlin.math.abs

// FIXME test
internal data object DiffsOnlyDiffImageGenerator : DiffImageGenerator {
    private val transparent = Color(0, 255, 255, 255) // Transparent

    override fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage {
        val diffImage = BufferedImage(actual.width, actual.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until actual.height) {
            for (x in 0 until actual.width) {
                val color =
                    when (diffMatrix[x, y]) {
                        0.0 -> transparent // No difference, draw transparent
                        else -> {
                            // Diff each color component and generate a new pixel based on the diffs.
                            val actualColor = Color(actual.getRGB(x, y))
                            val expectedColor = Color(expected.getRGB(x, y))
                            val avgAlpha = (actualColor.alpha + expectedColor.alpha) / 2
                            val deltaRed = abs(actualColor.red - expectedColor.red)
                            val deltaGreen = abs(actualColor.green - expectedColor.green)
                            val deltaBlue = abs(actualColor.blue - expectedColor.blue)
                            Color(avgAlpha, deltaRed, deltaGreen, deltaBlue)
                        }
                    }
                diffImage.setRGB(x, y, color.packedValue)
            }
        }
        return diffImage
    }
}
