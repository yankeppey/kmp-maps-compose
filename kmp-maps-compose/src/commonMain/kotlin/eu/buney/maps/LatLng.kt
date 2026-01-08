package eu.buney.maps

/**
 * Represents a geographical location with latitude and longitude.
 *
 * @property latitude The latitude in degrees. Valid range is [-90, 90].
 * @property longitude The longitude in degrees. Valid range is [-180, 180].
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
)
