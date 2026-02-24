package eu.buney.maps

import com.google.android.gms.maps.model.MapStyleOptions as AndroidMapStyleOptions

actual class MapStyleOptions(
    internal val android: AndroidMapStyleOptions,
) {
    actual companion object {
        actual fun fromJson(json: String) =
            MapStyleOptions(AndroidMapStyleOptions(json))
    }
}
