package se.gustavkarlsson.slapshot.core.configs

import se.gustavkarlsson.slapshot.core.SnapshotConfig

class IntSnapshot : SnapshotConfig<Int> {
    override val fileExtension: String = ".txt"

    override fun deserialize(bytes: ByteArray): Int {
        return bytes.decodeToString().toInt()
    }

    override fun serialize(value: Int): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
