package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.GroundOverlay as GoogleGroundOverlay
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds as GoogleLatLngBounds
import com.google.maps.android.compose.GroundOverlay as AndroidGroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition as AndroidGroundOverlayPosition

/**
 * Android implementation of [GroundOverlay].
 * Wraps the Google Maps SDK GroundOverlay.
 */
actual class GroundOverlay(
    private val googleGroundOverlay: GoogleGroundOverlay
) {
    actual val bounds: LatLngBounds
        get() {
            val googleBounds = googleGroundOverlay.bounds
            return LatLngBounds(
                southwest = LatLng(googleBounds.southwest.latitude, googleBounds.southwest.longitude),
                northeast = LatLng(googleBounds.northeast.latitude, googleBounds.northeast.longitude),
            )
        }

    actual val bearing: Float
        get() = googleGroundOverlay.bearing

    actual val transparency: Float
        get() = googleGroundOverlay.transparency
}

@Composable
@GoogleMapComposable
actual fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset,
    bearing: Float,
    clickable: Boolean,
    tag: Any?,
    transparency: Float,
    visible: Boolean,
    zIndex: Float,
    onClick: (GroundOverlay) -> Unit,
) {
    AndroidGroundOverlay(
        position = position.toAndroidPosition(),
        image = image.googleBitmapDescriptor,
        anchor = anchor,
        bearing = bearing,
        clickable = clickable,
        tag = tag,
        transparency = transparency,
        visible = visible,
        zIndex = zIndex,
        onClick = { googleGroundOverlay ->
            onClick(GroundOverlay(googleGroundOverlay))
        },
    )
}

/**
 * Converts our [GroundOverlayPosition] to the android-maps-compose GroundOverlayPosition.
 */
private fun GroundOverlayPosition.toAndroidPosition(): AndroidGroundOverlayPosition {
    return when {
        latLngBounds != null -> {
            AndroidGroundOverlayPosition.create(
                GoogleLatLngBounds(
                    GoogleLatLng(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude),
                    GoogleLatLng(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude),
                )
            )
        }
        location != null && width != null -> {
            val googleLocation = GoogleLatLng(location.latitude, location.longitude)
            if (height != null) {
                AndroidGroundOverlayPosition.create(googleLocation, width, height)
            } else {
                AndroidGroundOverlayPosition.create(googleLocation, width)
            }
        }
        else -> error("Invalid GroundOverlayPosition: must specify either latLngBounds or location+width")
    }
}
