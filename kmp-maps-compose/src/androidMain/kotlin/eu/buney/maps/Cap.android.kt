package eu.buney.maps

import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.SquareCap
import com.google.android.gms.maps.model.Cap as GoogleCap
import com.google.android.gms.maps.model.JointType as GoogleJointType

/**
 * Converts a multiplatform [Cap] to a Google Maps Android SDK Cap.
 */
internal fun Cap.toGoogleCap(): GoogleCap = when (this) {
    Cap.Butt -> ButtCap()
    Cap.Round -> RoundCap()
    Cap.Square -> SquareCap()
}

/**
 * Converts a multiplatform [JointType] to a Google Maps Android SDK JointType constant.
 */
internal fun JointType.toGoogleJointType(): Int = when (this) {
    JointType.Default -> GoogleJointType.DEFAULT
    JointType.Bevel -> GoogleJointType.BEVEL
    JointType.Round -> GoogleJointType.ROUND
}
