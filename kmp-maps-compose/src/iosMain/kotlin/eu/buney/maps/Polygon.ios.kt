package eu.buney.maps

import GoogleMaps.GMSMutablePath
import GoogleMaps.GMSPath
import GoogleMaps.GMSPolygon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

/**
 * iOS implementation of [Polygon] that wraps GMSPolygon.
 */
@OptIn(ExperimentalForeignApi::class)
actual class Polygon(
    private val gmsPolygon: GMSPolygon
) {
    actual val points: List<LatLng>
        get() {
            val path = gmsPolygon.path ?: return emptyList()
            return path.toLatLngList()
        }

    actual val holes: List<List<LatLng>>
        get() {
            @Suppress("UNCHECKED_CAST")
            val holesArray = gmsPolygon.holes as? List<GMSPath> ?: return emptyList()
            return holesArray.map { it.toLatLngList() }
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun GMSPath.toLatLngList(): List<LatLng> {
    val count = this.count().toInt()
    return (0 until count).map { index ->
        this.coordinateAtIndex(index.toULong()).useContents {
            LatLng(latitude, longitude)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
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
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("Polygon must be used within a GoogleMap composable")

    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val path = points.toGMSPath()
            val holesArray = holes.map { it.toGMSPath() }

            val gmsPolygon = GMSPolygon.polygonWithPath(path).apply {
                this.holes = holesArray
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.strokeWidth = strokeWidth.toDouble() / UIScreen.mainScreen.scale
                this.strokeColor = strokeColor.toUIColor()
                this.fillColor = fillColor.toUIColor()
                this.geodesic = geodesic
                this.tappable = clickable
                // iOS SDK uses int for zIndex; fractional values are truncated
                this.zIndex = zIndex.toInt()
                this.userData = tag
                // strokeJointType and strokePattern are not supported on iOS - ignored
                // attach to map (visibility is controlled by setting map to null or mapView)
                this.map = if (visible) mapApplier.mapView else null
            }

            PolygonNode(
                polygon = gmsPolygon,
                onPolygonClick = onClick,
            )
        },
        update = {
            update(onClick) { this.onPolygonClick = it }

            update(points) {
                this.polygon.path = it.toGMSPath()
            }
            update(holes) {
                this.polygon.holes = it.map { hole -> hole.toGMSPath() }
            }
            update(strokeWidth) {
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.polygon.strokeWidth = it.toDouble() / UIScreen.mainScreen.scale
            }
            update(strokeColor) { this.polygon.strokeColor = it.toUIColor() }
            update(fillColor) { this.polygon.fillColor = it.toUIColor() }
            update(geodesic) { this.polygon.geodesic = it }
            update(clickable) { this.polygon.tappable = it }
            update(zIndex) { this.polygon.zIndex = it.toInt() }
            update(tag) { this.polygon.userData = it }
            update(visible) {
                this.polygon.map = if (it) mapApplier.mapView else null
            }
            // strokeJointType and strokePattern updates are ignored on iOS
        }
    )
}

/**
 * Converts a list of [LatLng] to a [GMSMutablePath].
 */
@OptIn(ExperimentalForeignApi::class)
private fun List<LatLng>.toGMSPath(): GMSMutablePath {
    return GMSMutablePath().apply {
        this@toGMSPath.forEach { point ->
            addLatitude(point.latitude, longitude = point.longitude)
        }
    }
}
