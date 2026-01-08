package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 * Represents a ground overlay on the map.
 *
 * A ground overlay is an image that is fixed to a geographic area on the map.
 */
expect class GroundOverlay {
    /**
     * The bounds of the overlay.
     */
    val bounds: LatLngBounds

    /**
     * The bearing of the overlay in degrees clockwise from north.
     */
    val bearing: Float

    /**
     * The transparency of the overlay (0 = opaque, 1 = transparent).
     */
    val transparency: Float
}

/**
 * A composable that draws a ground overlay (image) on the map.
 *
 * @param position The position of the overlay, either bounds-based or location-based.
 * @param image The image to display as the overlay.
 * @param anchor The anchor point within the image. Default is center (0.5, 0.5).
 * @param bearing The rotation of the overlay in degrees clockwise from north. Default is 0f.
 * @param clickable Whether the overlay is clickable. Default is false.
 * @param tag An arbitrary object associated with the overlay.
 * @param transparency The transparency of the overlay (0 = opaque, 1 = transparent). Default is 0f.
 * @param visible Whether the overlay is visible. Default is true.
 * @param zIndex The z-index of the overlay. Default is 0f.
 *   Note: On iOS, fractional values are truncated to integers.
 * @param onClick A callback invoked when the overlay is clicked.
 */
@Composable
@GoogleMapComposable
expect fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset = Offset(0.5f, 0.5f),
    bearing: Float = 0f,
    clickable: Boolean = false,
    tag: Any? = null,
    transparency: Float = 0f,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (GroundOverlay) -> Unit = {},
)
