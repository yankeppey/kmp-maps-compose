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
 */
data class StyleSpan(
    val style: StrokeStyle,
    val stampStyle: StampStyle? = null,
    val segments: Double = 1.0
) {
    init {
        require(segments > 0) { "segments must be greater than 0, was: $segments" }
    }

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
