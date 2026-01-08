package eu.buney.maps

/**
 * Represents the camera position for a map.
 *
 * @property target The location that the camera is pointing at.
 * @property zoom The zoom level of the camera. Typically ranges from 0 (world view) to 21 (building view).
 * @property bearing The direction that the camera is pointing in, in degrees clockwise from north.
 *                   Values are normalized to [0, 360).
 * @property tilt The angle, in degrees, of the camera angle from the nadir (directly facing the Earth).
 *                0 means the camera is looking straight down at the map. Values are clamped to a
 *                maximum depending on the zoom level (typically 0-65 degrees).
 */
data class CameraPosition(
    val target: LatLng,
    val zoom: Float = 10f,
    val bearing: Float = 0f,
    val tilt: Float = 0f,
) {
    companion object {
        /**
         * Creates a [CameraPosition] with the specified target and zoom level.
         *
         * This is a convenience factory method for compatibility with android-maps-compose.
         *
         * @param target The target location for the camera.
         * @param zoom The zoom level for the camera.
         * @return A new [CameraPosition] with the specified target and zoom.
         */
        fun fromLatLngZoom(target: LatLng, zoom: Float): CameraPosition =
            CameraPosition(target = target, zoom = zoom)
    }
}
