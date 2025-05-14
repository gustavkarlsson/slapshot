package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ListSerializerTest {
    private val stringSerializer = StringSerializer()
    private val serializer = ListSerializer(stringSerializer)

    @Test
    fun `serialize and deserialize list`() {
        val list = listOf("a", "b", "c")
        val serialized = serializer.serialize(list)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(list)
    }

    @Test
    fun `serialize and deserialize empty list`() {
        val emptyList = emptyList<String>()
        val serialized = serializer.serialize(emptyList)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(emptyList)
    }

    @Test
    fun `serialize and deserialize list with empty strings`() {
        val listWithEmpty = listOf("", "content", "")
        val serialized = serializer.serialize(listWithEmpty)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(listWithEmpty)
    }

    @Test
    fun `serialize and deserialize nested lists`() {
        val nestedListSerializer = ListSerializer(ListSerializer(stringSerializer))
        val nestedList = listOf(listOf("a", "b"), listOf("c", "d"))
        val serialized = nestedListSerializer.serialize(nestedList)
        val deserialized = nestedListSerializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(nestedList)
    }
}
