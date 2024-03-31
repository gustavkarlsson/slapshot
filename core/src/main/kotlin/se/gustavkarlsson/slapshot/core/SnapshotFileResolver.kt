package se.gustavkarlsson.slapshot.core

import java.nio.file.Path

public fun interface SnapshotFileResolver<TI> {
    public fun resolve(
        rootDirectory: Path,
        testInfo: TI,
        fileExtension: String,
    ): Path
}
