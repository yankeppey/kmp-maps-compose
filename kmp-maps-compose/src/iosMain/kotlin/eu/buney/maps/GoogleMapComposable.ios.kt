package eu.buney.maps

import androidx.compose.runtime.ComposableTargetMarker

/**
 * An annotation that can be used to mark a composable function as being expected to be used
 * inside of a [GoogleMap] content lambda.
 */
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Google Map Composable")
@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
)
actual annotation class GoogleMapComposable
