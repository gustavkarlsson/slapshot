package se.gustavkarlsson.slapshot.junit5

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import se.gustavkarlsson.slapshot.core.formats.AnyToStringFormat
import se.gustavkarlsson.slapshot.core.formats.BooleanFormat
import se.gustavkarlsson.slapshot.core.formats.DoubleFormat
import se.gustavkarlsson.slapshot.core.formats.ImageFormat
import se.gustavkarlsson.slapshot.core.formats.JsonFormat
import se.gustavkarlsson.slapshot.core.formats.LongFormat
import se.gustavkarlsson.slapshot.core.formats.LongStringFormat
import se.gustavkarlsson.slapshot.core.formats.StringFormat
import javax.imageio.ImageIO

@ExtendWith(SnapshotExtension::class)
class Tests {
    private lateinit var snapshotContext: JUnit5SnapshotContext

    @BeforeEach
    fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
        this.snapshotContext = snapshotContext
    }

    private val jsonFormat = JsonFormat(explicitNulls = false)

    @Test
    fun `test string`() {
        snapshotContext.createSnapshotter(StringFormat(trim = true)).snapshot("I am data")
    }

    @Test
    fun `test boolean`() {
        snapshotContext.createSnapshotter(BooleanFormat()).snapshot(true)
    }

    @Test
    fun `test long`() {
        snapshotContext.createSnapshotter(LongFormat()).snapshot(5)
    }

    @Test
    fun `test double`() {
        snapshotContext.createSnapshotter(DoubleFormat()).snapshot(5.7)
    }

    @Test
    fun `test double with tolerance`() {
        snapshotContext.createSnapshotter(DoubleFormat(tolerance = 1.0)).snapshot(12.3)
    }

    @Test
    fun `test any value to string`() {
        snapshotContext.createSnapshotter(AnyToStringFormat()).snapshot(listOf(1, 2, 3))
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
        snapshotContext.createSnapshotter(jsonFormat).snapshot(json)
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
        snapshotContext.createSnapshotter(LongStringFormat()).snapshot(json)
    }

    @Test
    fun `test image`() {
        val image = ImageIO.read(Tests::class.java.classLoader.getResourceAsStream("image.bmp"))
        snapshotContext.createSnapshotter(ImageFormat(tolerance = 0.02)).snapshot(image)
    }

    @ParameterizedTest(name = "parameterized {0}")
    @ValueSource(longs = [1L, 2L])
    fun parameterized(value: Long) {
        snapshotContext.createSnapshotter(LongFormat()).snapshot(value)
    }

    @Nested
    @ExtendWith(SnapshotExtension::class)
    inner class NestedTest {
        private lateinit var snapshotContext: JUnit5SnapshotContext

        @BeforeEach
        fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
            this.snapshotContext = snapshotContext
        }

        @Test
        fun `just a nested class test`() {
            snapshotContext.createSnapshotter(StringFormat()).snapshot("bla")
        }
    }
}
