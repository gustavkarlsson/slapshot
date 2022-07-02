package se.gustavkarlsson.slapshot.core.configs

import se.gustavkarlsson.slapshot.core.SnapshotConfig

class StringSnapshot : SnapshotConfig<String> {
    override val fileExtension: String = ".txt"

    override fun deserialize(bytes: ByteArray): String {
        return bytes.decodeToString()
    }

    override fun serialize(value: String): ByteArray {
        return value.encodeToByteArray()
    }
}
