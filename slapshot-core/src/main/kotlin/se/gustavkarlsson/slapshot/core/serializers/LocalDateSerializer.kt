package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.LocalDate

/**
 * Handles serialization of LocalDate values as plain text using ISO-8601 standards.
 */
public data object LocalDateSerializer : Serializer<LocalDate> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): LocalDate {
        return LocalDate.parse(bytes.decodeToString())
    }

    override fun serialize(value: LocalDate): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
