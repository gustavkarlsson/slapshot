package se.gustavkarlsson.slapshot.core.serializers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import se.gustavkarlsson.slapshot.core.Serializer

/**
 * Handles serialization of JSON values. Snapshots are pretty printed when serialized.
 */
public data object JsonSerializer : Serializer<String> {
    private val json =
        Json {
            prettyPrint = true
        }

    override val fileExtension: String
        get() = "json"

    override fun deserialize(bytes: ByteArray): String {
        val decodedString = bytes.decodeToString()
        val jsonElement = json.decodeFromString<JsonElement>(decodedString)
        return json.encodeToString(jsonElement)
    }

    override fun serialize(value: String): ByteArray {
        val jsonElement = json.decodeFromString<JsonElement>(value)
        val encodedString = json.encodeToString(jsonElement)
        return encodedString.encodeToByteArray()
    }
}
