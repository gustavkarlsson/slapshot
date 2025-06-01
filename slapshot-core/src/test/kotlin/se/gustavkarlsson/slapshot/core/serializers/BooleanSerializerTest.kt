package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test

class BooleanSerializerTest {
    private val serializer = BooleanSerializer

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                true to "true",
                false to "false",
            )

        tableTestSerialization(table, serializer)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "true" to true,
                "false" to false,
                "TRUE" to true,
                "FALSE" to false,
                "TrUe" to true,
                "FaLsE" to false,
                "\n \ttrue" to true,
                "\n \tfalse" to false,
            )

        tableTestDeserialization(table, serializer)
    }

    @Test
    fun `deserialize invalid values`() {
        val strings =
            listOf(
                "yes",
                "no",
                "bla",
                "1",
                "0",
                "",
            )

        tableTestDeserializationFailure<IllegalArgumentException>(strings, serializer)
    }
}
