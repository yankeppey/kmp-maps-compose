package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 * iOS implementation of [MarkerComposable].
 *
 * Renders Compose content as a marker icon using the GraphicsLayer capture approach.
 * The content is captured asynchronously; a transparent placeholder is shown briefly
 * before the actual rendered content appears.
 */
@Composable
@GoogleMapComposable
actual fun MarkerComposable(
    vararg keys: Any,
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
    content: @Composable () -> Unit,
) {
    val icon = rememberComposeBitmapDescriptor(*keys) { content() }

    Marker(
        state = state,
        contentDescription = contentDescription,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon,
        infoWindowAnchor = infoWindowAnchor,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        onInfoWindowClick = onInfoWindowClick,
        onInfoWindowClose = onInfoWindowClose,
        onInfoWindowLongClick = onInfoWindowLongClick,
    )
}
