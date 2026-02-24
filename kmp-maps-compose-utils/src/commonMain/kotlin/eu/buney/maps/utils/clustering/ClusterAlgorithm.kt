package eu.buney.maps.utils.clustering

import eu.buney.maps.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm

interface ClusterAlgorithm<T : ClusterItem> {
    fun addItems(items: Collection<T>)
    fun clearItems()
    fun getClusters(zoom: Float): Set<Cluster<T>>
}

fun <T : ClusterItem> defaultClusterAlgorithm(): ClusterAlgorithm<T> =
    NonHierarchicalDistanceBasedAlgorithm()
