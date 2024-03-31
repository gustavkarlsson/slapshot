package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

private const val EXTENSION_NAME = "slapshot"
private const val DEFAULT_SNAPSHOT_ROOT_DIR_NAME = "snapshots"
private const val PROPERTY_KEY_SNAPSHOT_ROOT_DIR = "snapshotRootDir"
private const val PROPERTY_KEY_DEFAULT_ACTION = "defaultAction"
private const val ACTION_VALUE_COMPARE_ONLY = "compareOnly"
private const val ACTION_VALUE_COMPARE_AND_ADD = "compareAndAdd"
private const val ACTION_VALUE_OVERWRITE = "overwrite"

public class SlapshotPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.createExtension()
        project.addDependencies(extension.testFramework)
        val clearSnapshotsTask = project.createClearSnapshotsTask()
        project.afterEvaluate {
            configure(extension, clearSnapshotsTask)
        }
    }
}

private fun Project.createExtension(): SlapshotPluginExtension {
    return extensions.create<SlapshotPluginExtension>(EXTENSION_NAME).apply {
        val defaultSnapshotRootDir = getDefaultSnapshotRootDir()
        logger.info("Setting default snapshot root dir to $defaultSnapshotRootDir")
        testFramework.convention(TestFramework.JUnit5)
        snapshotRootDir.convention(defaultSnapshotRootDir)
        defaultAction.convention(SnapshotAction.CompareAndAdd)
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

// FIXME set correct versions
private fun Project.addDependencies(testFramework: Property<TestFramework>) {
    dependencies {
        add("testImplementation", "se.gustavkarlsson.slapshot:core:1.0-SNAPSHOT")

        // Add this lazily, so the framework can be overridden before the dependency is added.
        val testFrameworkDependency =
            testFramework.map { testFramework ->
                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                when (testFramework) {
                    TestFramework.JUnit4 -> "se.gustavkarlsson.slapshot:junit4:1.0-SNAPSHOT"
                    TestFramework.JUnit5 -> "se.gustavkarlsson.slapshot:junit5:1.0-SNAPSHOT"
                }
            }
        addProvider("testImplementation", testFrameworkDependency)
    }
}

private fun Project.createClearSnapshotsTask(): TaskProvider<Delete> {
    return tasks.register<Delete>("clearSnapshots") {
        group = "verification"
        description = "Deletes all existing snapshots from the project"
    }
}

// TODO Can we check the dependencies for junit 4/5 and set the correct TestFramework?
private fun Project.configure(
    extension: SlapshotPluginExtension,
    clearSnapshotsTask: TaskProvider<Delete>,
) {
    val snapshotRootDir = getSnapshotRootDir(extension)
    val defaultAction = getDefaultAction(extension)
    clearSnapshotsTask.configure {
        delete(snapshotRootDir)
    }
    tasks.withType<Test> {
        mustRunAfter(clearSnapshotsTask) // Only applicable if clearSnapshots DOES run
        inputs.files(fileTree(snapshotRootDir))
        inputs.property(PROPERTY_KEY_DEFAULT_ACTION, defaultAction)
        outputs.files(fileTree(snapshotRootDir))

        logger.info("Setting $PROPERTY_KEY_SNAPSHOT_ROOT_DIR system property to $snapshotRootDir")
        systemProperty(PROPERTY_KEY_SNAPSHOT_ROOT_DIR, snapshotRootDir)
        logger.info("Setting $PROPERTY_KEY_DEFAULT_ACTION system property to $defaultAction")
        systemProperty(PROPERTY_KEY_DEFAULT_ACTION, defaultAction.systemProperty)
    }
}

private fun Project.getSnapshotRootDir(extension: SlapshotPluginExtension): File {
    val property = findProperty(PROPERTY_KEY_SNAPSHOT_ROOT_DIR)?.toString()
    return if (property != null) {
        logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ROOT_DIR from project properties")
        File(property)
    } else {
        logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ROOT_DIR from extension")
        File(extension.snapshotRootDir.get().toString())
    }
}

private fun Project.getDefaultAction(extension: SlapshotPluginExtension): SnapshotAction {
    val property =
        when (findProperty(PROPERTY_KEY_DEFAULT_ACTION)) {
            ACTION_VALUE_COMPARE_ONLY -> SnapshotAction.CompareOnly
            ACTION_VALUE_COMPARE_AND_ADD -> SnapshotAction.CompareAndAdd
            ACTION_VALUE_OVERWRITE -> SnapshotAction.Overwrite
            else -> null
        }
    if (property != null) {
        logger.debug("Using $PROPERTY_KEY_DEFAULT_ACTION from project properties")
        return property
    }
    logger.debug("Using $PROPERTY_KEY_DEFAULT_ACTION from extension")
    return extension.defaultAction.get()
}
