package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.provider.Property

public interface SlapshotPluginExtension {
    public val snapshotRootDir: Property<Any>
    public val defaultAction: Property<SnapshotAction>
    public val testFramework: Property<TestFramework>
}
