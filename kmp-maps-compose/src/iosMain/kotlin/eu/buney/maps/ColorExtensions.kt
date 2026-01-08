package eu.buney.maps

import androidx.compose.ui.graphics.Color
import platform.UIKit.UIColor

/**
 * Converts a Compose [Color] to a UIKit [UIColor].
 */
internal fun Color.toUIColor(): UIColor {
    return UIColor(
        red = this.red.toDouble(),
        green = this.green.toDouble(),
        blue = this.blue.toDouble(),
        alpha = this.alpha.toDouble()
    )
}
