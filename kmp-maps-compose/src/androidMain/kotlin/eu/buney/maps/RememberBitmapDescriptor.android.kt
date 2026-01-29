package eu.buney.maps

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

/**
 * Android implementation: converts ImageBitmap to BitmapDescriptor via Android Bitmap.
 */
internal actual fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor {
    val androidBitmap = this.asAndroidBitmap()
    return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(androidBitmap))
}
