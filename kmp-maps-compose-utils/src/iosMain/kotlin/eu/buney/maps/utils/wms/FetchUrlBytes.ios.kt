package eu.buney.maps.utils.wms

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal actual fun fetchUrlBytes(url: String): ByteArray? {
    return try {
        val nsUrl = NSURL(string = url)
        val data = NSData.dataWithContentsOfURL(nsUrl) ?: return null
        val length = data.length.toInt()
        if (length == 0) return null
        ByteArray(length).also { bytes ->
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    } catch (_: Exception) {
        null
    }
}
