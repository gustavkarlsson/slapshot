package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of Double values as plain text.
 */
public data object DoubleSerializer : Serializer<Double> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Double {
        return bytes.decodeToString()
            .trim()
            .toDouble()
    }

    override fun serialize(value: Double): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
