package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonSerializer
import se.gustavkarlsson.slapshot.json.JsonTester

@Suppress("FunctionName")
public fun SnapshotTesting(snapshotContext: SnapshotContext<TestInfo>): ClientPlugin<SnapshotTestingConfig> =
    createClientPlugin(
        name = "SnapshotTesting",
        createConfiguration = {
            SnapshotTestingConfig(snapshotContext = snapshotContext)
        },
    ) {
        val tester = JsonTester(errorStyle = pluginConfig.errorStyle)
        val snapshotter = pluginConfig.snapshotContext.createSnapshotter(JsonSerializer, tester)
        onResponse { response ->
            val config = this@createClientPlugin.pluginConfig
            val json =
                response.call.toJsonString(
                    skipRequestHeaders = config.skipRequestHeaders.toList(),
                    skipResponseHeaders = config.skipResponseHeaders.toList(),
                    requestBodyToJson = config.requestBodyToJson,
                    responseBodyToJson = config.responseBodyToJson,
                )
            snapshotter.snapshot(json)
        }
    }
