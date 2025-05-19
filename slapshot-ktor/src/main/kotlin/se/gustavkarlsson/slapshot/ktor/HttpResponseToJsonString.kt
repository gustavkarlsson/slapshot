package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.content.TextContent
import io.ktor.util.toMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val json =
    Json {
        prettyPrint = true
    }

internal suspend fun HttpResponse.toJsonString(
    skipRequestHeaders: List<String> = emptyList(),
    skipResponseHeaders: List<String> = emptyList(),
): String {
    val root =
        JsonObject(
            mapOf(
                "request" to
                    JsonObject(
                        buildMap {
                            put("method", request.method.value.toJsonString())
                            put("url", request.url.toString().toJsonString())
                            put("headers", request.headers.toJsonObject(skipRequestHeaders))
                            // FIXME find a way to make this generic, so other types can be tested
                            val body = (request.content as? TextContent)?.text
                            if (body != null) {
                                put("body", body.toJsonString())
                            }
                        },
                    ),
                "response" to
                    JsonObject(
                        buildMap {
                            put("status", status.value.toJsonNumber())
                            put("headers", headers.toJsonObject(skipResponseHeaders))
                            // FIXME find a way to make this generic, so other types can be tested
                            val body = bodyAsText()
                            if (body.isNotEmpty()) {
                                put("body", body.toJsonString())
                            }
                        },
                    ),
            ),
        )
    return json.encodeToString(root)
}

private fun Int.toJsonNumber() = JsonPrimitive(this)

private fun String.toJsonString() = JsonPrimitive(this)

private fun Headers.toJsonObject(skipHeaders: List<String>): JsonObject {
    val map =
        toMap()
            .filterKeys { header ->
                header !in skipHeaders
            }
            .mapValues { (_, headerValues) ->
                val jsonStrings = headerValues.map(String::toJsonString)
                JsonArray(jsonStrings)
            }
    return JsonObject(map)
}
