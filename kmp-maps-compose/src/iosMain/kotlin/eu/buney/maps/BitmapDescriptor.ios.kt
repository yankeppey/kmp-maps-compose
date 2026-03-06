package eu.buney.maps

import platform.UIKit.UIImage

/**
 * iOS implementation of [BitmapDescriptor].
 * Wraps a [UIImage] that can be used for map overlays.
 */
actual class BitmapDescriptor(
    val uiImage: UIImage
)

/**
 * iOS implementation of [BitmapDescriptorFactory].
 */
actual object BitmapDescriptorFactory {

    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor {
        return BitmapDescriptor(argbBytesToUIImage(bytes, width, height))
    }

    actual fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        return BitmapDescriptor(encodedBytesToUIImage(data))
    }
}
