package eu.buney.maps.utils.wms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import eu.buney.maps.GoogleMapComposable
import eu.buney.maps.NativeTileOverlay
import eu.buney.maps.TileOverlay
import eu.buney.maps.TileOverlayState
import eu.buney.maps.rememberTileOverlayState

/**
 * A composable that displays a Web Map Service (WMS) layer using EPSG:3857 projection.
 *
 * @param urlFormatter Builds the WMS URL for the given bounding box coordinates and zoom level.
 * @param state The [TileOverlayState] to control this overlay.
 * @param fadeIn Whether the tiles should fade in.
 * @param transparency The transparency of the overlay (0 = opaque, 1 = transparent).
 * @param visible Whether the overlay is visible.
 * @param zIndex The z-index of the overlay.
 * @param onClick Callback invoked when the overlay is clicked (Android only).
 * @param tileWidth Tile width in pixels (default 256).
 * @param tileHeight Tile height in pixels (default 256).
 */
@Composable
@GoogleMapComposable
fun WmsTileOverlay(
    urlFormatter: (xMin: Double, yMin: Double, xMax: Double, yMax: Double, zoom: Int) -> String,
    state: TileOverlayState = rememberTileOverlayState(),
    fadeIn: Boolean = true,
    transparency: Float = 0f,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (NativeTileOverlay) -> Unit = {},
    tileWidth: Int = 256,
    tileHeight: Int = 256,
) {
    val tileProvider = remember(urlFormatter, tileWidth, tileHeight) {
        WmsTileProvider(
            urlFormatter = urlFormatter,
            tileWidth = tileWidth,
            tileHeight = tileHeight,
        )
    }

    TileOverlay(
        tileProvider = tileProvider,
        state = state,
        fadeIn = fadeIn,
        transparency = transparency,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
    )
}
