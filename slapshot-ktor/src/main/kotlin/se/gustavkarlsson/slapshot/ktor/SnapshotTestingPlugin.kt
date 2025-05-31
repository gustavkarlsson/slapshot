package se.gustavkarlsson.slapshot.ktor

import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.ResponseBodyReadyForSend
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.util.AttributeKey
import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.json.JsonSerializer
import se.gustavkarlsson.slapshot.json.JsonTester

@Suppress("FunctionName")
public fun SnapshotTesting(snapshotContext: SnapshotContext<TestInfo>): ApplicationPlugin<SnapshotTestingConfig> =
    createApplicationPlugin(
        name = "SnapshotTesting",
        createConfiguration = {
            SnapshotTestingConfig(snapshotContext = snapshotContext)
        },
    ) {
        val tester = JsonTester(errorStyle = pluginConfig.errorStyle)
        val snapshotter = pluginConfig.snapshotContext.createSnapshotter(JsonSerializer, tester)
        on(ResponseBodyReadyForSend) { call, content ->
            call.attributes.put(OutgoingContentKey, content)
        }
        on(ResponseSent) { call ->
            val content = call.attributes[OutgoingContentKey]
            val json =
                call.toJson(
                    outgoingContent = content,
                    skipRequestHeaders = pluginConfig.skipRequestHeaders.toList(),
                    skipResponseHeaders = pluginConfig.skipResponseHeaders.toList(),
                    requestBodyToJson = pluginConfig.requestBodyToJson,
                    responseBodyToJson = pluginConfig.responseBodyToJson,
                )
            snapshotter.snapshot(json)
        }
    }

private val OutgoingContentKey = AttributeKey<OutgoingContent>("OutgoingContent")
