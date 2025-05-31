package se.gustavkarlsson.slapshot.ktor

import io.ktor.http.Headers
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.response.ApplicationResponse
import io.ktor.server.util.url
import io.ktor.util.toMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val prettyPrintJson =
    Json {
        prettyPrint = true
    }

internal fun ApplicationCall.toJson(
    outgoingContent: OutgoingContent,
    skipRequestHeaders: List<String> = emptyList(),
    skipResponseHeaders: List<String> = emptyList(),
    requestBodyToJson: (ApplicationRequest) -> String?,
    responseBodyToJson: (ApplicationResponse, OutgoingContent) -> String?,
): String {
    val root =
        JsonObject(
            mapOf(
                "request" to
                    JsonObject(
                        buildMap {
                            put("method", request.httpMethod.value.toJsonString())
                            put("url", request.call.url().toJsonString())
                            put("headers", request.headers.toJsonObject(skipRequestHeaders))
                            val body = requestBodyToJson(request)
                            if (body != null) {
                                val bodyJson = Json.decodeFromString<JsonElement>(body)
                                put("body", bodyJson)
                            }
                        },
                    ),
                "response" to
                    JsonObject(
                        buildMap {
                            val status =
                                requireNotNull(response.status()) {
                                    "Status code not set for response. Have you installed the plugin too early?"
                                }
                            put("status", status.value.toJsonNumber())
                            put("headers", response.headers.allValues().toJsonObject(skipResponseHeaders))
                            val body = responseBodyToJson(response, outgoingContent)
                            if (body != null) {
                                val bodyJson = Json.decodeFromString<JsonElement>(body)
                                put("body", bodyJson)
                            }
                        },
                    ),
            ),
        )
    return prettyPrintJson.encodeToString(root)
}

private fun Int.toJsonNumber() = JsonPrimitive(this)

private fun String.toJsonString() = JsonPrimitive(this)

private fun Headers.toJsonObject(skipHeaders: List<String>): JsonObject {
    val map =
        toMap()
            .filterKeys { it !in skipHeaders }
            .filterValues { it.isNotEmpty() }
            .mapValues {
                    (_, values) ->
                values.flatMap { it.split(',') }
            } // Fixes a bug in ktor where multiple values in a header are not split
            .mapValues { (_, values) -> values.map(::JsonPrimitive) }
            .mapValues { (_, jsonStrings) ->
                if (jsonStrings.size == 1) {
                    jsonStrings.first()
                } else {
                    JsonArray(jsonStrings)
                }
            }
    return JsonObject(map)
}
