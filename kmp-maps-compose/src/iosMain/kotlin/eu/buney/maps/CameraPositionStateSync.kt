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
            cameraPositionState._cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION
            mapView.applyAnimatedCameraUpdate(request.update)
        }
    }

    // Handle move requests
    LaunchedEffect(Unit) {
        cameraPositionState.moveRequests.collect { update ->
            val position = mapView.applyInstantCameraUpdate(update)
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
    cameraPositionState._isMoving = false

    // Update rawPosition directly (no side effects, no feedback loop)
    idleAtCameraPosition.target.useContents {
        cameraPositionState.rawPosition = CameraPosition(
            target = LatLng(latitude, longitude),
            zoom = idleAtCameraPosition.zoom,
            bearing = idleAtCameraPosition.bearing.toFloat(),
            tilt = idleAtCameraPosition.viewingAngle.toFloat()
        )
    }

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

// region CameraUpdate Helpers

/**
 * Applies a [CameraUpdate] as an animation on the map view.
 */
@OptIn(ExperimentalForeignApi::class)
private fun GMSMapView.applyAnimatedCameraUpdate(update: CameraUpdate) {
    when (update) {
        is CameraUpdate.NewCameraPosition -> {
            animateToCameraPosition(update.position.toGMSCameraPosition())
        }
        is CameraUpdate.NewLatLngZoom -> {
            val position = CameraPosition(target = update.latLng, zoom = update.zoom)
            animateToCameraPosition(position.toGMSCameraPosition())
        }
        is CameraUpdate.NewLatLngBounds -> {
            val gmsBounds = update.bounds.toGMSCoordinateBounds()
            val padding = update.padding.toDouble()
            val insets = UIEdgeInsetsMake(padding, padding, padding, padding)
            val cameraPosition = cameraForBounds(gmsBounds, insets = insets)
            if (cameraPosition != null) {
                animateToCameraPosition(cameraPosition)
            }
        }
    }
}

/**
 * Applies a [CameraUpdate] instantly on the map view.
 * Returns the resulting [CameraPosition].
 */
@OptIn(ExperimentalForeignApi::class)
private fun GMSMapView.applyInstantCameraUpdate(update: CameraUpdate): CameraPosition {
    when (update) {
        is CameraUpdate.NewCameraPosition -> {
            camera = update.position.toGMSCameraPosition()
            return update.position
        }
        is CameraUpdate.NewLatLngZoom -> {
            val position = CameraPosition(target = update.latLng, zoom = update.zoom)
            camera = position.toGMSCameraPosition()
            return position
        }
        is CameraUpdate.NewLatLngBounds -> {
            val gmsBounds = update.bounds.toGMSCoordinateBounds()
            val padding = update.padding.toDouble()
            val insets = UIEdgeInsetsMake(padding, padding, padding, padding)
            val gmsPosition = cameraForBounds(gmsBounds, insets = insets)
            if (gmsPosition != null) {
                camera = gmsPosition
            }
            // Read back the actual camera position
            return camera.let { cam ->
                cam.target.useContents {
                    CameraPosition(
                        target = LatLng(latitude, longitude),
                        zoom = cam.zoom,
                        bearing = cam.bearing.toFloat(),
                        tilt = cam.viewingAngle.toFloat()
                    )
                }
            }
        }
    }
}

// endregion

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
