package se.gustavkarlsson.slapshot.images

import strikt.api.Assertion
import strikt.assertions.isEqualTo
import java.awt.image.BufferedImage

internal fun BufferedImage.pixels(): List<Color> {
    return getRGB(0, 0, this.width, this.height, null, 0, this.width).map { Color(it) }
}

internal fun Assertion.Builder<BufferedImage>.hasSamePixelsAs(other: BufferedImage): Assertion.Builder<BufferedImage> {
    return and {
        get("pixels") { pixels() }.isEqualTo(other.pixels())
    }
}

internal fun createImage(
    width: Int = 30,
    height: Int = 20,
    type: Int = BufferedImage.TYPE_INT_RGB,
    fillColor: Color = Color.Red,
): BufferedImage {
    return BufferedImage(width, height, type).fill(fillColor)
}

internal fun BufferedImage.fill(color: Color = Color.Red): BufferedImage {
    for (x in 0 until width) {
        for (y in 0 until height) {
            setPixel(x, y, color)
        }
    }
    return this
}
