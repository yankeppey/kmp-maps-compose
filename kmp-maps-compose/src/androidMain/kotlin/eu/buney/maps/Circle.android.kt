package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.Circle as GoogleCircle
import com.google.android.gms.maps.model.Dash as GoogleDash
import com.google.android.gms.maps.model.Dot as GoogleDot
import com.google.android.gms.maps.model.Gap as GoogleGap
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.PatternItem as GooglePatternItem
import com.google.maps.android.compose.Circle as AndroidCircle

/**
 * Android implementation of [Circle] that wraps the platform circle.
 */
actual class Circle(
    private val googleCircle: GoogleCircle
) {
    actual val center: LatLng
        get() = LatLng(googleCircle.center.latitude, googleCircle.center.longitude)

    actual val radius: Double
        get() = googleCircle.radius
}

@Composable
@GoogleMapComposable
actual fun Circle(
    center: LatLng,
    clickable: Boolean,
    fillColor: Color,
    radius: Double,
    strokeColor: Color,
    strokePattern: List<PatternItem>?,
    strokeWidth: Float,
    tag: Any?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Circle) -> Unit,
) {
    AndroidCircle(
        center = GoogleLatLng(center.latitude, center.longitude),
        clickable = clickable,
        fillColor = fillColor,
        radius = radius,
        strokeColor = strokeColor,
        strokePattern = strokePattern?.toGooglePatternItems(),
        strokeWidth = strokeWidth,
        tag = tag,
        visible = visible,
        zIndex = zIndex,
        onClick = { googleCircle ->
            onClick(Circle(googleCircle))
        },
    )
}

/**
 * Converts a list of multiplatform [PatternItem]s to Google Maps Android SDK pattern items.
 */
private fun List<PatternItem>.toGooglePatternItems(): List<GooglePatternItem> {
    return map { item ->
        when (item) {
            is PatternItem.Dash -> GoogleDash(item.length)
            is PatternItem.Gap -> GoogleGap(item.length)
            is PatternItem.Dot -> GoogleDot()
        }
    }
}
