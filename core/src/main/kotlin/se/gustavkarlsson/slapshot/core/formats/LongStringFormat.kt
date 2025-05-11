package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat
import java.nio.charset.Charset

/**
 * A snapshot format for long String values. Errors will indicate the position of the first mismatch,
 *  and the diffs will be trimmed to include only the part of the string that doesn't match.
 */
public data class LongStringFormat(
    /**
     * Determines whether the strings should be trimmed before comparison.
     */
    val trim: Boolean = false,
    /**
     * The character set to use for encoding and decoding the strings.
     */
    val charset: Charset = Charsets.UTF_8,
    /**
     * Limits the number of differing characters to display when a string comparison fails. Must be a positive value.
     */
    val diffLengthToPrint: Int = 50,
    override val fileExtension: String = "txt",
) : SnapshotFormat<String> {
    init {
        require(diffLengthToPrint > 0) {
            "diffLengthToPrint must positive but was: <$diffLengthToPrint>"
        }
    }

    override fun test(
        actual: String,
        expected: String,
    ): String? {
        val sanitizedExpected = expected.trimIfEnabled()
        val sanitizedActual = actual.trimIfEnabled()
        return if (sanitizedExpected != sanitizedActual) {
            createDiffString(sanitizedExpected, sanitizedActual)
        } else {
            null
        }
    }

    private fun createDiffString(
        expected: String,
        actual: String,
    ): String {
        val commonPrefixLength = expected.commonPrefixWith(actual).length
        val commonSuffixLength = expected.commonSuffixWith(actual).length
        val differingExpected = expected.drop(commonPrefixLength).dropLast(commonSuffixLength).take(diffLengthToPrint)
        val differingActual = actual.drop(commonPrefixLength).dropLast(commonSuffixLength).take(diffLengthToPrint)
        return "Strings differ at ${expected.positionOf(commonPrefixLength)}. " +
            "Expected: <$differingExpected> but was: <$differingActual>"
    }

    private fun String.positionOf(index: Int): String {
        val lines = this.take(index).split(Regex("\\r?\\n"))
        val line = lines.size
        val column = lines.last().length
        return "$line:$column"
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
