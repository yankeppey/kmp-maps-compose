package eu.buney.maps

import androidx.compose.ui.graphics.Color

/**
 * Describes the drawing style for polyline segments.
 *
 * Supports solid colors and gradients. For stamped/textured polylines,
 * combine with [StampStyle].
 */
sealed class StrokeStyle {
    /**
     * A solid color stroke style.
     *
     * @param color The color of the stroke.
     */
    data class SolidColor(val color: Color) : StrokeStyle()

    /**
     * A gradient stroke style that interpolates between two colors.
     *
     * @param fromColor The color at the start of the gradient.
     * @param toColor The color at the end of the gradient.
     */
    data class Gradient(val fromColor: Color, val toColor: Color) : StrokeStyle()
}
