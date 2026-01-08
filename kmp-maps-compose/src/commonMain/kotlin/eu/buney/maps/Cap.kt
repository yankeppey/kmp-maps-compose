package eu.buney.maps

/**
 * Cap styles for polyline endpoints.
 *
 * Note: Cap styles are only supported on Android. On iOS, this parameter is ignored
 * and the default cap style is used.
 */
sealed class Cap {
    data object Butt : Cap()
    data object Round : Cap()
    data object Square : Cap()
}
