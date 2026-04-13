package eu.buney.maps.utils.wms

import java.net.URL

internal actual fun fetchUrlBytes(url: String): ByteArray? {
    return try {
        URL(url).openStream().use { it.readBytes() }
    } catch (_: Exception) {
        null
    }
}
