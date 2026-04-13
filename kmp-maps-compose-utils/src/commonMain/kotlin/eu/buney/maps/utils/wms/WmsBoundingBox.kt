package eu.buney.maps.utils.wms

import kotlin.math.PI

/**
 * Computes EPSG:3857 (Web Mercator) bounding boxes from tile coordinates.
 *
 * The Web Mercator projection maps the Earth's surface onto a square grid.
 * At zoom level N, the grid is divided into 2^N × 2^N tiles.
 */
internal object WmsBoundingBox {

    /**
     * Maximum extent of the Web Mercator projection in meters.
     * Calculated as Earth's semi-major axis (6,378,137 m) × PI.
     */
    private const val WORLD_EXTENT = 6378137.0 * PI

    /** Total width/height of the world in meters. */
    private const val WORLD_SIZE_METERS = 2 * WORLD_EXTENT

    /**
     * Returns the EPSG:3857 bounding box for the given tile as [xMin, yMin, xMax, yMax].
     *
     * @param x Tile column.
     * @param y Tile row (origin at top-left, y increases southward).
     * @param zoom Zoom level.
     */
    fun getBoundingBox(x: Int, y: Int, zoom: Int): DoubleArray {
        val tilesPerDimension = 1 shl zoom
        val tileSizeMeters = WORLD_SIZE_METERS / tilesPerDimension.toDouble()

        val xMin = -WORLD_EXTENT + (x * tileSizeMeters)
        val xMax = -WORLD_EXTENT + ((x + 1) * tileSizeMeters)

        // Y-axis: tile row 0 is the top (north), WMS expects yMin < yMax
        val yMax = WORLD_EXTENT - (y * tileSizeMeters)
        val yMin = WORLD_EXTENT - ((y + 1) * tileSizeMeters)

        return doubleArrayOf(xMin, yMin, xMax, yMax)
    }
}
