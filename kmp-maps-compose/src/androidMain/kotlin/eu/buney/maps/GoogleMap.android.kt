package eu.buney.maps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds as GoogleLatLngBounds
import com.google.android.gms.maps.model.PointOfInterest as GooglePointOfInterest
import com.google.maps.android.compose.CameraPositionState as GoogleCameraPositionState
import com.google.maps.android.compose.GoogleMap as AndroidGoogleMap
import com.google.maps.android.compose.MapProperties as GoogleMapProperties
import com.google.maps.android.compose.MapType as GoogleMapType
import com.google.maps.android.compose.MapUiSettings as GoogleMapUiSettings

@Composable
actual fun GoogleMap(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    contentPadding: PaddingValues,
    onMapClick: ((LatLng) -> Unit)?,
    onMapLongClick: ((LatLng) -> Unit)?,
    onPOIClick: ((PointOfInterest) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    content: (@Composable @GoogleMapComposable () -> Unit)?,
) {
    // convert our CameraPositionState to Google's CameraPositionState
    val googleCameraPositionState = remember {
        GoogleCameraPositionState(
            position = GoogleCameraPosition(
                GoogleLatLng(
                    cameraPositionState.position.target.latitude,
                    cameraPositionState.position.target.longitude
                ),
                cameraPositionState.position.zoom,
                cameraPositionState.position.tilt,
                cameraPositionState.position.bearing
            )
        )
    }

    // sync camera position changes from our state to Google's state
    LaunchedEffect(cameraPositionState.position) {
        googleCameraPositionState.position = GoogleCameraPosition(
            GoogleLatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            ),
            cameraPositionState.position.zoom,
            cameraPositionState.position.tilt,
            cameraPositionState.position.bearing
        )
    }

    // sync isMoving state from Google's state to our state
    LaunchedEffect(googleCameraPositionState.isMoving) {
        cameraPositionState.isMoving = googleCameraPositionState.isMoving
    }

    // handle animation requests
    LaunchedEffect(Unit) {
        cameraPositionState.animationRequests.collect { request ->
            cameraPositionState.cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION
            when (request) {
                is CameraAnimationRequest.ToPosition -> {
                    val targetPosition = GoogleCameraPosition(
                        GoogleLatLng(
                            request.position.target.latitude,
                            request.position.target.longitude
                        ),
                        request.position.zoom,
                        request.position.tilt,
                        request.position.bearing
                    )
                    googleCameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(targetPosition),
                        request.durationMs
                    )
                    // update our position after animation
                    cameraPositionState.position = request.position
                }
                is CameraAnimationRequest.ToBounds -> {
                    val googleBounds = GoogleLatLngBounds.Builder()
                        .include(GoogleLatLng(request.bounds.southwest.latitude, request.bounds.southwest.longitude))
                        .include(GoogleLatLng(request.bounds.northeast.latitude, request.bounds.northeast.longitude))
                        .build()
                    googleCameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(googleBounds, request.padding),
                        request.durationMs
                    )
                    // position will be updated by the camera idle callback via sync
                }
            }
        }
    }

    // handle move requests
    LaunchedEffect(Unit) {
        cameraPositionState.moveRequests.collect { position ->
            val targetPosition = GoogleCameraPosition(
                GoogleLatLng(
                    position.target.latitude,
                    position.target.longitude
                ),
                position.zoom,
                position.tilt,
                position.bearing
            )
            googleCameraPositionState.move(CameraUpdateFactory.newCameraPosition(targetPosition))
            cameraPositionState.position = position
        }
    }

    // track previous padding to detect changes
    var previousPadding by remember { mutableStateOf(contentPadding) }

    // re-apply camera position when contentPadding changes
    // this ensures the target stays at the logical center after padding changes,
    // matching the initialization behavior where camera is set after padding.
    LaunchedEffect(contentPadding) {
        if (contentPadding != previousPadding) {
            // move camera to current position so target is at the new logical center
            googleCameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(googleCameraPositionState.position)
            )
            previousPadding = contentPadding
        }
    }

    // convert MapType
    val googleMapType = when (properties.mapType) {
        MapType.NONE -> GoogleMapType.NONE
        MapType.NORMAL -> GoogleMapType.NORMAL
        MapType.SATELLITE -> GoogleMapType.SATELLITE
        MapType.HYBRID -> GoogleMapType.HYBRID
        MapType.TERRAIN -> GoogleMapType.TERRAIN
    }

    AndroidGoogleMap(
        modifier = modifier,
        cameraPositionState = googleCameraPositionState,
        contentPadding = contentPadding,
        properties = GoogleMapProperties(
            isBuildingEnabled = properties.isBuildingEnabled,
            isIndoorEnabled = properties.isIndoorEnabled,
            isMyLocationEnabled = properties.isMyLocationEnabled,
            isTrafficEnabled = properties.isTrafficEnabled,
            mapType = googleMapType,
            minZoomPreference = properties.minZoomPreference,
            maxZoomPreference = properties.maxZoomPreference,
        ),
        uiSettings = GoogleMapUiSettings(
            compassEnabled = uiSettings.compassEnabled,
            indoorLevelPickerEnabled = uiSettings.indoorLevelPickerEnabled,
            mapToolbarEnabled = uiSettings.mapToolbarEnabled,
            myLocationButtonEnabled = uiSettings.myLocationButtonEnabled,
            rotationGesturesEnabled = uiSettings.rotationGesturesEnabled,
            scrollGesturesEnabled = uiSettings.scrollGesturesEnabled,
            scrollGesturesEnabledDuringRotateOrZoom = uiSettings.scrollGesturesEnabledDuringRotateOrZoom,
            tiltGesturesEnabled = uiSettings.tiltGesturesEnabled,
            zoomControlsEnabled = uiSettings.zoomControlsEnabled,
            zoomGesturesEnabled = uiSettings.zoomGesturesEnabled,
        ),
        onMapClick = onMapClick?.let { callback ->
            { googleLatLng ->
                callback(LatLng(googleLatLng.latitude, googleLatLng.longitude))
            }
        },
        onMapLongClick = onMapLongClick?.let { callback ->
            { googleLatLng ->
                callback(LatLng(googleLatLng.latitude, googleLatLng.longitude))
            }
        },
        onPOIClick = onPOIClick?.let { callback ->
            { googlePoi ->
                callback(
                    PointOfInterest(
                        latLng = LatLng(googlePoi.latLng.latitude, googlePoi.latLng.longitude),
                        name = googlePoi.name,
                        placeId = googlePoi.placeId
                    )
                )
            }
        },
        onMapLoaded = onMapLoaded,
        content = {
            content?.invoke()
        },
    )
}
