package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.util.toMap
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.Base64

// FIXME clean up, and support more content types

// FIXME Support ReadChannelContent? File uploads/downloads?
internal fun requestBodyToJson(request: HttpRequest): String? {
    val jsonObject: JsonObject =
        when (val content = request.content) {
            is OutgoingContent.NoContent -> return null
            is TextContent -> {
                if (content.text.isEmpty()) return null
                if (request.content.contentType?.match(ContentType.Application.Json) == true) {
                    jsonOrTextBody(content.text)
                } else {
                    textBody(content.text)
                }
            }

            is FormDataContent -> {
                if (content.formData.isEmpty()) return null
                formDataBody(content.formData)
            }

            is ByteArrayContent, is OutgoingContent.ByteArrayContent -> {
                if (content.bytes().isEmpty()) return null
                binaryBase64Body(content.bytes())
            }

            // TODO Can we do better?
            is MultiPartFormDataContent -> {
                unsupportedBody("multipart/form-data")
            }

            is OutgoingContent.WriteChannelContent -> {
                val bytes = content.writeBytes()
                if (bytes.isEmpty()) return null
                binaryBase64Body(bytes)
            }

            is OutgoingContent.ReadChannelContent -> {
                val bytes = content.readBytes()
                if (bytes.isEmpty()) return null
                binaryBase64Body(bytes)
            }

            is OutgoingContent.ProtocolUpgrade ->
                throw IllegalArgumentException("Unsupported request content type: ${content::class.qualifiedName}")
        }
    return Json.encodeToString(jsonObject)
}

private fun OutgoingContent.WriteChannelContent.writeBytes(): ByteArray =
    runBlocking {
        val channel = ByteChannel(autoFlush = true)
        try {
            writeTo(channel)
            channel.readRemaining().readBytes()
        } finally {
            channel.close()
        }
    }

private fun OutgoingContent.ReadChannelContent.readBytes(): ByteArray =
    runBlocking {
        readFrom().readRemaining().readBytes()
    }

internal suspend fun responseBodyToJson(response: HttpResponse): String? {
    val contentType = response.contentType()
    val jsonElement: JsonObject =
        when {
            contentType == null -> {
                // Binary?
                val bytes = response.body<ByteArray>()
                if (bytes.isNotEmpty()) {
                    binaryBase64Body(bytes)
                } else {
                    return null
                }
            }

            contentType.match(ContentType.Application.Json) -> {
                val text = response.bodyAsText()
                if (text.isNotEmpty()) {
                    jsonOrTextBody(text)
                } else {
                    return null
                }
            }

            contentType.match(ContentType.Text.Any) -> {
                val text = response.bodyAsText()
                if (text.isNotEmpty()) {
                    textBody(text)
                } else {
                    return null
                }
            }

            contentType.match(ContentType.Application.OctetStream) -> {
                val bytes = response.body<ByteArray>()
                if (bytes.isNotEmpty()) {
                    binaryBase64Body(bytes)
                } else {
                    return null
                }
            }

            else -> {
                // Binary?
                val bytes = response.body<ByteArray>()
                if (bytes.isNotEmpty()) {
                    binaryBase64Body(bytes)
                } else {
                    return null
                }
            }
        }
    return Json.encodeToString(jsonElement)
}

private fun jsonOrTextBody(string: String): JsonObject {
    return try {
        val data = Json.decodeFromString<JsonElement>(string)
        jsonBody(data)
    } catch (_: SerializationException) {
        textBody(string)
    }
}

private fun jsonBody(data: JsonElement): JsonObject {
    return createBodyJsonObject("json", data)
}

private fun textBody(text: String): JsonObject {
    return createBodyJsonObject("text", JsonPrimitive(text))
}

private fun unsupportedBody(type: String): JsonObject {
    return createBodyJsonObject("unsupported-type", JsonPrimitive(type))
}

private fun formDataBody(parameters: Parameters): JsonObject {
    val element = parameters.toJsonObject()
    return createBodyJsonObject("form-data", element)
}

private fun binaryBase64Body(bytes: ByteArray): JsonObject {
    val base64 = bytes.toBase64()
    val element = JsonPrimitive(base64)
    return createBodyJsonObject("binary-base64", element)
}

private fun createBodyJsonObject(
    format: String,
    data: JsonElement,
): JsonObject {
    val map =
        buildMap {
            put("format", JsonPrimitive(format))
            put("data", data)
        }
    return JsonObject(map)
}

private fun Parameters.toJsonObject(): JsonObject {
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

private fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}
