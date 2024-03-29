package se.gustavkarlsson.slapshot.junit4

import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import se.gustavkarlsson.slapshot.core.formats.*
import javax.imageio.ImageIO

@RunWith(Enclosed::class)
class EnclosedTests {

    class NestedTest {

        @get:Rule
        val snapshotContext = JUnit4SnapshotContext()

        @Test
        fun `just a nested class test`() {
            snapshotContext.createSnapshotter(StringFormat()).snapshot("bla")
        }
    }
}
