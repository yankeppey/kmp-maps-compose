package eu.buney.maps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val google = cameraPositionState.google

    // Re-apply camera position when contentPadding changes
    var previousPadding by remember { mutableStateOf(contentPadding) }
    LaunchedEffect(contentPadding) {
        if (contentPadding != previousPadding) {
            cameraPositionState.move(CameraUpdateFactory.newCameraPosition(cameraPositionState.position))
            previousPadding = contentPadding
        }
    }

    val googleMapType = when (properties.mapType) {
        MapType.NONE -> GoogleMapType.NONE
        MapType.NORMAL -> GoogleMapType.NORMAL
        MapType.SATELLITE -> GoogleMapType.SATELLITE
        MapType.HYBRID -> GoogleMapType.HYBRID
        MapType.TERRAIN -> GoogleMapType.TERRAIN
    }

    AndroidGoogleMap(
        modifier = modifier,
        cameraPositionState = google,
        contentPadding = contentPadding,
        properties = GoogleMapProperties(
            isBuildingEnabled = properties.isBuildingEnabled,
            isIndoorEnabled = properties.isIndoorEnabled,
            isMyLocationEnabled = properties.isMyLocationEnabled,
            isTrafficEnabled = properties.isTrafficEnabled,
            mapType = googleMapType,
            mapStyleOptions = properties.mapStyleOptions?.android,
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
            CompositionLocalProvider(
                LocalCameraPositionState provides cameraPositionState,
            ) {
                content?.invoke()
            }
        },
    )
}
