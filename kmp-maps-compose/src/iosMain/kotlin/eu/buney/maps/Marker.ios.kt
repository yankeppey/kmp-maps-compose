package eu.buney.maps

import GoogleMaps.GMSMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGPointMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIImage
import platform.UIKit.accessibilityLabel

/**
 * iOS implementation of [Marker] that wraps GMSMarker.
 */
@OptIn(ExperimentalForeignApi::class)
actual class Marker(
    private val gmsMarker: GMSMarker
) {
    actual val position: LatLng
        get() = gmsMarker.position.useContents {
            LatLng(latitude, longitude)
        }

    actual val title: String?
        get() = gmsMarker.title

    actual val snippet: String?
        get() = gmsMarker.snippet
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun Marker(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("Marker must be used within a GoogleMap composable")

    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val gmsMarker = GMSMarker().apply {
                position = CLLocationCoordinate2DMake(
                    state.position.latitude,
                    state.position.longitude
                )
                this.opacity = alpha
                this.groundAnchor = CGPointMake(anchor.x.toDouble(), anchor.y.toDouble())
                this.draggable = draggable
                this.flat = flat
                this.icon = icon?.uiImage
                this.infoWindowAnchor = CGPointMake(
                    infoWindowAnchor.x.toDouble(),
                    infoWindowAnchor.y.toDouble()
                )
                this.rotation = rotation.toDouble()
                this.snippet = snippet
                this.title = title
                // iOS SDK uses int for zIndex; fractional values are truncated
                this.zIndex = zIndex.toInt()
                this.accessibilityLabel = contentDescription
                this.userData = tag
                // attach to map (visibility is controlled by setting map to null or mapView)
                this.map = if (visible) mapApplier.mapView else null
            }

            state.showInfoWindowCallback = {
                gmsMarker.map?.selectedMarker = gmsMarker
            }
            state.hideInfoWindowCallback = {
                val mapView = gmsMarker.map
                if (mapView?.selectedMarker == gmsMarker) {
                    mapView.selectedMarker = null
                }
            }

            MarkerNode(
                marker = gmsMarker,
                markerState = state,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
            )
        },
        update = {
            update(onClick) { this.onMarkerClick = it }
            update(onInfoWindowClick) { this.onInfoWindowClick = it }
            update(onInfoWindowClose) { this.onInfoWindowClose = it }
            update(onInfoWindowLongClick) { this.onInfoWindowLongClick = it }

            update(alpha) { this.marker.opacity = it }
            update(anchor) {
                this.marker.groundAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(draggable) { this.marker.draggable = it }
            update(flat) { this.marker.flat = it }
            update(icon) { this.marker.icon = it?.uiImage }
            update(infoWindowAnchor) {
                this.marker.infoWindowAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(state.position) {
                this.marker.position = CLLocationCoordinate2DMake(it.latitude, it.longitude)
            }
            update(rotation) { this.marker.rotation = it.toDouble() }
            update(snippet) { this.marker.snippet = it }
            update(title) { this.marker.title = it }
            update(visible) {
                this.marker.map = if (it) mapApplier.mapView else null
            }
            update(zIndex) { this.marker.zIndex = it.toInt() }
            update(contentDescription) { this.marker.accessibilityLabel = it }
            update(tag) { this.marker.userData = it }
        }
    )
}

/**
 * iOS implementation of [MarkerInfoWindow].
 *
 * Renders the [content] composable to a bitmap which is displayed as a custom info window.
 * The bitmap is pre-rendered asynchronously when the marker is created and cached for display.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MarkerInfoWindow(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
    content: (@Composable (Marker) -> Unit)?,
) {
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("MarkerInfoWindow must be used within a GoogleMap composable")

    var cachedImage by remember { mutableStateOf<UIImage?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val gmsMarkerRef = remember { mutableStateOf<GMSMarker?>(null) }

    LaunchedEffect(content, state.position) {
        if (content != null && gmsMarkerRef.value != null) {
            try {
                val image = withContext(Dispatchers.Main) {
                    captureComposableToUIImage {
                        content(Marker(gmsMarkerRef.value!!))
                    }
                }
                cachedImage = image
            } catch (e: Exception) {
                println("MarkerInfoWindow: Failed to capture info window: ${e.message}")
            }
        }
    }

    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val gmsMarker = GMSMarker().apply {
                position = CLLocationCoordinate2DMake(
                    state.position.latitude,
                    state.position.longitude
                )
                this.opacity = alpha
                this.groundAnchor = CGPointMake(anchor.x.toDouble(), anchor.y.toDouble())
                this.draggable = draggable
                this.flat = flat
                this.icon = icon?.uiImage
                this.infoWindowAnchor = CGPointMake(
                    infoWindowAnchor.x.toDouble(),
                    infoWindowAnchor.y.toDouble()
                )
                this.rotation = rotation.toDouble()
                this.snippet = snippet
                this.title = title
                this.zIndex = zIndex.toInt()
                this.accessibilityLabel = contentDescription
                this.userData = tag
                this.map = if (visible) mapApplier.mapView else null
            }

            gmsMarkerRef.value = gmsMarker

            if (content != null) {
                coroutineScope.launch {
                    try {
                        val image = withContext(Dispatchers.Main) {
                            captureComposableToUIImage {
                                content(Marker(gmsMarker))
                            }
                        }
                        cachedImage = image
                    } catch (e: Exception) {
                        println("MarkerInfoWindow: Failed to capture info window: ${e.message}")
                    }
                }
            }

            state.showInfoWindowCallback = {
                gmsMarker.map?.selectedMarker = gmsMarker
            }
            state.hideInfoWindowCallback = {
                val mapView = gmsMarker.map
                if (mapView?.selectedMarker == gmsMarker) {
                    mapView.selectedMarker = null
                }
            }

            MarkerNode(
                marker = gmsMarker,
                markerState = state,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
            ).apply {
                infoWindowType = if (content != null) InfoWindowType.WINDOW else InfoWindowType.NONE
            }
        },
        update = {
            update(onClick) { this.onMarkerClick = it }
            update(onInfoWindowClick) { this.onInfoWindowClick = it }
            update(onInfoWindowClose) { this.onInfoWindowClose = it }
            update(onInfoWindowLongClick) { this.onInfoWindowLongClick = it }

            update(cachedImage) { this.cachedInfoWindowImage = it }

            update(alpha) { this.marker.opacity = it }
            update(anchor) {
                this.marker.groundAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(draggable) { this.marker.draggable = it }
            update(flat) { this.marker.flat = it }
            update(icon) { this.marker.icon = it?.uiImage }
            update(infoWindowAnchor) {
                this.marker.infoWindowAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(state.position) {
                this.marker.position = CLLocationCoordinate2DMake(it.latitude, it.longitude)
            }
            update(rotation) { this.marker.rotation = it.toDouble() }
            update(snippet) { this.marker.snippet = it }
            update(title) { this.marker.title = it }
            update(visible) {
                this.marker.map = if (it) mapApplier.mapView else null
            }
            update(zIndex) { this.marker.zIndex = it.toInt() }
            update(contentDescription) { this.marker.accessibilityLabel = it }
            update(tag) { this.marker.userData = it }
        }
    )
}

/**
 * iOS implementation of [MarkerInfoWindowContent].
 *
 * Renders the [content] composable to a bitmap which is displayed inside the default
 * info window frame. The bitmap is pre-rendered asynchronously when the marker is created.
 *
 * Note: On iOS, the "default info window frame" is not available in the same way as Android.
 * This implementation renders the content as a fully custom info window (same as [MarkerInfoWindow]).
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MarkerInfoWindowContent(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
    content: (@Composable (Marker) -> Unit)?,
) {
    val mapApplier = currentComposer.applier as? MapApplier
        ?: error("MarkerInfoWindowContent must be used within a GoogleMap composable")

    var cachedImage by remember { mutableStateOf<UIImage?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val gmsMarkerRef = remember { mutableStateOf<GMSMarker?>(null) }

    LaunchedEffect(content, state.position) {
        if (content != null && gmsMarkerRef.value != null) {
            try {
                val image = withContext(Dispatchers.Main) {
                    captureComposableToUIImage {
                        content(Marker(gmsMarkerRef.value!!))
                    }
                }
                cachedImage = image
            } catch (e: Exception) {
                println("MarkerInfoWindowContent: Failed to capture info window: ${e.message}")
            }
        }
    }

    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val gmsMarker = GMSMarker().apply {
                position = CLLocationCoordinate2DMake(
                    state.position.latitude,
                    state.position.longitude
                )
                this.opacity = alpha
                this.groundAnchor = CGPointMake(anchor.x.toDouble(), anchor.y.toDouble())
                this.draggable = draggable
                this.flat = flat
                this.icon = icon?.uiImage
                this.infoWindowAnchor = CGPointMake(
                    infoWindowAnchor.x.toDouble(),
                    infoWindowAnchor.y.toDouble()
                )
                this.rotation = rotation.toDouble()
                this.snippet = snippet
                this.title = title
                this.zIndex = zIndex.toInt()
                this.accessibilityLabel = contentDescription
                this.userData = tag
                this.map = if (visible) mapApplier.mapView else null
            }

            gmsMarkerRef.value = gmsMarker

            if (content != null) {
                coroutineScope.launch {
                    try {
                        val image = withContext(Dispatchers.Main) {
                            captureComposableToUIImage {
                                content(Marker(gmsMarker))
                            }
                        }
                        cachedImage = image
                    } catch (e: Exception) {
                        println("MarkerInfoWindowContent: Failed to capture info window: ${e.message}")
                    }
                }
            }

            state.showInfoWindowCallback = {
                gmsMarker.map?.selectedMarker = gmsMarker
            }
            state.hideInfoWindowCallback = {
                val mapView = gmsMarker.map
                if (mapView?.selectedMarker == gmsMarker) {
                    mapView.selectedMarker = null
                }
            }

            MarkerNode(
                marker = gmsMarker,
                markerState = state,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
            ).apply {
                infoWindowType = if (content != null) InfoWindowType.CONTENT else InfoWindowType.NONE
            }
        },
        update = {
            update(onClick) { this.onMarkerClick = it }
            update(onInfoWindowClick) { this.onInfoWindowClick = it }
            update(onInfoWindowClose) { this.onInfoWindowClose = it }
            update(onInfoWindowLongClick) { this.onInfoWindowLongClick = it }

            update(cachedImage) { this.cachedInfoWindowImage = it }

            update(alpha) { this.marker.opacity = it }
            update(anchor) {
                this.marker.groundAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(draggable) { this.marker.draggable = it }
            update(flat) { this.marker.flat = it }
            update(icon) { this.marker.icon = it?.uiImage }
            update(infoWindowAnchor) {
                this.marker.infoWindowAnchor = CGPointMake(it.x.toDouble(), it.y.toDouble())
            }
            update(state.position) {
                this.marker.position = CLLocationCoordinate2DMake(it.latitude, it.longitude)
            }
            update(rotation) { this.marker.rotation = it.toDouble() }
            update(snippet) { this.marker.snippet = it }
            update(title) { this.marker.title = it }
            update(visible) {
                this.marker.map = if (it) mapApplier.mapView else null
            }
            update(zIndex) { this.marker.zIndex = it.toInt() }
            update(contentDescription) { this.marker.accessibilityLabel = it }
            update(tag) { this.marker.userData = it }
        }
    )
}
