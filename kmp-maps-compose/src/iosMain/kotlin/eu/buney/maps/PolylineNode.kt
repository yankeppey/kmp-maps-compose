package eu.buney.maps

import GoogleMaps.GMSPolyline
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * [MapNode] implementation for polylines.
 *
 * Holds the [GMSPolyline] and associated callbacks. The node is managed by
 * [MapApplier] and its lifecycle methods are called when the polyline is
 * added to or removed from the composition.
 *
 * This mirrors the PolylineNode pattern used in android-maps-compose.
 *
 * @param polyline The underlying [GMSPolyline] instance.
 * @param onPolylineClick Callback invoked when the polyline is clicked.
 */
@OptIn(ExperimentalForeignApi::class)
internal class PolylineNode(
    val polyline: GMSPolyline,
    var onPolylineClick: (Polyline) -> Unit,
) : MapNode {

    override fun onRemoved() {
        polyline.map = null
    }

    override fun onCleared() {
        polyline.map = null
    }
}
