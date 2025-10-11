package se.gustavkarlsson.slapshot.images.diffimagegenerators

import org.junit.jupiter.api.Test
import se.gustavkarlsson.slapshot.images.Color
import se.gustavkarlsson.slapshot.images.DiffMatrixImpl
import se.gustavkarlsson.slapshot.images.copy
import se.gustavkarlsson.slapshot.images.createImage
import se.gustavkarlsson.slapshot.images.diffimagegenerators.DiffsOnlyDiffImageGenerator.generateDiffImage
import se.gustavkarlsson.slapshot.images.fill
import se.gustavkarlsson.slapshot.images.getPixel
import se.gustavkarlsson.slapshot.images.pixels
import se.gustavkarlsson.slapshot.images.setPixel
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo
import java.awt.image.BufferedImage
import kotlin.math.abs

class DiffsOnlyDiffImageGeneratorTest {
    @Test
    fun `generateDiffImage creates image with correct dimensions`() {
        val actual = createImage(width = 3, height = 2)
        val expected = actual.copy()
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(0.0, 0.5, 1.0, 0.2, 0.8, 0.3))

        val result = generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(3)
            get { height }.isEqualTo(2)
            get { type }.isEqualTo(BufferedImage.TYPE_INT_ARGB)
        }
    }

    @Test
    fun `generateDiffImage creates transparent pixels for zero diff values`() {
        val actual = createImage(width = 3, height = 2)
        val expected = actual.copy()
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

        val result = generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get("pixels") { pixels() }.all { isEqualTo(Color.Transparent) }
        }
    }

    @Test
    fun `generateDiffImage creates diff pixels for non-zero diff values with identical colors`() {
        val actual = createImage(width = 1, height = 1)
        val expected = actual.copy()
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(0.5))

        val result = generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)
        expectThat(resultColor) {
            get { alpha }.isEqualTo(127) // 0.5 diff -> 127 alpha
            get { red }.isEqualTo(0) // No difference
            get { green }.isEqualTo(0) // No difference
            get { blue }.isEqualTo(0) // No difference
        }
    }

    @Test
    fun `generateDiffImage calculates color differences correctly`() {
        val actualColor = Color(255, 200, 100, 50)
        val expectedColor = Color(200, 150, 120, 80)
        val actual = createImage(width = 1, height = 1, fillColor = actualColor)
        val expected = actual.copy().fill(expectedColor)
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(1.0))

        val result = generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)
        val expectedAlpha = 255 // 1.0 diff -> 255 alpha
        val expectedRed = abs(200 - 150)
        val expectedGreen = abs(100 - 120)
        val expectedBlue = abs(50 - 80)

        expectThat(resultColor) {
            get { alpha }.isEqualTo(expectedAlpha)
            get { red }.isEqualTo(expectedRed)
            get { green }.isEqualTo(expectedGreen)
            get { blue }.isEqualTo(expectedBlue)
        }
    }

    @Test
    fun `generateDiffImage handles maximum color differences`() {
        val actualColor = Color(255, 255, 255, 255)
        val expectedColor = Color(0, 0, 0, 0)
        val actual = createImage(width = 1, height = 1, fillColor = actualColor)
        val expected = actual.copy().fill(expectedColor)
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(1.0))

        val result = generateDiffImage(actual, expected, diffMatrix)

        val resultColor = result.getPixel(0, 0)
        expectThat(resultColor) {
            get { alpha }.isEqualTo(255) // 1.0 diff -> 255 alpha
            get { red }.isEqualTo(255) // abs(255 - 0) = 255
            get { green }.isEqualTo(255) // abs(255 - 0) = 255
            get { blue }.isEqualTo(255) // abs(255 - 0) = 255
        }
    }

    @Test
    fun `generateDiffImage handles mixed diff matrix values correctly`() {
        val actualColors =
            listOf(
                Color(255, 100, 100, 100),
                Color(200, 200, 50, 150),
                Color(100, 0, 255, 128),
            )
        val expectedColors =
            listOf(
                Color(255, 100, 100, 100),
                Color(220, 180, 70, 130),
                Color(150, 50, 200, 100),
            )

        val actual = createMultiColorImage(width = 3, height = 1, colors = actualColors)
        val expected = createMultiColorImage(width = 3, height = 1, colors = expectedColors)
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(0.0, 0.5, 1.0))

        val result = generateDiffImage(actual, expected, diffMatrix)

        // First pixel: no diff (0.0)
        val color1 = result.getPixel(0, 0)
        expectThat(color1).isEqualTo(Color.Transparent)

        // Second pixel: some diff (0.5)
        val color2 = result.getPixel(1, 0)
        expectThat(color2) {
            get { alpha }.isEqualTo(127) // 0.5 diff -> 127 alpha
            get { red }.isEqualTo(20) // abs(200 - 180)
            get { green }.isEqualTo(20) // abs(50 - 70)
            get { blue }.isEqualTo(20) // abs(150 - 130)
        }

        // Third pixel: max diff (1.0)
        val color3 = result.getPixel(2, 0)
        expectThat(color3) {
            get { alpha }.isEqualTo(255) // 1.0 diff -> 255 alpha
            get { red }.isEqualTo(50) // abs(0 - 50)
            get { green }.isEqualTo(55) // abs(255 - 200)
            get { blue }.isEqualTo(28) // abs(128 - 100)
        }
    }

    @Test
    fun `generateDiffImage preserves original image dimensions with different aspect ratios`() {
        val actual = createImage(width = 5, height = 3, fillColor = Color.White)
        val expected = actual.copy().fill(Color.Black)
        val diffMatrix = createDiffMatrix(actual, values = DoubleArray(15) { if (it % 2 == 0) 0.0 else 1.0 })

        val result = generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(5)
            get { height }.isEqualTo(3)
        }

        // Check that alternating pixels are transparent/diff
        for (y in 0 until 3) {
            for (x in 0 until 5) {
                val index = y * 5 + x
                val pixelColor = result.getPixel(x, y)
                if (index % 2 == 0) {
                    expectThat(pixelColor).isEqualTo(Color.Transparent)
                } else {
                    expectThat(pixelColor.alpha).isEqualTo(255) // 1.0 diff -> 255 alpha
                    expectThat(pixelColor.red).isEqualTo(255) // Max difference
                    expectThat(pixelColor.green).isEqualTo(255) // Max difference
                    expectThat(pixelColor.blue).isEqualTo(255) // Max difference
                }
            }
        }
    }

    @Test
    fun `generateDiffImage handles single pixel image`() {
        val actual = createImage(width = 1, height = 1, fillColor = Color(200, 128, 64, 32))
        val expected = actual.copy().fill(Color(180, 100, 80, 50))
        val diffMatrix = createDiffMatrix(actual, values = doubleArrayOf(0.75))

        val result = generateDiffImage(actual, expected, diffMatrix)

        expectThat(result) {
            get { width }.isEqualTo(1)
            get { height }.isEqualTo(1)
            get { getPixel(0, 0) }.and {
                get { alpha }.isEqualTo(191) // 0.75 diff -> 191 alpha
                get { red }.isEqualTo(28) // abs(128 - 100)
                get { green }.isEqualTo(16) // abs(64 - 80)
                get { blue }.isEqualTo(18) // abs(32 - 50)
            }
        }
    }
}

private fun createMultiColorImage(
    width: Int,
    height: Int,
    colors: List<Color>,
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
    sizeOf: BufferedImage,
    values: DoubleArray,
): DiffMatrixImpl {
    return DiffMatrixImpl(sizeOf.width, sizeOf.height, values)
}
