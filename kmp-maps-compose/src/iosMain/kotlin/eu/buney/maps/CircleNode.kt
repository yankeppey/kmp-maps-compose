package eu.buney.maps

import GoogleMaps.GMSCircle
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * [MapNode] implementation for circles.
 *
 * Holds the [GMSCircle] and associated callbacks. The node is managed by
 * [MapApplier] and its lifecycle methods are called when the circle is
 * added to or removed from the composition.
 *
 * This mirrors the CircleNode pattern used in android-maps-compose.
 *
 * @param circle The underlying [GMSCircle] instance.
 * @param onCircleClick Callback invoked when the circle is clicked.
 */
@OptIn(ExperimentalForeignApi::class)
internal class CircleNode(
    val circle: GMSCircle,
    var onCircleClick: (Circle) -> Unit,
) : MapNode {

    override fun onRemoved() {
        circle.map = null
    }

    override fun onCleared() {
        circle.map = null
    }
}
