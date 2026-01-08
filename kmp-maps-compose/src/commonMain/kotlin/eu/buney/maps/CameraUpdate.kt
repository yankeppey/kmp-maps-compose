package eu.buney.maps

/**
 * Represents an update to the camera position.
 *
 * This sealed class provides compatibility with android-maps-compose's CameraUpdate API.
 * Use [CameraUpdateFactory] to create instances.
 */
sealed class CameraUpdate {
    /**
     * Update to a specific camera position.
     */
    data class NewCameraPosition(val position: CameraPosition) : CameraUpdate()

    /**
     * Update to center on a location with a specific zoom level.
     */
    data class NewLatLngZoom(val latLng: LatLng, val zoom: Float) : CameraUpdate()

    /**
     * Update to fit bounds within the viewport.
     */
    data class NewLatLngBounds(val bounds: LatLngBounds, val padding: Int) : CameraUpdate()
}

/**
 * Factory for creating [CameraUpdate] objects.
 *
 * This object provides compatibility with android-maps-compose's CameraUpdateFactory API.
 */
object CameraUpdateFactory {
    /**
     * Creates a [CameraUpdate] that moves the camera to a specific position.
     *
     * @param position The target camera position.
     * @return A [CameraUpdate] representing the camera movement.
     */
    fun newCameraPosition(position: CameraPosition): CameraUpdate =
        CameraUpdate.NewCameraPosition(position)

    /**
     * Creates a [CameraUpdate] that moves the camera to center on a location with a specific zoom.
     *
     * @param latLng The target location.
     * @param zoom The target zoom level.
     * @return A [CameraUpdate] representing the camera movement.
     */
    fun newLatLngZoom(latLng: LatLng, zoom: Float): CameraUpdate =
        CameraUpdate.NewLatLngZoom(latLng, zoom)

    /**
     * Creates a [CameraUpdate] that moves the camera to fit the specified bounds.
     *
     * @param bounds The bounds to fit within the viewport.
     * @param padding Padding in pixels to apply around the bounds.
     * @return A [CameraUpdate] representing the camera movement.
     */
    fun newLatLngBounds(bounds: LatLngBounds, padding: Int): CameraUpdate =
        CameraUpdate.NewLatLngBounds(bounds, padding)
}
