package eu.buney.maps

/**
 * Base interface for all map overlay nodes (markers, polylines, polygons, etc.).
 *
 * Each map element that can be added to the map should implement this interface.
 * The lifecycle methods are called by [MapApplier] when nodes are added to,
 * removed from, or cleared from the composition.
 *
 * This mirrors the pattern used in android-maps-compose.
 */
internal interface MapNode {
    /**
     * Called when the node is attached to the composition tree.
     * This is where any setup after the node is added should occur.
     */
    fun onAttached() {}

    /**
     * Called when the node is removed from the composition tree.
     * This is where cleanup should occur (e.g., removing the overlay from the map).
     */
    fun onRemoved() {}

    /**
     * Called when the entire map is cleared.
     * This is where cleanup should occur, similar to [onRemoved].
     */
    fun onCleared() {}
}

/**
 * Root node for the [MapApplier].
 * This serves as a placeholder root of the composition tree.
 */
internal object MapNodeRoot : MapNode
