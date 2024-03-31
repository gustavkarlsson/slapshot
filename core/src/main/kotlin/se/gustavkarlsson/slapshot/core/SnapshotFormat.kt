package se.gustavkarlsson.slapshot.core

public interface SnapshotFormat<T> {
    public val fileExtension: String

    public fun test(
        actual: T,
        expected: T,
    ): String? {
        return if (actual != expected) {
            "expected: <$expected> but was: <$actual>"
        } else {
            null
        }
    }

    public fun deserialize(bytes: ByteArray): T

    public fun serialize(value: T): ByteArray
}
