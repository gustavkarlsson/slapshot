package se.gustavkarlsson.slapshot.core

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.lang.reflect.ParameterizedType
import java.nio.file.Path
import java.nio.file.Paths

class SlapshotJunitExtension : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == Slapshot::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val configClass = parameterContext.snapshotConfigClass()
        val config = checkNotNull(configClass.getConstructor().newInstance())
        return DefaultSlapshot(
            snapshotFileResolver = DefaultSnapshotFileResolver(),
            rootDirectory = getDefaultRootDirectory(),
            context = extensionContext,
            config = config,
        )
    }
}

private fun ParameterContext.snapshotConfigClass(): Class<SnapshotConfig<*>> {
    return (parameter.parameterizedType as ParameterizedType).actualTypeArguments[1] as Class<SnapshotConfig<*>>
}

private fun getDefaultRootDirectory(): Path {
    val dirString = System.getProperty("snapshotRootDir") ?: "snapshots"
    return Paths.get(dirString)
}
