package eu.buney.maps

import androidx.compose.runtime.Composable

/**
 * Renders Compose content to a [BitmapDescriptor] for use as marker icons.
 *
 * This function captures the provided Compose [content] and converts it to a bitmap
 * that can be used as a custom marker icon. The content is re-rendered whenever
 * any of the [keys] change.
 *
 * Example usage:
 * ```kotlin
 * val icon = rememberComposeBitmapDescriptor(isSelected, count) {
 *     Box(
 *         modifier = Modifier
 *             .size(48.dp)
 *             .background(if (isSelected) Color.Blue else Color.Red, CircleShape),
 *         contentAlignment = Alignment.Center
 *     ) {
 *         Text("$count", color = Color.White)
 *     }
 * }
 *
 * Marker(
 *     state = markerState,
 *     icon = icon
 * )
 * ```
 *
 * @param keys Cache invalidation keys. When any key changes, the content is re-rendered
 *   to a new bitmap. Include any state that affects the visual appearance of the content.
 * @param content The Compose content to render as a marker icon. The content should have
 *   a defined size (either explicit or intrinsic) as it will be measured and rendered
 *   to a bitmap.
 * @return A [BitmapDescriptor] containing the rendered content, suitable for use with
 *   [Marker]'s icon parameter.
 */
@Composable
@GoogleMapComposable
expect fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor
