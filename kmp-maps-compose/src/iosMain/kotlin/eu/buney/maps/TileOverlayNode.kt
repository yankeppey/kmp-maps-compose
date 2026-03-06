package eu.buney.maps

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * [MapNode] implementation for tile overlays.
 *
 * Holds the [KmpTileLayer] and manages its lifecycle. Unlike other overlay nodes,
 * there is no click callback because tile layers are not [GMSOverlay] subclasses and
 * do not receive tap events.
 *
 * @param tileLayer The underlying [KmpTileLayer].
 * @param tileOverlayState The [TileOverlayState] associated with this overlay.
 */
@OptIn(ExperimentalForeignApi::class)
internal class TileOverlayNode(
    val tileLayer: KmpTileLayer,
    var tileOverlayState: TileOverlayState,
) : MapNode {

    override fun onRemoved() {
        tileLayer.map = null
        tileOverlayState.tileLayer = null
    }

    override fun onCleared() {
        tileLayer.map = null
        tileOverlayState.tileLayer = null
    }
}
