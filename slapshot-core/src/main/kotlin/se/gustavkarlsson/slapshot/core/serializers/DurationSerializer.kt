package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.Duration

/**
 * Handles serialization of Duration values as plain text using ISO-8601 standards.
 */
public data object DurationSerializer : Serializer<Duration> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Duration {
        return Duration.parse(bytes.decodeToString())
    }

    override fun serialize(value: Duration): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
