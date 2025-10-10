package se.gustavkarlsson.slapshot.images.diffimagegenerators

import se.gustavkarlsson.slapshot.images.Color
import se.gustavkarlsson.slapshot.images.DiffImageGenerator
import se.gustavkarlsson.slapshot.images.DiffMatrix
import se.gustavkarlsson.slapshot.images.copy
import java.awt.Transparency
import java.awt.image.BufferedImage

// FIXME test
internal data class HighlightDiffImageGenerator(val color: Color) : DiffImageGenerator {
    override fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage {
        val diffImage = actual.copy()
        for (y in 0 until actual.height) {
            for (x in 0 until actual.width) {
                when (val difference = diffMatrix[x, y]) {
                    0.0 -> continue // No difference, leave pixel as is
                    1.0 -> diffImage.setRGB(x, y, color.packedValue) // Full difference, set to highlight color
                    else -> {
                        // Blend the original color with the highlight color based on the difference, and draw that
                        val originalColor = Color(diffImage.getRGB(x, y))
                        val newAlpha =
                            if (diffImage.transparency == Transparency.TRANSLUCENT) {
                                color.alphaDouble * difference
                            } else {
                                1.0
                            }
                        val blendColor = color.copy(alpha = newAlpha)
                        val newColor = originalColor.blendWith(blendColor, difference)
                        diffImage.setRGB(x, y, newColor.packedValue)
                    }
                }
            }
        }
        return diffImage
    }
}

private fun Color.blendWith(
    blendColor: Color,
    factor: Double,
): Color {
    val avgAlpha = (alpha + blendColor.alpha) / 2
    val newRed = (red * (1 - factor) + blendColor.red * factor).toInt()
    val newGreen = (green * (1 - factor) + blendColor.green * factor).toInt()
    val newBlue = (blue * (1 - factor) + blendColor.blue * factor).toInt()
    return Color(avgAlpha, newRed, newGreen, newBlue)
}
