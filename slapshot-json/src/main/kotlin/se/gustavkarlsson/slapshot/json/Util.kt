package se.gustavkarlsson.slapshot.json

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

private val json =
    Json {
        prettyPrint = true
    }

internal fun String.parseJsonElement(): JsonElement {
    return json.decodeFromString<JsonElement>(this).validate()
}

/**
 * Because of https://github.com/Kotlin/kotlinx.serialization/issues/2511,
 * decoded JSON primitives may accept unquoted strings. This function ensures there are none of them.
 */
private fun JsonElement.validate(): JsonElement {
    when (this) {
        JsonNull -> Unit
        is JsonPrimitive -> {
            when {
                this.contentOrNull == null -> Unit
                this.isString -> Unit
                this.booleanOrNull != null -> Unit
                this.longOrNull != null -> Unit
                this.doubleOrNull != null -> Unit
                else -> throw IllegalArgumentException("Unsupported json primitive found: ${this.content}")
            }
        }

        is JsonArray -> this.forEach { it.validate() }
        is JsonObject -> this.values.forEach { it.validate() }
    }
    return this
}

/**
 * Because of https://github.com/Kotlin/kotlinx.serialization/issues/2511,
 * decoded JSON primitives may accept unquoted strings. This function ensures there are none of them.
 */
internal fun String.validateJson(): String {
    this.parseJsonElement()
    return this
}

internal fun JsonElement.prettyPrint(): String {
    return json.encodeToString(this)
}

internal fun String.prettyPrint(): String {
    return this.parseJsonElement().prettyPrint()
}
