package eu.buney.maps

import android.graphics.Point
import com.google.android.gms.maps.Projection as GoogleProjection

/**
 * Android implementation of [Projection] that wraps [GoogleProjection].
 */
internal class AndroidProjection(
    private val googleProjection: GoogleProjection,
) : Projection {

    override fun toScreenLocation(latLng: LatLng): ScreenPoint {
        val point = googleProjection.toScreenLocation(latLng.toGoogleLatLng())
        return ScreenPoint(point.x.toFloat(), point.y.toFloat())
    }

    override fun fromScreenLocation(point: ScreenPoint): LatLng {
        val googleLatLng = googleProjection.fromScreenLocation(
            Point(point.x.toInt(), point.y.toInt())
        )
        return LatLng(googleLatLng.latitude, googleLatLng.longitude)
    }

    override val visibleBounds: LatLngBounds
        get() {
            val googleBounds = googleProjection.visibleRegion.latLngBounds
            return LatLngBounds(
                southwest = LatLng(
                    googleBounds.southwest.latitude,
                    googleBounds.southwest.longitude
                ),
                northeast = LatLng(
                    googleBounds.northeast.latitude,
                    googleBounds.northeast.longitude
                )
            )
        }

    override fun contains(latLng: LatLng): Boolean {
        return googleProjection.visibleRegion.latLngBounds.contains(latLng.toGoogleLatLng())
    }
}
