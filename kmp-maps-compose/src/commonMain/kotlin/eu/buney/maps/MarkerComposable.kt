package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 * A marker on the map that uses custom Compose content as its icon.
 *
 * The provided [content] composable is rendered to a bitmap and used as the marker's icon.
 * The content is re-rendered whenever any of the [keys] change, allowing for dynamic
 * marker visuals that update based on state.
 *
 * Example usage:
 * ```kotlin
 * MarkerComposable(
 *     keys = arrayOf(isSelected, count),
 *     state = rememberUpdatedMarkerState(position),
 *     onClick = { marker -> true }
 * ) {
 *     Box(
 *         modifier = Modifier
 *             .size(48.dp)
 *             .background(if (isSelected) Color.Blue else Color.Red, CircleShape),
 *         contentAlignment = Alignment.Center
 *     ) {
 *         Text("$count", color = Color.White)
 *     }
 * }
 * ```
 *
 * @param keys Cache invalidation keys. When any key changes, the [content] is re-rendered
 *   to a new bitmap. Include any state that affects the visual appearance of the content.
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
 * @param infoWindowAnchor the anchor point of the info window on the marker image
 * @param rotation the rotation of the marker in degrees clockwise about the marker's anchor point
 * @param snippet the snippet text shown in the info window when the marker is tapped
 * @param tag an optional tag to associate with the marker
 * @param title the title text shown in the info window when the marker is tapped
 * @param visible the visibility of the marker
 * @param zIndex the z-index of the marker, which determines the drawing order
 * @param onClick a lambda invoked when the marker is clicked. Return true to consume the event.
 * @param onInfoWindowClick a lambda invoked when the marker's info window is clicked
 * @param onInfoWindowClose a lambda invoked when the marker's info window is closed
 * @param onInfoWindowLongClick a lambda invoked when the marker's info window is long-pressed
 * @param content the Compose content to render as the marker icon. The content should have
 *   a defined size (either explicit or intrinsic) as it will be measured and rendered
 *   to a bitmap.
 * @throws IllegalStateException if the composable is measured to have a size of zero
 *   in either dimension
 */
@Composable
@GoogleMapComposable
expect fun MarkerComposable(
    vararg keys: Any,
    state: MarkerState = rememberUpdatedMarkerState(),
    contentDescription: String? = "",
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    flat: Boolean = false,
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
    content: @Composable () -> Unit,
)
