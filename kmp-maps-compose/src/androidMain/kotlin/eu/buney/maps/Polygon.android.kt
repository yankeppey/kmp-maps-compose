package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.Dash as GoogleDash
import com.google.android.gms.maps.model.Dot as GoogleDot
import com.google.android.gms.maps.model.Gap as GoogleGap
import com.google.android.gms.maps.model.PatternItem as GooglePatternItem
import com.google.android.gms.maps.model.Polygon as GooglePolygon
import com.google.maps.android.compose.Polygon as AndroidPolygon

/**
 * Android implementation of [Polygon] that wraps the platform polygon.
 */
actual class Polygon(
    val googlePolygon: GooglePolygon
) {
    actual val points: List<LatLng> =
        googlePolygon.points.map { LatLng(it.latitude, it.longitude) }

    actual val holes: List<List<LatLng>> =
        googlePolygon.holes.map { hole ->
            hole.map { LatLng(it.latitude, it.longitude) }
        }
}

@Composable
@GoogleMapComposable
actual fun Polygon(
    points: List<LatLng>,
    clickable: Boolean,
    fillColor: Color,
    geodesic: Boolean,
    holes: List<List<LatLng>>,
    strokeColor: Color,
    strokeJointType: JointType,
    strokePattern: List<PatternItem>?,
    strokeWidth: Float,
    tag: Any?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Polygon) -> Unit,
) {
    // Remember transformed lists to avoid recreating on every recomposition
    val googlePoints = remember(points) {
        points.map(LatLng::toGoogleLatLng)
    }
    val googleHoles = remember(holes) {
        holes.map { hole -> hole.map(LatLng::toGoogleLatLng) }
    }
    val googleStrokePattern = remember(strokePattern) {
        strokePattern?.toGooglePatternItems()
    }

    AndroidPolygon(
        points = googlePoints,
        clickable = clickable,
        fillColor = fillColor,
        geodesic = geodesic,
        holes = googleHoles,
        strokeColor = strokeColor,
        strokeJointType = strokeJointType.toGoogleJointType(),
        strokePattern = googleStrokePattern,
        strokeWidth = strokeWidth,
        tag = tag,
        visible = visible,
        zIndex = zIndex,
        onClick = { googlePolygon ->
            onClick(Polygon(googlePolygon))
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
