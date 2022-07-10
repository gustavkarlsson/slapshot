package se.gustavkarlsson.slapshot.core

import java.nio.file.Path
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
            SnapshotAction.CompareOnly -> {
                if (file.notExists()) {
                    onFail("Snapshot file not found: '$file'")
                }
                compareSnapshot(file, format, data)
            }

            SnapshotAction.CompareAndAdd -> {
                if (file.exists()) {
                    compareSnapshot(file, format, data)
                } else {
                    writeSnapshot(file, format, data)
                }
            }

            SnapshotAction.Overwrite -> {
                file.deleteIfExists()
                writeSnapshot(file, format, data)
            }
        }
    }

    private fun <T, F : SnapshotFormat<T>> compareSnapshot(file: Path, format: F, data: T) {
        val bytes = file.readBytes()
        val old = format.deserialize(bytes)
        val diffString = format.test(old, data)
        if (diffString != null) {
            val message = "Result did not match stored snapshot: '$file':\n$diffString"
            onFail(message)
        }
    }

    private fun <T, F : SnapshotFormat<T>> writeSnapshot(file: Path, format: F, data: T) {
        val bytes = format.serialize(data)
        file.parent.createDirectories()
        file.writeBytes(bytes)
    }
}
