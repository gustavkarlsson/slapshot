package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.provider.Property
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType

private const val EXTENSION_NAME = "slapshot"

class SlapshotPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.createExtension()
        // FIXME don't hard-code
        project.addDependencyOnce("testImplementation", "se.gustavkarlsson.slapshot:core:1.0-SNAPSHOT")
        project.addSnapshotTasks()
        project.afterEvaluate {
            tasks.withType<Test> {
                logger.info("writing snapshots dir")
                systemProperty("snapshotRootDir", extension.snapshotRootDir.get())
            }
        }
    }
}

private fun Project.createExtension(): SlapshotPluginExtension {
    return extensions.create<SlapshotPluginExtension>(EXTENSION_NAME).apply {
        // FIXME better default
        logger.info("settings default snapshot dir")
        snapshotRootDir.set(projectDir.resolve("src/test/snapshots"))
    }
}

private fun Project.addDependencyOnce(configuration: String, dependencyNotation: String) {
    gradle.addListener(object : DependencyResolutionListener {
        override fun beforeResolve(dependencies: ResolvableDependencies) {
            val dependency = project.dependencies.create(dependencyNotation)
            project.dependencies.add(configuration, dependency)
            project.gradle.removeListener(this)
        }

        override fun afterResolve(dependencies: ResolvableDependencies) {}
    })
}

private fun Project.addSnapshotTasks() {
    val updateSnapshotsTask = task<UpdateSnapshotsTask>(UpdateSnapshotsTask.name)
    val purgeSnapshotsTask = task<PurgeSnapshotsTask>(PurgeSnapshotsTask.name)
}

interface SlapshotPluginExtension {
    val snapshotRootDir: Property<Any>
}
