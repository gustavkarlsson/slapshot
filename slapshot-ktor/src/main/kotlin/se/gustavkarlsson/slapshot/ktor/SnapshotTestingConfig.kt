package se.gustavkarlsson.slapshot.ktor

import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonErrorStyle

@ConsistentCopyVisibility
public data class SnapshotTestingConfig internal constructor(
    internal val snapshotContext: SnapshotContext<TestInfo>,
    val skipRequestHeaders: MutableList<String> = mutableListOf(),
    val skipResponseHeaders: MutableList<String> = mutableListOf(),
    var errorStyle: JsonErrorStyle = JsonErrorStyle.JsonPath,
)
