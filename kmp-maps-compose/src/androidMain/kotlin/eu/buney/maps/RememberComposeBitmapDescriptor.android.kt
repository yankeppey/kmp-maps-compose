package eu.buney.maps

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

@Composable
actual fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    val parent = LocalView.current as ViewGroup
    val compositionContext = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    return remember(parent, compositionContext, currentContent, *keys) {
        renderComposableToBitmapDescriptor(parent, compositionContext, currentContent)
    }
}

private val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

private fun renderComposableToBitmapDescriptor(
    parent: ViewGroup,
    compositionContext: CompositionContext,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    val composeView = ComposeView(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        setParentCompositionContext(compositionContext)
        setContent(content)
    }

    parent.addView(composeView)

    try {
        composeView.measure(measureSpec, measureSpec)

        require(composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
            "The ComposeView was measured to have a width or height of zero. " +
                "Make sure that the content has a non-zero size."
        }

        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)
        bitmap.applyCanvas { composeView.draw(this) }

        val googleDescriptor = GoogleBitmapDescriptorFactory.fromBitmap(bitmap)
        return BitmapDescriptor(googleDescriptor)
    } finally {
        parent.removeView(composeView)
    }
}
