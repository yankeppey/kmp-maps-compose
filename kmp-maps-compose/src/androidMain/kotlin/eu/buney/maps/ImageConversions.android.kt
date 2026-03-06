package eu.buney.maps

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

/**
 * Converts raw ARGB pixel bytes into a [Bitmap].
 *
 * @param bytes Raw pixel data — 4 bytes per pixel: alpha, red, green, blue.
 * @param width Image width in pixels.
 * @param height Image height in pixels.
 */
internal fun argbBytesToBitmap(bytes: ByteArray, width: Int, height: Int): Bitmap {
    require(bytes.size == width * height * 4) {
        "Byte array size (${bytes.size}) must equal width * height * 4 (${width * height * 4})"
    }
    val pixelCount = width * height
    val colors = IntArray(pixelCount)
    for (i in 0 until pixelCount) {
        val base = i * 4
        val a = bytes[base].toInt() and 0xFF
        val r = bytes[base + 1].toInt() and 0xFF
        val g = bytes[base + 2].toInt() and 0xFF
        val b = bytes[base + 3].toInt() and 0xFF
        colors[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    val bitmap = createBitmap(width, height)
    bitmap.setPixels(colors, 0, width, 0, 0, width, height)
    return bitmap
}

/**
 * Compresses this [Bitmap] to PNG format and returns the encoded bytes.
 */
internal fun Bitmap.compressToPng(): ByteArray {
    return ByteArrayOutputStream().use { stream ->
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}
