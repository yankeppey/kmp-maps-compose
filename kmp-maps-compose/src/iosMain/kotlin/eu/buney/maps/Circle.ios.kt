package eu.buney.maps

import GoogleMaps.GMSCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIScreen

/**
 * iOS implementation of [Circle] that wraps GMSCircle.
 */
@OptIn(ExperimentalForeignApi::class)
actual class Circle(
    private val gmsCircle: GMSCircle
) {
    actual val center: LatLng
        get() = gmsCircle.position.useContents {
            LatLng(latitude, longitude)
        }

    actual val radius: Double
        get() = gmsCircle.radius
}

@OptIn(ExperimentalForeignApi::class)
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
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("Circle must be used within a GoogleMap composable")

    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val gmsCircle = GMSCircle.circleWithPosition(
                position = CLLocationCoordinate2DMake(center.latitude, center.longitude),
                radius = radius
            ).apply {
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.strokeWidth = strokeWidth.toDouble() / UIScreen.mainScreen.scale
                this.strokeColor = strokeColor.toUIColor()
                this.fillColor = fillColor.toUIColor()
                this.tappable = clickable
                // iOS SDK uses int for zIndex; fractional values are truncated
                this.zIndex = zIndex.toInt()
                this.userData = tag
                // strokePattern is not supported on iOS - ignored
                // attach to map (visibility is controlled by setting map to null or mapView)
                this.map = if (visible) mapApplier.mapView else null
            }

            CircleNode(
                circle = gmsCircle,
                onCircleClick = onClick,
            )
        },
        update = {
            update(onClick) { this.onCircleClick = it }

            update(center) {
                this.circle.position = CLLocationCoordinate2DMake(it.latitude, it.longitude)
            }
            update(radius) { this.circle.radius = it }
            update(strokeWidth) {
                // iOS uses points, Android uses pixels; divide by scale for consistency
                this.circle.strokeWidth = it.toDouble() / UIScreen.mainScreen.scale
            }
            update(strokeColor) { this.circle.strokeColor = it.toUIColor() }
            update(fillColor) { this.circle.fillColor = it.toUIColor() }
            update(clickable) { this.circle.tappable = it }
            update(zIndex) { this.circle.zIndex = it.toInt() }
            update(tag) { this.circle.userData = it }
            update(visible) {
                this.circle.map = if (it) mapApplier.mapView else null
            }
            // strokePattern updates are ignored on iOS
        }
    )
}
