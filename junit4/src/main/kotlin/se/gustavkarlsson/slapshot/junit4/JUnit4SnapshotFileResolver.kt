package se.gustavkarlsson.slapshot.junit4

import org.junit.runner.Description
import se.gustavkarlsson.slapshot.core.SnapshotFileResolver
import java.nio.file.Path

internal object JUnit4SnapshotFileResolver : SnapshotFileResolver<Description> {
    override fun resolve(
        rootDirectory: Path,
        testInfo: Description,
        fileExtension: String,
    ): Path {
        val directory = getDirectory(rootDirectory, testInfo)
        val fileName = "${testInfo.methodName}.$fileExtension"
        return directory.resolve(fileName)
    }
}

private fun getDirectory(
    root: Path,
    testInfo: Description,
): Path {
    val packageNamePath = testInfo.testClass.name.replace('.', '/')
    return root.resolve(packageNamePath)
}
