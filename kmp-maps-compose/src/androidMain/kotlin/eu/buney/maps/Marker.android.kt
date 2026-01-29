package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.google.maps.android.compose.Marker as AndroidMarker
import com.google.maps.android.compose.MarkerInfoWindow as AndroidMarkerInfoWindow
import com.google.maps.android.compose.MarkerInfoWindowContent as AndroidMarkerInfoWindowContent
import com.google.maps.android.compose.rememberUpdatedMarkerState as androidRememberUpdatedMarkerState

/**
 * Android implementation of [Marker] that wraps the platform marker.
 */
actual class Marker(
    val googleMarker: GoogleMarker
) {
    actual val position: LatLng = LatLng(googleMarker.position.latitude, googleMarker.position.longitude)

    actual val title: String? = googleMarker.title

    actual val snippet: String? = googleMarker.snippet
}

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
    // bridge to Android MarkerState
    val androidState = androidRememberUpdatedMarkerState(
        position = GoogleLatLng(state.position.latitude, state.position.longitude)
    )

    // sync dragging state from Android to our state
    LaunchedEffect(androidState.isDragging) {
        state.isDragging = androidState.isDragging
    }

    // sync position from Android when user drags the marker
    LaunchedEffect(androidState.position) {
        if (androidState.isDragging || state.position.latitude != androidState.position.latitude ||
            state.position.longitude != androidState.position.longitude
        ) {
            state.position = LatLng(androidState.position.latitude, androidState.position.longitude)
        }
    }

    // sync position from our state to Android state when changed programmatically
    LaunchedEffect(state.position) {
        if (!androidState.isDragging) {
            val newPosition = GoogleLatLng(state.position.latitude, state.position.longitude)
            if (androidState.position != newPosition) {
                androidState.position = newPosition
            }
        }
    }

    // set up info window callbacks using the Android state's methods
    LaunchedEffect(Unit) {
        state.showInfoWindowCallback = { androidState.showInfoWindow() }
        state.hideInfoWindowCallback = { androidState.hideInfoWindow() }
    }

    AndroidMarker(
        state = androidState,
        contentDescription = contentDescription,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon?.googleBitmapDescriptor,
        infoWindowAnchor = infoWindowAnchor,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = { googleMarker ->
            // store reference and invoke callback
            state.platformMarker = googleMarker
            onClick(Marker(googleMarker))
        },
        onInfoWindowClick = { googleMarker ->
            onInfoWindowClick(Marker(googleMarker))
        },
        onInfoWindowClose = { googleMarker ->
            onInfoWindowClose(Marker(googleMarker))
        },
        onInfoWindowLongClick = { googleMarker ->
            onInfoWindowLongClick(Marker(googleMarker))
        },
    )
}

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
    // bridge to Android MarkerState
    val androidState = androidRememberUpdatedMarkerState(
        position = GoogleLatLng(state.position.latitude, state.position.longitude)
    )

    // sync dragging state from Android to our state
    LaunchedEffect(androidState.isDragging) {
        state.isDragging = androidState.isDragging
    }

    // sync position from Android when user drags the marker
    LaunchedEffect(androidState.position) {
        if (androidState.isDragging || state.position.latitude != androidState.position.latitude ||
            state.position.longitude != androidState.position.longitude
        ) {
            state.position = LatLng(androidState.position.latitude, androidState.position.longitude)
        }
    }

    // sync position from our state to Android state when changed programmatically
    LaunchedEffect(state.position) {
        if (!androidState.isDragging) {
            val newPosition = GoogleLatLng(state.position.latitude, state.position.longitude)
            if (androidState.position != newPosition) {
                androidState.position = newPosition
            }
        }
    }

    // set up info window callbacks using the Android state's methods
    LaunchedEffect(Unit) {
        state.showInfoWindowCallback = { androidState.showInfoWindow() }
        state.hideInfoWindowCallback = { androidState.hideInfoWindow() }
    }

    AndroidMarkerInfoWindow(
        state = androidState,
        contentDescription = contentDescription,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon?.googleBitmapDescriptor,
        infoWindowAnchor = infoWindowAnchor,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = { googleMarker ->
            state.platformMarker = googleMarker
            onClick(Marker(googleMarker))
        },
        onInfoWindowClick = { googleMarker ->
            onInfoWindowClick(Marker(googleMarker))
        },
        onInfoWindowClose = { googleMarker ->
            onInfoWindowClose(Marker(googleMarker))
        },
        onInfoWindowLongClick = { googleMarker ->
            onInfoWindowLongClick(Marker(googleMarker))
        },
        content = content?.let { contentLambda ->
            { googleMarker -> contentLambda(Marker(googleMarker)) }
        },
    )
}

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
    // bridge to Android MarkerState
    val androidState = androidRememberUpdatedMarkerState(
        position = GoogleLatLng(state.position.latitude, state.position.longitude)
    )

    // sync dragging state from Android to our state
    LaunchedEffect(androidState.isDragging) {
        state.isDragging = androidState.isDragging
    }

    // sync position from Android when user drags the marker
    LaunchedEffect(androidState.position) {
        if (androidState.isDragging || state.position.latitude != androidState.position.latitude ||
            state.position.longitude != androidState.position.longitude
        ) {
            state.position = LatLng(androidState.position.latitude, androidState.position.longitude)
        }
    }

    // sync position from our state to Android state when changed programmatically
    LaunchedEffect(state.position) {
        if (!androidState.isDragging) {
            val newPosition = GoogleLatLng(state.position.latitude, state.position.longitude)
            if (androidState.position != newPosition) {
                androidState.position = newPosition
            }
        }
    }

    // set up info window callbacks using the Android state's methods
    LaunchedEffect(Unit) {
        state.showInfoWindowCallback = { androidState.showInfoWindow() }
        state.hideInfoWindowCallback = { androidState.hideInfoWindow() }
    }

    AndroidMarkerInfoWindowContent(
        state = androidState,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        flat = flat,
        icon = icon?.googleBitmapDescriptor,
        infoWindowAnchor = infoWindowAnchor,
        rotation = rotation,
        snippet = snippet,
        tag = tag,
        title = title,
        visible = visible,
        zIndex = zIndex,
        onClick = { googleMarker ->
            state.platformMarker = googleMarker
            onClick(Marker(googleMarker))
        },
        onInfoWindowClick = { googleMarker ->
            onInfoWindowClick(Marker(googleMarker))
        },
        onInfoWindowClose = { googleMarker ->
            onInfoWindowClose(Marker(googleMarker))
        },
        onInfoWindowLongClick = { googleMarker ->
            onInfoWindowLongClick(Marker(googleMarker))
        },
        content = content?.let { contentLambda ->
            { googleMarker -> contentLambda(Marker(googleMarker)) }
        },
    )
}
