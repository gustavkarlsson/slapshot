package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import se.gustavkarlsson.slapshot.core.SnapshotContext
import se.gustavkarlsson.slapshot.core.Snapshotter
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.util.Optional

/**
 * Extension used to enable snapshot testing in JUnit 5 Jupiter.
 *
 * Extend a test class with this and inject a [JUnit5SnapshotContext] to get started.
 * The context can then be used to create [Snapshotter] instances that are used in tests.
 *
 * Example:
 *  ```
 * @ExtendWith(SnapshotExtension::class)
 * class MyTests {
 *     private lateinit var snapshotter: Snapshotter<String>
 *
 *     @BeforeEach
 *     fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
 *         val serializer = StringSerializer()
 *         snapshotter = snapshotContext.createSnapshotter(serializer)
 *     }
 *
 *     @Test
 *     fun `test string`() {
 *         val result = "foo" + "bar"
 *         snapshotter.snapshot(result)
 *     }
 * }
 *  ```
 */
public class SnapshotExtension : ParameterResolver {
    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean {
        return when {
            isJUnit5SnapshotContext(parameterContext.parameter) -> true
            isValidSnapshotContext(parameterContext.parameter) -> true
            else -> false
        }
    }

    private fun isJUnit5SnapshotContext(parameter: Parameter): Boolean {
        return parameter.type == JUnit5SnapshotContext::class.java
    }

    private fun isValidSnapshotContext(parameter: Parameter): Boolean {
        val type = parameter.parameterizedType as? ParameterizedType ?: return false
        return type.rawType == SnapshotContext::class.java && type.actualTypeArguments.first() != TestInfo::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any {
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
