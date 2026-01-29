package eu.buney.maps

import GoogleMaps.GMSCoordinateBounds
import GoogleMaps.GMSGeometryOffset
import GoogleMaps.GMSGroundOverlay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreLocation.CLLocationCoordinate2DMake

/**
 * iOS implementation of [GroundOverlay].
 * Wraps a [GMSGroundOverlay].
 */
@OptIn(ExperimentalForeignApi::class)
actual class GroundOverlay(
    val gmsGroundOverlay: GMSGroundOverlay
) {
    actual val bounds: LatLngBounds = gmsGroundOverlay.bounds?.let { gmsBounds ->
        gmsBounds.southWest.useContents {
            val sw = LatLng(latitude, longitude)
            gmsBounds.northEast.useContents {
                val ne = LatLng(latitude, longitude)
                LatLngBounds(southwest = sw, northeast = ne)
            }
        }
    } ?: gmsGroundOverlay.position.useContents {
        // fallback if bounds is null - use position as center
        val center = LatLng(latitude, longitude)
        LatLngBounds(southwest = center, northeast = center)
    }

    actual val bearing: Float = gmsGroundOverlay.bearing.toFloat()

    actual val transparency: Float = (1.0 - gmsGroundOverlay.opacity).toFloat()
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset,
    bearing: Float,
    clickable: Boolean,
    tag: Any?,
    transparency: Float,
    visible: Boolean,
    zIndex: Float,
    onClick: (GroundOverlay) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("GroundOverlay must be used within a GoogleMap composable")

    ComposeNode<GroundOverlayNode, MapApplier>(
        factory = {
            val bounds = position.toGMSCoordinateBounds()
            val gmsGroundOverlay = GMSGroundOverlay.groundOverlayWithBounds(
                bounds = bounds,
                icon = image.uiImage
            ).apply {
                this.bearing = bearing.toDouble()
                this.opacity = 1f - transparency
                this.anchor = CGPointMake(anchor.x.toDouble(), anchor.y.toDouble())
                this.tappable = clickable
                // iOS SDK uses int for zIndex; fractional values are truncated
                this.zIndex = zIndex.toInt()
                this.userData = tag
                // attach to map (visibility is controlled by setting map to null or mapView)
                this.map = if (visible) mapApplier.mapView else null
            }

            GroundOverlayNode(
                groundOverlay = gmsGroundOverlay,
                onGroundOverlayClick = onClick,
            )
        },
        update = {
            update(onClick) { this.onGroundOverlayClick = it }

            update(position) {
                this.groundOverlay.bounds = it.toGMSCoordinateBounds()
            }
            update(image) {
                this.groundOverlay.icon = it.uiImage
            }
            update(bearing) {
                this.groundOverlay.bearing = it.toDouble()
            }
            update(transparency) {
                this.groundOverlay.opacity = 1f - it
            }
            update(anchor) {
                this.groundOverlay.anchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(clickable) { this.groundOverlay.tappable = it }
            update(zIndex) { this.groundOverlay.zIndex = it.toInt() }
            update(tag) { this.groundOverlay.userData = it }
            update(visible) {
                this.groundOverlay.map = if (it) mapApplier.mapView else null
            }
        }
    )
}

/**
 * Converts a [GroundOverlayPosition] to [GMSCoordinateBounds].
 *
 * For location-based positioning with width/height in meters, we use [GMSGeometryOffset]
 * to calculate the bounds corners using proper great circle geometry.
 *
 * Note: This conversion is only needed on iOS. On Android, the Google Maps SDK handles
 * location+dimensions positioning natively via GroundOverlayOptions.position(LatLng, width, height).
 */
@OptIn(ExperimentalForeignApi::class)
private fun GroundOverlayPosition.toGMSCoordinateBounds(): GMSCoordinateBounds {
    return when {
        latLngBounds != null -> {
            val sw = CLLocationCoordinate2DMake(
                latLngBounds.southwest.latitude,
                latLngBounds.southwest.longitude
            )
            val ne = CLLocationCoordinate2DMake(
                latLngBounds.northeast.latitude,
                latLngBounds.northeast.longitude
            )
            GMSCoordinateBounds(_coordinate = sw, coordinate = ne)
        }
        location != null && width != null -> {
            // calculate bounds from center location and dimensions in meters.
            // we use GMSGeometryOffset which computes destination coordinates along
            // great circle arcs - more accurate than simple degree approximations.
            val center = CLLocationCoordinate2DMake(location.latitude, location.longitude)

            val halfWidth = (width / 2).toDouble()
            val halfHeight = if (height != null) {
                (height / 2).toDouble()
            } else {
                // if no height specified, use width for square aspect ratio
                halfWidth
            }

            // offset from center to get south and west edges
            // heading: 0° = North, 90° = East, 180° = South, 270° = West
            val south = GMSGeometryOffset(center, halfHeight, 180.0) // South
            val west = GMSGeometryOffset(center, halfWidth, 270.0)   // West
            val north = GMSGeometryOffset(center, halfHeight, 0.0)   // North
            val east = GMSGeometryOffset(center, halfWidth, 90.0)    // East

            // build SW and NE corners from the edge coordinates
            val sw = CLLocationCoordinate2DMake(
                south.useContents { latitude },
                west.useContents { longitude }
            )
            val ne = CLLocationCoordinate2DMake(
                north.useContents { latitude },
                east.useContents { longitude }
            )
            GMSCoordinateBounds(_coordinate = sw, coordinate = ne)
        }
        else -> error("Invalid GroundOverlayPosition: must specify either latLngBounds or location+width")
    }
}
