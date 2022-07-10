package se.gustavkarlsson.slapshot.core

import java.nio.file.Path

fun interface SnapshotFileResolver<TI> {
    fun resolve(rootDirectory: Path, testInfo: TI, fileExtension: String): Path
}
