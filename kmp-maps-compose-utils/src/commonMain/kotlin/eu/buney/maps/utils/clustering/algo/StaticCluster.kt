package eu.buney.maps.utils.clustering.algo

import eu.buney.maps.LatLng
import eu.buney.maps.utils.clustering.Cluster
import eu.buney.maps.utils.clustering.ClusterItem

/**
 * A cluster whose center is determined upon creation.
 */
internal class StaticCluster<T : ClusterItem>(
    override val position: LatLng,
) : Cluster<T> {
    private val _items: MutableCollection<T> = linkedSetOf()

    fun add(item: T): Boolean = _items.add(item)

    fun remove(item: T): Boolean = _items.remove(item)

    override val items: Collection<T> get() = _items

    override val size: Int get() = _items.size

    override fun hashCode(): Int = 31 * position.hashCode() + _items.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is StaticCluster<*>) return false
        return other.position == position && other._items == _items
    }
}
