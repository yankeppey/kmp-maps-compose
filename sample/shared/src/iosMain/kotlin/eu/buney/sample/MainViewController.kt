package eu.buney.sample

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import GoogleMaps.GMSServices

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController(
        configure = {
            // initialize Google Maps SDK with API key from BuildKonfig
            GMSServices.provideAPIKey(BuildKonfig.MAPS_API_KEY)
        }
    ) {
        App()
    }
}
