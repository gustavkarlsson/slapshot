package se.gustavkarlsson.slapshot.images

import se.gustavkarlsson.slapshot.core.Serializer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Handles serialization of BufferedImage values using [ImageIO].
 * The file format can be configured if needed.
 */
public data class ImageSerializer(
    override val fileExtension: String = "png",
    /**
     * Specifies the file format for encoding the images. See [ImageIO] for reference.
     */
    val fileFormat: String = fileExtension,
) : Serializer<BufferedImage> {
    override fun deserialize(bytes: ByteArray): BufferedImage {
        return bytes.inputStream().use { stream ->
            ImageIO.read(stream)
        }
    }

    override fun serialize(value: BufferedImage): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            check(ImageIO.write(value, fileFormat, stream)) {
                "Failed to write image to ByteArrayOutputStream"
            }
            stream.toByteArray()
        }
    }
}
