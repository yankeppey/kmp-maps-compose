package eu.buney.maps

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.posix.memcpy

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
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual object BitmapDescriptorFactory {

    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor {
        require(bytes.size == width * height * 4) {
            "Byte array size (${bytes.size}) must equal width * height * 4 (${width * height * 4})"
        }

        val colorSpace = CGColorSpaceCreateDeviceRGB()
            ?: error("Failed to create color space")

        try {
            val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or kCGBitmapByteOrder32Big
            val bytesPerRow = width * 4

            val context = bytes.usePinned { pinned ->
                CGBitmapContextCreate(
                    data = pinned.addressOf(0),
                    width = width.toULong(),
                    height = height.toULong(),
                    bitsPerComponent = 8u,
                    bytesPerRow = bytesPerRow.toULong(),
                    space = colorSpace,
                    bitmapInfo = bitmapInfo
                )
            } ?: error("Failed to create bitmap context")

            try {
                val cgImage = CGBitmapContextCreateImage(context)
                    ?: error("Failed to create CGImage from context")
                val uiImage = UIImage(cGImage = cgImage)
                return BitmapDescriptor(uiImage)
            } finally {
                CGContextRelease(context)
            }
        } finally {
            CGColorSpaceRelease(colorSpace)
        }
    }

    actual fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        val nsData = data.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
        }
        val uiImage = UIImage(data = nsData)
        return BitmapDescriptor(uiImage)
    }
}
