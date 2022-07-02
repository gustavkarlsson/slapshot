package se.gustavkarlsson.slapshot.core

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.fail
import java.lang.reflect.Method
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

interface Slapshot<T, S : SnapshotConfig<T>> {
    var snapshotFileResolver: SnapshotFileResolver
    fun snapshot(data: T)
}

internal class DefaultSlapshot<T, S : SnapshotConfig<T>>(
    override var snapshotFileResolver: SnapshotFileResolver,
    private val rootDirectory: Path,
    private val context: ExtensionContext,
    val config: S,
) : Slapshot<T, S> {
    override fun snapshot(data: T) {
        val file = snapshotFileResolver.resolve(rootDirectory, config.fileExtension, context.toTestInfo())
        if (file.exists()) {
            compareSnapshot(file, data)
        } else {
            writeSnapshot(data, file)
        }
    }

    private fun compareSnapshot(file: Path, data: T) {
        val bytes = file.readBytes()
        val old = config.deserialize(bytes)
        val diff = config.test(old, data)
        if (diff != null) {
            fail(diff)
        }
    }

    private fun writeSnapshot(data: T, file: Path) {
        val bytes = config.serialize(data)
        file.parent.createDirectories()
        file.writeBytes(bytes)
    }
}

private fun ExtensionContext.toTestInfo(): TestInfo {
    return SnapshotTestInfo(displayName, tags, testClass, testMethod)
}

private class SnapshotTestInfo(
    private val displayName: String,
    private val tags: Set<String>,
    private val testClass: Optional<Class<*>>,
    private val testMethod: Optional<Method>,
) : TestInfo {
    override fun getDisplayName(): String = displayName
    override fun getTags(): Set<String> = tags
    override fun getTestClass(): Optional<Class<*>> = testClass
    override fun getTestMethod(): Optional<Method> = testMethod
}