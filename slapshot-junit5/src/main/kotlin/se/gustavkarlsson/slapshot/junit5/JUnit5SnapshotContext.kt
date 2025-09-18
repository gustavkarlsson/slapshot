package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.fail
import se.gustavkarlsson.slapshot.core.DefaultSnapshotter
import se.gustavkarlsson.slapshot.core.Serializer
import se.gustavkarlsson.slapshot.core.SnapshotAction
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import se.gustavkarlsson.slapshot.core.Snapshotter
import se.gustavkarlsson.slapshot.core.Tester
import se.gustavkarlsson.slapshot.core.getAction
import se.gustavkarlsson.slapshot.core.getRootDirectory

/**
 * Snapshot context implementation for JUnit 5 Jupiter.
 *
 * Used to create [Snapshotter] instances for use in JUnit 5 Jupiter tests.
 */
public class JUnit5SnapshotContext internal constructor(
    private val testInfo: TestInfo,
) : SnapshotContext<TestInfo> {
    override fun <T> createSnapshotter(
        serializer: Serializer<T>,
        tester: Tester<T>,
        overrideSnapshotFileResolver: SnapshotFileResolver<TestInfo>?,
        overrideAction: SnapshotAction?,
    ): Snapshotter<T> =
        DefaultSnapshotter(
            snapshotFileResolver = overrideSnapshotFileResolver ?: JUnit5SnapshotFileResolver,
            rootDirectory = getRootDirectory(),
            getTestInfo = ::testInfo,
            serializer = serializer,
            tester = tester,
            action = overrideAction ?: getAction(),
            onFail = ::fail,
        )
}
