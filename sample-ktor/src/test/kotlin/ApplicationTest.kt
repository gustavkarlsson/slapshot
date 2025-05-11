package se.gustavkarlsson.slapshot.sample.ktor

import io.ktor.client.request.get
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.core.Snapshotter
import se.gustavkarlsson.slapshot.core.formats.JsonFormat
import se.gustavkarlsson.slapshot.junit5.JUnit5SnapshotContext
import se.gustavkarlsson.slapshot.junit5.SnapshotExtension

@ExtendWith(SnapshotExtension::class)
class ApplicationTest {
    private lateinit var snapshotter: Snapshotter<String>

    @BeforeEach
    fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
        snapshotter = snapshotContext.createSnapshotter(JsonFormat())
    }

    @Test
    fun `hello world`() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        snapshotter.snapshot(response.toJsonString())
    }
}
