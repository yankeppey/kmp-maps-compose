package eu.buney.maps

import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor as GoogleBitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

/**
 * Android implementation of [BitmapDescriptor].
 * Wraps the Google Maps SDK BitmapDescriptor.
 */
actual class BitmapDescriptor(
    val googleBitmapDescriptor: GoogleBitmapDescriptor
)

/**
 * Android implementation of [BitmapDescriptorFactory].
 */
actual object BitmapDescriptorFactory {

    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor {
        val bitmap = argbBytesToBitmap(bytes, width, height)
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }

    actual fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: error("Failed to decode image from byte array")
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }
}
