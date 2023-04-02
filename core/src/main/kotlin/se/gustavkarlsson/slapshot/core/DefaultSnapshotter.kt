package se.gustavkarlsson.slapshot.core

import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

@InternalSlapshotApi
class DefaultSnapshotter<T, TI>(
    private val snapshotFileResolver: SnapshotFileResolver<TI>,
    private val rootDirectory: Path,
    private val getTestInfo: () -> TI,
    private val format: SnapshotFormat<T>,
    private val action: SnapshotAction,
    private val onFail: (message: String) -> Unit,
) : Snapshotter<T> {

    override fun snapshot(data: T) {
        val file = snapshotFileResolver.resolve(rootDirectory, getTestInfo(), format.fileExtension)
        when (action) {
            SnapshotAction.CompareOnly -> compareOnly(file, data)
            SnapshotAction.CompareAndAdd -> compareAndAdd(file, data)
            SnapshotAction.Overwrite -> overwrite(file, data)
        }
    }

    private fun compareOnly(file: Path, data: T) {
        if (file.notExists()) {
            onFail("Snapshot not found: '$file'")
        }
        compareSnapshot(file, format, data)
    }

    private fun compareAndAdd(file: Path, data: T) {
        if (file.exists()) {
            compareSnapshot(file, format, data)
        } else {
            val createNewOptions = arrayOf(
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE,
            )
            writeSnapshot(file, format, data, *createNewOptions)
            onFail("Missing snapshot created: '$file'")
        }
    }

    private fun overwrite(file: Path, data: T) {
        val overwriteOptions = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
        )
        writeSnapshot(file, format, data, *overwriteOptions)
    }

    private fun <T, F : SnapshotFormat<T>> compareSnapshot(file: Path, format: F, data: T) {
        val bytes = file.readBytes()
        val old = format.deserialize(bytes)
        val diffString = format.test(old, data)
        if (diffString != null) {
            onFail("Result did not match stored snapshot: '$file':\n$diffString")
        }
    }

    private fun <T, F : SnapshotFormat<T>> writeSnapshot(file: Path, format: F, data: T, vararg options: OpenOption) {
        val bytes = format.serialize(data)
        file.parent.createDirectories()
        file.writeBytes(bytes, *options)
    }
}
