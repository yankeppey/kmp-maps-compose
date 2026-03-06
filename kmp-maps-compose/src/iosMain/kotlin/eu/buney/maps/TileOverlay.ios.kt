package eu.buney.maps

import GoogleMaps.GMSSyncTileLayer
import GoogleMaps.GMSTileLayer
import GoogleMaps.kGMSTileLayerNoTile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentComposer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIScreen

/**
 * iOS implementation of [Tile].
 * Wraps a [UIImage] — avoids encode/decode roundtrips for raw pixel tiles.
 */
actual class Tile(val uiImage: UIImage)

/**
 * iOS implementation of [TileFactory].
 */
actual object TileFactory {
    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): Tile {
        return Tile(argbBytesToUIImage(bytes, width, height))
    }

    actual fun fromEncodedImage(data: ByteArray, width: Int, height: Int): Tile {
        return Tile(encodedBytesToUIImage(data))
    }
}

/**
 * iOS implementation of [TileOverlayState].
 * Holds a reference to the [GMSTileLayer] for cache clearing.
 */
@OptIn(ExperimentalForeignApi::class)
@Stable
actual class TileOverlayState actual constructor() {
    internal var tileLayer: GMSTileLayer? = null

    actual fun clearTileCache() {
        (tileLayer ?: error("This TileOverlayState is not associated with a TileOverlay"))
            .clearTileCache()
    }
}

/**
 * A [GMSSyncTileLayer] subclass that bridges the cross-platform [TileProvider]
 * to the iOS Google Maps SDK.
 *
 * [GMSSyncTileLayer.tileForX] is called on background threads by the SDK,
 * matching our [TileProvider.getTile] contract.
 */
@OptIn(ExperimentalForeignApi::class)
internal class KmpTileLayer(
    var tileProvider: TileProvider,
) : GMSSyncTileLayer() {

    override fun tileForX(x: ULong, y: ULong, zoom: ULong): UIImage {
        val tile = tileProvider.getTile(x.toInt(), y.toInt(), zoom.toInt())
            ?: return kGMSTileLayerNoTile
        return tile.uiImage
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun TileOverlay(
    tileProvider: TileProvider,
    state: TileOverlayState,
    fadeIn: Boolean,
    transparency: Float,
    visible: Boolean,
    zIndex: Float,
    onClick: (NativeTileOverlay) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("TileOverlay must be used within a GoogleMap composable")

    ComposeNode<TileOverlayNode, MapApplier>(
        factory = {
            val tileLayer = KmpTileLayer(tileProvider).apply {
                // iOS tileSize is in pixels, Android renders tiles at 256dp;
                // scale by screen density for consistency
                this.tileSize = (256.0 * UIScreen.mainScreen.scale).toLong()
                this.fadeIn = fadeIn
                this.opacity = 1f - transparency
                this.zIndex = zIndex.toInt()
                this.map = if (visible) mapApplier.mapView else null
            }

            state.tileLayer = tileLayer

            TileOverlayNode(
                tileLayer = tileLayer,
                tileOverlayState = state,
            )
        },
        update = {
            update(tileProvider) {
                this.tileLayer.tileProvider = it
                this.tileLayer.clearTileCache()
            }
            update(fadeIn) { this.tileLayer.fadeIn = it }
            update(transparency) { this.tileLayer.opacity = 1f - it }
            update(zIndex) { this.tileLayer.zIndex = it.toInt() }
            update(visible) {
                this.tileLayer.map = if (it) mapApplier.mapView else null
            }
            update(state) {
                this.tileOverlayState = it
                it.tileLayer = this.tileLayer
            }
        }
    )
}
