# Changelog

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
