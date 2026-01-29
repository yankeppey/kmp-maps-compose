package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

expect class Polyline {
    val points: List<LatLng>
}

/**
 * A composable that draws a polyline on the map.
 *
 * @param points The vertices of the polyline.
 * @param clickable Whether the polyline is clickable. Default is false.
 * @param color The color of the polyline. Default is [Color.Black].
 * @param endCap The cap at the end vertex. Default is [Cap.Butt]. Android-only, ignored on iOS.
 * @param geodesic Whether the polyline should be drawn as a geodesic. Default is false.
 * @param jointType The joint type for vertices. Default is [JointType.Default]. Android-only, ignored on iOS.
 * @param pattern The stroke pattern. Default is null (solid line). Android-only, ignored on iOS.
 * @param startCap The cap at the start vertex. Default is [Cap.Butt]. Android-only, ignored on iOS.
 * @param tag An arbitrary object associated with the polyline.
 * @param visible Whether the polyline is visible. Default is true.
 * @param width The width of the polyline in screen pixels. Default is 10f.
 * @param zIndex The z-index of the polyline. Default is 0f.
 *   Note: On iOS, fractional values are truncated to integers.
 * @param onClick A callback invoked when the polyline is clicked.
 */
@Composable
@GoogleMapComposable
expect fun Polyline(
    points: List<LatLng>,
    clickable: Boolean = false,
    color: Color = Color.Black,
    endCap: Cap = Cap.Butt,
    geodesic: Boolean = false,
    jointType: JointType = JointType.Default,
    pattern: List<PatternItem>? = null,
    startCap: Cap = Cap.Butt,
    tag: Any? = null,
    visible: Boolean = true,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {},
)

/**
 * A composable that draws a styled polyline on the map with span-based styling.
 *
 * This overload supports advanced styling through [StyleSpan]s, enabling:
 * - Per-segment coloring (solid or gradient)
 * - Stamped/textured polylines with repeating images
 *
 * @param points The vertices of the polyline.
 * @param spans The style spans defining appearance for polyline segments.
 *              If spans cover fewer segments than points, the final span
 *              style applies to the remaining length.
 * @param clickable Whether the polyline is clickable. Default is false.
 * @param endCap The cap at the end vertex. Default is [Cap.Butt]. Android-only, ignored on iOS.
 * @param geodesic Whether the polyline should be drawn as a geodesic. Default is false.
 * @param jointType The joint type for vertices. Default is [JointType.Default]. Android-only, ignored on iOS.
 * @param pattern The stroke pattern. Default is null (solid line). Android-only, ignored on iOS.
 * @param startCap The cap at the start vertex. Default is [Cap.Butt]. Android-only, ignored on iOS.
 * @param tag An arbitrary object associated with the polyline.
 * @param visible Whether the polyline is visible. Default is true.
 * @param width The width of the polyline in screen pixels. Default is 10f.
 * @param zIndex The z-index of the polyline. Default is 0f.
 *   Note: On iOS, fractional values are truncated to integers.
 * @param onClick A callback invoked when the polyline is clicked.
 */
@Composable
@GoogleMapComposable
expect fun Polyline(
    points: List<LatLng>,
    spans: List<StyleSpan>,
    clickable: Boolean = false,
    endCap: Cap = Cap.Butt,
    geodesic: Boolean = false,
    jointType: JointType = JointType.Default,
    pattern: List<PatternItem>? = null,
    startCap: Cap = Cap.Butt,
    tag: Any? = null,
    visible: Boolean = true,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {},
)
