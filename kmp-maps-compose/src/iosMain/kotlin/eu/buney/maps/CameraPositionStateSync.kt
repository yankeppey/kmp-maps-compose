package eu.buney.maps

import GoogleMaps.GMSCameraPosition
import GoogleMaps.GMSCoordinateBounds
import GoogleMaps.GMSMapView
import GoogleMaps.animateToCameraPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIEdgeInsetsMake

/**
 * Sets up synchronization between [CameraPositionState] and [GMSMapView].
 *
 * This handles:
 * - Setting up the position updater for programmatic camera changes
 * - Animation and move request forwarding
 * - Projection provider setup
 *
 * Note: The reverse sync (map → our state) is handled by [GMSMapViewDelegate]
 * through the `willMove`, `didChangeCameraPosition`, and `idleAtCameraPosition`
 * delegate callbacks, which write directly to [CameraPositionState.rawPosition].
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun SetupCameraPositionStateSync(
    cameraPositionState: CameraPositionState,
    mapView: GMSMapView,
) {
    // Set up position updater - called when user sets cameraPositionState.position
    DisposableEffect(mapView) {
        cameraPositionState.positionUpdater = { position ->
            mapView.camera = position.toGMSCameraPosition()
            // Also update rawPosition so getter returns the new value immediately
            cameraPositionState.rawPosition = position
        }
        onDispose {
            cameraPositionState.positionUpdater = null
        }
    }

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
            cameraPositionState.rawPosition = position
        }
    }

    // Set up projection provider
    LaunchedEffect(Unit) {
        cameraPositionState.projectionProvider = {
            IOSProjection(mapView.projection)
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

    // Update rawPosition directly (no side effects, no feedback loop)
    idleAtCameraPosition.target.useContents {
        cameraPositionState.rawPosition = CameraPosition(
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
 * Updates [CameraPositionState] from the map during camera movement (realtime).
 * Called from [GMSMapViewDelegate.mapView:didChangeCameraPosition:].
 */
@OptIn(ExperimentalForeignApi::class)
internal fun updateCameraPositionStateOnMove(
    cameraPositionState: CameraPositionState,
    cameraPosition: GMSCameraPosition,
) {
    // Update rawPosition directly (no side effects, no feedback loop)
    cameraPosition.target.useContents {
        cameraPositionState.rawPosition = CameraPosition(
            target = LatLng(latitude, longitude),
            zoom = cameraPosition.zoom,
            bearing = cameraPosition.bearing.toFloat(),
            tilt = cameraPosition.viewingAngle.toFloat()
        )
    }
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
