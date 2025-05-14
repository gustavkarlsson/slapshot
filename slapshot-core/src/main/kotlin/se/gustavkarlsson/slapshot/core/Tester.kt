package se.gustavkarlsson.slapshot.core

/**
 * Represents a tester for snapshot data, defining how it compares snapshots and what error messages it produces.
 *
 * @param T The type of data to be tested.
 */
public fun interface Tester<in T> {
    /**
     * Tests whether the given actual value matches the expected value.
     *
     * If the values do not match, a descriptive error message is returned indicating
     * the mismatch. If the values are equal, null is returned.
     *
     * @param actual The actual value to test.
     * @param expected The expected value to compare against.
     * @return A descriptive error message if the values do not match, or null if they are equal.
     */
    public fun test(
        actual: T,
        expected: T,
    ): String?
}
