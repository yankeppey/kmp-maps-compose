package eu.buney.maps

/**
 * Describes a repeating image/texture to stamp over a polyline segment.
 *
 * The image is repeated along the polyline's stroke. For best results,
 * use a square image as it will be compressed to fit the stroke width.
 *
 * The image is oriented with its top toward the start point and bottom
 * toward the end point of the polyline segment.
 *
 * @param image The image to repeat as a stamp. Use [BitmapDescriptorFactory]
 *              to create this from raw bytes or encoded image data.
 */
class StampStyle(
    val image: BitmapDescriptor
)
