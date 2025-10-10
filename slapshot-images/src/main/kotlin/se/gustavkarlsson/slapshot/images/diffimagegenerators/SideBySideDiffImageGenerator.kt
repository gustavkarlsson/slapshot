package se.gustavkarlsson.slapshot.images.diffimagegenerators

import se.gustavkarlsson.slapshot.images.DiffImageGenerator
import se.gustavkarlsson.slapshot.images.DiffMatrix
import java.awt.image.BufferedImage

// FIXME test
internal data object SideBySideDiffImageGenerator : DiffImageGenerator {
    override fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage {
        val diffImage =
            BufferedImage(
                actual.width + expected.width,
                actual.height,
                BufferedImage.TYPE_INT_ARGB,
            )
        val graphics = diffImage.graphics
        try {
            graphics.drawImage(expected, 0, 0, null)
            graphics.drawImage(actual, expected.width, 0, null)
        } finally {
            graphics.dispose()
        }
        return diffImage
    }
}
