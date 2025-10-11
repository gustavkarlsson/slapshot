package se.gustavkarlsson.slapshot.images

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEqualTo
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
        val originalImage = createImage()

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
        val originalImage = createImage(type = BufferedImage.TYPE_INT_ARGB, fillColor = Color(sourceAlpha, 64, 32, 16))

        val bytes = serializer.serialize(originalImage)
        val deserializedImage = serializer.deserialize(bytes)

        expectThat(deserializedImage) {
            get("transparency") { transparency }.isEqualTo(Transparency.TRANSLUCENT)
            get("first pixel") { pixels().first() }.get("alpha") { alpha }.isEqualTo(sourceAlpha)
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
                .fill(Color(254, 64, 32, 16)) // Every alpha < 255 becomes 0 in BITMASK
        val serializer = ImageSerializer(fileFormat = "gif")

        val bytes = serializer.serialize(originalImage)
        val deserializedImage = serializer.deserialize(bytes)

        expectThat(deserializedImage) {
            get("transparency") { transparency }.isEqualTo(Transparency.BITMASK)
            get("first pixel") { pixels().first() }.get("alpha") { alpha }.isEqualTo(0)
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
        val redImage = createImage(fillColor = Color.Red)
        val blueImage = createImage(fillColor = Color.Blue)

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
