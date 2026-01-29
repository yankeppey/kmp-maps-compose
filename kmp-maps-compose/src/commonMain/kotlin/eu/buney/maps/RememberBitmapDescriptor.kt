package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource

/**
 * Remembers and loads a [BitmapDescriptor] from a Compose [DrawableResource].
 *
 * This function leverages Compose Resources' built-in density handling via [imageResource],
 * which automatically selects the appropriate density variant and scales if needed.
 *
 * **Note**: Compose Resources only downscales (high-res → low-res device), never upscales.
 * If your resource is lower density than the device (e.g., mdpi resource on xxhdpi device),
 * the marker may appear smaller than intended.
 *
 * Example usage:
 * ```kotlin
 * val icon = rememberBitmapDescriptor(Res.drawable.my_marker_icon)
 * Marker(state = markerState, icon = icon)
 * ```
 *
 * @param resource The drawable resource to load
 * @return The [BitmapDescriptor] for use with map markers or overlays
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberBitmapDescriptor(resource: DrawableResource): BitmapDescriptor {
    val imageBitmap: ImageBitmap = imageResource(resource)
    return remember(imageBitmap) {
        imageBitmap.toBitmapDescriptor()
    }
}

/**
 * Converts an [ImageBitmap] to a platform-specific [BitmapDescriptor].
 *
 * Platform implementations:
 * - Android: Uses [ImageBitmap.asAndroidBitmap] and Google Maps BitmapDescriptorFactory
 * - iOS: Converts pixel data to UIImage via Core Graphics
 */
internal expect fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor
