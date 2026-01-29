package eu.buney.maps

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Reason for camera movement start.
 */
enum class CameraMoveStartedReason {
    /** Camera movement reason is unknown or not yet determined. */
    UNKNOWN,
    /** Camera has not moved yet. */
    NO_MOVEMENT_YET,
    /** Camera movement was initiated by user gesture. */
    GESTURE,
    /** Camera movement was initiated by API animation. */
    API_ANIMATION,
    /** Camera movement was initiated by developer animation. */
    DEVELOPER_ANIMATION,
}

/**
 * Default animation duration in milliseconds.
 */
const val DefaultAnimationDurationMs: Int = 300

/**
 * Internal sealed class to represent camera animation requests.
 */
internal sealed class CameraAnimationRequest {
    abstract val durationMs: Int

    /**
     * Animate to a specific camera position.
     */
    data class ToPosition(
        val position: CameraPosition,
        override val durationMs: Int,
    ) : CameraAnimationRequest()

    /**
     * Animate to fit bounds within the viewport.
     */
    data class ToBounds(
        val bounds: LatLngBounds,
        val padding: Int,
        override val durationMs: Int,
    ) : CameraAnimationRequest()
}

/**
 * A state holder for the camera position.
 *
 * This class holds the current camera position and allows it to be updated.
 * A [CameraPositionState] may only be used by a single [GoogleMap] composable at a time
 * as it reflects instance state for a single view of a map.
 *
 * @param position the initial camera position
 */
@Stable
class CameraPositionState(
    position: CameraPosition = CameraPosition(
        target = LatLng(0.0, 0.0),
        zoom = 10f
    )
) {
    /**
     * Whether the camera is currently moving or not. This includes any kind of movement:
     * panning, zooming, or rotation.
     */
    var isMoving: Boolean by mutableStateOf(false)
        internal set

    /**
     * The reason for the start of the most recent camera moment, or
     * [CameraMoveStartedReason.NO_MOVEMENT_YET] if the camera hasn't moved yet.
     */
    var cameraMoveStartedReason: CameraMoveStartedReason by mutableStateOf(
        CameraMoveStartedReason.NO_MOVEMENT_YET
    )
        internal set

    /**
     * Internal state - updated by platform callbacks (no side effects).
     * This is the source of truth for the current camera position.
     */
    internal var rawPosition: CameraPosition by mutableStateOf(position)

    /**
     * Platform-specific callback to update the native map.
     * Set by platform implementations when the map is attached.
     */
    internal var positionUpdater: ((CameraPosition) -> Unit)? = null

    /**
     * The current camera position.
     *
     * Reading this property returns the current position (from [rawPosition]).
     * Setting this property will update the native map via [positionUpdater].
     */
    var position: CameraPosition
        get() = rawPosition
        set(value) {
            val updater = positionUpdater
            if (updater != null) {
                // Map is attached - update via native API
                // The native map will update rawPosition via callbacks
                updater(value)
            } else {
                // No map attached yet - just store the value
                rawPosition = value
            }
        }

    /**
     * The visible region bounds of the map as a bounding rectangle.
     * Updated when the camera stops moving (idle state).
     * May be null if the map hasn't been laid out yet.
     *
     * Note: For tilted maps, this is the bounding rectangle of the visible
     * trapezoid, which may include areas outside the actual visible region.
     */
    var visibleBounds: LatLngBounds? by mutableStateOf(null)
        internal set

    /**
     * Internal projection provider set by platform implementations.
     * Returns a snapshot of the current projection, or null if the map
     * hasn't been laid out yet.
     */
    internal var projectionProvider: (() -> Projection?)? = null

    /**
     * Returns a snapshot of the current map projection for converting between
     * screen coordinates and geographic coordinates.
     *
     * May be null if the map hasn't been laid out yet or is not currently
     * associated with a [GoogleMap] composable.
     *
     * Note: This is a snapshot that does not automatically update when the
     * camera moves. Access this property again after camera movement to get
     * an updated projection.
     *
     * @see Projection
     */
    val projection: Projection?
        get() = projectionProvider?.invoke()

    /**
     * Internal flow for animation requests.
     */
    private val _animationRequests = MutableSharedFlow<CameraAnimationRequest>(extraBufferCapacity = 1)
    internal val animationRequests: SharedFlow<CameraAnimationRequest> = _animationRequests.asSharedFlow()

    /**
     * Internal flow for immediate move requests.
     */
    private val _moveRequests = MutableSharedFlow<CameraPosition>(extraBufferCapacity = 1)
    internal val moveRequests: SharedFlow<CameraPosition> = _moveRequests.asSharedFlow()

    /**
     * Animate the camera position to the specified [position].
     *
     * @param position The target camera position.
     * @param durationMs The duration of the animation in milliseconds.
     */
    suspend fun animate(
        position: CameraPosition,
        durationMs: Int = DefaultAnimationDurationMs
    ) {
        _animationRequests.emit(CameraAnimationRequest.ToPosition(position, durationMs))
    }

    /**
     * Animate the camera to fit the specified [bounds] within the viewport.
     *
     * @param bounds The bounds to fit within the viewport.
     * @param padding Padding in pixels to apply around the bounds.
     * @param durationMs The duration of the animation in milliseconds.
     *   Note: On iOS, animation duration is fixed (~500ms) and this parameter is ignored.
     */
    suspend fun animateToBounds(
        bounds: LatLngBounds,
        padding: Int = 64,
        durationMs: Int = DefaultAnimationDurationMs
    ) {
        _animationRequests.emit(CameraAnimationRequest.ToBounds(bounds, padding, durationMs))
    }

    /**
     * Animate the camera as specified by [update].
     *
     * This method provides compatibility with android-maps-compose's CameraUpdate API.
     *
     * @param update The [CameraUpdate] describing the camera movement.
     * @param durationMs The duration of the animation in milliseconds.
     *   Note: On iOS, animation duration is fixed (~500ms) and this parameter may be ignored.
     */
    suspend fun animate(
        update: CameraUpdate,
        durationMs: Int = DefaultAnimationDurationMs
    ) {
        when (update) {
            is CameraUpdate.NewCameraPosition -> animate(update.position, durationMs)
            is CameraUpdate.NewLatLngZoom -> animate(
                CameraPosition(target = update.latLng, zoom = update.zoom),
                durationMs
            )
            is CameraUpdate.NewLatLngBounds -> animateToBounds(
                update.bounds,
                update.padding,
                durationMs
            )
        }
    }

    /**
     * Move the camera instantaneously to the specified [position].
     *
     * @param position The target camera position.
     */
    fun move(position: CameraPosition) {
        _moveRequests.tryEmit(position)
    }
}

/**
 * Creates and remembers a [CameraPositionState].
 *
 * @param key An optional key to use for remembering the state. If the key changes,
 * a new state will be created.
 * @param init An initialization block that will be called when the state is first created.
 */
@Composable
fun rememberCameraPositionState(
    key: String? = null,
    init: CameraPositionState.() -> Unit = {}
): CameraPositionState {
    return remember(key) {
        CameraPositionState().apply(init)
    }
}
