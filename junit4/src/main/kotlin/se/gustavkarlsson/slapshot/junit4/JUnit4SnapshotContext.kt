package se.gustavkarlsson.slapshot.junit4

import org.junit.Assert
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import se.gustavkarlsson.slapshot.core.DefaultSnapshotter
import se.gustavkarlsson.slapshot.core.SnapshotAction
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import se.gustavkarlsson.slapshot.core.SnapshotFormat
import se.gustavkarlsson.slapshot.core.Snapshotter
import se.gustavkarlsson.slapshot.core.getAction
import se.gustavkarlsson.slapshot.core.getDefaultRootDirectory
import java.nio.file.Path

/**
 * Snapshot context implementation for JUnit 4.
 *
 * Add as a test rule to get started.
 * The context can then be used to create [Snapshotter] instances that are used in tests.
 *
 * Example:
 * ```
 * class MyTests {
 *     @get:Rule
 *     val snapshotContext = JUnit4SnapshotContext()
 *     val snapshotter = snapshotContext.createSnapshotter(StringFormat())
 *
 *     @Test
 *     fun `test string`() {
 *         val result = "foo" + "bar"
 *         snapshotter.snapshot(result)
 *     }
 * }
 * ```
 */
public class JUnit4SnapshotContext : SnapshotContext<Description>, TestWatcher() {
    private var description: Description? = null

    override fun starting(description: Description) {
        this.description = description
    }

    override fun finished(description: Description) {
        this.description = null
    }

    override fun <T, F : SnapshotFormat<T>> createSnapshotter(
        format: F,
        overrideRootDirectory: Path?,
        overrideSnapshotFileResolver: SnapshotFileResolver<Description>?,
        overrideAction: SnapshotAction?,
    ): Snapshotter<T> =
        DefaultSnapshotter(
            snapshotFileResolver = overrideSnapshotFileResolver ?: JUnit4SnapshotFileResolver,
            rootDirectory = overrideRootDirectory ?: getDefaultRootDirectory(),
            getTestInfo = {
                requireNotNull(description) { "Description not set. Test may not have been started yet." }
            },
            format = format,
            action = overrideAction ?: getAction(),
            onFail = Assert::fail,
        )
}
