package se.gustavkarlsson.slapshot.sample.ktor

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.content.TextContent
import io.ktor.util.toMap
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun HttpResponse.toJsonString(): String {
    return JsonObject(
        mapOf(
            "request" to JsonObject(
                buildMap {
                    put("method", request.method.value.toJsonString())
                    put("url", request.url.toString().toJsonString())
                    put("headers", request.headers.toJsonObject())
                    val body = (request.content as? TextContent)?.text
                    if (body != null) {
                        put("body", body.toJsonString())
                    }
                }
            ), "response" to JsonObject(
                buildMap {
                    put("status", status.value.toJsonNumber())
                    put("headers", headers.toJsonObject())
                    val body = bodyAsText()
                    if (body.isNotEmpty()) {
                        put("body", body.toJsonString())
                    }
                }
            )
        )
    ).toString()
}

private fun Int.toJsonNumber() = JsonPrimitive(this)

private fun String.toJsonString() = JsonPrimitive(this)

private fun Headers.toJsonObject(): JsonObject {
    val map = toMap().mapValues { (_, headerValues) ->
        val jsonStrings = headerValues.map(String::toJsonString)
        if (jsonStrings.size == 1) {
            jsonStrings.first()
        } else {
            JsonArray(jsonStrings)
        }
    }
    return JsonObject(map)
}
