package eu.buney.maps.utils.clustering.algo

import eu.buney.maps.utils.clustering.Cluster
import eu.buney.maps.utils.clustering.ClusterAlgorithm
import eu.buney.maps.utils.clustering.ClusterItem
import kotlin.math.pow

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 *
 * High level algorithm:
 * 1. Iterate over items in the order they were added (candidate clusters).
 * 2. Create a cluster with the center of the item.
 * 3. Add all items that are within a certain distance to the cluster.
 * 4. Move any items out of an existing cluster if they are closer to another cluster.
 * 5. Remove those items from the list of candidate clusters.
 *
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
class NonHierarchicalDistanceBasedAlgorithm<T : ClusterItem> : ClusterAlgorithm<T> {

    private val items: MutableCollection<QuadItem<T>> = linkedSetOf()
    private val quadTree = PointQuadTree<QuadItem<T>>(0.0, 1.0, 0.0, 1.0)

    override fun addItems(items: Collection<T>) {
        for (item in items) {
            val quadItem = QuadItem(item)
            if (this.items.add(quadItem)) {
                quadTree.add(quadItem)
            }
        }
    }

    override fun clearItems() {
        items.clear()
        quadTree.clear()
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()
        val zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / 2.0.pow(discreteZoom) / 256

        val visitedCandidates = hashSetOf<QuadItem<T>>()
        val results = hashSetOf<Cluster<T>>()
        val distanceToCluster = hashMapOf<QuadItem<T>, Double>()
        val itemToCluster = hashMapOf<QuadItem<T>, StaticCluster<T>>()

        for (candidate in items) {
            if (candidate in visitedCandidates) continue

            val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
            val clusterItems = quadTree.search(searchBounds)
            if (clusterItems.size == 1) {
                // Only the current marker is in range. Just add the single item to the results.
                results.add(candidate)
                visitedCandidates.add(candidate)
                distanceToCluster[candidate] = 0.0
                continue
            }

            val cluster = StaticCluster<T>(candidate.clusterItem.position)
            results.add(cluster)

            for (clusterItem in clusterItems) {
                val existingDistance = distanceToCluster[clusterItem]
                val distance = distanceSquared(clusterItem.point, candidate.point)
                if (existingDistance != null) {
                    if (existingDistance < distance) continue
                    // Move item to the closer cluster.
                    itemToCluster[clusterItem]?.remove(clusterItem.clusterItem)
                }
                distanceToCluster[clusterItem] = distance
                cluster.add(clusterItem.clusterItem)
                itemToCluster[clusterItem] = cluster
            }
            visitedCandidates.addAll(clusterItems)
        }
        return results
    }

    private fun distanceSquared(a: Point, b: Point): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }

    private fun createBoundsFromSpan(p: Point, span: Double): Bounds {
        val halfSpan = span / 2
        return Bounds(
            p.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan,
        )
    }

    internal class QuadItem<T : ClusterItem>(
        val clusterItem: T,
    ) : PointQuadTree.Item, Cluster<T> {

        override val point: Point = PROJECTION.toPoint(clusterItem.position)

        override val position get() = clusterItem.position
        override val items: Collection<T> get() = setOf(clusterItem)
        override val size: Int get() = 1

        override fun hashCode(): Int = clusterItem.hashCode()

        override fun equals(other: Any?): Boolean {
            if (other !is QuadItem<*>) return false
            return other.clusterItem == clusterItem
        }
    }

    companion object {
        private const val MAX_DISTANCE_AT_ZOOM = 100
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
