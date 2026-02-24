package eu.buney.maps.utils.clustering

import eu.buney.maps.LatLng

interface ClusterItem {
    val position: LatLng
    val title: String?
    val snippet: String?
    val zIndex: Float?
}
