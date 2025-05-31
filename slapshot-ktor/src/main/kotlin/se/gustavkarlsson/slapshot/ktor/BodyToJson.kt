package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.util.toMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.Base64

internal fun requestBodyToJson(request: HttpRequest): String? {
    val jsonElement: JsonElement =
        when (val content = request.content) {
            is OutgoingContent.NoContent -> return null
            is TextContent ->
                if (request.contentType()?.match(ContentType.Application.Json) == true) {
                    Json.decodeFromString(content.text)
                } else {
                    JsonPrimitive(content.text)
                }
            is FormDataContent -> content.toJsonObject()
            is ByteArrayContent, is OutgoingContent.ByteArrayContent -> JsonPrimitive(content.bytes().toBase64())
            is OutgoingContent.ProtocolUpgrade, is OutgoingContent.ReadChannelContent, is OutgoingContent.WriteChannelContent ->
                throw IllegalArgumentException("Unsupported request content type: ${content::class.qualifiedName}")
        }
    return Json.encodeToString(jsonElement)
}

private fun FormDataContent.toJsonObject(): JsonObject {
    val map =
        formData.toMap()
            .filterValues { it.isNotEmpty() }
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

internal suspend fun responseBodyToJson(response: HttpResponse): String? {
    val contentType = response.contentType()
    val jsonElement: JsonElement =
        when {
            contentType == null -> {
                // Binary?
                response.bytesToBase64JsonStringIfNotEmpty() ?: return null
            }

            contentType.match(ContentType.Application.Json) -> {
                val text = response.bodyAsTextIfNotEmpty() ?: return null
                Json.decodeFromString(text)
            }

            contentType.match(ContentType.Text.Any) -> {
                val text = response.bodyAsTextIfNotEmpty() ?: return null
                JsonPrimitive(text)
            }

            contentType.match(ContentType.Application.OctetStream) -> {
                response.bytesToBase64JsonStringIfNotEmpty() ?: return null
            }

            else -> {
                // Binary?
                response.bytesToBase64JsonStringIfNotEmpty() ?: return null
            }
        }
    return Json.encodeToString(jsonElement)
}

private suspend fun HttpResponse.bodyAsTextIfNotEmpty(): String? = bodyAsText().takeIf { it.isNotEmpty() }

private suspend fun HttpResponse.bytesToBase64JsonStringIfNotEmpty(): JsonPrimitive? {
    val bytes = body<ByteArray>()
    if (bytes.isEmpty()) return null
    val base64String = bytes.toBase64()
    return JsonPrimitive(base64String)
}

private fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}
