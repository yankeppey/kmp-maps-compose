package eu.buney.maps.utils.clustering.algo

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
internal class PointQuadTree<T : PointQuadTree.Item>(
    private val bounds: Bounds,
    private val depth: Int = 0,
) {
    interface Item {
        val point: Point
    }

    private var items: MutableSet<T>? = null
    private var children: MutableList<PointQuadTree<T>>? = null

    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) :
        this(Bounds(minX, maxX, minY, maxY))

    fun add(item: T) {
        val point = item.point
        if (bounds.contains(point.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        children?.let { c ->
            val index = childIndex(x, y)
            c[index].insert(x, y, item)
            return
        }
        val currentItems = items ?: linkedSetOf<T>().also { items = it }
        currentItems.add(item)
        if (currentItems.size > MAX_ELEMENTS && depth < MAX_DEPTH) {
            split()
        }
    }

    private fun childIndex(x: Double, y: Double): Int {
        val col = if (x < bounds.midX) 0 else 1
        val row = if (y < bounds.midY) 0 else 2
        return row + col
    }

    private fun split() {
        children = ArrayList<PointQuadTree<T>>(4).apply {
            add(PointQuadTree(Bounds(bounds.minX, bounds.midX, bounds.minY, bounds.midY), depth + 1))
            add(PointQuadTree(Bounds(bounds.midX, bounds.maxX, bounds.minY, bounds.midY), depth + 1))
            add(PointQuadTree(Bounds(bounds.minX, bounds.midX, bounds.midY, bounds.maxY), depth + 1))
            add(PointQuadTree(Bounds(bounds.midX, bounds.maxX, bounds.midY, bounds.maxY), depth + 1))
        }

        val old = items
        items = null
        old?.forEach { insert(it.point.x, it.point.y, it) }
    }

    fun clear() {
        children = null
        items?.clear()
    }

    fun search(searchBounds: Bounds): Collection<T> {
        val results = mutableListOf<T>()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!bounds.intersects(searchBounds)) return

        children?.let { c ->
            for (quad in c) {
                quad.search(searchBounds, results)
            }
            return
        }

        items?.let { currentItems ->
            if (bounds in searchBounds) {
                results.addAll(currentItems)
            } else {
                for (item in currentItems) {
                    if (item.point in searchBounds) {
                        results.add(item)
                    }
                }
            }
        }
    }

    companion object {
        private const val MAX_ELEMENTS = 50
        private const val MAX_DEPTH = 40
    }
}
