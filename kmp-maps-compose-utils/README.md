# kmp-maps-compose-utils

The [android-maps-compose](https://github.com/googlemaps/android-maps-compose) utils module transitively exposes the full [android-maps-utils](https://github.com/googlemaps/android-maps-utils) library, which includes heatmaps, KML/GeoJSON parsing, geometry and spherical math utilities, and more. This KMP module currently provides **clustering only**. The remaining utilities are candidates for porting to Kotlin common code in the future.

## Clustering

Clustering support is provided as its own Compose-first common Kotlin port, not as a wrapper to Google's [android-maps-utils](https://github.com/googlemaps/android-maps-utils) and [google-maps-ios-utils](https://github.com/googlemaps/google-maps-ios-utils). The reasons are: 
- both libraries are fully open source so porting is not that hard
- there are a number of small API and behavioral differences between the two that might make it confusing if the Kotlin code is the same
- wrapping and unwrapping the common types into native types has a comparable cost

For unclustered items backed by cached `BitmapDescriptor`s, use `clusterItemIcon` instead of
`clusterItemContent`. The item markers remain part of the same clustering and split/merge
transition pipeline, but do not create Compose-rendered marker bitmaps:

```kotlin
Clustering(
    items = vehicles,
    clusterItemIcon = { vehicle -> vehicleMarkerIcons.iconFor(vehicle) },
    clusterItemAnchor = Offset(0.5f, 0.5f),
)
```

`clusterItemIcon` takes precedence over `clusterItemContent` when both are provided.

`clusterItemAnchor` controls which point of the icon is placed at the item's coordinate. The
default is `Offset(0.5f, 1f)`, which suits pin-shaped markers. Use `Offset(0.5f, 0.5f)` for
vehicle or other centre-aligned icons.

### Compose item content keys

When using `clusterItemContent`, use `clusterItemContentKey` to specify when the generated marker
bitmap should be rebuilt. Return only values that affect the marker's appearance:

```kotlin
Clustering(
    items = vehicles,
    clusterItemContentKey = { vehicle -> vehicle.status },
    clusterItemContent = { vehicle ->
        VehicleMarkerContent(status = vehicle.status)
    },
)
```

When `clusterItemContentKey` is omitted, the item itself is used as the key.

### Marker visibility

Use `markerVisibility` to suppress marker output outside a visible area. This affects rendering
only: all items still participate in cluster calculation and split/merge transitions.

```kotlin
Clustering(
    items = vehicles,
    markerVisibility = { position ->
        cameraPositionState.projection?.contains(position) ?: true
    },
)
```

### Known limitations

Caching and performance have not really been battle-tested and there is definitely room for improvement in the future:

- **No zoom-level caching.** Android's `PreCachingAlgorithmDecorator` caches cluster results per zoom level and pre-computes adjacent levels in the background. This implementation recomputes from scratch on every zoom change.
- **No clustering throttle.** iOS throttles clustering requests with a 0.2-second delay to avoid redundant computation during rapid camera movements. This implementation triggers immediately when the camera settles.
- **Destructive item sync.** When the item collection changes, the quad tree is cleared and rebuilt entirely. The native Android `Algorithm` interface supports incremental `addItem`/`removeItem` operations, though notably the official [android-maps-compose](https://github.com/googlemaps/android-maps-compose) library also uses the clear-and-rebuild approach.
- **No viewport culling.** iOS's renderer only processes clusters within the visible bounds. This implementation renders all clusters regardless of visibility.
