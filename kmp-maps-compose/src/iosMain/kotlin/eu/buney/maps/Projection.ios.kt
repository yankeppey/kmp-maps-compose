package eu.buney.maps

import GoogleMaps.GMSProjection
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreLocation.CLLocationCoordinate2DMake

/**
 * iOS implementation of [Projection] that wraps [GMSProjection].
 */
@OptIn(ExperimentalForeignApi::class)
internal class IOSProjection(
    private val gmsProjection: GMSProjection,
) : Projection {

    override fun toScreenLocation(latLng: LatLng): ScreenPoint {
        val coordinate = CLLocationCoordinate2DMake(latLng.latitude, latLng.longitude)
        val point = gmsProjection.pointForCoordinate(coordinate)
        return point.useContents {
            ScreenPoint(x.toFloat(), y.toFloat())
        }
    }

    override fun fromScreenLocation(point: ScreenPoint): LatLng {
        val cgPoint = CGPointMake(point.x.toDouble(), point.y.toDouble())
        val coordinate = gmsProjection.coordinateForPoint(cgPoint)
        return coordinate.useContents {
            LatLng(latitude, longitude)
        }
    }

    override val visibleBounds: LatLngBounds
        get() {
            // Extract bounds from visible region (4 corners)
            val visibleRegion = gmsProjection.visibleRegion()
            return visibleRegion.useContents {
                val lats = listOf(
                    nearLeft.latitude,
                    nearRight.latitude,
                    farLeft.latitude,
                    farRight.latitude
                )
                val lngs = listOf(
                    nearLeft.longitude,
                    nearRight.longitude,
                    farLeft.longitude,
                    farRight.longitude
                )
                LatLngBounds(
                    southwest = LatLng(lats.min(), lngs.min()),
                    northeast = LatLng(lats.max(), lngs.max())
                )
            }
        }

    override fun contains(latLng: LatLng): Boolean {
        val coordinate = CLLocationCoordinate2DMake(latLng.latitude, latLng.longitude)
        return gmsProjection.containsCoordinate(coordinate)
    }
}
