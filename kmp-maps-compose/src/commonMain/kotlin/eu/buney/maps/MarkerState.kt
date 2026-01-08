package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * A state object that can be used to control or observe the marker state.
 *
 * @param position the initial position of the marker
 */
@Stable
class MarkerState(
    position: LatLng = LatLng(0.0, 0.0)
) {
    /**
     * The current position of the marker.
     */
    var position: LatLng by mutableStateOf(position)

    /**
     * Whether the marker is currently being dragged.
     */
    var isDragging: Boolean by mutableStateOf(false)
        internal set

    /**
     * Platform-specific marker reference. Used internally for bridging.
     */
    internal var platformMarker: Any? = null

    /**
     * Platform-specific callback for showing the info window.
     * Set by the platform implementation when the marker is created.
     */
    internal var showInfoWindowCallback: (() -> Unit)? = null

    /**
     * Platform-specific callback for hiding the info window.
     * Set by the platform implementation when the marker is created.
     */
    internal var hideInfoWindowCallback: (() -> Unit)? = null

    /**
     * Shows the info window for the underlying marker.
     *
     * Note: Only a single info window can be visible on the map at a time.
     * Calling this will hide any currently visible info window.
     *
     * Only use from Compose Effect APIs (e.g., LaunchedEffect, DisposableEffect),
     * never directly from composition, to avoid unexpected behavior.
     */
    fun showInfoWindow() {
        showInfoWindowCallback?.invoke()
    }

    /**
     * Hides the info window for the underlying marker.
     *
     * Only use from Compose Effect APIs (e.g., LaunchedEffect, DisposableEffect),
     * never directly from composition, to avoid unexpected behavior.
     */
    fun hideInfoWindow() {
        hideInfoWindowCallback?.invoke()
    }

    companion object {
        /**
         * The default saver implementation for [MarkerState].
         *
         * Note: android-maps-compose saves [LatLng] directly because Google's
         * [com.google.android.gms.maps.model.LatLng] implements [Parcelable].
         * Our [LatLng] is a multiplatform class and can't implement [Parcelable],
         * so we serialize to primitive Doubles which are [Bundle]-compatible.
         */
        val Saver: Saver<MarkerState, *> = listSaver(
            save = { listOf(it.position.latitude, it.position.longitude) },
            restore = { MarkerState(LatLng(it[0], it[1])) }
        )
    }
}

/**
 * Uses [rememberSaveable] to retain [MarkerState.position] across configuration changes,
 * for simple use cases.
 *
 * Other use cases may be better served syncing [MarkerState.position] with a data model.
 *
 * This cannot be used to preserve info window visibility across configuration changes.
 *
 * This function does not automatically update the MarkerState when the input parameters change.
 * If you need this behavior, use [rememberUpdatedMarkerState].
 *
 * @param key an optional key used to identify the state in [rememberSaveable]
 * @param position the initial position of the marker
 */
@Composable
@Deprecated(
    message = "Use 'rememberUpdatedMarkerState' instead - It may be confusing to think " +
            "that the state is automatically updated as the position changes, " +
            "so it will be changed or removed.",
    replaceWith = ReplaceWith(
        expression = """
            val markerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
                MarkerState(position)
            }
        """
    )
)
fun rememberMarkerState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

/**
 * This function updates the state value according to the update of the input parameter,
 * like [rememberUpdatedState][androidx.compose.runtime.rememberUpdatedState].
 *
 * This cannot be used to preserve state across configuration changes.
 *
 * @param position the position of the marker, which will be applied to the state on every recomposition
 */
@Composable
fun rememberUpdatedMarkerState(
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = remember {
    MarkerState(position = position)
}.also { it.position = position }
