package eu.buney.maps

/**
 * Platform-specific map type.
 *
 * On Android, this is [com.google.android.gms.maps.GoogleMap].
 * On iOS, this is `GMSMapView`.
 *
 * Used by [MapEffect] to provide access to the underlying native map object.
 */
expect class NativeMap
