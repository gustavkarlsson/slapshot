package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.nio.charset.Charset

/**
 * Handles serialization of String values using a configurable charset.
 */
public data class StringSerializer(
    /**
     * The character set to use for encoding and decoding the strings.
     */
    val charset: Charset = Charsets.UTF_8,
) : Serializer<String> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): String {
        return String(bytes, charset)
    }

    override fun serialize(value: String): ByteArray {
        return value.toByteArray(charset)
    }
}
