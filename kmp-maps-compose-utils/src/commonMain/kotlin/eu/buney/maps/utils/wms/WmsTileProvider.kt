package eu.buney.maps.utils.wms

import eu.buney.maps.Tile
import eu.buney.maps.TileFactory
import eu.buney.maps.TileProvider

/**
 * A [TileProvider] for Web Map Service (WMS) layers using EPSG:3857 (Web Mercator).
 *
 * Converts tile coordinates to a bounding box, builds a WMS URL via the supplied
 * [urlFormatter], fetches the image, and returns it as a [Tile].
 *
 * @param urlFormatter Builds the WMS URL for the given bounding box and zoom level.
 * @param tileWidth Tile width in pixels (default 256).
 * @param tileHeight Tile height in pixels (default 256).
 */
class WmsTileProvider(
    private val urlFormatter: (xMin: Double, yMin: Double, xMax: Double, yMax: Double, zoom: Int) -> String,
    private val tileWidth: Int = 256,
    private val tileHeight: Int = 256,
) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        val bbox = WmsBoundingBox.getBoundingBox(x, y, zoom)
        val url = urlFormatter(bbox[0], bbox[1], bbox[2], bbox[3], zoom)
        val bytes = fetchUrlBytes(url) ?: return null
        return TileFactory.fromEncodedImage(bytes, tileWidth, tileHeight)
    }
}
