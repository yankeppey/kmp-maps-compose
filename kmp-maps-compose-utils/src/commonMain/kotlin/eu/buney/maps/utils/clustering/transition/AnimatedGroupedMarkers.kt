package eu.buney.maps.utils.clustering.transition

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import eu.buney.maps.GoogleMapComposable
import eu.buney.maps.LatLng

/**
 * Renders a [TransitionPlan] on a GoogleMap with animated position transitions.
 *
 * - **Stable** elements render immediately at their target position.
 * - **Entering** elements animate from their source position to their target.
 * - **Exiting** elements animate from their current position to their target,
 *   then are removed from composition.
 *
 * The caller provides [clusterContent] and [itemContent] lambdas that receive
 * the current (possibly animating) position and render the appropriate marker.
 *
 * @param plan the transition plan produced by [rememberTransitionPlan]
 * @param enterAnimationSpec animation spec for entering elements
 * @param exitAnimationSpec animation spec for exiting elements
 * @param clusterContent renders a cluster element at the given position
 * @param itemContent renders an individual item at the given position
 */
@Composable
@GoogleMapComposable
fun <G : Any, I : Any> AnimatedGroupedMarkers(
    plan: TransitionPlan<G, I, LatLng>,
    enterAnimationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    exitAnimationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    clusterContent: @Composable @GoogleMapComposable (
        key: G, items: Set<I>, size: Int, position: LatLng
    ) -> Unit,
    itemContent: @Composable @GoogleMapComposable (
        item: I, position: LatLng
    ) -> Unit,
) {
    // Stable elements: no animation, render at final position
    for (transition in plan.stable) {
        key(transition.element.identityKey()) {
            RenderElement(transition.element, transition.to, clusterContent, itemContent)
        }
    }

    // Entering elements: animate from → to
    for (transition in plan.entering) {
        key(transition.element.identityKey()) {
            AnimatedElement(
                element = transition.element,
                from = transition.from,
                to = transition.to,
                animationSpec = enterAnimationSpec,
                clusterContent = clusterContent,
                itemContent = itemContent,
            )
        }
    }

    // Exiting elements: animate from → to, then remove.
    // Kept in a mutable state list so they stay composed until animation finishes.
    // When a new plan arrives, the list is replaced (old exits are canceled).
    val exitingElements = remember(plan) { plan.exiting.toMutableStateList() }

    for (transition in exitingElements) {
        key(transition.element.identityKey()) {
            AnimatedElement(
                element = transition.element,
                from = transition.from,
                to = transition.to,
                animationSpec = exitAnimationSpec,
                onFinished = { exitingElements.remove(transition) },
                clusterContent = clusterContent,
                itemContent = itemContent,
            )
        }
    }
}

@Composable
@GoogleMapComposable
private fun <G : Any, I : Any> AnimatedElement(
    element: VisualElement<G, I>,
    from: LatLng,
    to: LatLng,
    animationSpec: FiniteAnimationSpec<Float>,
    onFinished: (() -> Unit)? = null,
    clusterContent: @Composable @GoogleMapComposable (G, Set<I>, Int, LatLng) -> Unit,
    itemContent: @Composable @GoogleMapComposable (I, LatLng) -> Unit,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = animationSpec,
        )
        onFinished?.invoke()
    }

    val position = lerpLatLng(from, to, progress.value)
    RenderElement(element, position, clusterContent, itemContent)
}

@Composable
@GoogleMapComposable
private fun <G : Any, I : Any> RenderElement(
    element: VisualElement<G, I>,
    position: LatLng,
    clusterContent: @Composable @GoogleMapComposable (G, Set<I>, Int, LatLng) -> Unit,
    itemContent: @Composable @GoogleMapComposable (I, LatLng) -> Unit,
) {
    when (element) {
        is VisualElement.ClusterElement -> clusterContent(
            element.key, element.items, element.size, position
        )
        is VisualElement.ItemElement -> itemContent(element.item, position)
    }
}

/**
 * Lightweight identity key for Compose's [key] function.
 * Uses the group key for clusters and the item itself for individual elements,
 * avoiding the cost of hashing the full [VisualElement] data class (which
 * includes the items set for [VisualElement.ClusterElement]).
 */
private fun <G : Any, I : Any> VisualElement<G, I>.identityKey(): Any = when (this) {
    is VisualElement.ClusterElement -> this.key
    is VisualElement.ItemElement -> this.item
}

/** Linear interpolation between two [LatLng] values. */
private fun lerpLatLng(from: LatLng, to: LatLng, fraction: Float): LatLng {
    if (fraction == 0f) return from
    if (fraction == 1f) return to

    val lat = from.latitude + (to.latitude - from.latitude) * fraction
    var lngDelta = to.longitude - from.longitude

    // Handle 180° meridian crossing
    if (lngDelta > 180) lngDelta -= 360
    else if (lngDelta < -180) lngDelta += 360

    val lng = from.longitude + lngDelta * fraction

    return LatLng(lat, lng)
}
