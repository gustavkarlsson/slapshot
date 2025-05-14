package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message

class SetSerializerTest {
    private val stringSerializer = StringSerializer()
    private val serializer = SetSerializer(stringSerializer)

    @Test
    fun `serialize and deserialize set`() {
        val set = setOf("a", "b", "c")
        val serialized = serializer.serialize(set)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(set)
    }

    @Test
    fun `serialize and deserialize empty set`() {
        val emptySet = emptySet<String>()
        val serialized = serializer.serialize(emptySet)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(emptySet)
    }

    @Test
    fun `serialize and deserialize set with empty strings`() {
        val setWithEmpty = setOf("", "content")
        val serialized = serializer.serialize(setWithEmpty)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(setWithEmpty)
    }

    @Test
    fun `deserialize fails with duplicate elements`() {
        // Create a list with duplicates and serialize it using ListSerializer
        val listWithDuplicates = listOf("a", "a", "b")
        val listSerializer = ListSerializer(stringSerializer)
        val serializedWithDuplicates = listSerializer.serialize(listWithDuplicates)

        // Try to deserialize it with SetSerializer, which should fail
        expectThrows<IllegalStateException> {
            serializer.deserialize(serializedWithDuplicates)
        }.message.isEqualTo("Deserialized set contains duplicates: a")
    }
}
