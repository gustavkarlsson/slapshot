package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.time.Period

/**
 * Handles serialization of Period values as plain text using ISO-8601 standards.
 */
public data object PeriodSerializer : Serializer<Period> {
    override val fileExtension: String
        get() = "txt"

    override fun deserialize(bytes: ByteArray): Period {
        return Period.parse(bytes.decodeToString())
    }

    override fun serialize(value: Period): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
