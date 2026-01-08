package eu.buney.maps

import GoogleMaps.GMSMarker
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage

/**
 * Specifies the type of custom info window content.
 */
internal enum class InfoWindowType {
    /** No custom info window - use default with title/snippet */
    NONE,
    /** Custom content inside the default info window frame (markerInfoContents delegate) */
    CONTENT,
    /** Fully custom info window (markerInfoWindow delegate) */
    WINDOW
}

/**
 * [MapNode] implementation for markers.
 *
 * Holds the [GMSMarker] and associated callbacks. The node is managed by
 * [MapApplier] and its lifecycle methods are called when the marker is
 * added to or removed from the composition.
 *
 * This mirrors the MarkerNode pattern used in android-maps-compose.
 *
 * @param marker The underlying [GMSMarker] instance.
 * @param markerState The [MarkerState] associated with this marker.
 * @param onMarkerClick Callback invoked when the marker is clicked.
 * @param onInfoWindowClick Callback invoked when the marker's info window is clicked.
 * @param onInfoWindowClose Callback invoked when the marker's info window is closed.
 * @param onInfoWindowLongClick Callback invoked when the marker's info window is long-pressed.
 */
@OptIn(ExperimentalForeignApi::class)
internal class MarkerNode(
    val marker: GMSMarker,
    val markerState: MarkerState,
    var onMarkerClick: (Marker) -> Boolean,
    var onInfoWindowClick: (Marker) -> Unit,
    var onInfoWindowClose: (Marker) -> Unit,
    var onInfoWindowLongClick: (Marker) -> Unit,
) : MapNode {

    /**
     * The type of custom info window for this marker.
     */
    var infoWindowType: InfoWindowType = InfoWindowType.NONE

    /**
     * Cached UIImage for the custom info window content.
     * Pre-rendered from the composable and used when the delegate requests the info window.
     */
    var cachedInfoWindowImage: UIImage? = null

    override fun onAttached() {
        markerState.platformMarker = marker
    }

    override fun onRemoved() {
        markerState.platformMarker = null
        cachedInfoWindowImage = null
        marker.map = null
    }

    override fun onCleared() {
        markerState.platformMarker = null
        cachedInfoWindowImage = null
        marker.map = null
    }
}
