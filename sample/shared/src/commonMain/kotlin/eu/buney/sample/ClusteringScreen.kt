package eu.buney.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.buney.maps.CameraPosition
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.utils.clustering.Cluster
import eu.buney.maps.utils.clustering.ClusterItem
import eu.buney.maps.utils.clustering.Clustering
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.MarkerInfoWindow
import eu.buney.maps.rememberUpdatedMarkerState
import androidx.compose.animation.core.tween
import co.touchlab.kermit.Logger
import kotlin.random.Random

private val logger = Logger.withTag("ClusteringScreen")

// Center of Singapore - matches the android-maps-compose clustering sample
private val singapore = LatLng(1.35, 103.87)

private enum class ClusteringType {
    Default,
    CustomUi,
}

data class MyItem(
    override val position: LatLng,
    override val title: String?,
    override val snippet: String?,
    override val zIndex: Float?,
) : ClusterItem

@Composable
fun ClusteringScreen(modifier: Modifier = Modifier) {
    val items = remember { mutableStateListOf<MyItem>() }
    LaunchedEffect(Unit) {
        for (i in 1..10) {
            val position = LatLng(
                singapore.latitude + Random.nextFloat(),
                singapore.longitude + Random.nextFloat(),
            )
            items.add(MyItem(position, "Marker $i", "Snippet $i", 0f))
        }
    }

    var clusteringType by remember { mutableStateOf(ClusteringType.Default) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(target = singapore, zoom = 6f)
                }
            ) {
                when (clusteringType) {
                    ClusteringType.Default -> DefaultClustering(items)
                    ClusteringType.CustomUi -> CustomUiClustering(items)
                }

                // Standalone marker outside the clustering system
                MarkerInfoWindow(
                    state = rememberUpdatedMarkerState(position = singapore),
                    onClick = {
                        logger.i { "Non-cluster marker clicked! $it" }
                        true
                    }
                )
            }

            ClusteringTypeControls(
                selected = clusteringType,
                onClusteringTypeClick = { clusteringType = it },
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}

@Composable
private fun DefaultClustering(items: List<MyItem>) {
    Clustering(
        items = items,
        enterAnimationSpec = tween(durationMillis = 1000),
        exitAnimationSpec = tween(durationMillis = 1000),
        onClusterClick = {
            logger.i { "Cluster clicked! ${it.size} items" }
            false
        },
        onClusterItemClick = {
            logger.i { "Cluster item clicked! ${it.title}" }
            false
        },
        onClusterItemInfoWindowClick = {
            logger.i { "Cluster item info window clicked! ${it.title}" }
        },
    )
}

@Composable
private fun CustomUiClustering(items: List<MyItem>) {
    Clustering(
        items = items,
        onClusterClick = {
            logger.i { "Cluster clicked! ${it.size} items" }
            false
        },
        onClusterItemClick = {
            logger.i { "Cluster item clicked! ${it.title}" }
            false
        },
        onClusterItemInfoWindowClick = {
            logger.i { "Cluster item info window clicked! ${it.title}" }
        },
        clusterContent = { cluster ->
            CircleContent(
                modifier = Modifier.size(40.dp),
                text = "${cluster.size}",
                color = Color.Blue,
            )
        },
        clusterItemContent = { item ->
            CircleContent(
                modifier = Modifier.size(20.dp),
                text = "",
                color = Color.Red,
            )
        },
    )
}

@Composable
private fun CircleContent(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = CircleShape,
        color = color,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClusteringTypeControls(
    selected: ClusteringType,
    onClusteringTypeClick: (ClusteringType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(state = ScrollState(0)),
        horizontalArrangement = Arrangement.Start
    ) {
        ClusteringType.entries.forEach { type ->
            val isSelected = type == selected
            Button(
                modifier = Modifier.padding(4.dp),
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                },
                onClick = { onClusteringTypeClick(type) }
            ) {
                Text(
                    text = when (type) {
                        ClusteringType.Default -> "Default"
                        ClusteringType.CustomUi -> "Custom UI"
                    },
                )
            }
        }
    }
}
