package se.gustavkarlsson.slapshot.images.diffimagegenerators

import org.junit.jupiter.api.Test
import se.gustavkarlsson.slapshot.images.Color
import se.gustavkarlsson.slapshot.images.DiffMatrixImpl
import se.gustavkarlsson.slapshot.images.copy
import se.gustavkarlsson.slapshot.images.createImage
import se.gustavkarlsson.slapshot.images.fill
import se.gustavkarlsson.slapshot.images.getPixel
import se.gustavkarlsson.slapshot.images.setPixel
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class HighlightDiffImageGeneratorTest {
    @Test
    fun `generateDiffImage creates image with correct dimensions`() {
        val generator = HighlightDiffImageGenerator(Color.Red)
        val actual = createImage(width = 3, height = 2, fillColor = Color.White)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(width = 3, height = 2, values = doubleArrayOf(0.0, 0.5, 1.0, 0.2, 0.8, 0.3))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(3)
            get { height }.isEqualTo(2)
            get { type }.isEqualTo(BufferedImage.TYPE_INT_ARGB)
        }
    }

    @Test
    fun `generateDiffImage preserves original pixels for zero diff values`() {
        val generator = HighlightDiffImageGenerator(Color.Red)
        val originalColor = Color(100, 150, 200, 255)
        val actual = createImage(width = 2, height = 2, fillColor = originalColor)
        val expected = actual.copy()
        val diffMatrix = createDiffMatrix(width = 2, height = 2, values = doubleArrayOf(0.0, 0.0, 0.0, 0.0))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        // All pixels should remain unchanged
        expectThat(result) {
            get { getPixel(0, 0) }.isEqualTo(originalColor)
            get { getPixel(1, 0) }.isEqualTo(originalColor)
            get { getPixel(0, 1) }.isEqualTo(originalColor)
            get { getPixel(1, 1) }.isEqualTo(originalColor)
        }
    }

    @Test
    fun `generateDiffImage sets highlight color for maximum diff values`() {
        val highlightColor = Color(128, 255, 0, 128)
        val generator = HighlightDiffImageGenerator(highlightColor)
        val actual = createImage(width = 1, height = 1, fillColor = Color(0, 0, 0, 0))
        val expected = actual.copy().fill(Color(255, 255, 255, 255))
        val diffMatrix = createDiffMatrix(width = 1, height = 1, values = doubleArrayOf(1.0))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)
        expectThat(resultColor).isEqualTo(highlightColor)
    }

    @Test
    fun `generateDiffImage blends colors correctly for partial diff values on opaque image`() {
        val highlightColor = Color(200, 255, 0, 100)
        val generator = HighlightDiffImageGenerator(highlightColor)
        val originalColor = Color(100, 50, 150, 255)
        val actual = createImage(width = 1, height = 1, type = BufferedImage.TYPE_INT_RGB, fillColor = originalColor)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(width = 1, height = 1, values = doubleArrayOf(0.3))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)

        // For opaque images, alpha should be averaged
        val expectedAlpha = (255 + 200) / 2
        // Blend with factor 0.3: original * 0.7 + highlight * 0.3
        val expectedRed = (100 * 0.7 + 255 * 0.3).roundToInt()
        val expectedGreen = (50 * 0.7 + 0 * 0.3).roundToInt()
        val expectedBlue = (150 * 0.7 + 100 * 0.3).roundToInt()

        expectThat(resultColor) {
            get { alpha }.isEqualTo(expectedAlpha)
            get { red }.isEqualTo(expectedRed)
            get { green }.isEqualTo(expectedGreen)
            get { blue }.isEqualTo(expectedBlue)
        }
    }

    @Test
    fun `generateDiffImage handles translucent images with alpha blending`() {
        val highlightColor = Color(180, 255, 0, 100)
        val generator = HighlightDiffImageGenerator(highlightColor)
        val originalColor = Color(100, 50, 150, 200)
        val actual = createTranslucentImage(width = 1, height = 1, color = originalColor)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(width = 1, height = 1, values = doubleArrayOf(0.4))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)

        // For translucent images, highlight alpha is scaled by difference factor
        val scaledHighlightAlpha = (180 * 0.4).roundToInt()
        val blendColor = Color(scaledHighlightAlpha, 255, 0, 100)

        val expectedAlpha = (200 + scaledHighlightAlpha) / 2
        val expectedRed = (100 * 0.6 + 255 * 0.4).roundToInt()
        val expectedGreen = (50 * 0.6 + 0 * 0.4).roundToInt()
        val expectedBlue = (150 * 0.6 + 100 * 0.4).roundToInt()

        expectThat(resultColor) {
            get { alpha }.isEqualTo(expectedAlpha)
            get { red }.isEqualTo(expectedRed)
            get { green }.isEqualTo(expectedGreen)
            get { blue }.isEqualTo(expectedBlue)
        }
    }

    @Test
    fun `generateDiffImage handles mixed diff values correctly`() {
        val highlightColor = Color.Magenta
        val generator = HighlightDiffImageGenerator(highlightColor)

        val colors =
            arrayOf(
                Color(100, 100, 100, 255), // No diff (0.0)
                Color(200, 50, 150, 200), // Partial diff (0.5)
                Color(0, 255, 128, 100), // Full diff (1.0)
            )

        val actual = createMultiColorImage(width = 3, height = 1, colors = colors)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(width = 3, height = 1, values = doubleArrayOf(0.0, 0.5, 1.0))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        // First pixel: no diff (should remain unchanged)
        val color1 = result.getPixel(0, 0)
        expectThat(color1).isEqualTo(colors[0])

        // Second pixel: partial diff (should be blended)
        val color2 = result.getPixel(1, 0)
        val expectedAlpha2 = (200 + 255) / 2
        val expectedRed2 = (200 * 0.5 + 255 * 0.5).roundToInt()
        val expectedGreen2 = (50 * 0.5 + 0 * 0.5).roundToInt()
        val expectedBlue2 = (150 * 0.5 + 255 * 0.5).roundToInt()

        expectThat(color2) {
            get { alpha }.isEqualTo(expectedAlpha2)
            get { red }.isEqualTo(expectedRed2)
            get { green }.isEqualTo(expectedGreen2)
            get { blue }.isEqualTo(expectedBlue2)
        }

        // Third pixel: full diff (should be highlight color)
        val color3 = result.getPixel(2, 0)
        expectThat(color3).isEqualTo(highlightColor)
    }

    @Test
    fun `generateDiffImage preserves original image structure`() {
        val generator = HighlightDiffImageGenerator(Color.Yellow)
        val actual = createImage(width = 4, height = 3, fillColor = Color.Blue)
        val expected = actual.copy().fill(Color.Red)
        val diffMatrix =
            createDiffMatrix(
                width = 4,
                height = 3,
                values =
                    DoubleArray(12) {
                        if (it % 3 == 0) {
                            1.0
                        } else if (it % 3 == 1) {
                            0.0
                        } else {
                            0.6
                        }
                    },
            )

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(4)
            get { height }.isEqualTo(3)
        }

        // Verify pattern: full diff, no diff, partial diff, repeat
        for (y in 0 until 3) {
            for (x in 0 until 4) {
                val index = y * 4 + x
                val pixelColor = result.getPixel(x, y)
                when (index % 3) {
                    0 -> expectThat(pixelColor).isEqualTo(Color.Yellow) // Full diff
                    1 -> expectThat(pixelColor).isEqualTo(Color.Blue) // No diff
                    2 -> { // Partial diff - should be blended
                        val expectedAlpha = (255 + 255) / 2
                        val expectedRed = (0 * 0.4 + 255 * 0.6).roundToInt()
                        val expectedGreen = (0 * 0.4 + 255 * 0.6).roundToInt()
                        val expectedBlue = (255 * 0.4 + 0 * 0.6).roundToInt()

                        expectThat(pixelColor) {
                            get { alpha }.isEqualTo(expectedAlpha)
                            get { red }.isEqualTo(expectedRed)
                            get { green }.isEqualTo(expectedGreen)
                            get { blue }.isEqualTo(expectedBlue)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `generateDiffImage handles single pixel image`() {
        val highlightColor = Color(64, 128, 192, 96)
        val generator = HighlightDiffImageGenerator(highlightColor)
        val originalColor = Color(200, 100, 50, 180)
        val actual = createImage(width = 1, height = 1, fillColor = originalColor)
        val expected = actual.copy().fill(Color.White)
        val diffMatrix = createDiffMatrix(width = 1, height = 1, values = doubleArrayOf(0.75))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(1)
            get { height }.isEqualTo(1)
        }

        val resultColor = result.getPixel(0, 0)
        val expectedAlpha = (180 + 64) / 2
        val expectedRed = (200 * 0.25 + 128 * 0.75).roundToInt()
        val expectedGreen = (100 * 0.25 + 192 * 0.75).roundToInt()
        val expectedBlue = (50 * 0.25 + 96 * 0.75).roundToInt()

        expectThat(resultColor) {
            get { alpha }.isEqualTo(expectedAlpha)
            get { red }.isEqualTo(expectedRed)
            get { green }.isEqualTo(expectedGreen)
            get { blue }.isEqualTo(expectedBlue)
        }
    }

    @Test
    fun `generateDiffImage handles extreme color values`() {
        val highlightColor = Color(0, 255, 255, 255) // Max values except alpha
        val generator = HighlightDiffImageGenerator(highlightColor)
        val originalColor = Color(255, 0, 0, 0) // Max red, min others
        val actual = createImage(width = 1, height = 1, fillColor = originalColor)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(width = 1, height = 1, values = doubleArrayOf(0.5))

        val result = generator.generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)
        val expectedAlpha = (0 + 0) / 2
        val expectedRed = (255 * 0.5 + 255 * 0.5).roundToInt()
        val expectedGreen = (0 * 0.5 + 255 * 0.5).roundToInt()
        val expectedBlue = (0 * 0.5 + 255 * 0.5).roundToInt()

        expectThat(resultColor) {
            get { alpha }.isEqualTo(expectedAlpha)
            get { red }.isEqualTo(expectedRed)
            get { green }.isEqualTo(expectedGreen)
            get { blue }.isEqualTo(expectedBlue)
        }
    }

    @Test
    fun `data class properties work correctly`() {
        val color1 = Color.Red
        val color2 = Color.Blue
        val generator1 = HighlightDiffImageGenerator(color1)
        val generator2 = HighlightDiffImageGenerator(color1)
        val generator3 = HighlightDiffImageGenerator(color2)

        expectThat(generator1).isEqualTo(generator2)
        expectThat(generator1 == generator3).isEqualTo(false)
        expectThat(generator1.color).isEqualTo(color1)
        expectThat(generator3.color).isEqualTo(color2)
    }

    private fun createTranslucentImage(
        width: Int,
        height: Int,
        color: Color,
    ): BufferedImage {
        return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).fill(color)
    }

    private fun createMultiColorImage(
        width: Int,
        height: Int,
        colors: Array<Color>,
    ): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (i in 0 until width * height) {
            val x = i % width
            val y = i / width
            val color = colors[i % colors.size]
            image.setPixel(x, y, color)
        }
        return image
    }

    private fun createDiffMatrix(
        width: Int,
        height: Int,
        values: DoubleArray,
    ): DiffMatrixImpl {
        return DiffMatrixImpl(width, height, values)
    }
}
