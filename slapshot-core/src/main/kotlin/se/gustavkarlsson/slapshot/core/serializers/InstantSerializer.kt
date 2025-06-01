package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.Instant

/**
 * Handles serialization of Instant values as plain text using ISO-8601 standards.
 */
public data object InstantSerializer : Serializer<Instant> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Instant {
        return Instant.parse(bytes.decodeToString())
    }

    override fun serialize(value: Instant): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
