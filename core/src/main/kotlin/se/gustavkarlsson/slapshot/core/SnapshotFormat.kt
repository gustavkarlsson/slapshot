package se.gustavkarlsson.slapshot.core

interface SnapshotFormat<T> {
    val fileExtension: String

    fun test(actual: T, expected: T): String? {
        return if (actual != expected) {
            "expected: <$expected> but was: <$actual>"
        } else {
            null
        }
    }

    fun deserialize(bytes: ByteArray): T

    fun serialize(value: T): ByteArray
}
