package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 * Represents a marker on the map. This is passed to click callbacks.
 */
expect class Marker {
    /**
     * The position of the marker.
     */
    val position: LatLng

    /**
     * The title of the marker, shown in the info window.
     */
    val title: String?

    /**
     * The snippet of the marker, shown in the info window below the title.
     */
    val snippet: String?
}

/**
 * A composable for a marker on the map.
 *
 * @param state the [MarkerState] to control or observe the marker state
 * @param contentDescription the content description for accessibility purposes
 * @param alpha the alpha (opacity) of the marker, between 0.0 and 1.0
 * @param anchor the anchor point of the marker image. The anchor specifies the point in the
 *   marker image that is anchored to the marker's position on the map. The anchor point is
 *   specified as a ratio of the image size; (0.0, 0.0) is the top-left corner, (1.0, 1.0) is
 *   the bottom-right corner, and (0.5, 1.0) is the bottom-center (the default).
 * @param draggable sets whether the marker is draggable
 * @param flat sets whether the marker should be flat against the map (true) or a billboard
 *   facing the camera (false)
 * @param icon the [BitmapDescriptor] to use as the marker icon. If null, the default marker
 *   icon is used.
 * @param infoWindowAnchor the anchor point of the info window on the marker image. The anchor
 *   specifies the point in the marker image at which to anchor the info window. The anchor point
 *   is specified as a ratio; (0.5, 0.0) is the top-center (the default).
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet text shown in the info window when the marker is tapped
 * @param tag an optional tag to associate with the marker
 * @param title the title text shown in the info window when the marker is tapped
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker, which determines the drawing order.
 *   Note: On iOS, fractional values are truncated to integers.
 * @param onClick a lambda invoked when the marker is clicked. Return true to consume the event
 *   and prevent the default behavior (showing the info window).
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param onInfoWindowClose a lambda invoked when the marker's info window is closed
 * @param onInfoWindowLongClick a lambda invoked when the marker's info window is long-pressed
 */
@Composable
@GoogleMapComposable
expect fun Marker(
    state: MarkerState = rememberUpdatedMarkerState(),
    contentDescription: String? = "",
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    infoWindowAnchor: Offset = Offset(0.5f, 0.0f),
    rotation: Float = 0.0f,
    snippet: String? = null,
    tag: Any? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    onInfoWindowClose: (Marker) -> Unit = {},
    onInfoWindowLongClick: (Marker) -> Unit = {},
)

/**
 * A composable for a marker on the map wherein its entire info window can be customized.
 * If this customization is not required, use [Marker].
 *
 * Note: On both Android and iOS, custom info windows are rendered as static bitmaps.
 * Interactive elements (buttons, scrolling) inside the info window will not work.
 * Only [onInfoWindowClick] and [onInfoWindowLongClick] are supported for interaction.
 *
 * @param state the [MarkerState] to control or observe the marker state
 * @param contentDescription the content description for accessibility purposes
 * @param alpha the alpha (opacity) of the marker, between 0.0 and 1.0
 * @param anchor the anchor point of the marker image
 * @param draggable sets whether the marker is draggable
 * @param flat sets whether the marker should be flat against the map (true) or a billboard
 *   facing the camera (false)
 * @param icon the [BitmapDescriptor] to use as the marker icon
 * @param infoWindowAnchor the anchor point of the info window on the marker image
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet text (used as fallback if content capture fails)
 * @param tag an optional tag to associate with the marker
 * @param title the title text (used as fallback if content capture fails)
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param onInfoWindowClose a lambda invoked when the marker's info window is closed
 * @param onInfoWindowLongClick a lambda invoked when the marker's info window is long-pressed
 * @param content composable lambda expression for customizing the entire info window
 */
@Composable
@GoogleMapComposable
expect fun MarkerInfoWindow(
    state: MarkerState = rememberUpdatedMarkerState(),
    contentDescription: String? = "",
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    infoWindowAnchor: Offset = Offset(0.5f, 0.0f),
    rotation: Float = 0.0f,
    snippet: String? = null,
    tag: Any? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    onInfoWindowClose: (Marker) -> Unit = {},
    onInfoWindowLongClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null,
)

/**
 * A composable for a marker on the map wherein its info window contents can be customized.
 * The content will be placed within the default info window frame.
 * If this customization is not required, use [Marker].
 *
 * Note: On both Android and iOS, custom info window contents are rendered as static bitmaps.
 * Interactive elements (buttons, scrolling) inside the info window will not work.
 * Only [onInfoWindowClick] and [onInfoWindowLongClick] are supported for interaction.
 *
 * @param state the [MarkerState] to control or observe the marker state
 * @param contentDescription the content description for accessibility purposes
 * @param alpha the alpha (opacity) of the marker, between 0.0 and 1.0
 * @param anchor the anchor point of the marker image
 * @param draggable sets whether the marker is draggable
 * @param flat sets whether the marker should be flat against the map (true) or a billboard
 *   facing the camera (false)
 * @param icon the [BitmapDescriptor] to use as the marker icon
 * @param infoWindowAnchor the anchor point of the info window on the marker image
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet text (used as fallback on iOS)
 * @param tag an optional tag to associate with the marker
 * @param title the title text (used as fallback on iOS)
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker
 * @param onClick a lambda invoked when the marker is clicked
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param onInfoWindowClose a lambda invoked when the marker's info window is closed
 * @param onInfoWindowLongClick a lambda invoked when the marker's info window is long-pressed
 * @param content composable lambda expression for customizing the info window's content
 */
@Composable
@GoogleMapComposable
expect fun MarkerInfoWindowContent(
    state: MarkerState = rememberUpdatedMarkerState(),
    contentDescription: String? = "",
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: BitmapDescriptor? = null,
    infoWindowAnchor: Offset = Offset(0.5f, 0.0f),
    rotation: Float = 0.0f,
    snippet: String? = null,
    tag: Any? = null,
    title: String? = null,
    visible: Boolean = true,
    zIndex: Float = 0.0f,
    onClick: (Marker) -> Boolean = { false },
    onInfoWindowClick: (Marker) -> Unit = {},
    onInfoWindowClose: (Marker) -> Unit = {},
    onInfoWindowLongClick: (Marker) -> Unit = {},
    content: (@Composable (Marker) -> Unit)? = null,
)
