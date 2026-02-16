package eu.buney.maps

import GoogleMaps.GMSMapView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIEdgeInsets

/**
 * Base interface for all map overlay nodes (markers, polylines, polygons, etc.).
 *
 * Each map element that can be added to the map should implement this interface.
 * The lifecycle methods are called by [MapApplier] when nodes are added to,
 * removed from, or cleared from the composition.
 *
 * This mirrors the pattern used in android-maps-compose.
 */
internal interface MapNode {
    /**
     * Called when the node is attached to the composition tree.
     * This is where any setup after the node is added should occur.
     */
    fun onAttached() {}

    /**
     * Called when the node is removed from the composition tree.
     * This is where cleanup should occur (e.g., removing the overlay from the map).
     */
    fun onRemoved() {}

    /**
     * Called when the entire map is cleared.
     * This is where cleanup should occur, similar to [onRemoved].
     */
    fun onCleared() {}
}

/**
 * Root node for the [MapApplier].
 * This serves as a placeholder root of the composition tree.
 */
internal object MapNodeRoot : MapNode

@OptIn(ExperimentalForeignApi::class)
internal class IOSMapPropertiesNode(
    val mapView: GMSMapView,
    cameraPositionState: CameraPositionState,
    contentPadding: CValue<UIEdgeInsets>,
) : MapNode {
    init {
        mapView.padding = contentPadding
        cameraPositionState.setMap(mapView)
    }

    var cameraPositionState = cameraPositionState
        set(value) {
            if (value == field) return
            field.setMap(null)
            field = value
            value.setMap(mapView)
        }

    override fun onRemoved() {
        cameraPositionState.setMap(null)
    }

    override fun onCleared() {
        cameraPositionState.setMap(null)
    }
}
