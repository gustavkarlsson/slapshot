package se.gustavkarlsson.slapshot.core

interface SnapshotConfig<T> {
    val fileExtension: String

    fun test(expected: T, actual: T): String? {
        return if (expected != actual) {
            "expected: <$expected> but was: <$actual>"
        } else {
            null
        }
    }

    fun deserialize(bytes: ByteArray): T

    fun serialize(value: T): ByteArray
}
