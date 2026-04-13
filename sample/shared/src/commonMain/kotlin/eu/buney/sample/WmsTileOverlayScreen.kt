package eu.buney.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.buney.maps.CameraPosition
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.MapProperties
import eu.buney.maps.MapType
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.utils.wms.WmsTileOverlay

private val centerOfUs = LatLng(39.50, -98.35)

@Composable
fun WmsTileOverlayScreen(modifier: Modifier = Modifier) {
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var overlayVisible by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(target = centerOfUs, zoom = 4f)
            },
            properties = MapProperties(mapType = mapType),
        ) {
            WmsTileOverlay(
                urlFormatter = { xMin, yMin, xMax, yMax, _ ->
                    "https://basemap.nationalmap.gov/arcgis/services/USGSShadedReliefOnly/MapServer/WmsServer" +
                        "?SERVICE=WMS" +
                        "&VERSION=1.1.1" +
                        "&REQUEST=GetMap" +
                        "&FORMAT=image/png" +
                        "&TRANSPARENT=true" +
                        "&LAYERS=0" +
                        "&SRS=EPSG:3857" +
                        "&WIDTH=256" +
                        "&HEIGHT=256" +
                        "&STYLES=" +
                        "&BBOX=$xMin,$yMin,$xMax,$yMax"
                },
                transparency = 0.5f,
                visible = overlayVisible,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    mapType = if (mapType == MapType.NONE) MapType.NORMAL else MapType.NONE
                }
            ) {
                Text(if (mapType == MapType.NONE) "Show Base Map" else "Hide Base Map")
            }

            Button(onClick = { overlayVisible = !overlayVisible }) {
                Text(if (overlayVisible) "Hide WMS Overlay" else "Show WMS Overlay")
            }
        }
    }
}
