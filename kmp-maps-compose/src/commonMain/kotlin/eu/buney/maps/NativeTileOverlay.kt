package eu.buney.maps

/**
 * Platform-specific tile overlay type.
 *
 * On Android, this is [com.google.android.gms.maps.model.TileOverlay].
 * On iOS, this is [Nothing] because [GMSTileLayer] does not support tap events,
 * so the [TileOverlay] onClick callback never fires.
 */
expect class NativeTileOverlay
