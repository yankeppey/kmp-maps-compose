package eu.buney.maps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds as GoogleLatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason as GoogleCameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState as GoogleCameraPositionState

/**
 * Creates and remembers a [GoogleCameraPositionState] that stays synchronized
 * with the given [CameraPositionState].
 *
 * This handles:
 * - Bidirectional position sync (our state ↔ Google's state)
 * - State sync (isMoving, cameraMoveStartedReason)
 * - Animation and move request forwarding
 * - Padding change handling
 */
@Composable
internal fun rememberSyncedGoogleCameraPositionState(
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues,
): GoogleCameraPositionState {
    val googleCameraPositionState = remember {
        GoogleCameraPositionState(
            position = cameraPositionState.position.toGoogleCameraPosition()
        )
    }

    // Sync position: our state → Google's state (for programmatic updates)
    LaunchedEffect(cameraPositionState.position) {
        // Only sync if camera is not moving (i.e., this is a programmatic change)
        // During gestures, Google's state is the source of truth
        if (!googleCameraPositionState.isMoving) {
            googleCameraPositionState.position = cameraPositionState.position.toGoogleCameraPosition()
        }
    }

    // Sync isMoving and position when camera becomes idle
    LaunchedEffect(googleCameraPositionState.isMoving) {
        cameraPositionState.isMoving = googleCameraPositionState.isMoving

        // When camera becomes idle, sync position and visible bounds from Google's state
        if (!googleCameraPositionState.isMoving) {
            // Update position from Google's state (captures gesture result)
            val googlePos = googleCameraPositionState.position
            if (!googlePos.matches(cameraPositionState.position)) {
                cameraPositionState.position = googlePos.toCameraPosition()
            }

            // Update visible bounds
            googleCameraPositionState.projection?.visibleRegion?.latLngBounds?.let { googleBounds ->
                cameraPositionState.visibleBounds = LatLngBounds(
                    southwest = LatLng(
                        googleBounds.southwest.latitude,
                        googleBounds.southwest.longitude
                    ),
                    northeast = LatLng(
                        googleBounds.northeast.latitude,
                        googleBounds.northeast.longitude
                    )
                )
            }
        }
    }

    // Sync cameraMoveStartedReason: Google's state → our state
    LaunchedEffect(googleCameraPositionState.cameraMoveStartedReason) {
        cameraPositionState.cameraMoveStartedReason =
            googleCameraPositionState.cameraMoveStartedReason.toCameraMoveStartedReason()
    }

    // Handle animation requests
    LaunchedEffect(Unit) {
        cameraPositionState.animationRequests.collect { request ->
            when (request) {
                is CameraAnimationRequest.ToPosition -> {
                    googleCameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(request.position.toGoogleCameraPosition()),
                        request.durationMs
                    )
                }
                is CameraAnimationRequest.ToBounds -> {
                    val googleBounds = GoogleLatLngBounds.Builder()
                        .include(request.bounds.southwest.toGoogleLatLng())
                        .include(request.bounds.northeast.toGoogleLatLng())
                        .build()
                    googleCameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(googleBounds, request.padding),
                        request.durationMs
                    )
                }
            }
        }
    }

    // Handle move requests
    LaunchedEffect(Unit) {
        cameraPositionState.moveRequests.collect { position ->
            googleCameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(position.toGoogleCameraPosition())
            )
        }
    }

    // Re-apply camera position when contentPadding changes
    var previousPadding by remember { mutableStateOf(contentPadding) }
    LaunchedEffect(contentPadding) {
        if (contentPadding != previousPadding) {
            googleCameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(googleCameraPositionState.position)
            )
            previousPadding = contentPadding
        }
    }

    // Set up projection provider
    LaunchedEffect(Unit) {
        cameraPositionState.projectionProvider = {
            googleCameraPositionState.projection?.let { AndroidProjection(it) }
        }
    }

    return googleCameraPositionState
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

private fun GoogleCameraPosition.matches(other: CameraPosition): Boolean =
    target.latitude == other.target.latitude &&
        target.longitude == other.target.longitude &&
        zoom == other.zoom &&
        tilt == other.tilt &&
        bearing == other.bearing

private fun GoogleCameraMoveStartedReason.toCameraMoveStartedReason() = when (this) {
    GoogleCameraMoveStartedReason.GESTURE -> CameraMoveStartedReason.GESTURE
    GoogleCameraMoveStartedReason.API_ANIMATION -> CameraMoveStartedReason.API_ANIMATION
    GoogleCameraMoveStartedReason.DEVELOPER_ANIMATION -> CameraMoveStartedReason.DEVELOPER_ANIMATION
    else -> CameraMoveStartedReason.UNKNOWN
}

// endregion
