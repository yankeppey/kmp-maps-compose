package eu.buney.maps

import GoogleMaps.GMSCircle
import GoogleMaps.GMSGroundOverlay
import GoogleMaps.GMSMapView
import GoogleMaps.GMSMarker
import GoogleMaps.GMSPolygon
import GoogleMaps.GMSPolyline
import androidx.compose.runtime.AbstractApplier
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Custom [AbstractApplier] for managing map overlay nodes in the composition tree.
 *
 * This is the bridge between Compose's declarative model and the imperative
 * Google Maps iOS SDK. It manages a list of [MapNode] decorations and handles
 * their lifecycle as they are added, removed, or reordered in the composition.
 *
 * This mirrors the pattern used in android-maps-compose's MapApplier.
 *
 * @param mapView The [GMSMapView] that overlays are added to.
 */
@OptIn(ExperimentalForeignApi::class)
internal class MapApplier(
    val mapView: GMSMapView,
) : AbstractApplier<MapNode>(MapNodeRoot) {

    /**
     * List of all decoration nodes currently attached to this applier.
     */
    private val decorations = mutableListOf<MapNode>()

    override fun onClear() {
        // clear all overlays from the map
        mapView.clear()
        decorations.forEach { it.onCleared() }
        decorations.clear()
    }

    override fun insertBottomUp(index: Int, instance: MapNode) {
        decorations.add(index, instance)
        instance.onAttached()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {
        // insertBottomUp is preferred for this use case
    }

    override fun move(from: Int, to: Int, count: Int) {
        decorations.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            decorations[index + it].onRemoved()
        }
        decorations.remove(index, count)
    }

    /**
     * Find a [MarkerNode] that matches the given [GMSMarker].
     * Used by the delegate to route click events to the correct node.
     *
     * @param marker The [GMSMarker] to find the node for.
     * @return The matching [MarkerNode], or null if not found.
     */
    fun findMarkerNode(marker: GMSMarker): MarkerNode? {
        return decorations.firstOrNull {
            it is MarkerNode && it.marker == marker
        } as? MarkerNode
    }

    /**
     * Find a [CircleNode] that matches the given [GMSCircle].
     * Used by the delegate to route click events to the correct node.
     *
     * @param circle The [GMSCircle] to find the node for.
     * @return The matching [CircleNode], or null if not found.
     */
    fun findCircleNode(circle: GMSCircle): CircleNode? {
        return decorations.firstOrNull {
            it is CircleNode && it.circle == circle
        } as? CircleNode
    }

    /**
     * Find a [PolylineNode] that matches the given [GMSPolyline].
     * Used by the delegate to route click events to the correct node.
     *
     * @param polyline The [GMSPolyline] to find the node for.
     * @return The matching [PolylineNode], or null if not found.
     */
    fun findPolylineNode(polyline: GMSPolyline): PolylineNode? {
        return decorations.firstOrNull {
            it is PolylineNode && it.polyline == polyline
        } as? PolylineNode
    }

    /**
     * Find a [PolygonNode] that matches the given [GMSPolygon].
     * Used by the delegate to route click events to the correct node.
     *
     * @param polygon The [GMSPolygon] to find the node for.
     * @return The matching [PolygonNode], or null if not found.
     */
    fun findPolygonNode(polygon: GMSPolygon): PolygonNode? {
        return decorations.firstOrNull {
            it is PolygonNode && it.polygon == polygon
        } as? PolygonNode
    }

    /**
     * Find a [GroundOverlayNode] that matches the given [GMSGroundOverlay].
     * Used by the delegate to route click events to the correct node.
     *
     * @param groundOverlay The [GMSGroundOverlay] to find the node for.
     * @return The matching [GroundOverlayNode], or null if not found.
     */
    fun findGroundOverlayNode(groundOverlay: GMSGroundOverlay): GroundOverlayNode? {
        return decorations.firstOrNull {
            it is GroundOverlayNode && it.groundOverlay == groundOverlay
        } as? GroundOverlayNode
    }
}
