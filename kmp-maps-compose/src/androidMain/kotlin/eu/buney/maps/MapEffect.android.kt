package eu.buney.maps

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.MapsComposeExperimentalApi
import kotlinx.coroutines.CoroutineScope
import com.google.maps.android.compose.MapEffect as AndroidMapEffect

@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    AndroidMapEffect(key1 = key1) { block(it) }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, key2: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    AndroidMapEffect(key1 = key1, key2 = key2) { block(it) }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, key2: Any?, key3: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    AndroidMapEffect(key1 = key1, key2 = key2, key3 = key3) { block(it) }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
actual fun MapEffect(vararg keys: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    AndroidMapEffect(keys = keys) { block(it) }
}
