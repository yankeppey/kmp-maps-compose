package eu.buney.maps

/**
 * Specifies the position of a [GroundOverlay].
 *
 * A ground overlay can be positioned in two ways:
 * 1. **Bounds-based**: The overlay is stretched to fit the specified [LatLngBounds].
 * 2. **Location-based**: The overlay is positioned at a specific location with dimensions in meters.
 *
 * Use the companion object factory methods to create instances.
 */
class GroundOverlayPosition private constructor(
    /**
     * The bounds to fit the overlay to, or null if using location-based positioning.
     */
    val latLngBounds: LatLngBounds? = null,
    /**
     * The center location of the overlay, or null if using bounds-based positioning.
     */
    val location: LatLng? = null,
    /**
     * The width of the overlay in meters, or null if using bounds-based positioning.
     */
    val width: Float? = null,
    /**
     * The height of the overlay in meters. If null, the aspect ratio is preserved based on the image.
     */
    val height: Float? = null,
) {
    companion object {
        /**
         * Creates a position that fits the overlay to the specified bounds.
         *
         * @param latLngBounds The bounds to fit the overlay to.
         * @return A [GroundOverlayPosition] for bounds-based positioning.
         */
        fun create(latLngBounds: LatLngBounds): GroundOverlayPosition {
            return GroundOverlayPosition(latLngBounds = latLngBounds)
        }

        /**
         * Creates a position at the specified location with dimensions in meters.
         *
         * @param location The center location of the overlay.
         * @param width The width of the overlay in meters.
         * @param height The height of the overlay in meters. If null, the aspect ratio
         *               is preserved based on the image dimensions.
         * @return A [GroundOverlayPosition] for location-based positioning.
         */
        fun create(location: LatLng, width: Float, height: Float? = null): GroundOverlayPosition {
            return GroundOverlayPosition(location = location, width = width, height = height)
        }
    }
}
