package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat

public data class LongFormat(
    override val fileExtension: String = "txt",
) : SnapshotFormat<Long> {
    override fun deserialize(bytes: ByteArray): Long {
        return bytes.decodeToString()
            .trim()
            .toLong()
    }

    override fun serialize(value: Long): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
