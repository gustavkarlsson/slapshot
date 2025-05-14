package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure

fun <T> tableTestDeserialization(
    table: List<Pair<String, T>>,
    serializer: Serializer<T>,
) {
    expect {
        for ((actualStringValue, expected) in table) {
            that(actualStringValue).describedAs("string value")
                .get("deserialized") { serializer.deserialize(actualStringValue.encodeToByteArray()) }
                .isEqualTo(expected)
        }
    }
}

fun <T> tableTestSerialization(
    table: List<Pair<T, String>>,
    serializer: Serializer<T>,
) {
    expect {
        for ((value, expectedStringValue) in table) {
            that(value).describedAs("value")
                .get("serialized") { serializer.serialize(value).decodeToString() }
                .isEqualTo(expectedStringValue)
        }
    }
}

inline fun <reified E : Throwable> tableTestDeserializationFailure(
    stringValues: List<String>,
    serializer: Serializer<*>,
) {
    expect {
        for (stringValue in stringValues) {
            catching { serializer.deserialize(stringValue.encodeToByteArray()) }
                .describedAs("deserialized string value \"$stringValue\"")
                .isFailure()
                .isA<E>()
        }
    }
}
