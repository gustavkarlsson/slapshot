package se.gustavkarlsson.slapshot.core.testers

import se.gustavkarlsson.slapshot.core.Tester

/**
 * Tests equality between any two snapshots. Arrays are compared based on their content.
 */
public data object EqualsTester : Tester<Any?> {
    override fun test(
        actual: Any?,
        expected: Any?,
    ): String? {
        return when {
            actual === expected -> null

            actual is Array<*> && expected is Array<*> -> {
                if (!actual.contentDeepEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is BooleanArray && expected is BooleanArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is ByteArray && expected is ByteArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is CharArray && expected is CharArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is ShortArray && expected is ShortArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is IntArray && expected is IntArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is LongArray && expected is LongArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is FloatArray && expected is FloatArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual is DoubleArray && expected is DoubleArray -> {
                if (!actual.contentEquals(expected)) {
                    "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
                } else {
                    null
                }
            }

            actual != expected -> {
                "expected: <$expected> but was: <$actual>"
            }

            else -> null
        }
    }
}
