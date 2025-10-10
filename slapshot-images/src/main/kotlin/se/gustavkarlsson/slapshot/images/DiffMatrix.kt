package se.gustavkarlsson.slapshot.images

// FIXME document
public interface DiffMatrix {
    public val width: Int
    public val height: Int

    public operator fun get(
        x: Int,
        y: Int,
    ): Double

    public fun sequence(): Sequence<Double>
}

internal class DiffMatrixImpl(
    override val width: Int,
    override val height: Int,
    /**
     * Pixel data in row-major order.
     */
    private val data: DoubleArray,
) : DiffMatrix {
    init {
        require(width * height > 0) { "Dimensions must be > 0 (${width}x$height)" }
        require(data.size == width * height) { "Data size (${data.size}) does not match dimensions (${width}x$height)" }
    }

    override operator fun get(
        x: Int,
        y: Int,
    ): Double = data[indexOf(y, x)]

    private fun indexOf(
        y: Int,
        x: Int,
    ): Int {
        if (x !in 0..<width || y !in 0..<height) {
            throw IndexOutOfBoundsException("Index ($x, $y) is out of bounds for matrix of size ${width}x$height")
        }
        return y * width + x
    }

    override fun sequence(): Sequence<Double> = data.asSequence()
}
