package se.gustavkarlsson.slapshot.core

import se.gustavkarlsson.slapshot.core.testers.EqualsTester
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Provides a context for creating snapshotters used to capture and test snapshots of data.
 *
 * This interface allows configuring snapshotters with customizable options such as
 * the root directory, file resolution logic, and the action to perform on snapshots.
 *
 * @param TI The type of test information required for resolving snapshot file paths.
 */
public interface SnapshotContext<TI> {
    /**
     * Creates a new snapshotter configured with the specified serializer, tester, and optional overrides.
     *
     * @param serializer Serializes/deserializes snapshots so they can be stored as files.
     * @param tester Compares two snapshots for equality.
     * @param overrideSnapshotFileResolver An optional resolver to override the default logic for resolving snapshot file paths. *Note: The snapshot file MUST be somewhere within the root directory*
     * @param overrideAction An optional action to override the default behavior for handling snapshots.
     * @return A new snapshotter configured with the specified parameters.
     */
    public fun <T> createSnapshotter(
        serializer: Serializer<T>,
        tester: Tester<T> = EqualsTester,
        overrideSnapshotFileResolver: SnapshotFileResolver<TI>? = null,
        overrideAction: SnapshotAction? = null,
    ): Snapshotter<T>
}

@InternalSlapshotApi
public fun getRootDirectory(): Path {
    val dirString = System.getProperty("snapshotRootDir") ?: "snapshots"
    return Paths.get(dirString)
}

@InternalSlapshotApi
public fun getAction(): SnapshotAction {
    return when (val actionString = System.getProperty("snapshotAction")) {
        null -> SnapshotAction.CompareAndAdd
        "compareOnly" -> SnapshotAction.CompareOnly
        "compareAndAdd" -> SnapshotAction.CompareAndAdd
        "overwrite" -> SnapshotAction.Overwrite
        else -> error("Unsupported snapshotAction: $actionString")
    }
}
