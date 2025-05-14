package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.nio.ByteBuffer

class MapSerializerTest {
    private val stringSerializer = StringSerializer()
    private val longSerializer = LongSerializer
    private val serializer = MapSerializer(stringSerializer, longSerializer)

    @Test
    fun `serialize and deserialize map`() {
        val map = mapOf("a" to 1L, "b" to 2L, "c" to 3L)
        val serialized = serializer.serialize(map)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(map)
    }

    @Test
    fun `serialize and deserialize empty map`() {
        val emptyMap = emptyMap<String, Long>()
        val serialized = serializer.serialize(emptyMap)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(emptyMap)
    }

    @Test
    fun `serialize and deserialize map with empty keys`() {
        val mapWithEmptyKey = mapOf("" to 1L, "key" to 2L)
        val serialized = serializer.serialize(mapWithEmptyKey)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(mapWithEmptyKey)
    }

    @Test
    fun `deserialize fails with duplicate keys`() {
        // Manually create serialized data with duplicate keys
        val key = "duplicate"
        val keyBytes = stringSerializer.serialize(key)
        val value1 = 1L
        val value1Bytes = longSerializer.serialize(value1)
        val value2 = 2L
        val value2Bytes = longSerializer.serialize(value2)

        val keySize = keyBytes.size
        val value1Size = value1Bytes.size
        val value2Size = value2Bytes.size

        val totalSize = (Int.SIZE_BYTES + keySize + Int.SIZE_BYTES + value1Size) * 2
        val buffer =
            ByteBuffer.allocate(totalSize)
                // First key-value pair
                .putInt(keySize)
                .put(keyBytes)
                .putInt(value1Size)
                .put(value1Bytes)
                // Second key-value pair with same key
                .putInt(keySize)
                .put(keyBytes)
                .putInt(value2Size)
                .put(value2Bytes)

        val serializedWithDuplicates = buffer.array()

        // Try to deserialize it, which should fail
        expectThrows<IllegalStateException> {
            serializer.deserialize(serializedWithDuplicates)
        }.message.isEqualTo("Deserialized map contains duplicate keys: duplicate")
    }
}
