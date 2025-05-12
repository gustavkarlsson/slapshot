package se.gustavkarlsson.slapshot.core.formats

import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.awt.Color
import java.awt.image.BufferedImage

class ImageFormatTest {
    @Test
    fun `tolerance out of range throws exception`() {
        expectThrows<IllegalArgumentException> {
            ImageFormat(tolerance = -0.1)
        }
    }

    @Test
    fun `different dimensions`() {
        val format = ImageFormat()
        val image1 = createImage()
        val image2 = createImage(width = 20, height = 20)

        val error = format.test(image1, image2)
        expectThat(error).isNotNull()
    }

    @Test
    fun `different transparency`() {
        val format = ImageFormat()
        val image1 = createImage(type = BufferedImage.TYPE_INT_ARGB)
        val image2 = createImage(type = BufferedImage.TYPE_INT_RGB)

        val error = format.test(image1, image2)
        expectThat(error).isNotNull()
    }

    @Test
    fun `test images are identical`() {
        val format = ImageFormat()
        val image1 = createImage(fillColor = Color.RED)
        val image2 = createImage(fillColor = Color.RED)

        val error = format.test(image1, image2)
        expectThat(error).isNull()
    }

    @Test
    fun `images differ beyond tolerance`() {
        val format = ImageFormat(tolerance = 0.1)
        val image1 = createImage(fillColor = Color.RED)
        val image2 = createImage(fillColor = Color.BLUE)

        val error = format.test(image1, image2)
        expectThat(error).isNotNull()
    }

    @Test
    fun `images differ within tolerance`() {
        val format = ImageFormat(tolerance = 1.0)
        val image1 = createImage(fillColor = Color.WHITE)
        val image2 = createImage(fillColor = Color.BLACK)

        val error = format.test(image1, image2)
        expectThat(error).isNull()
    }

    @Test
    fun `serialize and deserialize jpeg`() {
        val format = ImageFormat(fileFormat = "jpeg")
        val image =
            createImage(
                type = BufferedImage.TYPE_INT_RGB,
                fillColor = Color(127, 127, 127),
            )

        testSerialization(format, image)
    }

    @Test
    fun `serialize and deserialize bmp`() {
        val format = ImageFormat(fileFormat = "bmp")
        val image =
            createImage(
                type = BufferedImage.TYPE_INT_RGB,
                fillColor = Color(127, 127, 127),
            )

        testSerialization(format, image)
    }

    @Test
    fun `serialize and deserialize png`() {
        val format = ImageFormat(fileFormat = "png")
        val image =
            createImage(
                type = BufferedImage.TYPE_INT_RGB,
                fillColor = Color(127, 127, 127),
            )

        testSerialization(format, image)
    }

    @Test
    fun `serialize and deserialize transparent png`() {
        val format = ImageFormat(fileFormat = "png")
        val image =
            createImage(
                fillColor = Color(127, 127, 127, 127),
            )

        testSerialization(format, image)
    }

    @Test
    fun `serialize and deserialize tiff`() {
        val format = ImageFormat(fileFormat = "tiff")
        val image =
            createImage(
                type = BufferedImage.TYPE_INT_RGB,
                fillColor = Color(127, 127, 127),
            )

        testSerialization(format, image)
    }

    @Test
    fun `serialize and deserialize transparent tiff`() {
        val format = ImageFormat(fileFormat = "tiff")
        val image =
            createImage(
                fillColor = Color(127, 127, 127, 127),
            )

        testSerialization(format, image)
    }
}

private fun createImage(
    width: Int = 10,
    height: Int = 10,
    type: Int = BufferedImage.TYPE_INT_ARGB,
    fillColor: Color? = null,
): BufferedImage {
    val image = BufferedImage(width, height, type)
    fillColor?.let { color ->
        val graphics = image.createGraphics()
        graphics.color = color
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()
    }
    return image
}

private fun testSerialization(
    format: ImageFormat,
    image: BufferedImage,
) {
    val bytes = format.serialize(image)
    val deserializedImage = format.deserialize(bytes)

    expectThat(deserializedImage).isEqualToImage(image)
}

private fun Assertion.Builder<BufferedImage>.isEqualToImage(expected: BufferedImage) =
    and {
        get("width") { width }.isEqualTo(expected.width)
        get("height") { height }.isEqualTo(expected.height)
        get("colorModel") { colorModel }.and {
            get("colorSpace") { colorSpace }.isEqualTo(expected.colorModel.colorSpace)
            get("transparency") { transparency }.isEqualTo(expected.colorModel.transparency)
            get("componentSize") { componentSize }.isEqualTo(expected.colorModel.componentSize)
        }
        get("has same pixels") {
            val thisHasAlpha = colorModel.hasAlpha()
            val otherHasAlpha = expected.colorModel.hasAlpha()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val thisColor = Color(getRGB(x, y), thisHasAlpha)
                    val otherColor = Color(expected.getRGB(x, y), otherHasAlpha)
                    if (thisColor != otherColor) {
                        return@get false
                    }
                }
            }
            true
        }.isTrue()
    }
