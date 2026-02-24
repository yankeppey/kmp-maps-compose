# Changelog

## 0.4.0

Based on [android-maps-compose 8.1.0](https://github.com/googlemaps/android-maps-compose).

**SDK Versions:**
- Google Maps iOS SDK: 10.8.0
- Google Play Services Maps: 20.0.0

### Features

- **Marker Clustering** - New `kmp-maps-compose-utils` module with Compose-first marker clustering. Includes `NonHierarchicalDistanceBasedAlgorithm` ported from android-maps-utils, animated split/merge transitions, and `DefaultClusterContent` matching android-maps-utils visual appearance. ([#6](https://github.com/yankeppey/kmp-maps-compose/issues/6))
- **`MapStyleOptions`** - New expect/actual class for custom map styling via JSON from the [Google Maps Styling Wizard](https://mapstyle.withgoogle.com/). Use `MapStyleOptions.fromJson()` with `MapProperties`. ([#5](https://github.com/yankeppey/kmp-maps-compose/issues/5))
- **`MapEffect` composable** - Cross-platform access to the underlying native map object (`GoogleMap` on Android, `GMSMapView` on iOS) via `NativeMap` type alias

### Improvements

- **iOS: Improved composable-to-UIImage rendering** - Rewritten rendering pipeline for marker and info window image capture

## 0.3.0

Based on [android-maps-compose 8.1.0](https://github.com/googlemaps/android-maps-compose).

**SDK Versions:**
- Google Maps iOS SDK: 10.8.0
- Google Play Services Maps: 20.0.0

### Breaking Changes

- **CameraUpdate-based API** - `animate()` and `move()` now take a `CameraUpdate` parameter instead of `CameraPosition` directly. Use `CameraUpdateFactory.newCameraPosition()`, `CameraUpdateFactory.newLatLngZoom()`, or `CameraUpdateFactory.newLatLngBounds()` to create updates. This aligns the API with android-maps-compose.
- **`animateToBounds()` removed** - Use `animate(CameraUpdateFactory.newLatLngBounds(bounds, padding))` instead.
- **`visibleBounds` removed** - Use `cameraPositionState.projection?.visibleBounds` instead, querying it when `isMoving` becomes false.
- **Default animation duration changed** - `animate()` now defaults to `Int.MAX_VALUE` (SDK default duration) instead of 300ms. Pass an explicit `durationMs` to control duration.
- **`rememberCameraPositionState` `key` parameter removed** - The function now uses positional `rememberSaveable` scoping.

### Features

- **`CameraUpdateFactory`** - New factory object with `newCameraPosition()`, `newLatLngZoom()`, and `newLatLngBounds()` methods matching the android-maps-compose API
- **`currentCameraPositionState`** - Composables inside `GoogleMap` content can now access the camera state implicitly via `currentCameraPositionState`, matching the android-maps-compose API
- **Camera state saved across configuration changes** - `rememberCameraPositionState()` now uses `rememberSaveable`, so camera position survives configuration changes and process death on Android
- **iOS: Coroutine-aware `animate()`** - `animate()` now properly suspends until the animation completes and supports cancellation, matching Android behavior. Uses `CATransaction.setCompletionBlock` for completion detection; cancellation snaps the camera to stop the animation.
- **iOS: Custom animation duration** - The `durationMs` parameter is now forwarded to the native SDK via `CATransaction.setAnimationDuration()`
- **iOS: Deferred camera operations** - `animate()` and `move()` called before the map is available are now deferred and executed when the map becomes available, matching Android behavior

### Architecture

- **`CameraPositionState` is now `expect/actual`** - On Android, delegates directly to android-maps-compose's `CameraPositionState` (single source of truth, no bidirectional sync). On iOS, manages `GMSMapView` directly via `setMap()`.
- **iOS: Node-based map property management** - Introduced `IOSMapPropertiesNode` for managing map properties, UI settings, content padding, and camera state binding via the Compose node tree, replacing the imperative `UIKitView` update approach.

### Other Changes

- Updated Kotlin to 2.3.10, Compose Multiplatform to 1.10.1, AGP to 9.0.1
- Updated maps-compose to 8.1.0, Gradle to 9.3.1

## 0.2.0

Based on [android-maps-compose 8.0.0](https://github.com/googlemaps/android-maps-compose).

**SDK Versions:**
- Google Maps iOS SDK: 10.8.0
- Google Play Services Maps: 20.0.0

### Features

- **Styled Polylines** - Added support for gradient and stamped polylines via new `StyleSpan`, `StrokeStyle`, and `StampStyle` APIs
- **Projection API** - Added `CameraPositionState.projection` for coordinate/screen conversions and `visibleBounds` for tracking the visible map region
- **Compose Resources Integration** - Added `rememberBitmapDescriptor()` for easy loading of marker icons from Compose Resources

### Fixes

- Fixed camera position feedback loop causing map trembling on Android
- Fixed Android camera position not updating on user gestures
- Fixed `@GoogleMapComposable` annotation mismatch warnings

### Other Changes

- Updated to Gradle 9.3.0 and AGP 9.0.0
- Updated Google Maps SDK versions
- Improved Compose stability configuration
