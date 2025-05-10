package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat

/**
 * A snapshot format for generic objects. Uses the `toString` method of objects to generate their serialized form.
 */
public data class AnyToStringFormat(
    override val fileExtension: String = "txt",
) : SnapshotFormat<Any?> {
    override fun test(
        actual: Any?,
        expected: Any?,
    ): String? {
        return super.test(actual.toString(), expected.toString())
    }

    override fun deserialize(bytes: ByteArray): String {
        return bytes.decodeToString()
    }

    override fun serialize(value: Any?): ByteArray {
        return value.toString().encodeToByteArray()
    }
}
