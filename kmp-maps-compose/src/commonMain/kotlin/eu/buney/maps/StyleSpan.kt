package eu.buney.maps

import androidx.compose.ui.graphics.Color

/**
 * Describes the style for a region of a polyline.
 *
 * A polyline can have multiple spans, each defining the style for a
 * consecutive number of segments. If the spans array contains fewer
 * segments than the polyline, the final span style is applied to the
 * remaining length.
 *
 * @param style The stroke style (solid color or gradient) for this span.
 * @param stampStyle Optional stamp/texture to repeat over this span.
 *                   When set, the stamp image is rendered on top of the stroke.
 * @param segments The number of segments this span covers. Must be > 0.
 *                 Defaults to 1.0. Can be fractional to cover partial segments.
 *
 * Note: intentionally a class and not a data class, so a later property stays binary
 * compatible. See https://jakewharton.com/public-api-challenges-in-kotlin/
 */
class StyleSpan(
    val style: StrokeStyle,
    val stampStyle: StampStyle? = null,
    val segments: Double = 1.0
) {
    init {
        require(segments > 0) { "segments must be greater than 0, was: $segments" }
    }

    fun copy(
        style: StrokeStyle = this.style,
        stampStyle: StampStyle? = this.stampStyle,
        segments: Double = this.segments,
    ): StyleSpan = StyleSpan(style, stampStyle, segments)

    override fun equals(other: Any?): Boolean = other is StyleSpan &&
        style == other.style &&
        stampStyle == other.stampStyle &&
        segments == other.segments

    override fun hashCode(): Int {
        var result = style.hashCode()
        result = 31 * result + stampStyle.hashCode()
        result = 31 * result + segments.hashCode()
        return result
    }

    override fun toString(): String =
        "StyleSpan(style=$style, stampStyle=$stampStyle, segments=$segments)"

    companion object {
        /**
         * Creates a solid color span of length one segment.
         *
         * Convenience factory for the common case of a single-segment solid color.
         */
        fun solidColor(color: Color, segments: Double = 1.0): StyleSpan =
            StyleSpan(StrokeStyle.SolidColor(color), segments = segments)

        /**
         * Creates a gradient span of length one segment.
         *
         * Convenience factory for gradient spans.
         */
        fun gradient(
            fromColor: Color,
            toColor: Color,
            segments: Double = 1.0
        ): StyleSpan = StyleSpan(
            StrokeStyle.Gradient(fromColor, toColor),
            segments = segments
        )
    }
}
