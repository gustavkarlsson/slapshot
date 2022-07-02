package se.gustavkarlsson.slapshot.sample

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.core.Slapshot
import se.gustavkarlsson.slapshot.core.SlapshotJunitExtension
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import se.gustavkarlsson.slapshot.core.typehandlers.IntSnapshot

@ExtendWith(SlapshotJunitExtension::class)
class AdderTest(private val slapshot: Slapshot<Int, IntSnapshot>) {
    init {
        slapshot.snapshotFileResolver = SnapshotFileResolver { rootDirectory, fileExtension, testInfo ->
            rootDirectory.resolve(testInfo.displayName + fileExtension)
        }
    }

    @Test
    fun `i am a test`() {
        slapshot.snapshot(5)
    }
}