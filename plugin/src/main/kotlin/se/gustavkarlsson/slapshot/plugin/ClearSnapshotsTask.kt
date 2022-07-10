package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ClearSnapshotsTask : DefaultTask() {
    companion object {
        const val name = "clearSnapshots"
    }

    init {
        group = "verification"
        description = "Deletes all existing snapshots from the project"
    }

    @OutputDirectory
    lateinit var snapshotRootDir: String

    @TaskAction
    fun clearSnapshots() {
        logger.info("Clearing snapshots in $snapshotRootDir")
        check(File(snapshotRootDir).deleteRecursively()) {
            "Failed to clear snapshots in $snapshotRootDir"
        }
    }
}
