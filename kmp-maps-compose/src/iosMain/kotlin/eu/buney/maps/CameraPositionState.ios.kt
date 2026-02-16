package eu.buney.maps

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Internal data class bundling a [CameraUpdate] with its animation duration.
 */
internal data class AnimateRequest(val update: CameraUpdate, val durationMs: Int)

/**
 * iOS implementation of [CameraPositionState].
 *
 * On iOS, there is no equivalent wrapper library like android-maps-compose.
 * We talk directly to GMSMapView, so this class manages its own state and
 * communicates with the native map via callbacks and flows.
 */
@Stable
actual class CameraPositionState actual constructor(
    position: CameraPosition
) {
    internal var _isMoving by mutableStateOf(false)
    actual val isMoving: Boolean get() = _isMoving

    internal var _cameraMoveStartedReason by mutableStateOf(CameraMoveStartedReason.NO_MOVEMENT_YET)
    actual val cameraMoveStartedReason: CameraMoveStartedReason get() = _cameraMoveStartedReason

    /**
     * Internal state — updated by platform callbacks (no side effects).
     * This is the source of truth for the current camera position.
     */
    internal var rawPosition: CameraPosition by mutableStateOf(position)

    /**
     * Platform-specific callback to update the native map.
     * Set by [SetupCameraPositionStateSync] when the map is attached.
     */
    internal var positionUpdater: ((CameraPosition) -> Unit)? = null

    actual var position: CameraPosition
        get() = rawPosition
        set(value) {
            val updater = positionUpdater
            if (updater != null) {
                // Map is attached — update via native API
                // The native map will update rawPosition via callbacks
                updater(value)
            } else {
                // No map attached yet — just store the value
                rawPosition = value
            }
        }

    /**
     * Internal projection provider set by [SetupCameraPositionStateSync].
     */
    internal var projectionProvider: (() -> Projection?)? = null

    actual val projection: Projection?
        get() = projectionProvider?.invoke()

    /**
     * Internal flow for animation requests.
     */
    private val _animationRequests = MutableSharedFlow<AnimateRequest>(extraBufferCapacity = 1)
    internal val animationRequests: SharedFlow<AnimateRequest> = _animationRequests.asSharedFlow()

    /**
     * Internal flow for immediate move requests.
     */
    private val _moveRequests = MutableSharedFlow<CameraUpdate>(extraBufferCapacity = 1)
    internal val moveRequests: SharedFlow<CameraUpdate> = _moveRequests.asSharedFlow()

    actual suspend fun animate(
        update: CameraUpdate,
        durationMs: Int
    ) {
        _animationRequests.emit(AnimateRequest(update, durationMs))
    }

    actual fun move(update: CameraUpdate) {
        _moveRequests.tryEmit(update)
    }
}
