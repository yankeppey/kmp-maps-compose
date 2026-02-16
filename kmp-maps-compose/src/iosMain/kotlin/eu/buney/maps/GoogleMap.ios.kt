package eu.buney.maps

import GoogleMaps.GMSCameraPosition
import GoogleMaps.GMSCircle
import GoogleMaps.GMSGroundOverlay
import GoogleMaps.GMSMapView
import GoogleMaps.GMSMapViewDelegateProtocol
import GoogleMaps.GMSMapViewType
import GoogleMaps.GMSMarker
import GoogleMaps.GMSOverlay
import GoogleMaps.GMSPolygon
import GoogleMaps.GMSPolyline
import GoogleMaps.kGMSTypeHybrid
import GoogleMaps.kGMSTypeNone
import GoogleMaps.kGMSTypeNormal
import GoogleMaps.kGMSTypeSatellite
import GoogleMaps.kGMSTypeTerrain
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.contentMode
import platform.darwin.NSObject

/**
 * Delegate class for handling GMSMapView callbacks.
 *
 * Routes marker events through [MapApplier] to find the correct [MarkerNode]
 * and invoke its callbacks.
 */
@OptIn(ExperimentalForeignApi::class)
private class GMSMapViewDelegate(
    private val onMapClick: ((LatLng) -> Unit)?,
    private val onMapLongClick: ((LatLng) -> Unit)?,
    private val onPOIClick: ((PointOfInterest) -> Unit)?,
    private val cameraPositionState: CameraPositionState,
    private val getMapApplier: () -> MapApplier?,
) : NSObject(), GMSMapViewDelegateProtocol {

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didTapAtCoordinate: CValue<CLLocationCoordinate2D>) {
        didTapAtCoordinate.useContents {
            onMapClick?.invoke(LatLng(latitude, longitude))
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didLongPressAtCoordinate: CValue<CLLocationCoordinate2D>) {
        didLongPressAtCoordinate.useContents {
            onMapLongClick?.invoke(LatLng(latitude, longitude))
        }
    }

    @ObjCSignatureOverride
    override fun mapView(
        mapView: GMSMapView,
        didTapPOIWithPlaceID: String,
        name: String,
        location: CValue<CLLocationCoordinate2D>
    ) {
        location.useContents {
            onPOIClick?.invoke(
                PointOfInterest(
                    latLng = LatLng(latitude, longitude),
                    name = name,
                    placeId = didTapPOIWithPlaceID
                )
            )
        }
    }

    override fun mapView(mapView: GMSMapView, willMove: Boolean) {
        cameraPositionState._isMoving = true
        cameraPositionState._cameraMoveStartedReason = if (willMove) {
            CameraMoveStartedReason.GESTURE
        } else {
            CameraMoveStartedReason.DEVELOPER_ANIMATION
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didChangeCameraPosition: GMSCameraPosition) {
        // Realtime position updates during gestures/animations
        updateCameraPositionStateOnMove(cameraPositionState, didChangeCameraPosition)
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, idleAtCameraPosition: GMSCameraPosition) {
        updateCameraPositionStateOnIdle(cameraPositionState, mapView, idleAtCameraPosition)
    }

    // marker tap handling - route to the correct MarkerNode via MapApplier
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didTapMarker: GMSMarker): Boolean {
        val applier = getMapApplier() ?: return false
        val node = applier.findMarkerNode(didTapMarker) ?: return false
        return node.onMarkerClick(Marker(didTapMarker))
    }

    // info window tap handling
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didTapInfoWindowOfMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didTapInfoWindowOfMarker) ?: return
        node.onInfoWindowClick(Marker(didTapInfoWindowOfMarker))
    }

    // info window close handling
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didCloseInfoWindowOfMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didCloseInfoWindowOfMarker) ?: return
        node.onInfoWindowClose(Marker(didCloseInfoWindowOfMarker))
    }

    // info window long press handling
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didLongPressInfoWindowOfMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didLongPressInfoWindowOfMarker) ?: return
        node.onInfoWindowLongClick(Marker(didLongPressInfoWindowOfMarker))
    }

    // custom info window - returns fully custom view for the entire info window
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, markerInfoWindow: GMSMarker): UIView? {
        val applier = getMapApplier() ?: return null
        val node = applier.findMarkerNode(markerInfoWindow) ?: return null

        // only return custom view if this marker uses InfoWindowType.WINDOW
        if (node.infoWindowType != InfoWindowType.WINDOW) return null

        val cachedImage = node.cachedInfoWindowImage ?: return null

        // create a UIImageView with the cached image
        return UIImageView(cachedImage).apply {
            // use ScaleAspectFit to maintain aspect ratio
            contentMode = platform.UIKit.UIViewContentMode.UIViewContentModeScaleAspectFit
        }
    }

    // custom info window content - returns custom view placed inside default info window frame
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, markerInfoContents: GMSMarker): UIView? {
        val applier = getMapApplier() ?: return null
        val node = applier.findMarkerNode(markerInfoContents) ?: return null

        // only return custom view if this marker uses InfoWindowType.CONTENT
        if (node.infoWindowType != InfoWindowType.CONTENT) return null

        val cachedImage = node.cachedInfoWindowImage ?: return null

        // create a UIImageView with the cached image
        return UIImageView(cachedImage).apply {
            contentMode = platform.UIKit.UIViewContentMode.UIViewContentModeScaleAspectFit
        }
    }

    // marker drag handling - update MarkerState during drag
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didBeginDraggingMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didBeginDraggingMarker) ?: return
        node.markerState.isDragging = true
        didBeginDraggingMarker.position.useContents {
            node.markerState.position = LatLng(latitude, longitude)
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didDragMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didDragMarker) ?: return
        didDragMarker.position.useContents {
            node.markerState.position = LatLng(latitude, longitude)
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didEndDraggingMarker: GMSMarker) {
        val applier = getMapApplier() ?: return
        val node = applier.findMarkerNode(didEndDraggingMarker) ?: return
        didEndDraggingMarker.position.useContents {
            node.markerState.position = LatLng(latitude, longitude)
        }
        node.markerState.isDragging = false
    }

    // overlay tap handling - routes to Circle, Polygon, Polyline, GroundOverlay nodes
    @ObjCSignatureOverride
    override fun mapView(mapView: GMSMapView, didTapOverlay: GMSOverlay) {
        val applier = getMapApplier() ?: return
        when (didTapOverlay) {
            is GMSCircle -> {
                val node = applier.findCircleNode(didTapOverlay) ?: return
                node.onCircleClick(Circle(didTapOverlay))
            }
            is GMSPolyline -> {
                val node = applier.findPolylineNode(didTapOverlay) ?: return
                node.onPolylineClick(Polyline(didTapOverlay))
            }
            is GMSPolygon -> {
                val node = applier.findPolygonNode(didTapOverlay) ?: return
                node.onPolygonClick(Polygon(didTapOverlay))
            }
            is GMSGroundOverlay -> {
                val node = applier.findGroundOverlayNode(didTapOverlay) ?: return
                node.onGroundOverlayClick(GroundOverlay(didTapOverlay))
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GoogleMap(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    contentPadding: PaddingValues,
    onMapClick: ((LatLng) -> Unit)?,
    onMapLongClick: ((LatLng) -> Unit)?,
    onPOIClick: ((PointOfInterest) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    content: (@Composable @GoogleMapComposable () -> Unit)?,
) {
    val mapView = remember { GMSMapView() }

    // convert padding to UIEdgeInsets
    val uiEdgeInsets = contentPadding.toUIEdgeInsets()

    // state to hold the MapApplier reference for the delegate
    var mapApplier: MapApplier? by remember { mutableStateOf(null) }

    // create and remember the delegate
    val delegate = remember {
        GMSMapViewDelegate(
            onMapClick = onMapClick,
            onMapLongClick = onMapLongClick,
            onPOIClick = onPOIClick,
            cameraPositionState = cameraPositionState,
            getMapApplier = { mapApplier },
        )
    }

    // subcomposition setup
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    // Setup camera position state sync (animation, move, position)
    SetupCameraPositionStateSync(
        cameraPositionState = cameraPositionState,
        mapView = mapView,
    )

    // clean up delegate when composable is disposed
    DisposableEffect(delegate) {
        mapView.delegate = delegate
        onDispose {
            mapView.delegate = null
        }
    }

    // launch subcomposition for map content
    DisposableEffect(mapView, parentComposition) {
        var composition: Composition? = null

        if (currentContent != null) {
            val applier = MapApplier(mapView)
            mapApplier = applier

            composition = Composition(
                applier = applier,
                parent = parentComposition
            )
            composition.setContent {
                CompositionLocalProvider(
                    LocalCameraPositionState provides cameraPositionState,
                ) {
                    currentContent?.invoke()
                }
            }
        }

        onDispose {
            composition?.dispose()
            mapApplier = null
        }
    }

    UIKitView(
        factory = {
            mapView.apply {
                // initial camera position
                val coordinate = CLLocationCoordinate2DMake(
                    latitude = cameraPositionState.position.target.latitude,
                    longitude = cameraPositionState.position.target.longitude
                )
                val camera = GMSCameraPosition.cameraWithTarget(
                    target = coordinate,
                    zoom = cameraPositionState.position.zoom,
                    bearing = cameraPositionState.position.bearing.toDouble(),
                    viewingAngle = cameraPositionState.position.tilt.toDouble()
                )
                this.camera = camera

                // apply map properties
                this.mapType = properties.mapType.toGoogleMapType()
                this.buildingsEnabled = properties.isBuildingEnabled
                this.indoorEnabled = properties.isIndoorEnabled
                this.trafficEnabled = properties.isTrafficEnabled
                this.myLocationEnabled = properties.isMyLocationEnabled

                // apply UI settings
                this.settings.compassButton = uiSettings.compassEnabled
                this.settings.indoorPicker = uiSettings.indoorLevelPickerEnabled
                this.settings.myLocationButton = uiSettings.myLocationButtonEnabled
                this.settings.rotateGestures = uiSettings.rotationGesturesEnabled
                this.settings.scrollGestures = uiSettings.scrollGesturesEnabled
                this.settings.tiltGestures = uiSettings.tiltGesturesEnabled
                this.settings.zoomGestures = uiSettings.zoomGesturesEnabled

                // apply content padding
                this.padding = uiEdgeInsets

                // set delegate
                this.delegate = delegate

                // notify that map is loaded
                onMapLoaded?.invoke()
            }
        },
        modifier = modifier,
        update = { view ->
            // update map properties
            view.mapType = properties.mapType.toGoogleMapType()
            view.buildingsEnabled = properties.isBuildingEnabled
            view.indoorEnabled = properties.isIndoorEnabled
            view.trafficEnabled = properties.isTrafficEnabled
            view.myLocationEnabled = properties.isMyLocationEnabled

            // update UI settings
            view.settings.compassButton = uiSettings.compassEnabled
            view.settings.indoorPicker = uiSettings.indoorLevelPickerEnabled
            view.settings.myLocationButton = uiSettings.myLocationButtonEnabled
            view.settings.rotateGestures = uiSettings.rotationGesturesEnabled
            view.settings.scrollGestures = uiSettings.scrollGesturesEnabled
            view.settings.tiltGestures = uiSettings.tiltGesturesEnabled
            view.settings.zoomGestures = uiSettings.zoomGesturesEnabled

            // update content padding
            // iOS GMSMapView automatically re-applies the camera position when
            // padding changes, keeping the target at the logical center
            view.padding = uiEdgeInsets
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun MapType.toGoogleMapType(): GMSMapViewType {
    return when (this) {
        MapType.NONE -> kGMSTypeNone
        MapType.NORMAL -> kGMSTypeNormal
        MapType.SATELLITE -> kGMSTypeSatellite
        MapType.HYBRID -> kGMSTypeHybrid
        MapType.TERRAIN -> kGMSTypeTerrain
    }
}

/**
 * Converts Compose [PaddingValues] to iOS [UIEdgeInsets].
 *
 * Note: iOS UIEdgeInsets uses "points" which are density-independent,
 * similar to Compose Dp. We use the Dp value directly without pixel conversion.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
private fun PaddingValues.toUIEdgeInsets(): CValue<UIEdgeInsets> {
    val layoutDirection = LocalLayoutDirection.current
    return UIEdgeInsetsMake(
        top = calculateTopPadding().value.toDouble(),
        left = calculateLeftPadding(layoutDirection).value.toDouble(),
        bottom = calculateBottomPadding().value.toDouble(),
        right = calculateRightPadding(layoutDirection).value.toDouble()
    )
}
