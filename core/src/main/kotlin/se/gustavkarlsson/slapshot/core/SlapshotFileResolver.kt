package se.gustavkarlsson.slapshot.core

import org.junit.jupiter.api.TestInfo
import java.nio.file.Path

fun interface SnapshotFileResolver {
    fun resolve(rootDirectory: Path, fileExtension: String, testInfo: TestInfo): Path
}

internal class DefaultSnapshotFileResolver : SnapshotFileResolver {
    override fun resolve(rootDirectory: Path, fileExtension: String, testInfo: TestInfo): Path {
        val directory = getDirectory(rootDirectory, testInfo)
        val fileName = testInfo.displayName + fileExtension
        return directory.resolve(fileName)
    }
}

private fun getDirectory(root: Path, testInfo: TestInfo): Path {
    return testInfo.testClass
        .map { clazz ->
            val packageNamePath = clazz.name.replace('.', '/')
            root.resolve(packageNamePath)
        }
        .orElse(root)
}
