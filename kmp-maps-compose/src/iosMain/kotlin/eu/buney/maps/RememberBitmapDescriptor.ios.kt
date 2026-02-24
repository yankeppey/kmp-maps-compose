package eu.buney.maps

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Image

/**
 * iOS implementation: converts ImageBitmap to BitmapDescriptor via UIImage.
 * Goes through Skia [Image] for efficient pixel access (see [toUIImage]).
 */
internal actual fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor {
    val skiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
    return BitmapDescriptor(skiaImage.toUIImage())
}
