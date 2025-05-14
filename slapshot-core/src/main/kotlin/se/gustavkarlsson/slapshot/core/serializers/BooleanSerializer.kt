package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of Boolean values as plain text, either "true" or "false".
 */
public data object BooleanSerializer : Serializer<Boolean> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Boolean {
        return bytes.decodeToString()
            .trim()
            .lowercase()
            .toBooleanStrict()
    }

    override fun serialize(value: Boolean): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
