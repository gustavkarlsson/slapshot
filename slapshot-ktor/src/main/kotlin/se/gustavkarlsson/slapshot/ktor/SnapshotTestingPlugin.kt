package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonSerializer
import se.gustavkarlsson.slapshot.json.JsonTester

@Suppress("FunctionName")
/**
 * Ktor testing plugin that performs snapshot testing of requests and responses.
 *
 * The plugin serializes each HTTP interaction (method, URL, headers, and bodies) into a pretty-printed JSON structure,
 * and compares those json structures with each other.
 *
 * The plugin is configured using [SnapshotTestingConfig], which lets you skip specific headers and customize how
 * request/response bodies are converted to JSON.
 *
 * Usage:
 * - Create a [SnapshotContext] for your test class (see Slapshot documentation).
 * - In the `testApplication` block. Install this plugin in the Ktor client, passing in that context.
 * - (Optionally) configure the plugin using a config block.
 * - Execute client requests in your tests; snapshots will be written/verified automatically.
 *
 * @param snapshotContext snapshot context originating from the current test, used to resolve snapshot file paths and
 * orchestrate snapshot verification.
 * @return a configured [ClientPlugin] instance that can be installed into a Ktor client.
 */
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
