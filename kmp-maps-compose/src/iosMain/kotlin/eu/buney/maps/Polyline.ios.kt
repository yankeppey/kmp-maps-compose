package eu.buney.maps

import GoogleMaps.GMSMutablePath
import GoogleMaps.GMSPolyline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

/**
 * iOS implementation of [Polyline] that wraps GMSPolyline.
 */
@OptIn(ExperimentalForeignApi::class)
actual class Polyline(
    private val gmsPolyline: GMSPolyline
) {
    actual val points: List<LatLng>
        get() {
            val path = gmsPolyline.path ?: return emptyList()
            val count = path.count().toInt()
            return (0 until count).map { index ->
                path.coordinateAtIndex(index.toULong()).useContents {
                    LatLng(latitude, longitude)
                }
            }
        }
}

@OptIn(ExperimentalForeignApi::class)
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
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("Polyline must be used within a GoogleMap composable")

    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val path = GMSMutablePath().apply {
                points.forEach { point ->
                    addLatitude(point.latitude, longitude = point.longitude)
                }
            }

            val gmsPolyline = GMSPolyline.polylineWithPath(path).apply {
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.strokeWidth = width.toDouble() / UIScreen.mainScreen.scale
                this.strokeColor = color.toUIColor()
                this.geodesic = geodesic
                this.tappable = clickable
                // iOS SDK uses int for zIndex; fractional values are truncated
                this.zIndex = zIndex.toInt()
                this.userData = tag
                // startCap, endCap, jointType, pattern are not supported on iOS - ignored
                // attach to map (visibility is controlled by setting map to null or mapView)
                this.map = if (visible) mapApplier.mapView else null
            }

            PolylineNode(
                polyline = gmsPolyline,
                onPolylineClick = onClick,
            )
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            update(points) {
                val path = GMSMutablePath().apply {
                    it.forEach { point ->
                        addLatitude(point.latitude, longitude = point.longitude)
                    }
                }
                this.polyline.path = path
            }
            update(width) {
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.polyline.strokeWidth = it.toDouble() / UIScreen.mainScreen.scale
            }
            update(color) { this.polyline.strokeColor = it.toUIColor() }
            update(geodesic) { this.polyline.geodesic = it }
            update(clickable) { this.polyline.tappable = it }
            update(zIndex) { this.polyline.zIndex = it.toInt() }
            update(tag) { this.polyline.userData = it }
            update(visible) {
                this.polyline.map = if (it) mapApplier.mapView else null
            }
            // startCap, endCap, jointType, pattern updates are ignored on iOS
        }
    )
}

/**
 * iOS implementation of styled [Polyline] with spans support.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun Polyline(
    points: List<LatLng>,
    spans: List<StyleSpan>,
    clickable: Boolean,
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
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("Polyline must be used within a GoogleMap composable")

    val gmsSpans = spans.toGMSStyleSpans()

    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val path = GMSMutablePath().apply {
                points.forEach { point ->
                    addLatitude(point.latitude, longitude = point.longitude)
                }
            }

            val gmsPolyline = GMSPolyline.polylineWithPath(path).apply {
                this.strokeWidth = width.toDouble() / UIScreen.mainScreen.scale
                // Note: When using spans, the individual span styles override strokeColor
                this.geodesic = geodesic
                this.tappable = clickable
                this.zIndex = zIndex.toInt()
                this.userData = tag
                // Apply the style spans
                this.spans = gmsSpans
                this.map = if (visible) mapApplier.mapView else null
            }

            PolylineNode(
                polyline = gmsPolyline,
                onPolylineClick = onClick,
            )
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            update(points) {
                val path = GMSMutablePath().apply {
                    it.forEach { point ->
                        addLatitude(point.latitude, longitude = point.longitude)
                    }
                }
                this.polyline.path = path
            }
            update(spans) {
                this.polyline.spans = it.toGMSStyleSpans()
            }
            update(width) {
                this.polyline.strokeWidth = it.toDouble() / UIScreen.mainScreen.scale
            }
            update(geodesic) { this.polyline.geodesic = it }
            update(clickable) { this.polyline.tappable = it }
            update(zIndex) { this.polyline.zIndex = it.toInt() }
            update(tag) { this.polyline.userData = it }
            update(visible) {
                this.polyline.map = if (it) mapApplier.mapView else null
            }
        }
    )
}
