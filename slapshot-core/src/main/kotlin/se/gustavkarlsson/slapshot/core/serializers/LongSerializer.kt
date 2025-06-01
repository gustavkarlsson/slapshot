package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of Long values as plain text
 */
public data object LongSerializer : Serializer<Long> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Long {
        return bytes.decodeToString()
            .trim()
            .toLong()
    }

    override fun serialize(value: Long): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
