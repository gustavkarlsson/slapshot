package se.gustavkarlsson.slapshot.images

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ColorTest {
    @Test
    fun `constructor with packed value creates color correctly`() {
        val packedValue = 0x12345678
        val color = Color(packedValue)

        expectThat(color) {
            get { packedValue }.isEqualTo(packedValue)
            get { alpha }.isEqualTo(0x12)
            get { red }.isEqualTo(0x34)
            get { green }.isEqualTo(0x56)
            get { blue }.isEqualTo(0x78)
        }
    }

    @Test
    fun `constructor with int components creates color correctly`() {
        val color = Color(alpha = 255, red = 128, green = 64, blue = 0)

        expectThat(color) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(128)
            get { green }.isEqualTo(64)
            get { blue }.isEqualTo(0)
            get { packedValue }.isEqualTo(0xFF804000.toInt())
        }
    }

    @Test
    fun `constructor with int components - minimum values`() {
        val color = Color(alpha = 0, red = 0, green = 0, blue = 0)

        expectThat(color) {
            get { alpha }.isEqualTo(0)
            get { red }.isEqualTo(0)
            get { green }.isEqualTo(0)
            get { blue }.isEqualTo(0)
            get { packedValue }.isEqualTo(0)
        }
    }

    @Test
    fun `constructor with int components - maximum values`() {
        val color = Color(alpha = 255, red = 255, green = 255, blue = 255)

        expectThat(color) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(255)
            get { green }.isEqualTo(255)
            get { blue }.isEqualTo(255)
            get { packedValue }.isEqualTo(-1) // 0xFFFFFFFF as signed int
        }
    }

    @Test
    fun `constructor with int components - invalid values throw exceptions`() {
        // Invalid alpha values
        assertThrows<IllegalArgumentException> {
            Color(alpha = -1, red = 128, green = 64, blue = 32)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 256, red = 128, green = 64, blue = 32)
        }

        // Invalid red values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = -1, green = 64, blue = 32)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = 256, green = 64, blue = 32)
        }

        // Invalid green values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = 128, green = -1, blue = 32)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = 128, green = 256, blue = 32)
        }

        // Invalid blue values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = 128, green = 64, blue = -1)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 255, red = 128, green = 64, blue = 256)
        }
    }

    @Test
    fun `constructor with double components creates color correctly`() {
        val color = Color(alpha = 1.0, red = 0.5, green = 0.25, blue = 0.0)

        expectThat(color) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(127) // 0.5 * 255 = 127.5 -> 127
            get { green }.isEqualTo(63) // 0.25 * 255 = 63.75 -> 63
            get { blue }.isEqualTo(0)
        }
    }

    @Test
    fun `constructor with double components - minimum values`() {
        val color = Color(alpha = 0.0, red = 0.0, green = 0.0, blue = 0.0)

        expectThat(color) {
            get { alpha }.isEqualTo(0)
            get { red }.isEqualTo(0)
            get { green }.isEqualTo(0)
            get { blue }.isEqualTo(0)
            get { packedValue }.isEqualTo(0)
        }
    }

    @Test
    fun `constructor with double components - maximum values`() {
        val color = Color(alpha = 1.0, red = 1.0, green = 1.0, blue = 1.0)

        expectThat(color) {
            get { alphaDouble }.isEqualTo(1.0)
            get { redDouble }.isEqualTo(1.0)
            get { greenDouble }.isEqualTo(1.0)
            get { blueDouble }.isEqualTo(1.0)
            get { packedValue }.isEqualTo(-1) // 0xFFFFFFFF as signed int
        }
    }

    @Test
    fun `constructor with double components - special values very small`() {
        val color = Color(alpha = 0.001, red = 0.001, green = 0.001, blue = 0.001)

        expectThat(color.alpha).isEqualTo(0) // 0.001 * 255 = 0.255 -> 0
    }

    @Test
    fun `constructor with double components - special values close to one`() {
        val color = Color(alpha = 0.999, red = 0.999, green = 0.999, blue = 0.999)

        expectThat(color.alpha).isEqualTo(254) // 0.999 * 255 = 254.745 -> 254
    }

    @Test
    fun `constructor with double components - invalid values throw exceptions`() {
        assertThrows<IllegalArgumentException> {
            Color(alpha = -0.1, red = 0.5, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.1, red = 0.5, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = Double.NEGATIVE_INFINITY, red = 0.5, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = Double.POSITIVE_INFINITY, red = 0.5, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = Double.NaN, red = 0.5, green = 0.25, blue = 0.0)
        }

        // Invalid red values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = -0.1, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 1.1, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = Double.NEGATIVE_INFINITY, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = Double.POSITIVE_INFINITY, green = 0.25, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = Double.NaN, green = 0.25, blue = 0.0)
        }

        // Invalid green values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = -0.1, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 1.1, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = Double.NEGATIVE_INFINITY, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = Double.POSITIVE_INFINITY, blue = 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = Double.NaN, blue = 0.0)
        }

        // Invalid blue values
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 0.25, blue = -0.1)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 0.25, blue = 1.1)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 0.25, blue = Double.NEGATIVE_INFINITY)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 0.25, blue = Double.POSITIVE_INFINITY)
        }
        assertThrows<IllegalArgumentException> {
            Color(alpha = 1.0, red = 0.5, green = 0.25, blue = Double.NaN)
        }
    }

    @Test
    fun `double properties return correct ratios`() {
        val color = Color(alpha = 255, red = 128, green = 64, blue = 0)

        expectThat(color) {
            get { alphaDouble }.isEqualTo(1.0)
            get { redDouble }.isEqualTo(0.5, tolerance = 0.01)
            get { greenDouble }.isEqualTo(0.25, tolerance = 0.01)
            get { blueDouble }.isEqualTo(0.0)
        }
    }

    @Test
    fun `double properties with minimum values`() {
        val color = Color(alpha = 0, red = 0, green = 0, blue = 0)

        expectThat(color) {
            get { alphaDouble }.isEqualTo(0.0)
            get { redDouble }.isEqualTo(0.0)
            get { greenDouble }.isEqualTo(0.0)
            get { blueDouble }.isEqualTo(0.0)
        }
    }

    @Test
    fun `double properties with maximum values`() {
        val color = Color(alpha = 255, red = 255, green = 255, blue = 255)

        expectThat(color) {
            get { alphaDouble }.isEqualTo(1.0)
            get { redDouble }.isEqualTo(1.0)
            get { greenDouble }.isEqualTo(1.0)
            get { blueDouble }.isEqualTo(1.0)
        }
    }

    @Test
    fun `copy function with int parameters - no changes`() {
        val original = Color(alpha = 255, red = 128, green = 64, blue = 32)
        val copy =
            original.copy(alpha = original.alpha, red = original.red, green = original.green, blue = original.blue)

        expectThat(copy) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(128)
            get { green }.isEqualTo(64)
            get { blue }.isEqualTo(32)
        }
    }

    @Test
    fun `copy function with int parameters - alpha change`() {
        val original = Color(alpha = 255, red = 128, green = 64, blue = 32)
        val copy = original.copy(alpha = 200)

        expectThat(copy) {
            get { alpha }.isEqualTo(200)
            get { red }.isEqualTo(128)
            get { green }.isEqualTo(64)
            get { blue }.isEqualTo(32)
        }
    }

    @Test
    fun `copy function with int parameters - multiple changes`() {
        val original = Color(alpha = 255, red = 128, green = 64, blue = 32)
        val copy = original.copy(red = 100, blue = 50)

        expectThat(copy) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(100)
            get { green }.isEqualTo(64)
            get { blue }.isEqualTo(50)
        }
    }

    @Test
    fun `copy function with double parameters - no changes`() {
        val original = Color(alpha = 1.0, red = 0.5, green = 0.25, blue = 0.125)
        val copy =
            original.copy(
                alpha = original.alphaDouble,
                red = original.redDouble,
                green = original.greenDouble,
                blue = original.blueDouble,
            )

        expectThat(copy) {
            get { alphaDouble }.isEqualTo(original.alphaDouble)
            get { redDouble }.isEqualTo(original.redDouble)
            get { greenDouble }.isEqualTo(original.greenDouble)
            get { blueDouble }.isEqualTo(original.blueDouble)
        }
    }

    @Test
    fun `copy function with double parameters - alpha change`() {
        val original = Color(alpha = 1.0, red = 0.5, green = 0.25, blue = 0.125)
        val copy = original.copy(alpha = 0.8)

        expectThat(copy) {
            get { alphaDouble }.isEqualTo(0.8, tolerance = 0.01)
            get { red }.isEqualTo(original.red)
            get { green }.isEqualTo(original.green)
            get { blue }.isEqualTo(original.blue)
        }
    }

    @Test
    fun `copy function with double parameters - multiple changes`() {
        val original = Color(alpha = 1.0, red = 0.5, green = 0.25, blue = 0.125)
        val copy = original.copy(red = 0.75, blue = 0.9)

        expectThat(copy) {
            get { alpha }.isEqualTo(original.alpha)
            get { redDouble }.isEqualTo(0.75, tolerance = 0.01)
            get { green }.isEqualTo(original.green)
            get { blueDouble }.isEqualTo(0.9, tolerance = 0.01)
        }
    }

    @Test
    fun `bit operations with alternating pattern`() {
        val color = Color(0xAA55AA55.toInt())

        expectThat(color) {
            get { alpha }.isEqualTo(0xAA)
            get { red }.isEqualTo(0x55)
            get { green }.isEqualTo(0xAA)
            get { blue }.isEqualTo(0x55)
        }
    }

    @Test
    fun `bit operations with all bits set`() {
        val color = Color(0xFFFFFFFF.toInt())

        expectThat(color) {
            get { alpha }.isEqualTo(255)
            get { red }.isEqualTo(255)
            get { green }.isEqualTo(255)
            get { blue }.isEqualTo(255)
        }
    }
}
