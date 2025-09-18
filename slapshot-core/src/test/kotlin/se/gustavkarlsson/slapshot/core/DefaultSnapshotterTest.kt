package se.gustavkarlsson.slapshot.core

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.gustavkarlsson.slapshot.core.serializers.StringSerializer
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.message
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private val serializer = StringSerializer()

private class SnapshotFailure(message: String) : RuntimeException(message)

class DefaultSnapshotterTest {
    private lateinit var fs: FileSystem
    private lateinit var fsRoot: Path

    @BeforeEach
    fun setUp() {
        fs = Jimfs.newFileSystem(Configuration.unix())
        fsRoot = fs.rootDirectories.first()
        fsRoot.createDirectories()
    }

    @AfterEach
    fun tearDown() {
        fs.close()
    }

    @Test
    fun `throws when resolved file is outside rootDirectory`() {
        val snapshotter =
            snapshotter(
                root = fsRoot.resolve("root"),
                file = fsRoot.resolve("not-root/snap.txt"),
                action = SnapshotAction.CompareOnly,
            )

        expectThrows<IllegalArgumentException> {
            snapshotter.snapshot("data")
        }
    }

    @Test
    fun `compareOnly succeeds when snapshot exists and matches`() {
        val existingSnapshot =
            fsRoot.resolve("dir/snap.txt").apply {
                parent.createDirectories()
                writeBytes(serializer.serialize("hello"))
            }
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = existingSnapshot,
                action = SnapshotAction.CompareOnly,
            )

        snapshotter.snapshot("hello")
    }

    @Test
    fun `compareOnly reports diff when snapshot exists but differs`() {
        val existingSnapshot =
            fsRoot.resolve("snap.txt").apply {
                parent.createDirectories()
                writeBytes(serializer.serialize("hello"))
            }
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = existingSnapshot,
                action = SnapshotAction.CompareOnly,
            )

        expectThrows<SnapshotFailure> { snapshotter.snapshot("goodbye") }
            .message
            .isNotNull()
            .contains("expected:")
    }

    @Test
    fun `compareOnly fails when snapshot is missing`() {
        val missingSnapshot = fsRoot.resolve("missing.txt")

        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = missingSnapshot,
                action = SnapshotAction.CompareOnly,
            )

        expectThrows<SnapshotFailure> { snapshotter.snapshot("whatever") }
            .message
            .isNotNull()
            .contains("Snapshot not found")
    }

    @Test
    fun `compareAndAdd succeeds when snapshot exists and matches`() {
        val existingSnapshot =
            fsRoot.resolve("dir/snap2.txt").apply {
                parent.createDirectories()
                writeBytes(serializer.serialize("hello"))
            }
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = existingSnapshot,
                action = SnapshotAction.CompareAndAdd,
            )

        snapshotter.snapshot("hello")
    }

    @Test
    fun `compareAndAdd reports diff when snapshot exists but differs`() {
        val existingSnapshot =
            fsRoot.resolve("snap3.txt").apply {
                parent.createDirectories()
                writeBytes(serializer.serialize("hello"))
            }
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = existingSnapshot,
                action = SnapshotAction.CompareAndAdd,
            )

        expectThrows<SnapshotFailure> { snapshotter.snapshot("goodbye") }
            .message
            .isNotNull()
            .contains("expected:")
    }

    @Test
    fun `compareAndAdd creates snapshot and throws when snapshot is missing`() {
        val missingSnapshot = fsRoot.resolve("dir/missing2.txt")

        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = missingSnapshot,
                action = SnapshotAction.CompareAndAdd,
            )

        expectThrows<SnapshotFailure> { snapshotter.snapshot("hello") }
            .message
            .isNotNull()
            .contains("Missing snapshot created")
        expectThat(serializer.deserialize(missingSnapshot.readBytes())).isEqualTo("hello")
    }

    @Test
    fun `running compareAndAdd a second time succeeds`() {
        val missingSnapshot = fsRoot.resolve("dir/missing2.txt")

        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = missingSnapshot,
                action = SnapshotAction.CompareAndAdd,
            )

        expectThrows<SnapshotFailure> { snapshotter.snapshot("hello") }
            .message
            .isNotNull()
            .contains("Missing snapshot created")

        snapshotter.snapshot("hello")
    }

    @Test
    fun `overwrite replaces existing snapshot`() {
        val file =
            fsRoot.resolve("dir/overwrite.txt").apply {
                parent.createDirectories()
                writeBytes(serializer.serialize("old"))
            }
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = file,
                action = SnapshotAction.Overwrite,
            )

        snapshotter.snapshot("new")

        expectThat(file.readBytes()).isEqualTo(serializer.serialize("new"))
    }

    @Test
    fun `overwrite creates snapshot when missing`() {
        val file = fsRoot.resolve("dir/overwrite2.txt")
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = file,
                action = SnapshotAction.Overwrite,
            )

        snapshotter.snapshot("hello")

        expectThat(serializer.deserialize(file.readBytes())).isEqualTo("hello")
    }

    @Test
    fun `running overwrite twice stores the latest content`() {
        val file = fsRoot.resolve("dir/overwrite3.txt")
        val snapshotter =
            snapshotter(
                root = fsRoot,
                file = file,
                action = SnapshotAction.Overwrite,
            )

        snapshotter.snapshot("first")
        snapshotter.snapshot("second")

        expectThat(serializer.deserialize(file.readBytes())).isEqualTo("second")
    }
}

private fun snapshotter(
    root: Path,
    file: Path,
    action: SnapshotAction,
): Snapshotter<String> {
    return DefaultSnapshotter(
        snapshotFileResolver = { _, _, _ -> file },
        rootDirectory = root,
        getTestInfo = { Any() },
        serializer = serializer,
        tester = { actual, expected ->
            if (actual == expected) {
                null
            } else {
                "expected: <$expected> but was: <$actual>"
            }
        },
        action = action,
        onFail = { message -> throw SnapshotFailure(message) },
    )
}
