package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import se.gustavkarlsson.slapshot.core.SnapshotContext
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.Optional

class SnapshotSupport : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        if (parameterContext.parameter.type == JUnit5SnapshotContext::class.java) {
            return true
        } else {
            val type = parameterContext.parameter.parameterizedType as? ParameterizedType
            if (type?.rawType != SnapshotContext::class.java) return false
            if (type.actualTypeArguments.first() != TestInfo::class.java) return false
            return true
        }
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        require(extensionContext.testInstance.isPresent) {
            "Slapshot most be declared for a test instance such as a @Test or @BeforeEach method"
        }
        return JUnit5SnapshotContext(extensionContext.toTestInfo())
    }
}

private fun ExtensionContext.toTestInfo() = ExtensionContextTestInfo(displayName, tags, testClass, testMethod)

private class ExtensionContextTestInfo(
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
