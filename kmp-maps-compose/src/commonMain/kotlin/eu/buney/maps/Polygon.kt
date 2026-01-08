package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

expect class Polygon {
    val points: List<LatLng>
    val holes: List<List<LatLng>>
}

/**
 * A composable that draws a polygon on the map.
 *
 * @param points The vertices of the polygon outline.
 * @param clickable Whether the polygon is clickable. Default is false.
 * @param fillColor The fill color of the polygon. Default is [Color.Black].
 * @param geodesic Whether the polygon should be drawn as a geodesic. Default is false.
 * @param holes A list of holes in the polygon, where each hole is a list of vertices.
 * @param strokeColor The color of the polygon outline. Default is [Color.Black].
 * @param strokeJointType The joint type for vertices. Default is [JointType.Default]. Android-only, ignored on iOS.
 * @param strokePattern The stroke pattern. Default is null (solid line). Android-only, ignored on iOS.
 * @param strokeWidth The width of the polygon outline in screen pixels. Default is 10f.
 * @param tag An arbitrary object associated with the polygon.
 * @param visible Whether the polygon is visible. Default is true.
 * @param zIndex The z-index of the polygon. Default is 0f.
 *   Note: On iOS, fractional values are truncated to integers.
 * @param onClick A callback invoked when the polygon is clicked.
 */
@Composable
@GoogleMapComposable
expect fun Polygon(
    points: List<LatLng>,
    clickable: Boolean = false,
    fillColor: Color = Color.Black,
    geodesic: Boolean = false,
    holes: List<List<LatLng>> = emptyList(),
    strokeColor: Color = Color.Black,
    strokeJointType: JointType = JointType.Default,
    strokePattern: List<PatternItem>? = null,
    strokeWidth: Float = 10f,
    tag: Any? = null,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (Polygon) -> Unit = {},
)
