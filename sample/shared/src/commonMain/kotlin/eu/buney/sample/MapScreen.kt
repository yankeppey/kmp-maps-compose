package eu.buney.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.buney.maps.BitmapDescriptor
import eu.buney.maps.BitmapDescriptorFactory
import eu.buney.maps.CameraPosition
import eu.buney.maps.Circle
import eu.buney.maps.GoogleMap
import eu.buney.maps.GroundOverlay
import eu.buney.maps.GroundOverlayPosition
import eu.buney.maps.LatLng
import eu.buney.maps.LatLngBounds
import eu.buney.maps.MapProperties
import eu.buney.maps.MapType
import eu.buney.maps.MapUiSettings
import eu.buney.maps.Marker
import eu.buney.maps.MarkerComposable
import eu.buney.maps.MarkerInfoWindow
import eu.buney.maps.PointOfInterest
import eu.buney.maps.Polygon
import eu.buney.maps.Polyline
import eu.buney.maps.StampStyle
import eu.buney.maps.StrokeStyle
import eu.buney.maps.StyleSpan
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import mapscomposemultiplatform.sample.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

// sample locations
private val sanFrancisco = LatLng(37.7749, -122.4194)
private val newYork = LatLng(40.7128, -74.0060)
private val london = LatLng(51.5074, -0.1278)

// custom marker locations in San Francisco
private val customMarkerLocations = listOf(
    LatLng(37.7849, -122.4094), // North of downtown
    LatLng(37.7649, -122.4294), // South-west
    LatLng(37.7749, -122.4094), // East
)

// bus marker locations in San Francisco
private val busLocations = listOf(
    LatLng(37.7799, -122.4144) to "38R",  // Near Market St
    LatLng(37.7699, -122.4244) to "14",   // Near Mission
)

// polyline points - a path through Manhattan
private val manhattanRoute = listOf(
    LatLng(40.7484, -73.9857), // Empire State Building
    LatLng(40.7580, -73.9855), // Times Square
    LatLng(40.7614, -73.9776), // MoMA
    LatLng(40.7794, -73.9632), // Met Museum
    LatLng(40.7829, -73.9654), // Central Park North
)

// Polyline style options for the Manhattan route demo
private enum class PolylineStyle {
    SOLID, GRADIENT, STAMPED
}

// polygon around Westminster, London
private val westminsterPolygon = listOf(
    LatLng(51.5014, -0.1419), // Buckingham Palace
    LatLng(51.5007, -0.1246), // Westminster Abbey
    LatLng(51.5074, -0.1278), // Big Ben
    LatLng(51.5119, -0.1216), // Trafalgar Square
    LatLng(51.5104, -0.1380), // Green Park
)

// a hole in the polygon (small area around St. James's Park)
private val westminsterHole = listOf(
    LatLng(51.5030, -0.1340),
    LatLng(51.5030, -0.1300),
    LatLng(51.5055, -0.1300),
    LatLng(51.5055, -0.1340),
)

// ground overlay bounds (near Central Park, NYC)
private val groundOverlayBounds = LatLngBounds(
    southwest = LatLng(40.7650, -73.9800),
    northeast = LatLng(40.7750, -73.9700),
)

// NYC famous places for bounds animation demo
private val timesSquare = LatLng(40.7580, -73.9855)
private val centralPark = LatLng(40.7829, -73.9654)
private val empireStateBuilding = LatLng(40.7484, -73.9857)
private val statueOfLiberty = LatLng(40.6892, -74.0445)
private val brooklynBridge = LatLng(40.7061, -73.9969)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var selectedLocation by remember { mutableStateOf(sanFrancisco) }
    var selectedCustomMarkerIndex by remember { mutableStateOf<Int?>(null) }

    // state for content padding demo
    var contentPaddingEnabled by remember { mutableStateOf(false) }

    // state for POI click demo
    var lastClickedPOI by remember { mutableStateOf<PointOfInterest?>(null) }

    // state for programmatic info window control demo
    val sfMarkerState = rememberUpdatedMarkerState(position = sanFrancisco)
    var showSfInfoWindow by remember { mutableStateOf(false) }

    // state for polyline style (cycles through solid, gradient, stamped)
    var polylineStyle by remember { mutableStateOf(PolylineStyle.SOLID) }

    // react to info window toggle
    LaunchedEffect(showSfInfoWindow) {
        if (showSfInfoWindow) {
            sfMarkerState.showInfoWindow()
        } else {
            sfMarkerState.hideInfoWindow()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            target = selectedLocation,
            zoom = 12f
        )
    }

    // coroutine scope for animated camera movements
    val coroutineScope = rememberCoroutineScope()


    Column(modifier = modifier) {
        // map with FAB overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true
            ),
            contentPadding = if (contentPaddingEnabled) {
                PaddingValues(start = 50.dp, top = 100.dp, end = 50.dp, bottom = 50.dp)
            } else {
                PaddingValues(0.dp)
            },
            onMapLoaded = {
                println("Map loaded successfully!")
            },
            onPOIClick = { poi ->
                println("POI clicked: ${poi.name} (${poi.placeId}) at ${poi.latLng}")
                lastClickedPOI = poi
            }
        ) {
            // markers for each location
            // San Francisco marker - demonstrates programmatic info window control
            Marker(
                state = sfMarkerState,
                title = "San Francisco",
                snippet = "Use button below to toggle!",
                onClick = { marker ->
                    println("Marker clicked: ${marker.title}")
                    showSfInfoWindow = true // Sync state when user clicks marker
                    false // Return false to show info window
                },
                onInfoWindowClick = { marker ->
                    println("Info window clicked: ${marker.title}")
                },
                onInfoWindowClose = { marker ->
                    println("Info window closed: ${marker.title}")
                    showSfInfoWindow = false // Sync state when user closes info window
                },
                onInfoWindowLongClick = { marker ->
                    println("Info window long-clicked: ${marker.title}")
                },
            )
            // MarkerInfoWindow example - custom composable info window
            // note: on iOS, the custom content is ignored and falls back to title/snippet
            MarkerInfoWindow(
                state = rememberUpdatedMarkerState(position = newYork),
                title = "New York", // Fallback for iOS
                snippet = "The Big Apple", // Fallback for iOS
                onInfoWindowClick = { marker ->
                    println("Custom info window clicked: ${marker.title}")
                },
            ) { marker ->
                // custom info window content (Android only)
                CustomInfoWindow(
                    title = "New York City",
                    subtitle = "The Big Apple",
                    description = "Population: 8.3M"
                )
            }
            Marker(
                state = rememberUpdatedMarkerState(position = london),
                title = "London",
                snippet = "The Capital of England",
            )

            // custom MarkerComposable examples - circles that change color when selected
            customMarkerLocations.forEachIndexed { index, position ->
                val isSelected = selectedCustomMarkerIndex == index
                MarkerComposable(
                    keys = arrayOf(index, selectedCustomMarkerIndex ?: -1),
                    state = rememberUpdatedMarkerState(position = position),
                    title = "Custom Marker ${index + 1}",
                    snippet = if (isSelected) "Selected!" else "Tap to select",
                    onClick = {
                        selectedCustomMarkerIndex = if (isSelected) null else index
                        println("Custom marker ${index + 1} clicked, selected: ${!isSelected}")
                        false // Show info window
                    }
                ) {
                    // custom circle marker - changes color when selected
                    CustomCircleMarker(isSelected = isSelected)
                }
            }

            // bus markers - similar to EpaCarris/paragem app
            busLocations.forEach { (position, routeLabel) ->
                MarkerComposable(
                    keys = arrayOf(routeLabel),
                    state = rememberUpdatedMarkerState(position = position),
                    title = "Bus $routeLabel",
                    snippet = "Tap for details",
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), // Center anchor for bus icon
                    onClick = {
                        println("Bus $routeLabel clicked")
                        false // Show info window
                    }
                ) {
                    BusMarker(routeLabel = routeLabel)
                }
            }

            // NYC famous places markers (for bounds animation demo)
            Marker(
                state = rememberUpdatedMarkerState(position = timesSquare),
                title = "Times Square",
                snippet = "The Crossroads of the World"
            )
            Marker(
                state = rememberUpdatedMarkerState(position = centralPark),
                title = "Central Park",
                snippet = "Urban oasis in Manhattan"
            )
            Marker(
                state = rememberUpdatedMarkerState(position = empireStateBuilding),
                title = "Empire State Building",
                snippet = "Iconic Art Deco skyscraper"
            )
            Marker(
                state = rememberUpdatedMarkerState(position = statueOfLiberty),
                title = "Statue of Liberty",
                snippet = "Symbol of freedom"
            )
            Marker(
                state = rememberUpdatedMarkerState(position = brooklynBridge),
                title = "Brooklyn Bridge",
                snippet = "Historic suspension bridge"
            )

            // circle around San Francisco (5km radius)
            Circle(
                center = sanFrancisco,
                radius = 5000.0, // 5km
                fillColor = Color(0x220000FF), // Semi-transparent blue
                strokeColor = Color.Blue,
                strokeWidth = 3f,
                clickable = true,
                onClick = { circle ->
                    println("Circle clicked! Center: ${circle.center}, Radius: ${circle.radius}m")
                }
            )

            // polyline showing Manhattan route - style changes based on polylineStyle state
            var arrowStampImage by remember { mutableStateOf<BitmapDescriptor?>(null) }
            LaunchedEffect(Unit) {
                val imageBytes = Res.readBytes("drawable/arrow_stamp.png")
                arrowStampImage = BitmapDescriptorFactory.fromEncodedImage(imageBytes)
            }

            when (polylineStyle) {
                PolylineStyle.SOLID -> {
                    Polyline(
                        points = manhattanRoute,
                        color = Color.Red,
                        width = 8f,
                        clickable = true,
                        onClick = { polyline ->
                            println("Solid polyline clicked! Points: ${polyline.points.size}")
                        }
                    )
                }
                PolylineStyle.GRADIENT -> {
                    Polyline(
                        points = manhattanRoute,
                        spans = listOf(
                            StyleSpan.solidColor(Color.Red, segments = 1.0),
                            StyleSpan.gradient(Color.Red, Color.Yellow, segments = 1.0),
                            StyleSpan.solidColor(Color.Yellow, segments = 1.0),
                            StyleSpan.gradient(Color.Yellow, Color.Green, segments = 1.0),
                        ),
                        width = 8f,
                        clickable = true,
                        onClick = { polyline ->
                            println("Gradient polyline clicked! Points: ${polyline.points.size}")
                        }
                    )
                }
                PolylineStyle.STAMPED -> {
                    arrowStampImage?.let { arrowImage ->
                        Polyline(
                            points = manhattanRoute,
                            spans = listOf(
                                StyleSpan(
                                    style = StrokeStyle.SolidColor(Color.Blue),
                                    stampStyle = StampStyle(arrowImage),
                                    segments = manhattanRoute.size.toDouble()
                                )
                            ),
                            width = 16f,
                            clickable = true,
                            onClick = { polyline ->
                                println("Stamped polyline clicked! Points: ${polyline.points.size}")
                            }
                        )
                    }
                }
            }

            // polygon around Westminster, London (with a hole)
            Polygon(
                points = westminsterPolygon,
                holes = listOf(westminsterHole),
                fillColor = Color(0x3300FF00), // Semi-transparent green
                strokeColor = Color.Green,
                strokeWidth = 3f,
                clickable = true,
                onClick = { polygon ->
                    println("Polygon clicked! Points: ${polygon.points.size}, Holes: ${polygon.holes.size}")
                }
            )

            // ground overlay near Central Park, NYC
            // Note: BitmapDescriptorFactory must be called inside GoogleMap content
            // because it requires the Maps SDK to be initialized
            var groundOverlayImage by remember { mutableStateOf<BitmapDescriptor?>(null) }
            LaunchedEffect(Unit) {
                val imageBytes = Res.readBytes("drawable/overlay_image.jpg")
                groundOverlayImage = BitmapDescriptorFactory.fromEncodedImage(imageBytes)
            }
            groundOverlayImage?.let { image ->
                GroundOverlay(
                    position = GroundOverlayPosition.create(groundOverlayBounds),
                    image = image,
                    transparency = 0.2f,
                    clickable = true,
                    onClick = { overlay ->
                        println("GroundOverlay clicked! Bounds: ${overlay.bounds}")
                    }
                )
            }
        }

            // FAB to cycle polyline styles (only visible in New York)
            if (selectedLocation == newYork) {
                FloatingActionButton(
                    onClick = {
                        polylineStyle = when (polylineStyle) {
                            PolylineStyle.SOLID -> PolylineStyle.GRADIENT
                            PolylineStyle.GRADIENT -> PolylineStyle.STAMPED
                            PolylineStyle.STAMPED -> PolylineStyle.SOLID
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = when (polylineStyle) {
                        PolylineStyle.SOLID -> Color(0xFFEF9A9A)      // Soft pastel red
                        PolylineStyle.GRADIENT -> Color(0xFFFFCC80)   // Soft pastel orange
                        PolylineStyle.STAMPED -> Color(0xFF90CAF9)    // Soft pastel blue
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Polyline,
                        contentDescription = "Change Polyline Style",
                    )
                }
            }
        }

        // controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedLocation = sanFrancisco
                            cameraPositionState.position = CameraPosition(selectedLocation, 12f)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("San Francisco")
                    }
                    Button(
                        onClick = {
                            // move camera to SF first if not there
                            if (selectedLocation != sanFrancisco) {
                                selectedLocation = sanFrancisco
                                cameraPositionState.position = CameraPosition(sanFrancisco, 12f)
                            }
                            showSfInfoWindow = !showSfInfoWindow
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showSfInfoWindow) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (showSfInfoWindow) "Hide Info" else "Show Info")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedLocation = newYork
                            cameraPositionState.position = CameraPosition(selectedLocation, 12f)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New York")
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val bounds = LatLngBounds.Builder()
                                    .include(timesSquare)
                                    .include(centralPark)
                                    .include(empireStateBuilding)
                                    .include(statueOfLiberty)
                                    .include(brooklynBridge)
                                    .build()
                                cameraPositionState.animateToBounds(bounds, padding = 0)
                            }
                        }
                    ) {
                        Text("Fit All")
                    }
                }

                Button(
                    onClick = {
                        selectedLocation = london
                        cameraPositionState.position = CameraPosition(selectedLocation, 12f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("London")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // content padding toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = contentPaddingEnabled,
                        onCheckedChange = { contentPaddingEnabled = it }
                    )
                    Text(
                        text = "Enable Content Padding",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // POI click info display
                lastClickedPOI?.let { poi ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Last Clicked POI: ${poi.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * A custom circle marker that changes color based on selection state.
 * Demonstrates using Compose UI as a marker icon via MarkerComposable.
 */
@Composable
private fun CustomCircleMarker(isSelected: Boolean) {
    val fillColor = if (isSelected) Color(0xFFFF9800) else Color(0xFF4CAF50) // Orange or Green

    Box(
        modifier = Modifier
            .size(24.dp)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(fillColor)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        )
    }
}

/**
 * A bus marker with route label, similar to EpaCarris/paragem app.
 * Shows a bus icon with the route number above it.
 */
@Composable
private fun BusMarker(routeLabel: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = routeLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2) // Blue
        )
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = "Bus",
            tint = Color(0xFF1976D2), // Blue
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * A custom info window composable for MarkerInfoWindow.
 * Demonstrates using Compose UI for a fully custom info window appearance.
 * Note: Only works on Android; iOS falls back to default info window.
 */
@Composable
private fun CustomInfoWindow(
    title: String,
    subtitle: String,
    description: String,
) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1976D2)
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}
