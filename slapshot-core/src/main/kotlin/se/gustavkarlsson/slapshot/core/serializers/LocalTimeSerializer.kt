package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.LocalTime

/**
 * Handles serialization of LocalTime values as plain text using ISO-8601 standards.
 */
public data object LocalTimeSerializer : Serializer<LocalTime> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): LocalTime {
        return LocalTime.parse(bytes.decodeToString())
    }

    override fun serialize(value: LocalTime): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
