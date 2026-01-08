package eu.buney.maps

import GoogleMaps.GMSGroundOverlay
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * [MapNode] implementation for ground overlays.
 *
 * Holds the [GMSGroundOverlay] and associated callbacks. The node is managed by
 * [MapApplier] and its lifecycle methods are called when the ground overlay is
 * added to or removed from the composition.
 *
 * @param groundOverlay The underlying [GMSGroundOverlay] instance.
 * @param onGroundOverlayClick Callback invoked when the ground overlay is clicked.
 */
@OptIn(ExperimentalForeignApi::class)
internal class GroundOverlayNode(
    val groundOverlay: GMSGroundOverlay,
    var onGroundOverlayClick: (GroundOverlay) -> Unit,
) : MapNode {

    override fun onRemoved() {
        groundOverlay.map = null
    }

    override fun onCleared() {
        groundOverlay.map = null
    }
}
