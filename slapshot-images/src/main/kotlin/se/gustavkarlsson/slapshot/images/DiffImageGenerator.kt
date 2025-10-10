package se.gustavkarlsson.slapshot.images

import se.gustavkarlsson.slapshot.images.diffimagegenerators.DiffsOnlyDiffImageGenerator
import se.gustavkarlsson.slapshot.images.diffimagegenerators.HighlightDiffImageGenerator
import se.gustavkarlsson.slapshot.images.diffimagegenerators.SideBySideDiffImageGenerator
import java.awt.image.BufferedImage

// FIXME document
public fun interface DiffImageGenerator {
    public fun generateDiffImage(
        actual: BufferedImage,
        expected: BufferedImage,
        diffMatrix: DiffMatrix,
    ): BufferedImage

    public companion object {
        // FIXME document
        @Suppress("FunctionName")
        public fun Highlight(color: Color = Color(255, 255, 0, 255)): DiffImageGenerator {
            return HighlightDiffImageGenerator(color)
        }

        // FIXME document
        public val SideBySide: DiffImageGenerator = SideBySideDiffImageGenerator

        // FIXME document
        public val DiffsOnly: DiffImageGenerator = DiffsOnlyDiffImageGenerator
    }
}
