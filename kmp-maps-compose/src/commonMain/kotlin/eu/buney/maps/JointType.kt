package eu.buney.maps

/**
 * Joint types for polyline vertices.
 *
 * Note: Joint types are only supported on Android. On iOS, this parameter is ignored
 * and the default joint type is used.
 */
enum class JointType {
    Default,
    Bevel,
    Round,
}
