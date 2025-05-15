package se.gustavkarlsson.slapshot.json

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of JSON snapshots. Pretty prints JSON.
 */
public data object JsonSerializer : Serializer<String> {
    override val fileExtension: String
        get() = "json"

    override fun deserialize(bytes: ByteArray): String {
        return bytes.decodeToString().validateJson().prettyPrint()
    }

    override fun serialize(value: String): ByteArray {
        return value.validateJson().prettyPrint().encodeToByteArray()
    }
}
