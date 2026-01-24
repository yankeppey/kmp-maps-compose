package eu.buney.maps

import GoogleMaps.GMSCameraPosition
import GoogleMaps.GMSCoordinateBounds
import GoogleMaps.GMSMapView
import GoogleMaps.animateToCameraPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIEdgeInsetsMake

/**
 * Sets up synchronization between [CameraPositionState] and [GMSMapView].
 *
 * This handles:
 * - Syncing position changes from our state to the map
 * - Animation and move request forwarding
 *
 * Note: The reverse sync (map → our state) is handled by [GMSMapViewDelegate]
 * through the `willMove` and `idleAtCameraPosition` delegate callbacks.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun SetupCameraPositionStateSync(
    cameraPositionState: CameraPositionState,
    mapView: GMSMapView,
) {
    // Handle animation requests
    LaunchedEffect(Unit) {
        cameraPositionState.animationRequests.collect { request ->
            cameraPositionState.cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION
            when (request) {
                is CameraAnimationRequest.ToPosition -> {
                    mapView.animateToCameraPosition(request.position.toGMSCameraPosition())
                }
                is CameraAnimationRequest.ToBounds -> {
                    val gmsBounds = request.bounds.toGMSCoordinateBounds()
                    val padding = request.padding.toDouble()
                    val insets = UIEdgeInsetsMake(padding, padding, padding, padding)
                    val cameraPosition = mapView.cameraForBounds(gmsBounds, insets = insets)
                    if (cameraPosition != null) {
                        mapView.animateToCameraPosition(cameraPosition)
                    }
                }
            }
        }
    }

    // Handle move requests
    LaunchedEffect(Unit) {
        cameraPositionState.moveRequests.collect { position ->
            mapView.camera = position.toGMSCameraPosition()
            cameraPositionState.position = position
        }
    }

    // Track the last position we set programmatically to avoid feedback loops
    var lastSetPosition by remember { mutableStateOf<CameraPosition?>(null) }

    // Sync direct position assignments from cameraPositionState to mapView
    // This handles cases like: cameraPositionState.position = CameraPosition(target, zoom)
    LaunchedEffect(cameraPositionState.position) {
        val position = cameraPositionState.position
        // Only update if this is a new position we haven't set yet
        // (to avoid feedback loop with idleAtCameraPosition delegate)
        if (lastSetPosition != position) {
            mapView.camera = position.toGMSCameraPosition()
            lastSetPosition = position
        }
    }
}

/**
 * Updates [CameraPositionState] from the map when the camera becomes idle.
 * Called from [GMSMapViewDelegate.mapView:idleAtCameraPosition:].
 */
@OptIn(ExperimentalForeignApi::class)
internal fun updateCameraPositionStateOnIdle(
    cameraPositionState: CameraPositionState,
    mapView: GMSMapView,
    idleAtCameraPosition: GMSCameraPosition,
) {
    cameraPositionState.isMoving = false

    // Update the camera position state from the map
    idleAtCameraPosition.target.useContents {
        cameraPositionState.position = CameraPosition(
            target = LatLng(latitude, longitude),
            zoom = idleAtCameraPosition.zoom,
            bearing = idleAtCameraPosition.bearing.toFloat(),
            tilt = idleAtCameraPosition.viewingAngle.toFloat()
        )
    }

    // Update visible bounds from the map's projection
    updateVisibleBounds(cameraPositionState, mapView)
}

/**
 * Updates [CameraPositionState.visibleBounds] from the map's projection.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun updateVisibleBounds(
    cameraPositionState: CameraPositionState,
    mapView: GMSMapView,
) {
    // GMSVisibleRegion has 4 corners - extract and compute bounding box
    mapView.projection.visibleRegion().useContents {
        // Collect all corner latitudes and longitudes
        val lats = listOf(
            nearLeft.latitude,
            nearRight.latitude,
            farLeft.latitude,
            farRight.latitude
        )
        val lngs = listOf(
            nearLeft.longitude,
            nearRight.longitude,
            farLeft.longitude,
            farRight.longitude
        )

        // Compute bounding rectangle
        val minLat = lats.min()
        val maxLat = lats.max()
        val minLng = lngs.min()
        val maxLng = lngs.max()

        cameraPositionState.visibleBounds = LatLngBounds(
            southwest = LatLng(minLat, minLng),
            northeast = LatLng(maxLat, maxLng)
        )
    }
}

// region Type Conversions

@OptIn(ExperimentalForeignApi::class)
internal fun CameraPosition.toGMSCameraPosition(): GMSCameraPosition {
    val coordinate = CLLocationCoordinate2DMake(
        latitude = target.latitude,
        longitude = target.longitude
    )
    return GMSCameraPosition.cameraWithTarget(
        target = coordinate,
        zoom = zoom,
        bearing = bearing.toDouble(),
        viewingAngle = tilt.toDouble()
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun LatLngBounds.toGMSCoordinateBounds(): GMSCoordinateBounds {
    val swCoord = CLLocationCoordinate2DMake(
        latitude = southwest.latitude,
        longitude = southwest.longitude
    )
    val neCoord = CLLocationCoordinate2DMake(
        latitude = northeast.latitude,
        longitude = northeast.longitude
    )
    return GMSCoordinateBounds()
        .includingCoordinate(swCoord)
        .includingCoordinate(neCoord)
}

// endregion
