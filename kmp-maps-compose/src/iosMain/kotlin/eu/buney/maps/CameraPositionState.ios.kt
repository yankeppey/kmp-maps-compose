package eu.buney.maps

import GoogleMaps.GMSCameraPosition
import GoogleMaps.GMSCoordinateBounds
import GoogleMaps.GMSMapView
import GoogleMaps.animateToCameraPosition
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.QuartzCore.CATransaction
import platform.QuartzCore.CATransaction.Companion.setAnimationDuration
import platform.QuartzCore.CATransaction.Companion.setCompletionBlock
import platform.UIKit.UIEdgeInsetsMake
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS implementation of [CameraPositionState].
 *
 * On iOS, there is no equivalent wrapper library like android-maps-compose.
 * We talk directly to GMSMapView, so this class manages its own state and
 * holds a direct reference to the native map view (set via [setMap]).
 */
@OptIn(ExperimentalForeignApi::class)
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
     * The native map view currently associated with this CameraPositionState.
     * Set via [setMap] when the map is composed, cleared when it's disposed.
     */
    internal var map: GMSMapView? = null
        private set

    /**
     * Callback for deferred operations that need to wait for a map.
     * Only one callback can be pending at a time; newer calls supersede older ones.
     */
    private fun interface OnMapChangedCallback {
        fun onMapChanged(newMap: GMSMapView?)
        fun onCancel() {}
    }

    private var onMapChanged: OnMapChangedCallback? = null

    private fun doOnMapChanged(callback: OnMapChangedCallback) {
        onMapChanged?.onCancel()
        onMapChanged = callback
    }

    /**
     * Associates or disassociates a [GMSMapView] with this state.
     * When a map is set, the current [position] is applied to it.
     * When cleared, [isMoving] is reset to false.
     */
    internal fun setMap(map: GMSMapView?) {
        if (this.map == null && map == null) return
        if (this.map != null && map != null) {
            error("CameraPositionState may only be associated with one GMSMapView at a time")
        }
        this.map = map
        if (map == null) {
            _isMoving = false
        } else {
            map.camera = rawPosition.toGMSCameraPosition()
        }
        // Drain any pending callback (animate or move that was deferred)
        onMapChanged?.let {
            onMapChanged = null
            it.onMapChanged(map)
        }
    }

    actual var position: CameraPosition
        get() = rawPosition
        set(value) {
            // Cancel any pending deferred animate/move — direct position set takes precedence
            onMapChanged?.onCancel()
            onMapChanged = null
            val map = map
            if (map != null) {
                map.camera = value.toGMSCameraPosition()
                rawPosition = value
            } else {
                rawPosition = value
            }
        }

    actual val projection: Projection?
        get() = map?.let { IOSProjection(it.projection) }

    actual suspend fun animate(
        update: CameraUpdate,
        durationMs: Int
    ) {
        val map = map ?: awaitMap()
        _cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION
        map.applyAnimatedCameraUpdate(update, durationMs)
    }

    /**
     * Suspends until a [GMSMapView] becomes available via [setMap].
     *
     * Throws [CancellationException] if the map is cleared before it becomes
     * available, or if another camera operation supersedes this one.
     */
    private suspend fun awaitMap(): GMSMapView {
        return suspendCancellableCoroutine { continuation ->
            val callback = object : OnMapChangedCallback {
                override fun onMapChanged(newMap: GMSMapView?) {
                    if (newMap == null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                CancellationException(
                                    "animate cancelled: map was cleared before it became available"
                                )
                            )
                        }
                        return
                    }
                    if (continuation.isActive) continuation.resume(newMap)
                }

                override fun onCancel() {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            CancellationException(
                                "animate cancelled: superseded by another camera operation"
                            )
                        )
                    }
                }
            }
            doOnMapChanged(callback)
            continuation.invokeOnCancellation {
                if (onMapChanged === callback) {
                    onMapChanged = null
                }
            }
        }
    }

    actual fun move(update: CameraUpdate) {
        val map = map
        if (map != null) {
            val position = map.applyInstantCameraUpdate(update)
            rawPosition = position
        } else {
            // Defer until map is available
            doOnMapChanged { newMap ->
                if (newMap != null) {
                    val position = newMap.applyInstantCameraUpdate(update)
                    rawPosition = position
                }
            }
        }
    }
}

// region CameraUpdate Helpers

/**
 * Applies a [CameraUpdate] as an animation on the map view, suspending until the
 * animation completes.
 *
 * When [durationMs] is [Int.MAX_VALUE] the SDK's default animation duration is used.
 * Otherwise the duration is overridden via [CATransaction.setAnimationDuration].
 *
 * Completion is detected via [CATransaction.setCompletionBlock].
 * Cancellation snaps the camera to its current position to stop the animation.
 */
@OptIn(ExperimentalForeignApi::class)
private suspend fun GMSMapView.applyAnimatedCameraUpdate(
    update: CameraUpdate,
    durationMs: Int,
) {
    suspendCancellableCoroutine { continuation ->
        CATransaction.withTransaction {
            if (durationMs != Int.MAX_VALUE) {
                setAnimationDuration(durationMs / 1000.0)
            }
            setCompletionBlock {
                if (continuation.isActive) continuation.resume(Unit)
            }
            when (update) {
                is CameraUpdate.NewCameraPosition -> {
                    animateToCameraPosition(update.position.toGMSCameraPosition())
                }
                is CameraUpdate.NewLatLngZoom -> {
                    val position = CameraPosition(target = update.latLng, zoom = update.zoom)
                    animateToCameraPosition(position.toGMSCameraPosition())
                }
                is CameraUpdate.NewLatLngBounds -> {
                    val gmsBounds = update.bounds.toGMSCoordinateBounds()
                    val padding = update.padding.toDouble()
                    val insets = UIEdgeInsetsMake(padding, padding, padding, padding)
                    val cameraPosition = cameraForBounds(gmsBounds, insets = insets)
                    if (cameraPosition != null) {
                        animateToCameraPosition(cameraPosition)
                    }
                }
            }
        }
        continuation.invokeOnCancellation {
            // Stop the in-flight animation by snapping to the current
            // interpolated position. Reading `camera` during an animation
            // returns the in-flight value; setting it back cancels the animation.
            setCamera(camera)
        }
    }
}

/**
 * Applies a [CameraUpdate] instantly on the map view.
 * Returns the resulting [CameraPosition].
 */
@OptIn(ExperimentalForeignApi::class)
private fun GMSMapView.applyInstantCameraUpdate(update: CameraUpdate): CameraPosition {
    when (update) {
        is CameraUpdate.NewCameraPosition -> {
            camera = update.position.toGMSCameraPosition()
            return update.position
        }
        is CameraUpdate.NewLatLngZoom -> {
            val position = CameraPosition(target = update.latLng, zoom = update.zoom)
            camera = position.toGMSCameraPosition()
            return position
        }
        is CameraUpdate.NewLatLngBounds -> {
            val gmsBounds = update.bounds.toGMSCoordinateBounds()
            val padding = update.padding.toDouble()
            val insets = UIEdgeInsetsMake(padding, padding, padding, padding)
            val gmsPosition = cameraForBounds(gmsBounds, insets = insets)
            if (gmsPosition != null) {
                camera = gmsPosition
            }
            // Read back the actual camera position
            return camera.let { cam ->
                cam.target.useContents {
                    CameraPosition(
                        target = LatLng(latitude, longitude),
                        zoom = cam.zoom,
                        bearing = cam.bearing.toFloat(),
                        tilt = cam.viewingAngle.toFloat()
                    )
                }
            }
        }
    }
}

// endregion

// region Type Conversions

@OptIn(ExperimentalForeignApi::class)
internal fun CameraPosition.toGMSCameraPosition(): GMSCameraPosition {
    val coordinate = CLLocationCoordinate2DMake(
        latitude = target.latitude,
        longitude = target.longitude
    )
    return GMSCameraPosition.cameraWithTarget(
        target = coordinate,
        zoom = zoom,
        bearing = bearing.toDouble(),
        viewingAngle = tilt.toDouble()
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun LatLngBounds.toGMSCoordinateBounds(): GMSCoordinateBounds {
    val swCoord = CLLocationCoordinate2DMake(
        latitude = southwest.latitude,
        longitude = southwest.longitude
    )
    val neCoord = CLLocationCoordinate2DMake(
        latitude = northeast.latitude,
        longitude = northeast.longitude
    )
    return GMSCoordinateBounds()
        .includingCoordinate(swCoord)
        .includingCoordinate(neCoord)
}

// endregion
