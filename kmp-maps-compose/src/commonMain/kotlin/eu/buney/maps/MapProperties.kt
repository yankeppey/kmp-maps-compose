package eu.buney.maps

/**
 * Data class for properties that can be modified on the map.
 *
 * Note: This is intentionally a class and not a data class for binary
 * compatibility on future changes.
 * See: https://jakewharton.com/public-api-challenges-in-kotlin/
 *
 * @property isBuildingEnabled Whether 3D buildings should be enabled.
 * @property isIndoorEnabled Whether indoor maps should be enabled.
 * @property isMyLocationEnabled Whether the my-location layer should be enabled.
 * @property isTrafficEnabled Whether the traffic layer should be enabled.
 * @property mapType The type of map tiles to display.
 * @property mapStyleOptions Custom map styling options, or null for the default style.
 *   Use [MapStyleOptions.fromJson] with a JSON string from the
 *   [Google Maps Styling Wizard](https://mapstyle.withgoogle.com/).
 * @property minZoomPreference The preferred minimum zoom level.
 * @property maxZoomPreference The preferred maximum zoom level.
 */
class MapProperties(
    val isBuildingEnabled: Boolean = false,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val mapStyleOptions: MapStyleOptions? = null,
    val minZoomPreference: Float = 3.0f,
    val maxZoomPreference: Float = 21.0f,
) {
    override fun toString(): String = "MapProperties(" +
        "isBuildingEnabled=$isBuildingEnabled, isIndoorEnabled=$isIndoorEnabled, " +
        "isMyLocationEnabled=$isMyLocationEnabled, isTrafficEnabled=$isTrafficEnabled, " +
        "mapType=$mapType, mapStyleOptions=${mapStyleOptions != null}, " +
        "minZoomPreference=$minZoomPreference, " +
        "maxZoomPreference=$maxZoomPreference)"

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        isBuildingEnabled == other.isBuildingEnabled &&
        isIndoorEnabled == other.isIndoorEnabled &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        mapType == other.mapType &&
        mapStyleOptions == other.mapStyleOptions &&
        minZoomPreference == other.minZoomPreference &&
        maxZoomPreference == other.maxZoomPreference

    override fun hashCode(): Int {
        var result = isBuildingEnabled.hashCode()
        result = 31 * result + isIndoorEnabled.hashCode()
        result = 31 * result + isMyLocationEnabled.hashCode()
        result = 31 * result + isTrafficEnabled.hashCode()
        result = 31 * result + mapType.hashCode()
        result = 31 * result + (mapStyleOptions?.hashCode() ?: 0)
        result = 31 * result + minZoomPreference.hashCode()
        result = 31 * result + maxZoomPreference.hashCode()
        return result
    }

    fun copy(
        isBuildingEnabled: Boolean = this.isBuildingEnabled,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        mapType: MapType = this.mapType,
        mapStyleOptions: MapStyleOptions? = this.mapStyleOptions,
        minZoomPreference: Float = this.minZoomPreference,
        maxZoomPreference: Float = this.maxZoomPreference,
    ): MapProperties = MapProperties(
        isBuildingEnabled = isBuildingEnabled,
        isIndoorEnabled = isIndoorEnabled,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        mapType = mapType,
        mapStyleOptions = mapStyleOptions,
        minZoomPreference = minZoomPreference,
        maxZoomPreference = maxZoomPreference,
    )
}
