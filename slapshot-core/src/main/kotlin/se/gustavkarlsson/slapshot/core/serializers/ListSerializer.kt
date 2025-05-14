package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer
import java.nio.ByteBuffer

/**
 * A serializer for serializing and deserializing lists of type T.
 *
 * The provided serializer is used to serialize the individual elements.
 *
 * @param T The type of elements contained in the list.
 */
public data class ListSerializer<T>(
    /**
     * The serializer used for the individual elements of the list.
     */
    val elementSerializer: Serializer<T>,
) : Serializer<List<T>> {
    override val fileExtension: String
        get() = "snap"

    override fun deserialize(bytes: ByteArray): List<T> {
        val list = mutableListOf<T>()
        val buffer = ByteBuffer.wrap(bytes)
        while (buffer.hasRemaining()) {
            val elementSize = buffer.getInt()
            val elementBytes = ByteArray(elementSize)
            buffer.get(elementBytes)
            val element = elementSerializer.deserialize(elementBytes)
            list.add(element)
        }
        return list
    }

    override fun serialize(value: List<T>): ByteArray {
        val bytesInList =
            value.map { element ->
                val elementBytes = elementSerializer.serialize(element)
                val elementSize = elementBytes.size
                val byteCount = Int.SIZE_BYTES + elementSize
                ByteBuffer.allocate(byteCount)
                    .putInt(elementSize)
                    .put(elementBytes)
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
