package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonErrorStyle

@ConsistentCopyVisibility
public data class SnapshotTestingConfig internal constructor(
    internal val snapshotContext: SnapshotContext<TestInfo>,
    val skipRequestHeaders: MutableList<String> = mutableListOf(),
    val skipResponseHeaders: MutableList<String> = mutableListOf(),
    var errorStyle: JsonErrorStyle = JsonErrorStyle.JsonPath,
    var requestBodyToJson: suspend (HttpRequest) -> String? = ::requestBodyToJson,
    var responseBodyToJson: suspend (HttpResponse) -> String? = ::responseBodyToJson,
)
