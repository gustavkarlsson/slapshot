package se.gustavkarlsson.slapshot.core.formats

import se.gustavkarlsson.slapshot.core.SnapshotFormat
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.isNull

fun <T> tableTestValuesPassing(
    table: List<Pair<T, T>>,
    format: SnapshotFormat<T>,
) {
    expect {
        for ((actual, expected) in table) {
            that(format).get("test($actual, $expected)") { test(actual, expected) }.isNull()
        }
    }
}

fun <T> tableTestValuesFailing(
    table: List<Pair<T, T>>,
    format: SnapshotFormat<T>,
) {
    expect {
        for ((actual, expected) in table) {
            that(format).get("test($actual, $expected)") { test(actual, expected) }.isNotNull()
        }
    }
}

fun <T> tableTestDeserialization(
    table: List<Pair<String, T>>,
    format: SnapshotFormat<T>,
) {
    expect {
        for ((text, expected) in table) {
            that(text).get("deserialized") { format.deserialize(text.encodeToByteArray()) }
                .isEqualTo(expected)
        }
    }
}

fun <T> tableTestSerialization(
    table: List<Pair<T, String>>,
    format: SnapshotFormat<T>,
) {
    expect {
        for ((value, expected) in table) {
            that(value).get("serialized") { format.serialize(value).decodeToString() }
                .isEqualTo(expected)
        }
    }
}

fun tableTestDeserializationFailure(
    table: List<String>,
    format: SnapshotFormat<*>,
) {
    expect {
        for (invalid in table) {
            catching { format.deserialize(invalid.encodeToByteArray()) }
                .describedAs("deserialize(\"$invalid\")")
                .isFailure()
                .isA<IllegalArgumentException>()
        }
    }
}
