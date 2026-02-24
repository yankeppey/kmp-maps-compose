package eu.buney.maps.utils.clustering

import eu.buney.maps.LatLng

interface Cluster<T : ClusterItem> {
    val position: LatLng
    val size: Int
    val items: Collection<T>
}
