package eu.buney.maps

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.StrokeStyle as GoogleStrokeStyle
import com.google.android.gms.maps.model.StyleSpan as GoogleStyleSpan
import com.google.android.gms.maps.model.TextureStyle as GoogleTextureStyle

/**
 * Converts a kmp-maps-compose [StyleSpan] to a Google Maps Android SDK [GoogleStyleSpan].
 */
internal fun StyleSpan.toGoogleStyleSpan(): GoogleStyleSpan {
    val googleStrokeStyle = when (val s = style) {
        is StrokeStyle.SolidColor -> GoogleStrokeStyle.colorBuilder(s.color.toArgb()).build()
        is StrokeStyle.Gradient -> GoogleStrokeStyle.gradientBuilder(
            s.fromColor.toArgb(),
            s.toColor.toArgb()
        ).build()
    }

    // Apply stamp style if present
    val finalStrokeStyle = if (stampStyle != null) {
        val textureStyle = GoogleTextureStyle.newBuilder(
            stampStyle.image.googleBitmapDescriptor
        ).build()
        // Note: Android SDK applies stamp via StrokeStyle.Builder.stamp()
        // We need to rebuild with the stamp
        when (val s = style) {
            is StrokeStyle.SolidColor -> GoogleStrokeStyle.colorBuilder(s.color.toArgb())
                .stamp(textureStyle)
                .build()
            is StrokeStyle.Gradient -> GoogleStrokeStyle.gradientBuilder(
                s.fromColor.toArgb(),
                s.toColor.toArgb()
            ).stamp(textureStyle).build()
        }
    } else {
        googleStrokeStyle
    }

    return GoogleStyleSpan(finalStrokeStyle, segments)
}

/**
 * Converts a list of [StyleSpan]s to Google Maps Android SDK style spans.
 */
internal fun List<StyleSpan>.toGoogleStyleSpans(): List<GoogleStyleSpan> =
    map { it.toGoogleStyleSpan() }
