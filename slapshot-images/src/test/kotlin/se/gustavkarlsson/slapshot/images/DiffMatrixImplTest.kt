package se.gustavkarlsson.slapshot.images

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DiffMatrixImplTest {
    @Test
    fun `constructor with valid data creates matrix correctly`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val matrix = DiffMatrixImpl(width = 2, height = 2, data = data)

        expectThat(matrix) {
            get { width }.isEqualTo(2)
            get { height }.isEqualTo(2)
        }
    }

    @Test
    fun `constructor with single element creates matrix correctly`() {
        val data = doubleArrayOf(5.5)
        val matrix = DiffMatrixImpl(width = 1, height = 1, data = data)

        expectThat(matrix) {
            get { width }.isEqualTo(1)
            get { height }.isEqualTo(1)
        }
    }

    @Test
    fun `constructor with zero width height throws exception`() {
        assertThrows<IllegalArgumentException> {
            DiffMatrixImpl(width = 0, height = 0, data = doubleArrayOf())
        }
    }

    @Test
    fun `constructor with zero width and non-zero height throws exception`() {
        assertThrows<IllegalArgumentException> {
            DiffMatrixImpl(width = 0, height = 3, data = doubleArrayOf())
        }
    }

    @Test
    fun `constructor with non-zero width and zero height throws exception`() {
        assertThrows<IllegalArgumentException> {
            DiffMatrixImpl(width = 3, height = 0, data = doubleArrayOf())
        }
    }

    @Test
    fun `constructor with data size too small throws exception`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0)

        assertThrows<IllegalArgumentException> {
            DiffMatrixImpl(width = 2, height = 2, data = data)
        }
    }

    @Test
    fun `constructor with data size too large throws exception`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0)

        assertThrows<IllegalArgumentException> {
            DiffMatrixImpl(width = 2, height = 2, data = data)
        }
    }

    @Test
    fun `get operator returns correct values for all corners`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val matrix = DiffMatrixImpl(width = 2, height = 2, data = data)

        expectThat(matrix) {
            get { get(0, 0) }.isEqualTo(1.0) // top-left
            get { get(1, 0) }.isEqualTo(2.0) // top-right
            get { get(0, 1) }.isEqualTo(3.0) // bottom-left
            get { get(1, 1) }.isEqualTo(4.0) // bottom-right
        }
    }

    @Test
    fun `get operator works correctly with rectangular matrix`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        val matrix = DiffMatrixImpl(width = 3, height = 2, data = data)

        expectThat(matrix[2, 1]).isEqualTo(6.0)
    }

    @Test
    fun `get operator works correctly with single element matrix`() {
        val data = doubleArrayOf(42.5)
        val matrix = DiffMatrixImpl(width = 1, height = 1, data = data)

        expectThat(matrix[0, 0]).isEqualTo(42.5)
    }

    @Test
    fun `get operator works with large matrix values`() {
        val data =
            doubleArrayOf(
                1.1, 1.2, 1.3,
                2.1, 2.2, 2.3,
                3.1, 3.2, 3.3,
            )
        val matrix = DiffMatrixImpl(width = 3, height = 3, data = data)

        expectThat(matrix[1, 2]).isEqualTo(3.2)
    }

    @Test
    fun `get operator throws IndexOutOfBoundsException for invalid indices`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val matrix = DiffMatrixImpl(width = 2, height = 2, data = data)

        // Test negative x coordinate
        assertThrows<IndexOutOfBoundsException> {
            matrix[-1, 0]
        }

        // Test negative y coordinate
        assertThrows<IndexOutOfBoundsException> {
            matrix[0, -1]
        }

        // Test x coordinate >= width
        assertThrows<IndexOutOfBoundsException> {
            matrix[2, 0]
        }

        // Test y coordinate >= height
        assertThrows<IndexOutOfBoundsException> {
            matrix[0, 2]
        }
    }

    @Test
    fun `sequence returns correct values for rectangular matrix`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        val matrix = DiffMatrixImpl(width = 3, height = 2, data = data)

        val sequence = matrix.sequence().toList()

        expectThat(sequence).isEqualTo(listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0))
    }

    @Test
    fun `sequence is lazy and can be iterated multiple times`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0)
        val matrix = DiffMatrixImpl(width = 3, height = 1, data = data)

        val sequence = matrix.sequence()
        val firstIteration = sequence.toList()
        val secondIteration = sequence.toList()

        expectThat(firstIteration).isEqualTo(listOf(1.0, 2.0, 3.0))
        expectThat(secondIteration).isEqualTo(listOf(1.0, 2.0, 3.0))
    }

    @Test
    fun `implements DiffMatrix interface correctly`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val matrix: DiffMatrix = DiffMatrixImpl(width = 2, height = 2, data = data)

        expectThat(matrix) {
            get { width }.isEqualTo(2)
            get { height }.isEqualTo(2)
            get { get(0, 0) }.isEqualTo(1.0)
            get { get(1, 1) }.isEqualTo(4.0)
        }
    }
}
