package eu.buney.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIScreen

/**
 * iOS implementation of [rememberComposeBitmapDescriptor] using GraphicsLayer capture.
 *
 * This implementation:
 * 1. Creates a temporary ComposeUIViewController to render in a separate composition
 * 2. Uses Compose's rememberGraphicsLayer() API to capture drawing operations
 * 3. Converts the captured ImageBitmap to UIImage via direct pixel buffer transfer
 * 4. Returns a transparent placeholder initially, then the actual image once captured
 *
 * Note: A separate ComposeUIViewController is needed because this composable runs inside
 * GoogleMap's MapApplier context, which doesn't support regular UI composables.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    var capturedImage by remember { mutableStateOf<UIImage?>(null) }

    LaunchedEffect(*keys) {
        try {
            val image = withContext(Dispatchers.Main) {
                captureComposableToUIImage(content)
            }
            capturedImage = image
        } catch (e: Exception) {
            println("MarkerComposable: Failed to capture composable: ${e.message}")
        }
    }

    return remember(capturedImage) {
        capturedImage?.let { BitmapDescriptor(it) }
            ?: BitmapDescriptor(createTransparentPlaceholder())
    }
}

/**
 * Captures Compose content to a UIImage using GraphicsLayer inside a ComposeUIViewController.
 * The ComposeUIViewController provides a separate composition context.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
internal suspend fun captureComposableToUIImage(
    content: @Composable () -> Unit
): UIImage {
    val sizeDeferred = CompletableDeferred<IntSize>()
    val captureComplete = CompletableDeferred<ImageBitmap>()

    val controller = ComposeUIViewController(
        configure = { opaque = false }
    ) {
        val graphicsLayer = rememberGraphicsLayer()
        var contentSize by remember { mutableStateOf(IntSize.Zero) }
        var shouldCapture by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .drawWithContent {
                    if (shouldCapture && contentSize.width > 0 && contentSize.height > 0) {
                        graphicsLayer.record(size = contentSize) {
                            this@drawWithContent.drawContent()
                        }
                    }
                    drawContent()
                }
        ) {
            Box(
                modifier = Modifier.onSizeChanged { size ->
                    if (size.width > 0 && size.height > 0 && contentSize == IntSize.Zero) {
                        contentSize = size
                        sizeDeferred.complete(size)
                    }
                }
            ) {
                content()
            }
        }

        LaunchedEffect(shouldCapture, contentSize) {
            if (shouldCapture && contentSize.width > 0 && contentSize.height > 0) {
                try {
                    val bitmap = graphicsLayer.toImageBitmap()
                    captureComplete.complete(bitmap)
                } catch (e: Exception) {
                    captureComplete.completeExceptionally(e)
                }
            }
        }

        LaunchedEffect(contentSize) {
            if (contentSize.width > 0 && contentSize.height > 0) {
                kotlinx.coroutines.delay(50)
                shouldCapture = true
            }
        }
    }

    val view = controller.view
    val scale = UIScreen.mainScreen.scale
    val initialSize = 200.0

    val keyWindow = platform.UIKit.UIApplication.sharedApplication.keyWindow
    if (keyWindow != null) {
        view.setFrame(CGRectMake(-1000.0, -1000.0, initialSize, initialSize))
        keyWindow.addSubview(view)
    }

    view.setNeedsLayout()
    view.layoutIfNeeded()
    kotlinx.coroutines.delay(100)

    val measuredSize = kotlinx.coroutines.withTimeoutOrNull(5000) {
        sizeDeferred.await()
    } ?: error("Composable content failed to measure within 5s - ensure it has non-zero intrinsic size")

    val widthPoints = measuredSize.width.toDouble() / scale
    val heightPoints = measuredSize.height.toDouble() / scale
    view.setFrame(CGRectMake(-1000.0, -1000.0, widthPoints, heightPoints))
    view.setNeedsLayout()
    view.layoutIfNeeded()

    val bitmap = try {
        kotlinx.coroutines.withTimeoutOrNull(5000) {
            captureComplete.await()
        } ?: error("GraphicsLayer capture failed to complete within 5s - content may not be rendering correctly")
    } finally {
        view.removeFromSuperview()
    }

    return bitmap.toUIImage()
}

/**
 * Converts an ImageBitmap to a UIImage via direct pixel buffer transfer.
 * This avoids PNG encoding/decoding overhead by directly copying pixel data.
 * The resulting UIImage is scaled appropriately for the device's screen density.
 */
@OptIn(ExperimentalForeignApi::class)
private fun ImageBitmap.toUIImage(): UIImage {
    val width = this.width
    val height = this.height
    val scale = UIScreen.mainScreen.scale

    require(width > 0 && height > 0) {
        "ImageBitmap must have positive dimensions"
    }

    val buffer = IntArray(width * height)
    this.readPixels(buffer)

    val colorSpace = CGColorSpaceCreateDeviceRGB()
        ?: error("Failed to create color space")

    try {
        // CGBitmapContext with ARGB pixel format (premultiplied alpha first)
        // kCGBitmapByteOrder32Little + kCGImageAlphaPremultipliedFirst = BGRA in memory
        // which matches how Compose stores pixels on little-endian systems
        val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or kCGBitmapByteOrder32Little

        val cgImage = buffer.usePinned { pinned ->
            val context = CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = (width * 4).toULong(),
                space = colorSpace,
                bitmapInfo = bitmapInfo
            ) ?: error("Failed to create bitmap context")

            try {
                CGBitmapContextCreateImage(context)
                    ?: error("Failed to create CGImage from context")
            } finally {
                CGContextRelease(context)
            }
        }

        return UIImage.imageWithCGImage(cgImage, scale, orientation = platform.UIKit.UIImageOrientation.UIImageOrientationUp)
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
}

/**
 * Creates a 1x1 transparent placeholder UIImage.
 * Used as a fallback before the actual content is captured.
 */
@OptIn(ExperimentalForeignApi::class)
private fun createTransparentPlaceholder(): UIImage {
    val size = CGSizeMake(1.0, 1.0)

    UIGraphicsBeginImageContextWithOptions(size, false, UIScreen.mainScreen.scale)
    try {
        val context = UIGraphicsGetCurrentContext()
        if (context != null) {
            platform.CoreGraphics.CGContextClearRect(context, CGRectMake(0.0, 0.0, 1.0, 1.0))
        }

        return requireNotNull(UIGraphicsGetImageFromCurrentImageContext()) {
            "Failed to create transparent placeholder image"
        }
    } finally {
        UIGraphicsEndImageContext()
    }
}
