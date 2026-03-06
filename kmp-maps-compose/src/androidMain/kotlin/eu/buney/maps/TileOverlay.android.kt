package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.Tile as GoogleTile
import com.google.android.gms.maps.model.TileProvider as GoogleTileProvider
import com.google.maps.android.compose.TileOverlay as AndroidTileOverlay
import com.google.maps.android.compose.TileOverlayState as AndroidTileOverlayState
import com.google.maps.android.compose.rememberTileOverlayState as androidRememberTileOverlayState

/**
 * Android implementation of [Tile].
 * Wraps a Google Maps SDK [GoogleTile] containing encoded image bytes.
 */
actual class Tile(val googleTile: GoogleTile)

/**
 * Android implementation of [TileFactory].
 */
actual object TileFactory {
    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): Tile {
        val bitmap = argbBytesToBitmap(bytes, width, height)
        val pngBytes = bitmap.compressToPng()
        bitmap.recycle()
        return Tile(GoogleTile(width, height, pngBytes))
    }

    actual fun fromEncodedImage(data: ByteArray, width: Int, height: Int): Tile {
        return Tile(GoogleTile(width, height, data))
    }
}

/**
 * Android implementation of [TileOverlayState].
 * Wraps the android-maps-compose [AndroidTileOverlayState].
 */
@Stable
actual class TileOverlayState actual constructor() {
    internal val androidState = AndroidTileOverlayState()

    actual fun clearTileCache() {
        androidState.clearTileCache()
    }
}

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
    val googleTileProvider = remember(tileProvider) {
        GoogleTileProvider { x, y, zoom ->
            tileProvider.getTile(x, y, zoom)?.googleTile ?: GoogleTileProvider.NO_TILE
        }
    }

    AndroidTileOverlay(
        tileProvider = googleTileProvider,
        state = state.androidState,
        fadeIn = fadeIn,
        transparency = transparency,
        visible = visible,
        zIndex = zIndex,
        onClick = { onClick(it) },
    )
}
