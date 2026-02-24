package eu.buney.maps.utils.clustering

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.buney.maps.LatLng
import kotlin.math.min

/**
 * Buckets matching android-maps-utils
 * [DefaultClusterRenderer.BUCKETS](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L86).
 */
private val BUCKETS = intArrayOf(10, 20, 50, 100, 200, 500, 1000)

/**
 * Semi-transparent white, matching android-maps-utils
 * [outline color](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L191).
 */
private val OutlineColor = Color(0x80FFFFFF)

@Composable
internal fun <T : ClusterItem> DefaultClusterContent(cluster: Cluster<T>) {
    val size = cluster.size
    val bucket = getBucket(size)
    val text = getClusterText(bucket)
    val color = getClusterColor(bucket)
    // Layer structure matches android-maps-utils makeClusterBackground():
    // https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L188-L196
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(OutlineColor, CircleShape)  // outer outline circle (L190-L191)
            .padding(3.dp)                           // strokeWidth = 3 * density (L193)
            .background(color, CircleShape)          // inner colored circle (L189)
            .squareLayout()                          // SquareTextView.onMeasure (L48-L61)
            .padding(12.dp),                         // 12 * density padding (L203-L204)
    ) {
        // Text style matching amu_ClusterIcon.TextAppearance:
        // https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/res/values/styles.xml#L29-L33
        BasicText(
            text = text,
            style = TextStyle(
                color = Color(0xFFEEEEEE),  // #ffeeeeee
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

/**
 * Forces the layout to be square, using the larger of width/height.
 * Mirrors android-maps-utils
 * [SquareTextView.onMeasure()](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/ui/SquareTextView.java#L48-L61)
 * which calls setMeasuredDimension(max(width, height), max(width, height)).
 */
private fun Modifier.squareLayout() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val size = maxOf(placeable.width, placeable.height)
    layout(size, size) {
        placeable.placeRelative(
            (size - placeable.width) / 2,
            (size - placeable.height) / 2,
        )
    }
}

/**
 * Snap [size] to the nearest bucket, or return exact size if below the first bucket.
 *
 * See [DefaultClusterRenderer.getBucket()](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L237-L248).
 */
private fun getBucket(size: Int): Int {
    if (size <= BUCKETS[0]) return size
    for (i in 0 until BUCKETS.size - 1) {
        if (size < BUCKETS[i + 1]) return BUCKETS[i]
    }
    return BUCKETS.last()
}

/**
 * Format the bucket value: exact count for small clusters, "N+" for bucketed ones.
 *
 * See [DefaultClusterRenderer.getClusterText()](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L226-L231).
 */
private fun getClusterText(bucket: Int): String {
    if (bucket < BUCKETS[0]) return "$bucket"
    return "$bucket+"
}

/**
 * Compute cluster color using the same HSV formula as android-maps-utils
 * [DefaultClusterRenderer.getColor()](https://github.com/googlemaps/android-maps-utils/blob/58a0179f/library/src/main/java/com/google/maps/android/clustering/view/DefaultClusterRenderer.java#L209-L217).
 * The hue shifts from blue (220°) for small clusters to red (0°) for large
 * ones, with saturation=1 and value=0.6.
 */
private fun getClusterColor(clusterSize: Int): Color {
    val hueRange = 220f
    val sizeRange = 300f
    val size = min(clusterSize.toFloat(), sizeRange)
    val hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange
    return Color.hsv(hue, 1f, 0.6f)
}

// region Previews

private data class PreviewClusterItem(
    override val position: LatLng = LatLng(0.0, 0.0),
    override val title: String? = null,
    override val snippet: String? = null,
    override val zIndex: Float? = null,
) : ClusterItem

private fun previewCluster(size: Int): Cluster<PreviewClusterItem> = object : Cluster<PreviewClusterItem> {
    override val position = LatLng(0.0, 0.0)
    override val size = size
    override val items = List(size) { PreviewClusterItem() }
}

@Preview
@Composable
private fun DefaultClusterContentPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp),
    ) {
        for (size in listOf(3, 10, 25, 50, 100, 200, 500, 1000)) {
            DefaultClusterContent(previewCluster(size))
        }
    }
}

// endregion
