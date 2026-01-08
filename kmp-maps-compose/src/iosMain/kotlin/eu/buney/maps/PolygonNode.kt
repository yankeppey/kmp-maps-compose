package eu.buney.maps

import GoogleMaps.GMSPolygon
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * [MapNode] implementation for polygons.
 *
 * Holds the [GMSPolygon] and associated callbacks. The node is managed by
 * [MapApplier] and its lifecycle methods are called when the polygon is
 * added to or removed from the composition.
 *
 * This mirrors the PolygonNode pattern used in android-maps-compose.
 *
 * @param polygon The underlying [GMSPolygon] instance.
 * @param onPolygonClick Callback invoked when the polygon is clicked.
 */
@OptIn(ExperimentalForeignApi::class)
internal class PolygonNode(
    val polygon: GMSPolygon,
    var onPolygonClick: (Polygon) -> Unit,
) : MapNode {

    override fun onRemoved() {
        polygon.map = null
    }

    override fun onCleared() {
        polygon.map = null
    }
}
