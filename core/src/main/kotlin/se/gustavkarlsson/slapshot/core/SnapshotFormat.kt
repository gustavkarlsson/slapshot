package se.gustavkarlsson.slapshot.core

/**
 * Represents a format specification for snapshot data, defining how it is serialized, deserialized,
 * and tested.
 *
 * @param T The type of data handled by the snapshot format.
 */
public interface SnapshotFormat<T> {
    /**
     * The file extension used to identify file formats associated with the snapshot data.
     *
     * Implementations of the snapshot format define this extension to specify
     * how their serialized data is stored and recognized in the file system.
     */
    public val fileExtension: String

    /**
     * Tests whether the given actual value matches the expected value.
     *
     * If the values do not match, a descriptive error message is returned indicating
     * the mismatch. If the values are equal, null is returned.
     *
     * @param actual The actual value to test.
     * @param expected The expected value to compare against.
     * @return A descriptive error message if the values do not match, or null if they are equal.
     */
    public fun test(
        actual: T,
        expected: T,
    ): String? {
        return if (actual != expected) {
            "expected: <$expected> but was: <$actual>"
        } else {
            null
        }
    }

    /**
     * Deserializes the given byte array into an instance of type T.
     *
     * This method converts the serialized representation of data into its
     * corresponding object form based on the snapshot format implementation.
     *
     * @param bytes The byte array representing the serialized data.
     * @return The deserialized object of type T reconstructed from the input byte array.
     */
    public fun deserialize(bytes: ByteArray): T

    /**
     * Serializes the given value of type T into a byte array.
     *
     * Converts the object representation of the provided value into its
     * serialized byte array form according to the implementation of the snapshot format.
     *
     * @param value The value of type T to be serialized.
     * @return A byte array representing the serialized form of the input value.
     */
    public fun serialize(value: T): ByteArray
}
