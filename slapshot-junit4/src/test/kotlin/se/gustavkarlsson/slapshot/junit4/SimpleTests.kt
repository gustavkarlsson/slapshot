package se.gustavkarlsson.slapshot.junit4

import org.junit.Rule
import org.junit.Test
import se.gustavkarlsson.slapshot.core.serializers.BooleanSerializer
import se.gustavkarlsson.slapshot.core.serializers.DoubleSerializer
import se.gustavkarlsson.slapshot.core.serializers.JsonSerializer
import se.gustavkarlsson.slapshot.core.serializers.LongSerializer
import se.gustavkarlsson.slapshot.core.serializers.StringSerializer
import se.gustavkarlsson.slapshot.core.testers.DoubleWithToleranceTester
import se.gustavkarlsson.slapshot.core.testers.JsonTester
import se.gustavkarlsson.slapshot.core.testers.LongStringTester

class SimpleTests {
    @get:Rule
    val snapshotContext = JUnit4SnapshotContext()

    @Test
    fun `test string`() {
        snapshotContext.createSnapshotter(StringSerializer()).snapshot("I am data")
    }

    @Test
    fun `test boolean`() {
        snapshotContext.createSnapshotter(BooleanSerializer).snapshot(true)
    }

    @Test
    fun `test long`() {
        snapshotContext.createSnapshotter(LongSerializer).snapshot(5)
    }

    @Test
    fun `test double`() {
        snapshotContext.createSnapshotter(DoubleSerializer).snapshot(5.7)
    }

    @Test
    fun `test double with tolerance`() {
        snapshotContext.createSnapshotter(DoubleSerializer, DoubleWithToleranceTester(tolerance = 1.0)).snapshot(12.3)
    }

    @Test
    fun `test json`() {
        val json =
            """
            {
              "num": 5.0,
              "obj": {
                "o": [true, "str"],
                "b": [true, "sr"]
              },
              "newNull": null
            }
            """.trimIndent()
        snapshotContext.createSnapshotter(JsonSerializer, JsonTester).snapshot(json)
    }

    @Test
    fun `test long string`() {
        val json =
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin malesuada et dui et egestas.
            Praesent eu lectus quam. Maecenas facilisis commodo justo et placerat. Vivamus maximus vehicula dolor,
            quis interdum sem ultricies at. Sed eu odio eget nisl accumsan condimentum id eget elit.
            Nulla quis interdum nisi, id venenatis sem. Praesent orci nisl, malesuada convallis sapien in,
            tincidunt tristique lectus. Nam sit amet urna tincidunt ante pellentesque luctus.
            Vivamus lobortis malesuada lorem, at consectetur urna. Sed in magna ac neque eleifend consectetur.
            Donec ullamcorper erat velit, eget aliquet enim egestas non.
            """.trimIndent()
        snapshotContext.createSnapshotter(StringSerializer(), LongStringTester()).snapshot(json)
    }
}
