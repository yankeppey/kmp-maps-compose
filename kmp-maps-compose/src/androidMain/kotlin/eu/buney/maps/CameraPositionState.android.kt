package eu.buney.maps

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.google.android.gms.maps.CameraUpdateFactory as GmsCameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds as GoogleLatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason as GoogleCameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState as GoogleCameraPositionState

/**
 * Android implementation of [CameraPositionState] that wraps [GoogleCameraPositionState].
 *
 * Instead of maintaining a separate state and syncing it bidirectionally,
 * this class delegates directly to the underlying android-maps-compose state.
 * This eliminates feedback loops and camera trembling during gestures.
 *
 * The [google] instance is passed directly to the Android GoogleMap composable,
 * so there is only ONE source of truth for camera state.
 */
@Stable
actual class CameraPositionState actual constructor(
    position: CameraPosition
) {
    /**
     * The underlying android-maps-compose CameraPositionState.
     * Passed directly to AndroidGoogleMap — no sync layer needed.
     */
    internal val google: GoogleCameraPositionState = GoogleCameraPositionState(
        position = position.toGoogleCameraPosition()
    )

    actual val isMoving: Boolean by derivedStateOf { google.isMoving }

    actual val cameraMoveStartedReason: CameraMoveStartedReason by derivedStateOf {
        google.cameraMoveStartedReason.toCameraMoveStartedReason()
    }

    private val _position by derivedStateOf { google.position.toCameraPosition() }
    actual var position: CameraPosition
        get() = _position
        set(value) {
            google.position = value.toGoogleCameraPosition()
        }

    actual val projection: Projection?
        get() = google.projection?.let { AndroidProjection(it) }

    actual suspend fun animate(
        update: CameraUpdate,
        durationMs: Int
    ) {
        google.animate(update.toGmsCameraUpdate(), durationMs)
    }

    actual fun move(update: CameraUpdate) {
        google.move(update.toGmsCameraUpdate())
    }

    private fun CameraUpdate.toGmsCameraUpdate(): com.google.android.gms.maps.CameraUpdate =
        when (this) {
            is CameraUpdate.NewCameraPosition ->
                GmsCameraUpdateFactory.newCameraPosition(position.toGoogleCameraPosition())
            is CameraUpdate.NewLatLngZoom ->
                GmsCameraUpdateFactory.newLatLngZoom(latLng.toGoogleLatLng(), zoom)
            is CameraUpdate.NewLatLngBounds ->
                GmsCameraUpdateFactory.newLatLngBounds(
                    GoogleLatLngBounds.Builder()
                        .include(bounds.southwest.toGoogleLatLng())
                        .include(bounds.northeast.toGoogleLatLng())
                        .build(),
                    padding
                )
        }
}

// region Type Conversions

internal fun CameraPosition.toGoogleCameraPosition() = GoogleCameraPosition(
    target.toGoogleLatLng(),
    zoom,
    tilt,
    bearing
)

private fun GoogleCameraPosition.toCameraPosition() = CameraPosition(
    target = LatLng(target.latitude, target.longitude),
    zoom = zoom,
    tilt = tilt,
    bearing = bearing
)

internal fun LatLng.toGoogleLatLng() = GoogleLatLng(latitude, longitude)

private fun GoogleCameraMoveStartedReason.toCameraMoveStartedReason() = when (this) {
    GoogleCameraMoveStartedReason.GESTURE -> CameraMoveStartedReason.GESTURE
    GoogleCameraMoveStartedReason.API_ANIMATION -> CameraMoveStartedReason.API_ANIMATION
    GoogleCameraMoveStartedReason.DEVELOPER_ANIMATION -> CameraMoveStartedReason.DEVELOPER_ANIMATION
    else -> CameraMoveStartedReason.UNKNOWN
}

// endregion
