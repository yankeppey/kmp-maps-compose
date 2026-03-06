package eu.buney.maps

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGColorRenderingIntent
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGDataProviderCreateWithData
import platform.CoreGraphics.CGDataProviderRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreate
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

/**
 * Converts raw ARGB pixel bytes into a [UIImage].
 *
 * Uses kCGImageAlphaFirst (non-premultiplied ARGB), which CGImageCreate
 * supports directly — no pixel premultiplication needed.
 *
 * @param bytes Raw pixel data — 4 bytes per pixel: alpha, red, green, blue.
 * @param width Image width in pixels.
 * @param height Image height in pixels.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun argbBytesToUIImage(bytes: ByteArray, width: Int, height: Int): UIImage {
    require(bytes.size == width * height * 4) {
        "Byte array size (${bytes.size}) must equal width * height * 4 (${width * height * 4})"
    }

    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: error("Failed to create color space")

    try {
        val bitmapInfo =
            CGImageAlphaInfo.kCGImageAlphaFirst.value or kCGBitmapByteOrder32Big
        val bytesPerRow = (width * 4).toULong()
        val bufferSize = bytes.size.toULong()

        val dataProvider = bytes.usePinned { pinned ->
            CGDataProviderCreateWithData(
                info = null,
                data = pinned.addressOf(0),
                size = bufferSize,
                releaseData = null,
            )
        } ?: error("Failed to create data provider")

        try {
            val cgImage = CGImageCreate(
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bitsPerPixel = 32u,
                bytesPerRow = bytesPerRow,
                space = colorSpace,
                bitmapInfo = bitmapInfo,
                provider = dataProvider,
                decode = null,
                shouldInterpolate = false,
                intent = CGColorRenderingIntent.kCGRenderingIntentDefault,
            ) ?: error("Failed to create CGImage from data provider")

            try {
                return UIImage(cGImage = cgImage)
            } finally {
                CGImageRelease(cgImage)
            }
        } finally {
            CGDataProviderRelease(dataProvider)
        }
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
}

/**
 * Decodes encoded image bytes (PNG, JPEG, etc.) into a [UIImage].
 *
 * @param data Encoded image data.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun encodedBytesToUIImage(data: ByteArray): UIImage {
    val nsData = data.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
    }
    return UIImage(data = nsData)
}
