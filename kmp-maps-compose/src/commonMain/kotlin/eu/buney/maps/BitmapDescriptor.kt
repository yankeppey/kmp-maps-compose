package eu.buney.maps

/**
 * Represents an image that can be used for map overlays such as markers and ground overlays.
 *
 * This is a multiplatform abstraction over platform-specific image types:
 * - Android: `com.google.android.gms.maps.model.BitmapDescriptor`
 * - iOS: `UIImage`
 *
 * Use [BitmapDescriptorFactory] to create instances.
 */
expect class BitmapDescriptor

/**
 * Factory for creating [BitmapDescriptor] instances from various image sources.
 */
expect object BitmapDescriptorFactory {
    /**
     * Creates a [BitmapDescriptor] from raw ARGB pixel data.
     *
     * @param bytes Raw pixel data in ARGB format (4 bytes per pixel: alpha, red, green, blue).
     *              The array size must be exactly `width * height * 4`.
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @return A [BitmapDescriptor] representing the image.
     */
    fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor

    /**
     * Creates a [BitmapDescriptor] from encoded image data (PNG, JPEG, etc.).
     *
     * @param data The encoded image data.
     * @return A [BitmapDescriptor] representing the decoded image.
     */
    fun fromEncodedImage(data: ByteArray): BitmapDescriptor
}
