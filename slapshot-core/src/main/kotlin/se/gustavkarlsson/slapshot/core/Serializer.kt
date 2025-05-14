package se.gustavkarlsson.slapshot.core

/**
 * Interface for serializing and deserializing objects of type T.
 *
 * Implementations of this interface define the logic for converting objects
 * into a byte array representation and reconstructing objects from that
 * serialized form.
 *
 * @param T The type of objects to be serialized and deserialized.
 */
public interface Serializer<T> {
    /**
     * The file extension used save the snapshot data.
     *
     * Implementations of the serializer define this extension to specify
     * how their serialized data is stored and recognized in the file system.
     */
    public val fileExtension: String

    /**
     * Deserializes the given byte array into an instance of type T.
     *
     * This method converts the serialized representation of data into its
     * corresponding object form.
     *
     * @param bytes The byte array representing the serialized data.
     * @return The deserialized object of type T reconstructed from the input byte array.
     */
    public fun deserialize(bytes: ByteArray): T

    /**
     * Serializes the given value of type T into a byte array.
     *
     * Converts the object representation of the provided value into its
     * serialized byte array form.
     *
     * @param value The value of type T to be serialized.
     * @return A byte array representing the serialized form of the input value.
     */
    public fun serialize(value: T): ByteArray
}
