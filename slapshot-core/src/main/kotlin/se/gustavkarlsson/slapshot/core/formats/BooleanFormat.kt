package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat

/**
 * A snapshot format for serializing and deserializing Boolean values.
 *
 * Boolean values are stored as plain text, either "true" or "false".
 */
public data class BooleanFormat(
    override val fileExtension: String = "txt",
) : SnapshotFormat<Boolean> {
    override fun deserialize(bytes: ByteArray): Boolean {
        return bytes.decodeToString()
            .trim()
            .lowercase()
            .toBooleanStrict()
    }

    override fun serialize(value: Boolean): ByteArray {
        return value.toString()
            .encodeToByteArray()
    }
}
