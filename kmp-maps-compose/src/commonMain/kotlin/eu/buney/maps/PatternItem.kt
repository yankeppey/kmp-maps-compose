package eu.buney.maps

/**
 * Describes the shape of a dash used in the stroke pattern of a [Circle] or other overlays.
 *
 * This is a multiplatform abstraction over Android's `com.google.android.gms.maps.model.PatternItem`.
 * Note: On iOS, stroke patterns are not supported and will be ignored.
 */
sealed class PatternItem {
    /**
     * A dash pattern item. A dash is a solid line segment of the given length.
     *
     * @param length The length of the dash in pixels.
     */
    data class Dash(val length: Float) : PatternItem()

    /**
     * A gap pattern item. A gap is a transparent segment of the given length.
     *
     * @param length The length of the gap in pixels.
     */
    data class Gap(val length: Float) : PatternItem()

    /**
     * A dot pattern item. A dot is a small circle.
     */
    data object Dot : PatternItem()
}
