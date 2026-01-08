package eu.buney.maps

/**
 * Types of map tiles to display.
 */
enum class MapType {
    /**
     * No base map tiles. Useful for displaying only custom overlays.
     */
    NONE,

    /**
     * Basic map with roads, labels, and political boundaries.
     */
    NORMAL,

    /**
     * Satellite imagery without labels.
     */
    SATELLITE,

    /**
     * Satellite imagery with roads and labels overlaid.
     */
    HYBRID,

    /**
     * Topographic data showing terrain and vegetation.
     * Note: On iOS, this falls back to NORMAL as Apple Maps doesn't have a terrain type.
     */
    TERRAIN
}
