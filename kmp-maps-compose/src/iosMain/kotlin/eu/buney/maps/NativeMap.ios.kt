package eu.buney.maps

import GoogleMaps.GMSMapView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual typealias NativeMap = GMSMapView
