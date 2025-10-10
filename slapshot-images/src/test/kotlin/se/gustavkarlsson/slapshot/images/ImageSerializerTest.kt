package se.gustavkarlsson.slapshot.images

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.contentEquals
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEqualTo
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.Raster

class ImageSerializerTest {
    @Test
    fun `default constructor uses png extension and format`() {
        val serializer = ImageSerializer()

        expectThat(serializer) {
            get { fileExtension }.isEqualTo("png")
            get { fileFormat }.isEqualTo("png")
        }
    }

    @Test
    fun `constructor with custom extension uses extension as format`() {
        val serializer = ImageSerializer(fileExtension = "tiff")

        expectThat(serializer) {
            get { fileExtension }.isEqualTo("tiff")
            get { fileFormat }.isEqualTo("tiff")
        }
    }

    @Test
    fun `constructor with custom extension and format uses both`() {
        val serializer = ImageSerializer(fileExtension = "jpeg", fileFormat = "jpg")

        expectThat(serializer) {
            get { fileExtension }.isEqualTo("jpeg")
            get { fileFormat }.isEqualTo("jpg")
        }
    }

    @Test
    fun `serialize creates non-empty byte array for simple image`() {
        val serializer = ImageSerializer()
        val image = createImage()

        val bytes = serializer.serialize(image)

        expectThat(bytes.size).isGreaterThan(0)
    }

    @Test
    fun `serialize and deserialize preserves image data`() {
        val serializer = ImageSerializer()
        val originalImage = createImage().draw()

        val bytes = serializer.serialize(originalImage)
        val deserializedImage = serializer.deserialize(bytes)

        expectThat(deserializedImage) {
            get("transparency") { transparency }.isEqualTo(originalImage.transparency)
            hasSamePixelsAs(originalImage)
            get { width }.isEqualTo(originalImage.width)
            get { height }.isEqualTo(originalImage.height)
        }
    }

    @Test
    fun `serialize and deserialize preserves transparent pixels`() {
        val serializer = ImageSerializer(fileFormat = "png")
        val sourceAlpha = 128
        val originalImage = createImage(type = BufferedImage.TYPE_INT_ARGB).draw(Color(64, 32, 16, sourceAlpha))

        val bytes = serializer.serialize(originalImage)
        val deserializedImage = serializer.deserialize(bytes)

        expectThat(deserializedImage) {
            get("transparency") { transparency }.isEqualTo(Transparency.TRANSLUCENT)
            get("first pixel") { firstPixelColor() }.get("alpha") { alpha }.isEqualTo(sourceAlpha)
            hasSamePixelsAs(originalImage)
        }
    }

    @Test
    fun `serialize and deserialize preserves bitmasked transparency pixels`() {
        val colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB)
        val colorModel =
            ComponentColorModel(colorSpace, true, false, Transparency.BITMASK, DataBuffer.TYPE_BYTE)
        val raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 10, 10, 4, null)

        val originalImage =
            BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null)
                .draw(Color(64, 32, 16, 254)) // Every alpha < 255 becomes 0 in BITMASK
        val serializer = ImageSerializer(fileFormat = "gif")

        val bytes = serializer.serialize(originalImage)
        val deserializedImage = serializer.deserialize(bytes)

        expectThat(deserializedImage) {
            get("transparency") { transparency }.isEqualTo(Transparency.BITMASK)
            get("first pixel") { firstPixelColor() }.get("alpha") { alpha }.isEqualTo(0)
        }
    }

    @Test
    fun `deserialize throws exception with invalid byte array`() {
        val serializer = ImageSerializer()
        val invalidBytes = byteArrayOf(1, 2, 3, 4, 5)

        assertThrows<Exception> {
            serializer.deserialize(invalidBytes)
        }
    }

    @Test
    fun `serialize produces different bytes for different images`() {
        val serializer = ImageSerializer()
        val redImage = createImage().draw(Color.RED)
        val blueImage = createImage().draw(Color.BLUE)

        val redBytes = serializer.serialize(redImage)
        val blueBytes = serializer.serialize(blueImage)

        expectThat(redBytes.toList()).isNotEqualTo(blueBytes.toList())
    }

    @Test
    fun `serialize throws exception for unsupported format`() {
        val serializer = ImageSerializer(fileFormat = "unsupported")
        val image = createImage()

        assertThrows<IllegalStateException> {
            serializer.serialize(image)
        }
    }
}

private fun createImage(
    width: Int = 20,
    height: Int = 10,
    type: Int = BufferedImage.TYPE_INT_RGB,
): BufferedImage {
    return BufferedImage(width, height, type)
}

private fun BufferedImage.draw(color: Color = Color.RED): BufferedImage {
    val graphics = createGraphics()
    try {
        graphics.color = color
        graphics.fillRect(0, 0, width, height)
    } finally {
        graphics.dispose()
    }
    return this
}

private fun Assertion.Builder<BufferedImage>.hasSamePixelsAs(other: BufferedImage): Assertion.Builder<BufferedImage> {
    return and {
        get("pixels") { pixels() }.contentEquals(other.pixels())
    }
}

private fun BufferedImage.pixels(): IntArray = getRGB(0, 0, width, height, null, 0, width)

private fun BufferedImage.firstPixelColor(): Color = Color(getRGB(0, 0), transparency != BufferedImage.OPAQUE)
