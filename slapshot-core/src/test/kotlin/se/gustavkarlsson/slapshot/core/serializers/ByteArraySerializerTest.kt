package se.gustavkarlsson.slapshot.core.serializers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ByteArraySerializerTest {
    private val serializer = ByteArraySerializer

    @Test
    fun `serialize and deserialize byte array`() {
        val byteArray = byteArrayOf(1, 2, 3, 4, 5)
        val serialized = serializer.serialize(byteArray)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(byteArray)
    }

    @Test
    fun `serialize and deserialize empty byte array`() {
        val emptyByteArray = byteArrayOf()
        val serialized = serializer.serialize(emptyByteArray)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(emptyByteArray)
    }

    @Test
    fun `serialize and deserialize byte array with zeros`() {
        val zeroByteArray = byteArrayOf(0, 0, 0)
        val serialized = serializer.serialize(zeroByteArray)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(zeroByteArray)
    }

    @Test
    fun `serialize and deserialize byte array with all possible byte values`() {
        val allValues = ByteArray(256) { i -> (i - 128).toByte() }
        val serialized = serializer.serialize(allValues)
        val deserialized = serializer.deserialize(serialized)

        expectThat(deserialized).isEqualTo(allValues)
    }
}
