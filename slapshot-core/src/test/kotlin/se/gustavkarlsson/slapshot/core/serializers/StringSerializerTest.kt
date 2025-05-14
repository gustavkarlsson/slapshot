package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test

class StringSerializerTest {
    private val serializer = StringSerializer()

    @Test
    fun `serialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
            )

        tableTestSerialization(table, serializer)
    }

    @Test
    fun `deserialize values`() {
        val table =
            listOf(
                "foobar" to "foobar",
            )

        tableTestDeserialization(table, serializer)
    }
}
