package se.gustavkarlsson.slapshot.sample.ktor

import io.ktor.client.request.get
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.junit5.JUnit5SnapshotContext
import se.gustavkarlsson.slapshot.junit5.SnapshotExtension
import se.gustavkarlsson.slapshot.ktor3.SnapshotTesting
import se.gustavkarlsson.slapshot.json.JsonErrorStyle

@ExtendWith(SnapshotExtension::class)
class ApplicationTest {
    private lateinit var snapshotContext: JUnit5SnapshotContext

    @BeforeEach
    fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
        this.snapshotContext = snapshotContext
    }

    @Test
    fun `hello world`() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(SnapshotTesting(snapshotContext))
        }
        client.get("/")
    }

    @Test
    fun `advanced configuration`() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(SnapshotTesting(snapshotContext)) {
                skipRequestHeaders.add("Content-Length")
                skipResponseHeaders.add("Content-Length")
                // errorStyle = JsonErrorStyle.JsonPath
                requestBodyToJson = { request ->
                    "\"custom request body\""
                }
                responseBodyToJson = { response ->
                    "\"custom response body\""
                }
            }
        }
        client.get("/")
    }
}
