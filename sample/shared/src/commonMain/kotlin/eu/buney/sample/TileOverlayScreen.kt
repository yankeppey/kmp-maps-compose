package eu.buney.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.buney.maps.CameraPosition
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.TileFactory
import eu.buney.maps.TileOverlay
import eu.buney.maps.TileProvider
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.rememberTileOverlayState

private val sanFrancisco = LatLng(37.7749, -122.4194)

@Composable
fun TileOverlayScreen(modifier: Modifier = Modifier) {
    var tileVersion by remember { mutableIntStateOf(0) }
    val state = rememberTileOverlayState()

    val tileProvider = remember(tileVersion) {
        object : TileProvider {
            override fun getTile(x: Int, y: Int, zoom: Int) = createColoredTile(
                tileSize = 256,
                r = ((x * 37 + tileVersion * 60) % 256),
                g = ((y * 73 + tileVersion * 90) % 256),
                b = ((zoom * 51 + tileVersion * 120) % 256),
                a = 100,
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(target = sanFrancisco, zoom = 12f)
            },
        ) {
            TileOverlay(
                tileProvider = tileProvider,
                state = state,
                transparency = 0f,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Button(onClick = { state.clearTileCache() }) {
                Text("Clear Cache")
            }
            Button(
                onClick = { tileVersion++ },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("New Colors")
            }
        }
    }
}

/**
 * Creates a solid-color tile from raw ARGB pixels using platform-native encoding.
 */
private fun createColoredTile(tileSize: Int, r: Int, g: Int, b: Int, a: Int) =
    TileFactory.fromBytes(
        bytes = createSolidArgbPixels(tileSize, tileSize, a, r, g, b),
        width = tileSize,
        height = tileSize,
    )

/**
 * Builds a raw ARGB pixel buffer for a solid-color rectangle.
 * Format: 4 bytes per pixel — alpha, red, green, blue.
 */
private fun createSolidArgbPixels(
    width: Int,
    height: Int,
    a: Int,
    r: Int,
    g: Int,
    b: Int,
): ByteArray {
    val pixels = ByteArray(width * height * 4)
    for (i in pixels.indices step 4) {
        pixels[i] = a.toByte()
        pixels[i + 1] = r.toByte()
        pixels[i + 2] = g.toByte()
        pixels[i + 3] = b.toByte()
    }
    return pixels
}
