package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.Dash as GoogleDash
import com.google.android.gms.maps.model.Dot as GoogleDot
import com.google.android.gms.maps.model.Gap as GoogleGap
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.PatternItem as GooglePatternItem
import com.google.android.gms.maps.model.Polyline as GooglePolyline
import com.google.maps.android.compose.Polyline as AndroidPolyline

/**
 * Android implementation of [Polyline] that wraps the platform polyline.
 */
actual class Polyline(
    private val googlePolyline: GooglePolyline
) {
    actual val points: List<LatLng>
        get() = googlePolyline.points.map { LatLng(it.latitude, it.longitude) }
}

@Composable
@GoogleMapComposable
actual fun Polyline(
    points: List<LatLng>,
    clickable: Boolean,
    color: Color,
    endCap: Cap,
    geodesic: Boolean,
    jointType: JointType,
    pattern: List<PatternItem>?,
    startCap: Cap,
    tag: Any?,
    visible: Boolean,
    width: Float,
    zIndex: Float,
    onClick: (Polyline) -> Unit,
) {
    AndroidPolyline(
        points = points.map { GoogleLatLng(it.latitude, it.longitude) },
        clickable = clickable,
        color = color,
        endCap = endCap.toGoogleCap(),
        geodesic = geodesic,
        jointType = jointType.toGoogleJointType(),
        pattern = pattern?.toGooglePatternItems(),
        startCap = startCap.toGoogleCap(),
        tag = tag,
        visible = visible,
        width = width,
        zIndex = zIndex,
        onClick = { googlePolyline ->
            onClick(Polyline(googlePolyline))
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
