package eu.buney.maps.utils.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ClusterManager<T : ClusterItem>(
    val algorithm: ClusterAlgorithm<T> = defaultClusterAlgorithm(),
    private val coroutineContext: CoroutineContext = Dispatchers.Default,
) {
    var minClusterSize: Int = 4

    fun setItems(items: Collection<T>) {
        algorithm.clearItems()
        algorithm.addItems(items)
    }

    suspend fun getClusters(zoom: Float): Set<Cluster<T>> {
        return withContext(coroutineContext) {
            algorithm.getClusters(zoom)
        }
    }

    internal var onClusterClick: ((Cluster<T>) -> Boolean)? = null
    internal var onClusterItemClick: ((T) -> Boolean)? = null
    internal var onClusterItemInfoWindowClick: ((T) -> Unit)? = null
    internal var onClusterItemInfoWindowLongClick: ((T) -> Unit)? = null

    fun setOnClusterClickListener(listener: ((Cluster<T>) -> Boolean)?) {
        onClusterClick = listener
    }

    fun setOnClusterItemClickListener(listener: ((T) -> Boolean)?) {
        onClusterItemClick = listener
    }

    fun setOnClusterItemInfoWindowClickListener(listener: ((T) -> Unit)?) {
        onClusterItemInfoWindowClick = listener
    }

    fun setOnClusterItemInfoWindowLongClickListener(listener: ((T) -> Unit)?) {
        onClusterItemInfoWindowLongClick = listener
    }
}

@Composable
fun <T : ClusterItem> rememberClusterManager(
    algorithm: ClusterAlgorithm<T> = remember { defaultClusterAlgorithm() },
    coroutineContext: CoroutineContext = Dispatchers.Default,
): ClusterManager<T> = remember { ClusterManager(algorithm, coroutineContext) }
