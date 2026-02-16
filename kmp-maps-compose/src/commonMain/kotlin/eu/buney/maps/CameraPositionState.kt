package eu.buney.maps

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

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
 * A state holder for the camera position.
 *
 * This class holds the current camera position and allows it to be updated.
 * A [CameraPositionState] may only be used by a single [GoogleMap] composable at a time
 * as it reflects instance state for a single view of a map.
 *
 * @param position the initial camera position
 */
@Stable
expect class CameraPositionState(
    position: CameraPosition = CameraPosition(
        target = LatLng(0.0, 0.0),
        zoom = 10f
    )
) {
    /**
     * Whether the camera is currently moving or not. This includes any kind of movement:
     * panning, zooming, or rotation.
     */
    val isMoving: Boolean

    /**
     * The reason for the start of the most recent camera moment, or
     * [CameraMoveStartedReason.NO_MOVEMENT_YET] if the camera hasn't moved yet.
     */
    val cameraMoveStartedReason: CameraMoveStartedReason

    /**
     * The current camera position.
     *
     * Reading this property returns the current position.
     * Setting this property will update the native map.
     */
    var position: CameraPosition

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

    /**
     * Animate the camera position as specified by [update], returning once the animation has
     * completed. [position] will reflect the position of the camera as the animation proceeds.
     *
     * @param update The [CameraUpdate] describing the camera movement.
     * @param durationMs The duration of the animation in milliseconds. If [Int.MAX_VALUE] is
     *   provided, the default animation duration will be used. Otherwise, the value provided must be
     *   strictly positive, otherwise an [IllegalArgumentException] will be thrown.
     */
    suspend fun animate(
        update: CameraUpdate,
        durationMs: Int = Int.MAX_VALUE
    )

    /**
     * Move the camera instantaneously as specified by [update]. Any calls to [animate] in progress
     * will be cancelled. [position] will be updated when the bound map's position has been updated.
     *
     * @param update The [CameraUpdate] describing the camera movement.
     */
    fun move(update: CameraUpdate)
}

/**
 * The default saver implementation for [CameraPositionState].
 *
 * Our [CameraPosition] is a multiplatform class and can't implement [Parcelable],
 * so we serialize to primitive Doubles which are [Bundle]-compatible.
 */
private val CameraPositionStateSaver: Saver<CameraPositionState, *> = mapSaver(
    save = {
        val pos = it.position
        mapOf(
            "latitude" to pos.target.latitude,
            "longitude" to pos.target.longitude,
            "zoom" to pos.zoom,
            "bearing" to pos.bearing,
            "tilt" to pos.tilt,
        )
    },
    restore = {
        CameraPositionState(
            CameraPosition(
                target = LatLng(it["latitude"] as Double, it["longitude"] as Double),
                zoom = it["zoom"] as Float,
                bearing = it["bearing"] as Float,
                tilt = it["tilt"] as Float,
            )
        )
    }
)

/**
 * Creates and remembers a [CameraPositionState] using [rememberSaveable].
 *
 * The camera position state is saved across configuration changes and process death,
 * ensuring the map retains its last position.
 *
 * @param init An initialization block that will be called when the state is first created.
 */
@Composable
fun rememberCameraPositionState(
    init: CameraPositionState.() -> Unit = {}
): CameraPositionState {
    return rememberSaveable(saver = CameraPositionStateSaver) {
        CameraPositionState().apply(init)
    }
}

/** Provides the [CameraPositionState] used by the map. */
internal val LocalCameraPositionState = staticCompositionLocalOf { CameraPositionState() }

/** The current [CameraPositionState] used by the map. */
val currentCameraPositionState: CameraPositionState
    @[GoogleMapComposable ReadOnlyComposable Composable]
    get() = LocalCameraPositionState.current
