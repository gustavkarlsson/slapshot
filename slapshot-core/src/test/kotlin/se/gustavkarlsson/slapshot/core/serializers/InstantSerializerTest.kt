package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.format.DateTimeParseException

class InstantSerializerTest {
    private val serializer = InstantSerializer

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                Instant.EPOCH to "1970-01-01T00:00:00Z",
                Instant.parse("2023-01-01T12:34:56.789Z") to "2023-01-01T12:34:56.789Z",
                Instant.parse("2023-01-01T00:00:00Z") to "2023-01-01T00:00:00Z",
                Instant.parse("2023-12-31T23:59:59.999999999Z") to "2023-12-31T23:59:59.999999999Z",
                // Instant.MAX and Instant.MIN are too extreme for this test
                Instant.parse("9999-12-31T23:59:59.999999999Z") to "9999-12-31T23:59:59.999999999Z",
                Instant.parse("-9999-01-01T00:00:00Z") to "-9999-01-01T00:00:00Z",
            )

        tableTestSerialization(table, serializer)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "1970-01-01T00:00:00Z" to Instant.EPOCH,
                "2023-01-01T12:34:56.789Z" to Instant.parse("2023-01-01T12:34:56.789Z"),
                "2023-01-01T00:00:00Z" to Instant.parse("2023-01-01T00:00:00Z"),
                "2023-12-31T23:59:59.999999999Z" to Instant.parse("2023-12-31T23:59:59.999999999Z"),
                "9999-12-31T23:59:59.999999999Z" to Instant.parse("9999-12-31T23:59:59.999999999Z"),
                "-9999-01-01T00:00:00Z" to Instant.parse("-9999-01-01T00:00:00Z"),
            )

        tableTestDeserialization(table, serializer)
    }

    @Test
    fun `deserialize invalid values`() {
        val strings =
            listOf(
                "",
                "not a date",
                // Missing time part
                "2023-01-01",
                // Missing date part
                "12:34:56",
                // Missing Z timezone designator
                "2023-01-01T12:34:56",
                // Invalid format (space instead of T)
                "2023-01-01 12:34:56Z",
                // Invalid month (13)
                "2023-13-01T12:34:56Z",
                // Invalid day (32)
                "2023-01-32T12:34:56Z",
                // Invalid hour (24)
                "2023-01-01T24:34:56Z",
                // Invalid minute (60)
                "2023-01-01T12:60:56Z",
                // Invalid second (60)
                "2023-01-01T12:34:60Z",
            )

        tableTestDeserializationFailure<DateTimeParseException>(strings, serializer)
    }
}
