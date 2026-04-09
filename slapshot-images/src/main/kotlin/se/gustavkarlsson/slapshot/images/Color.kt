package se.gustavkarlsson.slapshot.images

/**
 * An ARGB color with 8-bit color depth.
 */
@JvmInline
public value class Color(public val packedValue: Int) {
    public val alpha: Int get() = (packedValue shr 24) and 0xFF
    public val red: Int get() = (packedValue shr 16) and 0xFF
    public val green: Int get() = (packedValue shr 8) and 0xFF
    public val blue: Int get() = (packedValue shr 0) and 0xFF

    public val alphaDouble: Double get() = alpha / 255.0
    public val redDouble: Double get() = red / 255.0
    public val greenDouble: Double get() = green / 255.0
    public val blueDouble: Double get() = blue / 255.0

    /**
     * Creates a new color from byte values (0-255).
     */
    public constructor(alpha: Int, red: Int, green: Int, blue: Int) : this(
        (requireInByteRange(alpha, "alpha") and 0xFF shl 24) or
            (requireInByteRange(red, "red") and 0xFF shl 16) or
            (requireInByteRange(green, "green") and 0xFF shl 8) or
            (requireInByteRange(blue, "blue") and 0xFF shl 0),
    )

    /**
     * Creates a new color from ratio values (0.0-1.0).
     */
    public constructor(alpha: Double, red: Double, green: Double, blue: Double) : this(
        alpha = (requireInRatioRange(alpha, "alpha") * 255).toInt(),
        red = (requireInRatioRange(red, "red") * 255).toInt(),
        green = (requireInRatioRange(green, "green") * 255).toInt(),
        blue = (requireInRatioRange(blue, "blue") * 255).toInt(),
    )

    /**
     * Creates a new color from byte values (0-255).
     */
    public constructor(red: Int, green: Int, blue: Int) : this(255, red, green, blue)

    /**
     * Creates a new color from ratio values (0.0-1.0).
     */
    public constructor(red: Double, green: Double, blue: Double) : this(1.0, red, green, blue)

    public fun copy(
        alpha: Int = this.alpha,
        red: Int = this.red,
        green: Int = this.green,
        blue: Int = this.blue,
    ): Color = Color(alpha, red, green, blue)

    public fun copy(
        alpha: Double = this.alphaDouble,
        red: Double = this.redDouble,
        green: Double = this.greenDouble,
        blue: Double = this.blueDouble,
    ): Color = Color(alpha, red, green, blue)

    override fun toString(): String = "Color(alpha=$alpha, red=$red, green=$green, blue=$blue)"

    public companion object {
        public val Transparent: Color = Color(0, 255, 255, 255)
        public val Black: Color = Color(0, 0, 0)
        public val White: Color = Color(255, 255, 255)
        public val Red: Color = Color(255, 0, 0)
        public val Green: Color = Color(0, 255, 0)
        public val Blue: Color = Color(0, 0, 255)
        public val Magenta: Color = Color(255, 0, 255)
        public val Yellow: Color = Color(255, 255, 0)
        public val Cyan: Color = Color(0, 255, 255)
    }
}

private fun requireInByteRange(
    value: Int,
    name: String,
): Int {
    require(value in 0..255) { "$name ($value) must be in range 0..255" }
    return value
}

private fun requireInRatioRange(
    value: Double,
    name: String,
): Double {
    require(value in 0.0..1.0) { "$name ($value) must be in range 0..1" }
    return value
}
