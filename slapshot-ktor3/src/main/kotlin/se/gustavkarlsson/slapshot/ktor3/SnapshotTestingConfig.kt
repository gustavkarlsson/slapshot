package se.gustavkarlsson.slapshot.ktor3

import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonErrorStyle

/**
 * Configuration for [SnapshotTesting]. Update to configure how snapshotting is done.
 */
@ConsistentCopyVisibility
public data class SnapshotTestingConfig internal constructor(
    internal val snapshotContext: SnapshotContext<TestInfo>,
    /**
     * A list of request headers to ignore when creating the snapshot.
     */
    val skipRequestHeaders: MutableList<String> = mutableListOf(),
    /**
     * A list of response headers to ignore when creating the snapshot.
     */
    val skipResponseHeaders: MutableList<String> = mutableListOf(),
    /**
     * The style to use for formatting JSON errors.
     */
    var errorStyle: JsonErrorStyle = JsonErrorStyle.JUnit,
    /**
     * A function to convert the request body to a JSON string.
     */
    var requestBodyToJson: suspend (HttpRequest) -> String? = ::requestBodyToJson,
    /**
     * A function to convert the response body to a JSON string.
     */
    var responseBodyToJson: suspend (HttpResponse) -> String? = ::responseBodyToJson,
)
