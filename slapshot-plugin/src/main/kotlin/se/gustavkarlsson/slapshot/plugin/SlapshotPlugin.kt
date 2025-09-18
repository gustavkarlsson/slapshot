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
import java.util.Properties

private const val EXTENSION_NAME = "slapshot"
private const val DEFAULT_SNAPSHOT_ROOT_DIR_NAME = "snapshots"
private const val PROPERTY_KEY_SNAPSHOT_ROOT_DIR = "snapshotRootDir"
private const val PROPERTY_KEY_SNAPSHOT_ACTION = "snapshotAction"
private const val ACTION_VALUE_COMPARE_ONLY = "compareOnly"
private const val ACTION_VALUE_COMPARE_AND_ADD = "compareAndAdd"
private const val ACTION_VALUE_OVERWRITE = "overwrite"

/**
 * Gradle plugin for configuring and managing snapshot testing using Slapshot.
 *
 * This plugin integrates with a project's test lifecycle, allowing for automated handling
 * of snapshot generation, comparison, and validation. It relies on a test framework,
 * such as JUnit4 or JUnit5, as well as a snapshot handling action defined via the plugin's configuration.
 */
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
        snapshotAction.convention(SnapshotAction.CompareAndAdd)
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

private fun Project.addDependencies(testFramework: Property<TestFramework>) {
    val releaseVersion = readReleaseVersion()
    dependencies {
        add("implementation", platform("se.gustavkarlsson.slapshot:slapshot-bom:$releaseVersion"))
        add("testImplementation", "se.gustavkarlsson.slapshot:slapshot-core") // Version from bom
        // Add this lazily, so the framework can be overridden before the dependency is added.
        val testFrameworkDependency =
            testFramework.map { testFramework ->
                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                when (testFramework) {
                    TestFramework.JUnit4 -> "se.gustavkarlsson.slapshot:slapshot-junit4" // Version from bom
                    TestFramework.JUnit5 -> "se.gustavkarlsson.slapshot:slapshot-junit5" // Version from bom
                }
            }
        addProvider("testImplementation", testFrameworkDependency)
    }
}

private fun readReleaseVersion(): String =
    Thread.currentThread().contextClassLoader.getResourceAsStream("plugin.properties").use { resourceStream ->
        Properties().run {
            load(resourceStream)
            getProperty("releaseVersion") as String
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
    val snapshotAction = getSnapshotAction(extension)
    clearSnapshotsTask.configure {
        delete(snapshotRootDir)
    }
    tasks.withType<Test> {
        mustRunAfter(clearSnapshotsTask) // Only applicable if clearSnapshots DOES run
        inputs.files(fileTree(snapshotRootDir))
        inputs.property(PROPERTY_KEY_SNAPSHOT_ACTION, snapshotAction)
        outputs.files(fileTree(snapshotRootDir))

        logger.info("Setting $PROPERTY_KEY_SNAPSHOT_ROOT_DIR system property to $snapshotRootDir")
        systemProperty(PROPERTY_KEY_SNAPSHOT_ROOT_DIR, snapshotRootDir)
        logger.info("Setting $PROPERTY_KEY_SNAPSHOT_ACTION system property to ${snapshotAction.systemProperty}")
        systemProperty(PROPERTY_KEY_SNAPSHOT_ACTION, snapshotAction.systemProperty)
    }
}

private fun Project.getSnapshotRootDir(extension: SlapshotPluginExtension): File {
    val property = findProperty(PROPERTY_KEY_SNAPSHOT_ROOT_DIR)?.toString()
    return if (property != null) {
        logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ROOT_DIR from project properties")
        File(property)
    } else {
        logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ROOT_DIR from extension")
        file(extension.snapshotRootDir.get())
    }
}

private fun Project.getSnapshotAction(extension: SlapshotPluginExtension): SnapshotAction {
    val property =
        when (findProperty(PROPERTY_KEY_SNAPSHOT_ACTION)) {
            ACTION_VALUE_COMPARE_ONLY -> SnapshotAction.CompareOnly
            ACTION_VALUE_COMPARE_AND_ADD -> SnapshotAction.CompareAndAdd
            ACTION_VALUE_OVERWRITE -> SnapshotAction.Overwrite
            else -> null
        }
    if (property != null) {
        logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ACTION from project properties")
        return property
    }
    logger.debug("Using $PROPERTY_KEY_SNAPSHOT_ACTION from extension")
    return extension.snapshotAction.get()
}
