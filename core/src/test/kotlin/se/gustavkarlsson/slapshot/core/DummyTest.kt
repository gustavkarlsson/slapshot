package se.gustavkarlsson.slapshot.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.core.configs.IntSnapshot
import se.gustavkarlsson.slapshot.core.configs.JsonSnapshot
import se.gustavkarlsson.slapshot.core.configs.StringSnapshot

@ExtendWith(SlapshotJunitExtension::class)
class DummyTest {
    @Test
    fun `i am a test1`(slapshot: Slapshot<String, StringSnapshot>) {
        slapshot.snapshot("I am data2")
    }

    @Test
    fun `i am a test2`(slapshot: Slapshot<Int, IntSnapshot>) {
        slapshot.snapshot(5)
    }

    @Test
    fun `i am a test3`(slapshot: Slapshot<String, JsonSnapshot>) {
        val json = """
            {
              "num": 5.0,
              "obj": {
                "o": [true, "str"],
                "b": [true, "sr"]
              }
            }
        """.trimIndent()
        slapshot.snapshot(json)
    }
}