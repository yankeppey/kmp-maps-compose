package eu.buney.maps.utils.clustering

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import eu.buney.maps.LatLng
import eu.buney.maps.Marker
import eu.buney.maps.MarkerComposable
import eu.buney.maps.currentCameraPositionState
import eu.buney.maps.utils.clustering.transition.AnimatedGroupedMarkers
import eu.buney.maps.utils.clustering.transition.Group
import eu.buney.maps.utils.clustering.transition.GroupedSnapshot
import eu.buney.maps.utils.clustering.transition.MembershipResolver
import eu.buney.maps.utils.clustering.transition.VisualElement
import eu.buney.maps.utils.clustering.transition.rememberTransitionPlan
import eu.buney.maps.rememberUpdatedMarkerState

/**
 * Core overload — syncs items with the given [clusterManager], observes camera state,
 * and emits animated [Marker] / [MarkerComposable] composables for the computed clusters.
 *
 * Clusters animate when splitting (zoom in: one cluster marker explodes into individual
 * markers) and merging (zoom out: individual markers collapse into a cluster marker).
 */
@Composable
fun <T : ClusterItem> Clustering(
    items: Collection<T>,
    clusterManager: ClusterManager<T>,
    enterAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    exitAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    clusterContent: (@Composable (Cluster<T>) -> Unit)? = null,
    clusterItemContent: (@Composable (T) -> Unit)? = null,
) {
    val cameraPositionState = currentCameraPositionState
    var clusters by remember { mutableStateOf<Set<Cluster<T>>>(emptySet()) }
    val currentItems by rememberUpdatedState(items)

    // Sync items to algorithm when items change
    LaunchedEffect(Unit) {
        snapshotFlow { currentItems.toList() }
            .collect { itemsList ->
                clusterManager.setItems(itemsList)
                clusters = clusterManager.getClusters(cameraPositionState.position.zoom)
            }
    }

    // Re-cluster when camera becomes idle
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving to cameraPositionState.position.zoom }
            .collect { (isMoving, zoom) ->
                if (!isMoving) {
                    clusters = clusterManager.getClusters(zoom)
                }
            }
    }

    // Convert Set<Cluster<T>> → GroupedSnapshot for the transition system
    val snapshot = remember(clusters) {
        toGroupedSnapshot(clusters)
    }

    val resolver = remember(clusterManager.minClusterSize) {
        MembershipResolver<Cluster<T>, T, LatLng>(
            itemPosition = { it.position },
            groupPosition = { group ->
                // The cluster position is embedded in the group key
                group.key.position
            },
            isCluster = { it.items.size >= clusterManager.minClusterSize },
        )
    }

    val plan = rememberTransitionPlan(snapshot, resolver)

    // Render with animated transitions
    AnimatedGroupedMarkers(
        plan = plan,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        clusterContent = { cluster, _, _, position ->
            val content: @Composable (Cluster<T>) -> Unit =
                clusterContent ?: { DefaultClusterContent(it) }
            MarkerComposable(
                cluster.size,
                state = rememberUpdatedMarkerState(position),
                zIndex = cluster.items.firstOrNull()?.zIndex ?: 0f,
                onClick = {
                    clusterManager.onClusterClick?.invoke(cluster) ?: false
                },
            ) {
                content(cluster)
            }
        },
        itemContent = { item, position ->
            if (clusterItemContent != null) {
                MarkerComposable(
                    item,
                    state = rememberUpdatedMarkerState(position),
                    title = item.title,
                    snippet = item.snippet,
                    zIndex = item.zIndex ?: 0f,
                    onClick = {
                        clusterManager.onClusterItemClick?.invoke(item) ?: false
                    },
                    onInfoWindowClick = {
                        clusterManager.onClusterItemInfoWindowClick?.invoke(item)
                    },
                    onInfoWindowLongClick = {
                        clusterManager.onClusterItemInfoWindowLongClick?.invoke(item)
                    },
                ) {
                    clusterItemContent(item)
                }
            } else {
                Marker(
                    state = rememberUpdatedMarkerState(position),
                    title = item.title,
                    snippet = item.snippet,
                    zIndex = item.zIndex ?: 0f,
                    onClick = {
                        clusterManager.onClusterItemClick?.invoke(item) ?: false
                    },
                    onInfoWindowClick = {
                        clusterManager.onClusterItemInfoWindowClick?.invoke(item)
                    },
                    onInfoWindowLongClick = {
                        clusterManager.onClusterItemInfoWindowLongClick?.invoke(item)
                    },
                )
            }
        },
    )
}

private val DefaultAnimationSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = 300, easing = FastOutSlowInEasing)

/** Convert clustering algorithm output to the generic grouping model. */
private fun <T : ClusterItem> toGroupedSnapshot(
    clusters: Set<Cluster<T>>,
): GroupedSnapshot<Cluster<T>, T> {
    val groups = clusters.map { cluster ->
        Group(
            key = cluster,
            items = cluster.items.toSet(),
        )
    }
    return GroupedSnapshot(groups)
}

/**
 * Simple overload — creates a [ClusterManager] internally,
 * wires up the provided callbacks, and delegates to [Clustering].
 */
@Composable
fun <T : ClusterItem> Clustering(
    items: Collection<T>,
    onClusterClick: (Cluster<T>) -> Boolean = { false },
    onClusterItemClick: (T) -> Boolean = { false },
    onClusterItemInfoWindowClick: (T) -> Unit = {},
    onClusterItemInfoWindowLongClick: (T) -> Unit = {},
    enterAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    exitAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    clusterContent: (@Composable (Cluster<T>) -> Unit)? = null,
    clusterItemContent: (@Composable (T) -> Unit)? = null,
) {
    Clustering(
        items = items,
        onClusterClick = onClusterClick,
        onClusterItemClick = onClusterItemClick,
        onClusterItemInfoWindowClick = onClusterItemInfoWindowClick,
        onClusterItemInfoWindowLongClick = onClusterItemInfoWindowLongClick,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        clusterContent = clusterContent,
        clusterItemContent = clusterItemContent,
        onClusterManager = null,
    )
}

/**
 * Advanced overload — same as the simple overload but with an [onClusterManager] hook
 * invoked once the [ClusterManager] is ready.
 */
@Composable
fun <T : ClusterItem> Clustering(
    items: Collection<T>,
    onClusterClick: (Cluster<T>) -> Boolean = { false },
    onClusterItemClick: (T) -> Boolean = { false },
    onClusterItemInfoWindowClick: (T) -> Unit = {},
    onClusterItemInfoWindowLongClick: (T) -> Unit = {},
    enterAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    exitAnimationSpec: FiniteAnimationSpec<Float> = DefaultAnimationSpec,
    clusterContent: (@Composable (Cluster<T>) -> Unit)? = null,
    clusterItemContent: (@Composable (T) -> Unit)? = null,
    onClusterManager: ((ClusterManager<T>) -> Unit)?,
) {
    val clusterManager = rememberClusterManager<T>()

    SideEffect {
        clusterManager.setOnClusterClickListener(onClusterClick)
        clusterManager.setOnClusterItemClickListener(onClusterItemClick)
        clusterManager.setOnClusterItemInfoWindowClickListener(onClusterItemInfoWindowClick)
        clusterManager.setOnClusterItemInfoWindowLongClickListener(onClusterItemInfoWindowLongClick)
        onClusterManager?.invoke(clusterManager)
    }

    Clustering(
        items = items,
        clusterManager = clusterManager,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        clusterContent = clusterContent,
        clusterItemContent = clusterItemContent,
    )
}
