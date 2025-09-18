package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Configuration for the Slapshot Gradle plugin.
 */
public interface SlapshotPluginExtension {
    /**
     * Specifies the root directory for storing snapshot files.
     *
     * This property determines where snapshot files are located within the project directory structure.
     * By default, the root directory is derived from the project's test source set, if available.
     * If no test source set is configured, a fallback directory in the project root is used.
     *
     * File resolution works like [Project.file].
     */
    public val snapshotRootDir: Property<Any>

    /**
     * Specifies the default behavior for handling snapshots during testing.
     *
     * The default value is `CompareAndAdd`, which ensures tests are validated and missing snapshots
     * are created as needed, but this can be overridden to suit specific requirements.
     */
    public val snapshotAction: Property<SnapshotAction>

    /**
     * Specifies the test framework to be used for the Slapshot Gradle plugin.
     *
     * Select the one that matches your project configuration.
     */
    public val testFramework: Property<TestFramework>
}
