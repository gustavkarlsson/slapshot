package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class UpdateSnapshotsTask : DefaultTask() {
    companion object {
        const val name = "updateSnapshots"
    }

    init {
        group = "verification"
        description = "Run all tests updating snapshots when differences are found."
    }

    @TaskAction
    fun updateSnapshots() {
        logger.info("updating snapshot")
    }
}

open class PurgeSnapshotsTask : DefaultTask() {
    companion object {
        const val name = "purgeSnapshots"
    }

    init {
        group = "verification"
        description = "Delete all snapshots and update them all"
    }

    @TaskAction
    fun updateSnapshots() {
        logger.info("purging snapshot")
    }
}
