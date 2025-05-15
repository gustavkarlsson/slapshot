package se.gustavkarlsson.slapshot.core.testers

import se.gustavkarlsson.slapshot.core.Tester

/**
 * A tester for long string snapshots that may span multiple lines. Errors will indicate the position of the first mismatch,
 *  and the diffs will be trimmed to include only the part of the string that doesn't match.
 */
public data class LongStringTester(
    /**
     * Limits the number of differing characters to display when a string comparison fails. Must be a positive value.
     */
    val diffLengthToPrint: Int = 50,
) : Tester<String> {
    init {
        require(diffLengthToPrint > 0) {
            "diffLengthToPrint must positive but was: <$diffLengthToPrint>"
        }
    }

    override fun test(
        actual: String,
        expected: String,
    ): String? {
        return if (expected != actual) {
            createDiffString(expected, actual)
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

        val differingExpected =
            expected
                .drop(commonPrefixLength)
                .dropLast(commonSuffixLength)

        val differingActual =
            actual
                .drop(commonPrefixLength)
                .dropLast(commonSuffixLength)

        val position = expected.positionOf(commonPrefixLength)

        return buildString {
            append("Strings differ at $position. ")
            if (differingExpected.length > diffLengthToPrint) {
                append("Expected: <${differingExpected.take(diffLengthToPrint)}>…")
            } else {
                append("Expected: <$differingExpected>")
            }
            if (differingActual.length > diffLengthToPrint) {
                append(" but was: <${differingActual.take(diffLengthToPrint)}>…")
            } else {
                append(" but was: <$differingActual>")
            }
        }
    }

    private fun String.positionOf(index: Int): String {
        val lines = this.take(index).split(Regex("\\r?\\n"))
        val line = lines.size
        val column = lines.last().length + 1
        return "$line:$column"
    }
}
