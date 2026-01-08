package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Represents a circle on the map. This is passed to click callbacks.
 */
expect class Circle {
    /**
     * The center position of the circle.
     */
    val center: LatLng

    /**
     * The radius of the circle in meters.
     */
    val radius: Double
}

/**
 * A composable for a circle on the map.
 *
 * @param center the [LatLng] to use for the center of this circle
 * @param clickable boolean indicating if the circle is clickable or not
 * @param fillColor the fill color of the circle
 * @param radius the radius of the circle in meters
 * @param strokeColor the stroke color of the circle
 * @param strokePattern a sequence of [PatternItem] to be repeated along the circle's outline.
 *   `null` represents a solid line. Note: This parameter is only supported on Android and will
 *   be ignored on iOS.
 * @param strokeWidth the width of the circle's outline in screen pixels
 * @param tag optional tag to be associated with the circle
 * @param visible the visibility of the circle
 * @param zIndex the z-index of the circle. Note: On iOS, fractional values are truncated to integers.
 * @param onClick a lambda invoked when the circle is clicked
 */
@Composable
@GoogleMapComposable
expect fun Circle(
    center: LatLng,
    clickable: Boolean = false,
    fillColor: Color = Color.Transparent,
    radius: Double = 0.0,
    strokeColor: Color = Color.Black,
    strokePattern: List<PatternItem>? = null,
    strokeWidth: Float = 10f,
    tag: Any? = null,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (Circle) -> Unit = {},
)
