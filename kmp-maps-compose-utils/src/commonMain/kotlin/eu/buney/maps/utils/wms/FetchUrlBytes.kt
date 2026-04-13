package eu.buney.maps.utils.wms

/**
 * Fetches raw bytes from a URL. Called from background threads by the tile provider.
 *
 * @return The response bytes, or null on failure.
 */
internal expect fun fetchUrlBytes(url: String): ByteArray?
