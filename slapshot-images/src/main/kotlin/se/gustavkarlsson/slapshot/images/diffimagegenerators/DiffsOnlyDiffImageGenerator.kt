package se.gustavkarlsson.slapshot.images.diffimagegenerators

import se.gustavkarlsson.slapshot.images.Color
import se.gustavkarlsson.slapshot.images.DiffImageGenerator
import se.gustavkarlsson.slapshot.images.DiffMatrix
import se.gustavkarlsson.slapshot.images.getPixel
import se.gustavkarlsson.slapshot.images.setPixel
import java.awt.image.BufferedImage
import kotlin.math.abs

// FIXME test
internal data object DiffsOnlyDiffImageGenerator : DiffImageGenerator {
    override fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage {
        val diffImage = BufferedImage(actual.width, actual.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until actual.height) {
            for (x in 0 until actual.width) {
                val color =
                    when (val difference = diffMatrix[x, y]) {
                        // FIXME make background configurable?
                        0.0 -> Color.Transparent // No difference, draw transparent
                        else -> {
                            val actualColor = actual.getPixel(x, y)
                            val expectedColor = expected.getPixel(x, y)
                            val alpha = (difference * 255).toInt()
                            // Diff each color component and generate a new pixel based on the diffs.
                            val deltaRed = abs(actualColor.red - expectedColor.red)
                            val deltaGreen = abs(actualColor.green - expectedColor.green)
                            val deltaBlue = abs(actualColor.blue - expectedColor.blue)
                            Color(alpha, deltaRed, deltaGreen, deltaBlue)
                        }
                    }
                diffImage.setPixel(x, y, color)
            }
        }
        return diffImage
    }
}
