package se.gustavkarlsson.slapshot.plugin

/**
 * Defines how snapshotting should work.
 */
public enum class SnapshotAction(public val systemProperty: String) {
    /**
     * Compares the new snapshots with existing snapshots. Does not store any new snapshots.
     *
     * If the snapshot file does not exist or if the comparison fails, an exception is thrown.
     * No new snapshots will be created, and existing snapshots will remain unchanged.
     *
     * Typically used to enforce strict validation of test results without modifying the
     * snapshot storage.
     */
    CompareOnly("compareOnly"),

    /**
     * Compares the new snapshots with existing snapshots and stores them if no existing snapshots exist.
     *
     * If the snapshot file exists, the result will be compared with the existing snapshot.
     * In the event of a mismatch, an exception is thrown. If the snapshot file is missing,
     * the provided snapshot will be stored, and an exception is thrown as a warning indicating that a missing
     * snapshot has been added to the storage.
     *
     * This action is typically used as a default behavior, ensuring that tests are validated against existing snapshots
     * while automatically accommodating missing snapshots by creating them.
     */
    CompareAndAdd("compareAndAdd"),

    /**
     * Overwrites existing snapshots with the new snapshots unconditionally.
     *
     * If the snapshot file exists, it will be replaced with the new snapshot. If the file
     * does not exist, it will be created. This action ensures that the stored snapshots
     * always match the latest captured data without any comparison or validation.
     *
     * Typically used when it is necessary to update snapshots without manually validating changes.
     */
    Overwrite("overwrite"),
}
