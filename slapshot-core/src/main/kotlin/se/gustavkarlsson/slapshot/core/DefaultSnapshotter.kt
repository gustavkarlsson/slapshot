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
        when (action) {
            SnapshotAction.CompareOnly -> compareOnly(file, data)
            SnapshotAction.CompareAndAdd -> compareAndAdd(file, data)
            SnapshotAction.Overwrite -> overwrite(file, data)
        }
    }

    private fun compareOnly(
        file: Path,
        data: T,
    ) {
        if (file.notExists()) {
            onFail("Snapshot not found: '$file'")
        }
        compareSnapshot(file, data)
    }

    private fun compareAndAdd(
        file: Path,
        data: T,
    ) {
        if (file.exists()) {
            compareSnapshot(file, data)
        } else {
            val createNewOptions =
                arrayOf(
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE,
                )
            writeSnapshot(file, data, *createNewOptions)
            onFail("Missing snapshot created: '$file'")
        }
    }

    private fun overwrite(
        file: Path,
        data: T,
    ) {
        val overwriteOptions =
            arrayOf(
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )
        writeSnapshot(file, data, *overwriteOptions)
        onFail("Overwrote existing snapshot: '$file'") // FIXME should this fail?
    }

    private fun compareSnapshot(
        file: Path,
        data: T,
    ) {
        val bytes = file.readBytes()
        val old = serializer.deserialize(bytes)
        val diffString = tester.test(old, data)
        if (diffString != null) {
            onFail("Result did not match stored snapshot: '$file':\n$diffString")
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
