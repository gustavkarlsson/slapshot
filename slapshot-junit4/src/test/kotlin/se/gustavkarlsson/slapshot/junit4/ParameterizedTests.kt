package se.gustavkarlsson.slapshot.junit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import se.gustavkarlsson.slapshot.core.serializers.LongSerializer

@RunWith(Parameterized::class)
class ParameterizedTests(private val a: Long, private val b: Long) {
    companion object {
        @JvmStatic
        @Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(1L, 1L),
                arrayOf(2L, 2L),
            )
        }
    }

    @get:Rule
    val snapshotContext = JUnit4SnapshotContext()

    @Test
    fun `test addition`() {
        val result = a + b
        snapshotContext.createSnapshotter(LongSerializer).snapshot(result)
    }
}
