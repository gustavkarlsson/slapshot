package se.gustavkarlsson.slapshot.core

import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

@InternalSlapshotApi
public class DefaultSnapshotter<T, TI>(
    private val snapshotFileResolver: SnapshotFileResolver<TI>,
    private val rootDirectory: Path,
    private val getTestInfo: () -> TI,
    private val serializer: Serializer<T>,
    private val tester: Tester<T>,
    private val action: SnapshotAction,
    private val onFail: (message: String) -> Unit,
) : Snapshotter<T> {
    override fun snapshot(data: T) {
        val file = snapshotFileResolver.resolve(rootDirectory, getTestInfo(), serializer.fileExtension)
        require(rootDirectory.isAncestorOf(file)) {
            "Resolved snapshot file is outside of rootDirectory: '$file' (root: '$rootDirectory')"
        }
        when (action) {
            SnapshotAction.CompareOnly -> compareOnly(file, data)
            SnapshotAction.CompareAndAdd -> compareAndAdd(file, data)
            SnapshotAction.Overwrite -> overwrite(file, data)
        }
    }

    private fun compareOnly(
        expectedFile: Path,
        actual: T,
    ) {
        if (expectedFile.notExists()) {
            onFail("Snapshot not found: '$expectedFile'")
        }
        compareSnapshot(expectedFile, actual)
    }

    private fun compareAndAdd(
        expectedFile: Path,
        actual: T,
    ) {
        if (expectedFile.exists()) {
            compareSnapshot(expectedFile, actual)
        } else {
            val createNewOptions =
                arrayOf(
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE,
                )
            writeSnapshot(expectedFile, actual, *createNewOptions)
            onFail("Missing snapshot created: '$expectedFile'")
        }
    }

    private fun overwrite(
        file: Path,
        newData: T,
    ) {
        val overwriteOptions =
            arrayOf(
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )
        writeSnapshot(file, newData, *overwriteOptions)
    }

    private fun compareSnapshot(
        expectedFile: Path,
        actual: T,
    ) {
        val bytes = expectedFile.readBytes()
        val existing = serializer.deserialize(bytes)
        val diffString = tester.test(actual, existing)
        if (diffString != null) {
            onFail(diffString)
        }
    }

    private fun writeSnapshot(
        file: Path,
        data: T,
        vararg options: OpenOption,
    ) {
        val bytes = serializer.serialize(data)
        file.parent.createDirectories()
        file.writeBytes(bytes, *options)
    }
}

private fun Path.isAncestorOf(file: Path): Boolean {
    val normalizedAncestor = this.toAbsolutePath().normalize()
    val normalizedChild = file.toAbsolutePath().normalize()
    return normalizedChild.startsWith(normalizedAncestor)
}
