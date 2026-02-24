package eu.buney.maps

import GoogleMaps.GMSMapStyle
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class MapStyleOptions(
    internal val ios: GMSMapStyle,
) {
    actual companion object {
        actual fun fromJson(json: String): MapStyleOptions {
            val style = GMSMapStyle.styleWithJSONString(json, error = null)
                ?: error("Invalid map style JSON")
            return MapStyleOptions(style)
        }
    }
}
