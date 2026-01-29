package eu.buney.maps

import androidx.compose.ui.graphics.ImageBitmap

/**
 * iOS implementation: converts ImageBitmap to BitmapDescriptor via UIImage.
 * Uses the existing toUIImage() extension which handles pixel format conversion
 * and proper device scale.
 */
internal actual fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor {
    return BitmapDescriptor(this.toUIImage())
}
