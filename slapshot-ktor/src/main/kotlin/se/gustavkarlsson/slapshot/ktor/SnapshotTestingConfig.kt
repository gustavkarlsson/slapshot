package se.gustavkarlsson.slapshot.ktor

import io.ktor.http.content.OutgoingContent
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.ApplicationResponse
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonErrorStyle

@ConsistentCopyVisibility
public data class SnapshotTestingConfig internal constructor(
    internal val snapshotContext: SnapshotContext<TestInfo>,
    val skipRequestHeaders: MutableList<String> = mutableListOf(),
    val skipResponseHeaders: MutableList<String> = mutableListOf(),
    var errorStyle: JsonErrorStyle = JsonErrorStyle.JUnit,
    var requestBodyToJson: (ApplicationRequest) -> String? = ::requestBodyToJson,
    var responseBodyToJson: (ApplicationResponse, OutgoingContent) -> String? = ::responseBodyToJson,
)
