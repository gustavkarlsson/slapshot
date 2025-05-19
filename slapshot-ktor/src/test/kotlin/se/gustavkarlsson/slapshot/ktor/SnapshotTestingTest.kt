package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.junit5.JUnit5SnapshotContext
import se.gustavkarlsson.slapshot.junit5.SnapshotExtension

@ExtendWith(SnapshotExtension::class)
class SnapshotTestingTest {
    private lateinit var snapshotContext: JUnit5SnapshotContext

    @BeforeEach
    fun setUp(snapshotContext: JUnit5SnapshotContext) {
        this.snapshotContext = snapshotContext
    }

    @Test
    fun `get root endpoint with text response`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.respondText("Hello, World!")
                }
            },
        ) { client ->
            client.get("/")
        }

    private fun testSnapshotting(
        configureRouting: Routing.() -> Unit,
        configurePlugin: SnapshotTestingConfig.() -> Unit = {},
        block: suspend (client: HttpClient) -> Unit,
    ) {
        testApplication {
            application { routing(configureRouting) }
            val client =
                createClient {
                    install(SnapshotTesting(snapshotContext), configurePlugin)
                }
            block(client)
        }
    }
}
