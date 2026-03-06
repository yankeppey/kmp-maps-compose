package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * Provides tile images for a [TileOverlay].
 *
 * Implementations must be thread-safe as [getTile] may be called from
 * background threads on both platforms.
 */
interface TileProvider {
    /**
     * Returns a [Tile] for the given tile coordinates, or null if no tile
     * is available at this location.
     *
     * @param x The x coordinate of the tile (column).
     * @param y The y coordinate of the tile (row).
     * @param zoom The zoom level.
     * @return A [Tile] containing encoded image data, or null for no tile.
     */
    fun getTile(x: Int, y: Int, zoom: Int): Tile?
}

/**
 * Represents a single map tile backed by a platform-native image type.
 *
 * On Android, wraps a `com.google.android.gms.maps.model.Tile` (encoded bytes).
 * On iOS, wraps a `UIImage` — enabling zero-copy when tiles are created from raw pixels.
 *
 * Use [TileFactory] to create instances.
 */
expect class Tile

/**
 * Factory for creating [Tile] instances from pixel data or encoded images.
 */
expect object TileFactory {
    /**
     * Creates a [Tile] from raw ARGB pixel bytes.
     *
     * @param bytes Raw pixel data — 4 bytes per pixel: alpha, red, green, blue.
     *              The array size must be exactly `width * height * 4`.
     * @param width The width of the tile in pixels.
     * @param height The height of the tile in pixels.
     */
    fun fromBytes(bytes: ByteArray, width: Int, height: Int): Tile

    /**
     * Creates a [Tile] from an already-encoded image (PNG, JPEG, etc.).
     *
     * @param data The encoded image data.
     * @param width The width of the tile in pixels.
     * @param height The height of the tile in pixels.
     */
    fun fromEncodedImage(data: ByteArray, width: Int, height: Int): Tile
}

/**
 * A state object that can be hoisted to control the state of a [TileOverlay].
 * A [TileOverlayState] may only be used by a single [TileOverlay] composable at a time.
 */
@Stable
expect class TileOverlayState() {
    /**
     * Clears the tile cache so that all tiles will be requested again from the [TileProvider].
     *
     * Call this when the tiles provided by the [TileProvider] become stale
     * and need to be refreshed.
     */
    fun clearTileCache()
}

/**
 * Creates and remembers a [TileOverlayState].
 */
@Composable
fun rememberTileOverlayState(): TileOverlayState {
    return remember { TileOverlayState() }
}

/**
 * A composable that adds a tile overlay to the map.
 *
 * @param tileProvider The [TileProvider] to use for this tile overlay.
 * @param state The [TileOverlayState] to control this overlay (e.g., cache clearing).
 * @param fadeIn Whether the tiles should fade in. Default is true.
 * @param transparency The transparency of the tile overlay (0 = opaque, 1 = transparent).
 * @param visible Whether the tile overlay is visible. Default is true.
 * @param zIndex The z-index of the tile overlay. Default is 0f.
 * @param onClick A callback invoked when the tile overlay is clicked.
 *   Note: This only fires on Android. iOS GMSTileLayer does not support tap events.
 */
@Composable
@GoogleMapComposable
expect fun TileOverlay(
    tileProvider: TileProvider,
    state: TileOverlayState = rememberTileOverlayState(),
    fadeIn: Boolean = true,
    transparency: Float = 0f,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (NativeTileOverlay) -> Unit = {},
)
