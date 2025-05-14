package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.LocalDateTime

/**
 * Handles serialization of LocalDateTime values as plain text using ISO-8601 standards.
 */
public data object LocalDateTimeSerializer : Serializer<LocalDateTime> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): LocalDateTime {
        return LocalDateTime.parse(bytes.decodeToString())
    }

    override fun serialize(value: LocalDateTime): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
