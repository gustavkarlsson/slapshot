package se.gustavkarlsson.slapshot.sample.junit4

import org.junit.Rule
import org.junit.Test
import se.gustavkarlsson.slapshot.core.serializers.LongSerializer
import se.gustavkarlsson.slapshot.junit4.JUnit4SnapshotContext

class AdderTest {

    @get:Rule
    val snapshotContext = JUnit4SnapshotContext()

    @Test
    fun `i am a test`() {
        val snapshotter = snapshotContext.createSnapshotter(
            serializer = LongSerializer,
            overrideSnapshotFileResolver = { rootDirectory, testInfo, fileExtension ->
                rootDirectory.resolve(testInfo.displayName + '.' + fileExtension)
            },
        )
        snapshotter.snapshot(5)
    }
}