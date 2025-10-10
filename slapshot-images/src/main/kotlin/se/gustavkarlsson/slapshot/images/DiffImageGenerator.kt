package se.gustavkarlsson.slapshot.images

import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import java.util.Hashtable
import kotlin.math.abs

// FIXME document
public fun interface DiffImageGenerator {
    // FIXME document
    public fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage

    // FIXME document
    public data class Highlight(val color: Color = Color(255, 255, 0, 255)) : DiffImageGenerator {
        override fun generateDiffImage(
            actual: BufferedImage,
            expected: BufferedImage,
            diffMatrix: DiffMatrix,
        ): BufferedImage {
            val diffImage = actual.copy() // FIXME What if not ARGB 8-bit?
            for (y in 0 until actual.height) {
                for (x in 0 until actual.width) {
                    when (val difference = diffMatrix[x, y]) {
                        0.0 -> continue // No difference, leave pixel as is
                        1.0 -> diffImage.setRGB(x, y, color.packedValue) // Full difference, set to highlight color
                        else -> {
                            // Blend the original color with the highlight color based on the difference, and draw that
                            val originalColor = Color(diffImage.getRGB(x, y))
                            val blendColor = color.copy(alpha = color.alphaDouble * difference)
                            val newColor = originalColor.blendWith(blendColor)
                            diffImage.setRGB(x, y, newColor.packedValue)
                        }
                    }
                }
            }
            return diffImage
        }
    }

    // FIXME document
    public data object SideBySide : DiffImageGenerator {
        override fun generateDiffImage(
            actual: BufferedImage,
            expected: BufferedImage,
            diffMatrix: DiffMatrix,
        ): BufferedImage {
            val diffImage =
                BufferedImage(
                    actual.width + expected.width,
                    actual.height,
                    BufferedImage.TYPE_INT_ARGB,
                )
            val graphics = diffImage.graphics
            try {
                graphics.drawImage(expected, 0, 0, null)
                graphics.drawImage(actual, expected.width, 0, null)
            } finally {
                graphics.dispose()
            }
            return diffImage
        }
    }

    // FIXME document
    public data object OnlyDiffs : DiffImageGenerator {
        private val transparent = Color(0x00808080)

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
                            0.0 -> transparent // No difference, draw background
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
}

private fun BufferedImage.copy(): BufferedImage {
    return BufferedImage(colorModel, copyRaster(), isAlphaPremultiplied, properties)
}

private fun BufferedImage.copyRaster(): WritableRaster {
    return raster.createCompatibleWritableRaster().also(::copyData)
}

private val BufferedImage.properties: Hashtable<String, Any?>?
    get() {
        if (propertyNames == null) {
            return null
        }
        val newProperties = Hashtable<String, Any?>()
        for (propName in propertyNames) {
            newProperties[propName] = getProperty(propName)
        }
        return newProperties
    }

// FIXME handle alpha
private fun Color.blendWith(blendColor: Color): Color {
    // Normalize blend alpha to 0..1
    val blendFactor = blendColor.alphaDouble

    val avgAlpha = (alpha + blendColor.alpha) / 2
    // FIXME do we need to coerce?
    val newRed = (red * (1 - blendFactor) + blendColor.red * blendFactor).toInt().coerceIn(0, 255)
    val newGreen = (green * (1 - blendFactor) + blendColor.green * blendFactor).toInt().coerceIn(0, 255)
    val newBlue = (blue * (1 - blendFactor) + blendColor.blue * blendFactor).toInt().coerceIn(0, 255)

    return Color(avgAlpha, newRed, newGreen, newBlue)
}

public fun main() {
    val actualRgb = 0xFF112233.toInt()
    val expectedRgb = 0xFF223344.toInt()
    val deltaR = ((actualRgb and 0xFF0000) ushr 16) - ((expectedRgb and 0xFF0000) ushr 16)
    println(deltaR)
}
