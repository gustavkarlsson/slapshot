package se.gustavkarlsson.slapshot.core

import java.nio.file.Path
import java.nio.file.Paths

interface SnapshotContext<TI> {
    fun <T, F : SnapshotFormat<T>> createSnapshotter(
        format: F,
        overrideRootDirectory: Path? = null,
        overrideSnapshotFileResolver: SnapshotFileResolver<TI>? = null,
        overrideAction: SnapshotAction? = null,
    ): Snapshotter<T>
}

@InternalSlapshotApi
fun getDefaultRootDirectory(): Path {
    val dirString = System.getProperty("snapshotRootDir") ?: "snapshots"
    return Paths.get(dirString)
}

@InternalSlapshotApi
fun getAction(): SnapshotAction {
    return when (val actionString = System.getProperty("snapshotAction")) {
        null -> SnapshotAction.CompareAndAdd
        "compareOnly" -> SnapshotAction.CompareOnly
        "compareAndAdd" -> SnapshotAction.CompareAndAdd
        "overwrite" -> SnapshotAction.Overwrite
        else -> error("Unsupported action: $actionString")
    }
}
