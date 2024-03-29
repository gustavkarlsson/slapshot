package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat
import java.nio.charset.Charset

public data class StringFormat(
    val trim: Boolean = false,
    val charset: Charset = Charsets.UTF_8,
    override val fileExtension: String = "txt",
) : SnapshotFormat<String> {
    override fun test(actual: String, expected: String): String? {
        val sanitizedExpected = expected.trimIfEnabled()
        val sanitizedActual = actual.trimIfEnabled()
        return if (sanitizedExpected != sanitizedActual) {
            "expected: <$sanitizedExpected> but was: <$sanitizedActual>"
        } else {
            null
        }
    }

    override fun deserialize(bytes: ByteArray): String {
        return String(bytes, charset)
    }

    override fun serialize(value: String): ByteArray {
        return value.toByteArray(charset)
    }

    private fun String.trimIfEnabled(): String {
        return if (trim) trim() else this
    }
}
