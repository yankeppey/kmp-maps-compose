package eu.buney.maps

/**
 * Contains information about a point of interest that was clicked on the map.
 *
 * @property latLng The location of the POI.
 * @property name The name of the POI.
 * @property placeId The place ID of the POI, which can be used with the Google Places API.
 */
data class PointOfInterest(
    val latLng: LatLng,
    val name: String,
    val placeId: String
)
