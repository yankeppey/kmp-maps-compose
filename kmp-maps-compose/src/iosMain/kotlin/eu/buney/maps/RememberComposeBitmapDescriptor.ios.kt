package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.interpretCPointer
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextClearRect
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation
import platform.UIKit.UIScreen

/**
 * iOS implementation of [rememberComposeBitmapDescriptor] using [ImageComposeScene].
 *
 * This implementation uses Compose Multiplatform's CPU-only Skia raster rendering
 * ([ImageComposeScene]) instead of the heavier ComposeUIViewController + GraphicsLayer +
 * Metal pipeline approach.
 *
 * Rendering is fully synchronous (matching the Android pattern), so the result is
 * available immediately — no placeholder, no `LaunchedEffect`.
 */
@Composable
@GoogleMapComposable
actual fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    return remember(*keys) {
        BitmapDescriptor(captureComposableToUIImage(content))
    }
}

/**
 * Captures Compose content to a UIImage using [ImageComposeScene] / [renderComposeScene].
 *
 * Uses a lightweight CPU-only Skia raster surface — no UIView hierarchy, no Metal pipeline.
 * All operations are synchronous.
 *
 * Two-pass approach (the Skia surface size is fixed at construction, so it cannot be resized):
 * 1. Create a minimal 1x1 [ImageComposeScene], warmup [render][ImageComposeScene.render],
 *    then [calculateContentSize][ImageComposeScene.calculateContentSize] to measure.
 * 2. [renderComposeScene] at the measured size, convert to [UIImage].
 *
 * Must be called on the main thread when a Compose application is also running.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
fun captureComposableToUIImage(
    content: @Composable () -> Unit
): UIImage {
    val density = Density(UIScreen.mainScreen.scale.toFloat())

    // Pass 1: Measure the content's intrinsic size
    val contentSize = ImageComposeScene(
        width = 1, height = 1, density = density, content = content
    ).use { scene ->
        scene.render() // warmup: triggers initial layout settlement
        scene.calculateContentSize()
    }

    if (contentSize.width <= 0 || contentSize.height <= 0) {
        return createTransparentPlaceholder()
    }

    // Pass 2: Render at the exact measured size
    val image = renderComposeScene(
        width = contentSize.width,
        height = contentSize.height,
        density = density,
        content = content,
    )

    return image.toUIImage()
}

/**
 * Converts a Skia [Image] to a [UIImage] with minimal copies.
 *
 * Uses [Image.peekPixels] for zero-copy access to the raster image's pixel buffer, then
 * passes the raw pointer directly to [CGBitmapContextCreate]. The only pixel copy is
 * [CGBitmapContextCreateImage] creating the CGImage's own backing store.
 *
 * The CoreGraphics bitmap info is chosen dynamically based on the actual [ColorType] reported
 * by the pixmap, rather than relying on [ColorType.N32] (which is hardcoded in Kotlin and
 * never queried from the native Skia binary at runtime — the native `kN32_SkColorType` may
 * differ).
 *
 * Supported pixel formats:
 * - **BGRA_8888** (B,G,R,A in memory) → `kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Little`
 * - **RGBA_8888** (R,G,B,A in memory) → `kCGImageAlphaPremultipliedLast | kCGBitmapByteOrder32Big`
 */
@OptIn(ExperimentalForeignApi::class)
internal fun Image.toUIImage(): UIImage {
    val scale = UIScreen.mainScreen.scale
    val pixmap = peekPixels() ?: error("Cannot peek pixels — image may not be raster-backed")
    val width = pixmap.info.width
    val height = pixmap.info.height
    val rowBytes = pixmap.rowBytes
    val addr = pixmap.addr
    val colorType = pixmap.info.colorType

    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: error("Failed to create color space")

    try {
        // Choose CoreGraphics bitmap info based on the actual pixel format from Skia.
        // The Kotlin ColorType.N32 companion is hardcoded to BGRA_8888 at compile time
        // and never synced with the native Skia kN32_SkColorType, so we check the pixmap
        // directly to avoid R↔B channel swaps.
        val bitmapInfo = when (colorType) {
            ColorType.BGRA_8888 ->
                // BGRA in memory = ARGB logical with little-endian 32-bit word
                CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or kCGBitmapByteOrder32Little
            ColorType.RGBA_8888 ->
                // RGBA in memory = RGBA logical with big-endian 32-bit word
                CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value or kCGBitmapByteOrder32Big
            else -> error("Unsupported Skia ColorType for CGBitmapContext: $colorType")
        }

        val context = CGBitmapContextCreate(
            data = interpretCPointer<ByteVar>(addr),
            width = width.toULong(),
            height = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = rowBytes.toULong(),
            space = colorSpace,
            bitmapInfo = bitmapInfo
        ) ?: error("Failed to create bitmap context")

        try {
            val cgImage = CGBitmapContextCreateImage(context)
                ?: error("Failed to create CGImage from context")
            return UIImage.imageWithCGImage(cgImage, scale, orientation = UIImageOrientation.UIImageOrientationUp)
        } finally {
            CGContextRelease(context)
        }
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
}

/**
 * Creates a 1x1 transparent placeholder UIImage.
 * Used as a fallback when content has zero measured size.
 */
@OptIn(ExperimentalForeignApi::class)
private fun createTransparentPlaceholder(): UIImage {
    val size = CGSizeMake(1.0, 1.0)

    UIGraphicsBeginImageContextWithOptions(size, false, UIScreen.mainScreen.scale)
    try {
        val context = UIGraphicsGetCurrentContext()
        if (context != null) {
            CGContextClearRect(context, CGRectMake(0.0, 0.0, 1.0, 1.0))
        }

        return requireNotNull(UIGraphicsGetImageFromCurrentImageContext()) {
            "Failed to create transparent placeholder image"
        }
    } finally {
        UIGraphicsEndImageContext()
    }
}
