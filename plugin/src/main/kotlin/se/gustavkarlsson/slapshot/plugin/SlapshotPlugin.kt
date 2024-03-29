package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.File

private const val EXTENSION_NAME = "slapshot"
private const val DEFAULT_SNAPSHOT_ROOT_DIR_NAME = "snapshots"
private const val PROPERTY_KEY_ROOT_DIR = "snapshotRootDir"
private const val PROPERTY_KEY_ACTION = "snapshotAction"
private const val ACTION_VALUE_COMPARE_ONLY = "compareOnly"
private const val ACTION_VALUE_COMPARE_AND_ADD = "compareAndAdd"
private const val ACTION_VALUE_OVERWRITE = "overwrite"

@Suppress("unused")
class SlapshotPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.createExtension()
        project.addDependencies(extension)
        val clearSnapshotsTask = project.createClearSnapshotsTask()
        project.afterEvaluate {
            val snapshotRootDir = getSnapshotRootDir(project, extension)
            val snapshotAction = getSnapshotAction(project, extension)
            clearSnapshotsTask.configure {
                delete(snapshotRootDir)
            }
            tasks.withType<Test> {
                mustRunAfter(clearSnapshotsTask) // Only applicable if clearSnapshots DOES run
                inputs.files(fileTree(snapshotRootDir))
                inputs.property(PROPERTY_KEY_ACTION, snapshotAction)
                outputs.files(fileTree(snapshotRootDir))

                logger.info("Setting $PROPERTY_KEY_ROOT_DIR system property to $snapshotRootDir")
                systemProperty(PROPERTY_KEY_ROOT_DIR, snapshotRootDir)
                logger.info("Setting $PROPERTY_KEY_ACTION system property to $snapshotAction")
                systemProperty(PROPERTY_KEY_ACTION, snapshotAction.systemProperty)
            }
        }
    }
}

private fun Project.createExtension(): SlapshotPluginExtension {
    return extensions.create<SlapshotPluginExtension>(EXTENSION_NAME).apply {
        val defaultSnapshotRootDir = getDefaultSnapshotRootDir()
        logger.info("Setting default snapshot root dir to $defaultSnapshotRootDir")
        testFramework.convention(TestFramework.JUnit5)
        snapshotRootDir.convention(defaultSnapshotRootDir)
        snapshotAction.convention(SnapshotAction.CompareAndAdd)
    }
}

// FIXME correct versions
private fun Project.addDependencies(extension: SlapshotPluginExtension) {
    dependencies {
        add("testImplementation", "se.gustavkarlsson.slapshot:core:1.0-SNAPSHOT")
        val variant = extension.testFramework.map { testFramework ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (testFramework) {
                TestFramework.JUnit4 -> "se.gustavkarlsson.slapshot:junit4:1.0-SNAPSHOT"
                TestFramework.JUnit5 -> "se.gustavkarlsson.slapshot:junit5:1.0-SNAPSHOT"
            }
        }
        addProvider("testImplementation", variant)
    }
}

private const val NO_TEST_SOURCE_SET_FOUND_ERROR_MESSAGE =
    "No test source set found to use as default snapshot root directory"

private fun Project.getDefaultSnapshotRootDir(): File {
    val fallbackDir = projectDir.resolve(DEFAULT_SNAPSHOT_ROOT_DIR_NAME)
    val sourceSets = extensions.findByType<SourceSetContainer>()?.asMap
    if (sourceSets == null) {
        logger.warn(NO_TEST_SOURCE_SET_FOUND_ERROR_MESSAGE)
        return fallbackDir
    }
    val testSourceSet = sourceSets["test"]
    if (testSourceSet == null) {
        logger.warn(NO_TEST_SOURCE_SET_FOUND_ERROR_MESSAGE)
        return fallbackDir
    }
    val testSourcesDir = testSourceSet.allSource.srcDirs.firstOrNull()?.parentFile
    if (testSourcesDir == null) {
        logger.warn(NO_TEST_SOURCE_SET_FOUND_ERROR_MESSAGE)
        return fallbackDir
    }
    return testSourcesDir.resolve(DEFAULT_SNAPSHOT_ROOT_DIR_NAME)
}

private fun Project.createClearSnapshotsTask(): TaskProvider<Delete> {
    return tasks.register<Delete>("clearSnapshots") {
        group = "verification"
        description = "Deletes all existing snapshots from the project"
    }
}

private fun getSnapshotRootDir(project: Project, extension: SlapshotPluginExtension): File {
    val property = project.findProperty(PROPERTY_KEY_ROOT_DIR)?.toString()
    val dir = if (property != null) {
        project.logger.debug("Using $PROPERTY_KEY_ROOT_DIR from project properties")
        File(property)
    } else {
        project.logger.debug("Using $PROPERTY_KEY_ROOT_DIR from extension")
        File(extension.snapshotRootDir.get().toString())
    }
    // FIXME seems necessary because working dir differs between gradle tasks and test runs but is it a good idea?
    return if (dir.isAbsolute) {
        dir
    } else {
        project.projectDir.resolve(dir)
    }
}

private fun getSnapshotAction(project: Project, extension: SlapshotPluginExtension): SnapshotAction {
    val propertySnapshotAction = when (project.findProperty(PROPERTY_KEY_ACTION)) {
        ACTION_VALUE_COMPARE_ONLY -> SnapshotAction.CompareOnly
        ACTION_VALUE_COMPARE_AND_ADD -> SnapshotAction.CompareAndAdd
        ACTION_VALUE_OVERWRITE -> SnapshotAction.Overwrite
        else -> null
    }
    if (propertySnapshotAction != null) {
        project.logger.debug("Using $PROPERTY_KEY_ACTION from project properties")
        return propertySnapshotAction
    }
    project.logger.debug("Using $PROPERTY_KEY_ACTION from extension")
    return extension.snapshotAction.get()
}
