package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val mapView = (currentComposer.applier as MapApplier).mapView
    LaunchedEffect(key1 = key1) { block(mapView) }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, key2: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val mapView = (currentComposer.applier as MapApplier).mapView
    LaunchedEffect(key1 = key1, key2 = key2) { block(mapView) }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, key2: Any?, key3: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val mapView = (currentComposer.applier as MapApplier).mapView
    LaunchedEffect(key1 = key1, key2 = key2, key3 = key3) { block(mapView) }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(vararg keys: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val mapView = (currentComposer.applier as MapApplier).mapView
    LaunchedEffect(keys = keys) { block(mapView) }
}
