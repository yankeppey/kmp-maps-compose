package eu.buney.maps

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope

/**
 * A side-effect backed by a `LaunchedEffect` which provides access to the underlying
 * platform map object ([NativeMap]) within a coroutine scope.
 *
 * On Android, [NativeMap] is `GoogleMap`. On iOS, it is `GMSMapView`.
 *
 * This effect will be re-launched when [key1] changes.
 */
@Composable
@GoogleMapComposable
expect fun MapEffect(key1: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit)

/**
 * Overload of [MapEffect] that re-launches when [key1] or [key2] changes.
 */
@Composable
@GoogleMapComposable
expect fun MapEffect(key1: Any?, key2: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit)

/**
 * Overload of [MapEffect] that re-launches when [key1], [key2], or [key3] changes.
 */
@Composable
@GoogleMapComposable
expect fun MapEffect(key1: Any?, key2: Any?, key3: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit)

/**
 * Overload of [MapEffect] that re-launches when any of [keys] changes.
 */
@Composable
@GoogleMapComposable
expect fun MapEffect(vararg keys: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit)
