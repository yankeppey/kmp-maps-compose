package eu.buney.maps

/**
 * Represents a rectangular geographic area defined by two corner points.
 *
 * @property southwest The south-west corner of the bounds.
 * @property northeast The north-east corner of the bounds.
 */
data class LatLngBounds(
    val southwest: LatLng,
    val northeast: LatLng,
) {
    /**
     * The center point of this bounds.
     */
    val center: LatLng
        get() = LatLng(
            latitude = (southwest.latitude + northeast.latitude) / 2,
            longitude = (southwest.longitude + northeast.longitude) / 2,
        )

    /**
     * Builder for creating [LatLngBounds] instances.
     */
    class Builder {
        private var southWestLat = Double.MAX_VALUE
        private var southWestLng = Double.MAX_VALUE
        private var northEastLat = -Double.MAX_VALUE
        private var northEastLng = -Double.MAX_VALUE

        /**
         * Includes the given [LatLng] in the bounds being built.
         */
        fun include(point: LatLng): Builder {
            southWestLat = minOf(southWestLat, point.latitude)
            southWestLng = minOf(southWestLng, point.longitude)
            northEastLat = maxOf(northEastLat, point.latitude)
            northEastLng = maxOf(northEastLng, point.longitude)
            return this
        }

        /**
         * Builds the [LatLngBounds] from the included points.
         *
         * @throws IllegalStateException if no points have been included.
         */
        fun build(): LatLngBounds {
            require(southWestLat != Double.MAX_VALUE) {
                "Cannot build LatLngBounds: no points have been included"
            }
            return LatLngBounds(
                southwest = LatLng(southWestLat, southWestLng),
                northeast = LatLng(northEastLat, northEastLng),
            )
        }
    }
}
