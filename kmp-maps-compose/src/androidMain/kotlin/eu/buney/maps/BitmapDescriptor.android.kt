package eu.buney.maps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.nio.ByteBuffer
import com.google.android.gms.maps.model.BitmapDescriptor as GoogleBitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

/**
 * Android implementation of [BitmapDescriptor].
 * Wraps the Google Maps SDK BitmapDescriptor.
 */
actual class BitmapDescriptor(
    internal val googleBitmapDescriptor: GoogleBitmapDescriptor
)

/**
 * Android implementation of [BitmapDescriptorFactory].
 */
actual object BitmapDescriptorFactory {

    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor {
        require(bytes.size == width * height * 4) {
            "Byte array size (${bytes.size}) must equal width * height * 4 (${width * height * 4})"
        }
        // convert from ARGB byte order to RGBA byte order
        // Android's copyPixelsFromBuffer expects RGBA, despite ARGB_8888 config name
        val rgbaBytes = ByteArray(bytes.size)
        for (i in bytes.indices step 4) {
            rgbaBytes[i] = bytes[i + 1]     // R (was at index 1)
            rgbaBytes[i + 1] = bytes[i + 2] // G (was at index 2)
            rgbaBytes[i + 2] = bytes[i + 3] // B (was at index 3)
            rgbaBytes[i + 3] = bytes[i]     // A (was at index 0)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgbaBytes))
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }

    actual fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: error("Failed to decode image from byte array")
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }
}
