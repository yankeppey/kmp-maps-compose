package eu.buney.maps

/**
 * A point in screen coordinates (pixels).
 *
 * Screen location is in screen pixels relative to the top left corner of the map
 * (not necessarily of the whole screen).
 *
 * @property x The x coordinate in pixels from the left edge of the map.
 * @property y The y coordinate in pixels from the top edge of the map.
 */
data class ScreenPoint(
    val x: Float,
    val y: Float,
)

/**
 * A projection for converting between screen coordinates and geographic coordinates.
 *
 * A projection is used to translate between on-screen location and geographic
 * coordinates on the surface of the Earth ([LatLng]).
 *
 * This is a snapshot of the projection at a specific point in time and does not
 * automatically update when the camera moves. To get an updated projection,
 * access [CameraPositionState.projection] again after camera movement.
 *
 * @see CameraPositionState.projection
 */
interface Projection {
    /**
     * Converts a geographic coordinate to a screen point.
     *
     * @param latLng The geographic coordinate to convert.
     * @return The screen point corresponding to the coordinate, in pixels relative
     *         to the top-left corner of the map view.
     */
    fun toScreenLocation(latLng: LatLng): ScreenPoint

    /**
     * Converts a screen point to a geographic coordinate.
     *
     * @param point The screen point to convert, in pixels relative to the
     *              top-left corner of the map view.
     * @return The geographic coordinate corresponding to the screen point.
     */
    fun fromScreenLocation(point: ScreenPoint): LatLng

    /**
     * The visible region as a bounding rectangle.
     *
     * Note: For tilted cameras, the visible region is actually a trapezoid.
     * This returns the smallest bounding rectangle that contains the entire
     * visible trapezoid, which may include areas outside the actual visible region.
     */
    val visibleBounds: LatLngBounds

    /**
     * Returns whether the given coordinate is contained within the visible region.
     *
     * @param latLng The coordinate to check.
     * @return `true` if the coordinate is within the visible region, `false` otherwise.
     */
    fun contains(latLng: LatLng): Boolean
}
