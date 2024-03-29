package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.fail
import se.gustavkarlsson.slapshot.core.SnapshotAction
import se.gustavkarlsson.slapshot.core.DefaultSnapshotter
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import se.gustavkarlsson.slapshot.core.SnapshotFormat
import se.gustavkarlsson.slapshot.core.Snapshotter
import se.gustavkarlsson.slapshot.core.getAction
import se.gustavkarlsson.slapshot.core.getDefaultRootDirectory
import java.nio.file.Path

public data class JUnit5SnapshotContext internal constructor(
    private val testInfo: TestInfo,
) : SnapshotContext<TestInfo> {
    override fun <T, F : SnapshotFormat<T>> createSnapshotter(
        format: F,
        overrideRootDirectory: Path?,
        overrideSnapshotFileResolver: SnapshotFileResolver<TestInfo>?,
        overrideAction: SnapshotAction?,
    ): Snapshotter<T> = DefaultSnapshotter(
        snapshotFileResolver = overrideSnapshotFileResolver ?: JUnit5SnapshotFileResolver,
        rootDirectory = overrideRootDirectory ?: getDefaultRootDirectory(),
        getTestInfo = ::testInfo,
        format = format,
        action = overrideAction ?: getAction(),
        onFail = ::fail,
    )
}
