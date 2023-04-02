package se.gustavkarlsson.slapshot.plugin

import org.gradle.api.provider.Property

interface SlapshotPluginExtension {
    val snapshotRootDir: Property<Any>
    val snapshotSnapshotAction: Property<SnapshotAction>
    val testFramework: Property<TestFramework>
}
