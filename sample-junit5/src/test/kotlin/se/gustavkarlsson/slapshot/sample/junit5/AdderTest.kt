package se.gustavkarlsson.slapshot.sample.junit5

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.core.formats.LongFormat
import se.gustavkarlsson.slapshot.junit5.JUnit5SnapshotContext
import se.gustavkarlsson.slapshot.junit5.SnapshotExtension

@ExtendWith(SnapshotExtension::class)
class AdderTest {
    @Test
    fun `i am a test`(snapshotContext: JUnit5SnapshotContext) {
        val snapshotter = snapshotContext.createSnapshotter(
            format = LongFormat(),
            overrideSnapshotFileResolver = { rootDirectory, testInfo, fileExtension ->
                rootDirectory.resolve(testInfo.displayName + '.' + fileExtension)
            },
        )
        snapshotter.snapshot(5)
    }
}