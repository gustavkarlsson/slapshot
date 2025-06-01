package se.gustavkarlsson.slapshot.core

/**
 * Captures and compares snapshots of test results.
 *
 * Implementations of this interface could differ in how snapshots are stored or compared, but the core
 * responsibilities remain centered around snapshot creation and validation.
 *
 * @param T The type of data to be captured in the snapshot.
 */
public fun interface Snapshotter<T> {
    /**
     * Captures a snapshot of the provided data.
     *
     * Depending on the configured [SnapshotAction],
     * the results may be stored and/or compared with existing snapshots.
     * If the comparison fails, an exception is thrown.
     *
     * @param data The data to be captured in the snapshot.
     */
    public fun snapshot(data: T)
}
