package se.gustavkarlsson.slapshot.ktor

import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application
import io.ktor.http.ContentType.Text
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.ApplicationResponse
import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.Base64

// FIXME extend with a bunch of other types, like XML, HTML, etc.
internal fun requestBodyToJson(request: ApplicationRequest): String? {
    val contentType = request.contentType()
    val jsonElement: JsonElement =
        when {
            contentType.match(Application.Json) -> {
                val textBody = runBlocking { request.call.receiveText() }
                Json.decodeFromString(textBody)
            }

            contentType.match(Text.Any) -> {
                val textBody = runBlocking { request.call.receiveText() }
                JsonPrimitive(textBody)
            }

            contentType.match(Application.OctetStream) -> {
                val bytesBody = runBlocking { request.call.receive<ByteArray>() }
                JsonPrimitive(bytesBody.toBase64())
            }

            contentType.match(Application.FormUrlEncoded) -> {
                val parameters = runBlocking { request.call.receiveParameters() }
                parameters.toJsonObject()
            }

            else -> {
                // Default to binary (or null if empty)
                val bytesBody = runBlocking { request.call.receive<ByteArray>() }.takeIf { it.isNotEmpty() } ?: return null
                JsonPrimitive(bytesBody.toBase64())
            }
        }
    return Json.encodeToString(jsonElement)
}

private fun StringValues.toJsonObject(): JsonObject {
    val map =
        toMap()
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

internal fun responseBodyToJson(
    response: ApplicationResponse,
    content: OutgoingContent,
): String? {
    val contentType = content.contentType ?: ContentType.Any

    val jsonElement: JsonElement =
        when (content) {
            is OutgoingContent.NoContent -> return null

            is TextContent ->
                if (contentType.match(Application.Json)) {
                    Json.decodeFromString(content.text)
                } else {
                    JsonPrimitive(content.text)
                }

            is ByteArrayContent,
            is OutgoingContent.ByteArrayContent,
            ->
                JsonPrimitive(content.bytes().toBase64())

            is OutgoingContent.ProtocolUpgrade,
            is OutgoingContent.ReadChannelContent,
            is OutgoingContent.WriteChannelContent,
            ->
                throw IllegalArgumentException("Unsupported request content type: ${content::class.qualifiedName}")
        }
    return Json.encodeToString(jsonElement)
}

private fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}
