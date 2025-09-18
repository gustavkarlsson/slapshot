package se.gustavkarlsson.slapshot.ktor3

import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.http.headers
import io.ktor.util.toMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val prettyPrintJson =
    Json {
        prettyPrint = true
    }

internal suspend fun HttpClientCall.toJsonString(
    skipRequestHeaders: List<String> = emptyList(),
    skipResponseHeaders: List<String> = emptyList(),
    requestBodyToJson: suspend (HttpRequest) -> String?,
    responseBodyToJson: suspend (HttpResponse) -> String?,
): String {
    val root =
        JsonObject(
            mapOf(
                "request" to
                    JsonObject(
                        buildMap {
                            put("method", request.method.value.toJsonString())
                            put("url", request.url.toString().toJsonString())
                            put("headers", request.allHeaders.toJsonObject(skipRequestHeaders))
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
                            put("status", response.status.value.toJsonNumber())
                            put("headers", response.headers.toJsonObject(skipResponseHeaders))
                            val body = responseBodyToJson(response)
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
            .filterKeys { header ->
                header !in skipHeaders
            }
            .mapValues { (_, headerValues) ->
                val jsonStrings = headerValues.map(String::toJsonString)
                if (jsonStrings.size == 1) {
                    jsonStrings.first()
                } else {
                    JsonArray(jsonStrings)
                }
            }
    return JsonObject(map)
}

// For some dumb reason, Ktor's HttpRequest doesn't expose all headers directly.
private val HttpRequest.allHeaders: Headers
    get() =
        headers {
            appendAll(headers)
            content.contentType?.let { contentType ->
                append(HttpHeaders.ContentType, contentType)
            }
            content.contentLength?.let { contentLength ->
                append(HttpHeaders.ContentLength, contentLength.toString())
            }
        }
