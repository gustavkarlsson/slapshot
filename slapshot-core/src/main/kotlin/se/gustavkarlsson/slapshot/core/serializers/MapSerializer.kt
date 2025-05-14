package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.nio.ByteBuffer

/**
 * A serializer for serializing and deserializing maps.
 *
 * The provided serializers are used to serialize the individual keys and values.
 *
 * @param K The type of keys contained in the map.
 * @param V The type of values contained in the map.
 */
public data class MapSerializer<K, V>(
    /**
     * The serializer used for the individual keys of the map.
     */
    val keySerializer: Serializer<K>,
    /**
     * The serializer used for the individual values of the map.
     */
    val valueSerializer: Serializer<V>,
) : Serializer<Map<K, V>> {
    override val fileExtension: String
        get() = "snap"

    override fun deserialize(bytes: ByteArray): Map<K, V> {
        val map = mutableMapOf<K, V>()
        val buffer = ByteBuffer.wrap(bytes)
        while (buffer.hasRemaining()) {
            val keySize = buffer.getInt()
            val keyBytes = ByteArray(keySize)
            buffer.get(keyBytes)
            val valueSize = buffer.getInt()
            val valueBytes = ByteArray(valueSize)
            buffer.get(valueBytes)
            val key = keySerializer.deserialize(keyBytes)
            val value = valueSerializer.deserialize(valueBytes)
            check(!map.containsKey(key)) {
                "Deserialized map contains duplicate keys: $key"
            }
            map.put(key, value)
        }
        return map
    }

    override fun serialize(value: Map<K, V>): ByteArray {
        val bytesInList =
            value.map { (key, value) ->
                val keyBytes = keySerializer.serialize(key)
                val valueBytes = valueSerializer.serialize(value)
                val keySize = keyBytes.size
                val valueSize = valueBytes.size
                val byteCount = Int.SIZE_BYTES + keySize + Int.SIZE_BYTES + valueSize
                ByteBuffer.allocate(byteCount)
                    .putInt(keySize)
                    .put(keyBytes)
                    .putInt(valueSize)
                    .put(valueBytes)
                    .array()
            }
        val totalSize = bytesInList.sumOf { it.size }
        val bytesBuffer = ByteBuffer.wrap(ByteArray(totalSize))
        for (bytes in bytesInList) {
            bytesBuffer.put(bytes)
        }
        return bytesBuffer.array()
    }
}
