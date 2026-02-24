package eu.buney.maps.utils.clustering.algo

import eu.buney.maps.LatLng
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sin

internal class SphericalMercatorProjection(private val worldWidth: Double) {

    fun toPoint(latLng: LatLng): Point {
        val x = latLng.longitude / 360 + 0.5
        val siny = sin(latLng.latitude * PI / 180.0)
        val y = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * PI) + 0.5
        return Point(x * worldWidth, y * worldWidth)
    }
}
