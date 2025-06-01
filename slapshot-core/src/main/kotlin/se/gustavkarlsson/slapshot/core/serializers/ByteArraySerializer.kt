package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of ByteArrays.
 */
public data object ByteArraySerializer : Serializer<ByteArray> {
    override val fileExtension: String
        get() = "snap"

    override fun deserialize(bytes: ByteArray): ByteArray = bytes

    override fun serialize(value: ByteArray): ByteArray = value
}
