package eu.buney.maps

import GoogleMaps.GMSStrokeStyle
import GoogleMaps.GMSStyleSpan
import GoogleMaps.GMSTextureStyle
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Converts a kmp-maps-compose [StyleSpan] to a Google Maps iOS SDK [GMSStyleSpan].
 */
@OptIn(ExperimentalForeignApi::class)
internal fun StyleSpan.toGMSStyleSpan(): GMSStyleSpan {
    val gmsStrokeStyle: GMSStrokeStyle = when (val s = style) {
        is StrokeStyle.SolidColor -> GMSStrokeStyle.solidColor(s.color.toUIColor())
        is StrokeStyle.Gradient -> GMSStrokeStyle.gradientFromColor(
            s.fromColor.toUIColor(),
            toColor = s.toColor.toUIColor()
        )
    }

    // Apply stamp style if present
    if (stampStyle != null) {
        val gmsTextureStyle = GMSTextureStyle(image = stampStyle.image.uiImage)
        gmsStrokeStyle.stampStyle = gmsTextureStyle
    }

    return GMSStyleSpan.spanWithStyle(gmsStrokeStyle, segments = segments)
}

/**
 * Converts a list of [StyleSpan]s to Google Maps iOS SDK style spans.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun List<StyleSpan>.toGMSStyleSpans(): List<GMSStyleSpan> =
    map { it.toGMSStyleSpan() }
