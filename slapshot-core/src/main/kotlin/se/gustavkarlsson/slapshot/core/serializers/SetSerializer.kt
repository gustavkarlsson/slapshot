package se.gustavkarlsson.slapshot.core.serializers

import se.gustavkarlsson.slapshot.core.Serializer

/**
 * A serializer for serializing and deserializing sets of type T.
 *
 * The provided serializer is used to serialize the individual elements.
 *
 * @param T The type of elements contained in the set.
 */
public data class SetSerializer<T>(
    /**
     * The serializer used for the individual elements of the set.
     */
    val elementSerializer: Serializer<T>,
) : Serializer<Set<T>> {
    override val fileExtension: String
        get() = "snap"

    private val listSerializer = ListSerializer(elementSerializer)

    override fun deserialize(bytes: ByteArray): Set<T> {
        val list = listSerializer.deserialize(bytes)
        val set = list.toSet()
        check(list.size == set.size) {
            createDuplicatesString(list)
        }
        return set
    }

    private fun createDuplicatesString(list: List<T>): String {
        list.forEachIndexed { index, value ->
            val rest = list.drop(index + 1)
            rest.forEach { restValue ->
                if (value == restValue) {
                    return "Deserialized set contains duplicates: $value"
                }
            }
        }
        error("Failed to find duplicates")
    }

    override fun serialize(value: Set<T>): ByteArray {
        return listSerializer.serialize(value.toList())
    }
}
