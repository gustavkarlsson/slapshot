package se.gustavkarlsson.slapshot.core

import java.nio.file.Path

/**
 * Resolves the file path for storing or retrieving a snapshot, based on the provided parameters.
 *
 * This functional interface defines custom logic for determining how snapshot files should be organized
 * and located within the root directory. The implementation can use test information and the file extension
 * to customize the resolution process.
 *
 *  *Note: The snapshot file MUST be somewhere within the root directory*
 *
 * @param TI Type of test information required for resolving the snapshot file path.
 */
public fun interface SnapshotFileResolver<TI> {
    public fun resolve(
        rootDirectory: Path,
        testInfo: TI,
        fileExtension: String,
    ): Path
}
