package se.gustavkarlsson.slapshot.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path

@OptIn(InternalSlapshotApi::class)
class DefaultSnapshotterTest {
    @Test
    fun `throws when resolved file is outside rootDirectory`() {
        val root = Path.of("root")
        val snapshotFileResolver =
            SnapshotFileResolver<Any> { rootDir, _, ext ->
                rootDir.parent.resolve("outside").resolve("snap.$ext")
            }
        val snapshotter =
            DefaultSnapshotter(
                snapshotFileResolver = snapshotFileResolver,
                rootDirectory = root,
                getTestInfo = { Any() },
                serializer = MinimalStringSerializer,
                tester = { _, _ -> null },
                action = SnapshotAction.CompareOnly,
                onFail = { },
            )

        assertThrows<IllegalArgumentException> {
            snapshotter.snapshot("data")
        }
    }
}

private object MinimalStringSerializer : Serializer<String> {
    override val fileExtension: String = "txt"

    override fun deserialize(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)

    override fun serialize(value: String): ByteArray = value.toByteArray(Charsets.UTF_8)
}
