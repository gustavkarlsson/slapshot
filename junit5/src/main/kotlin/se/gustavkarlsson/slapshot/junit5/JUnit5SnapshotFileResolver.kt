package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.TestInfo
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import java.nio.file.Path

internal object JUnit5SnapshotFileResolver : SnapshotFileResolver<TestInfo> {
    override fun resolve(
        rootDirectory: Path,
        testInfo: TestInfo,
        fileExtension: String,
    ): Path {
        val directory = getDirectory(rootDirectory, testInfo)
        val fileName = "${testInfo.displayName}.$fileExtension"
        return directory.resolve(fileName)
    }
}

private fun getDirectory(
    root: Path,
    testInfo: TestInfo,
): Path {
    val packageNamePath = testInfo.testClass.get().name.replace('.', '/')
    return root.resolve(packageNamePath)
}
