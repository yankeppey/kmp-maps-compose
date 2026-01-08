package eu.buney.maps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that displays a map.
 *
 * On Android, this uses Google Maps. On iOS, this uses Google Maps iOS SDK.
 *
 * @param modifier Modifier to be applied to the map.
 * @param cameraPositionState The camera position state that controls the map's camera.
 * @param properties Properties of the map, such as map type and location settings.
 * @param uiSettings UI settings for the map, such as gesture and control visibility.
 * @param contentPadding The padding to apply to the map content. This affects the position of
 *   the map's UI controls (compass, my location button, etc.) and the effective visible region.
 * @param onMapClick Lambda invoked when the map is clicked.
 * @param onMapLongClick Lambda invoked when the map is long-clicked.
 * @param onPOIClick Lambda invoked when a point of interest is clicked. Note: On Android, POI
 *   clicks take precedence over clickable overlays (circles, polygons, etc.), so tapping a POI
 *   inside a clickable overlay will trigger this callback. On iOS, clickable overlays take
 *   precedence, so tapping a POI inside a clickable overlay will trigger the overlay's click
 *   handler instead.
 * @param onMapLoaded Callback invoked when the map is loaded and ready to use.
 * @param content A lambda for placing map content such as markers.
 */
@Composable
expect fun GoogleMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    properties: MapProperties = MapProperties(),
    uiSettings: MapUiSettings = MapUiSettings(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onMapClick: ((LatLng) -> Unit)? = null,
    onMapLongClick: ((LatLng) -> Unit)? = null,
    onPOIClick: ((PointOfInterest) -> Unit)? = null,
    onMapLoaded: (() -> Unit)? = null,
    content: (@Composable @GoogleMapComposable () -> Unit)? = null,
)
