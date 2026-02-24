package eu.buney.maps.utils.clustering.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * What appears on screen: either a group rendered as a single element,
 * or an individual item. The Compose layer pattern-matches on this
 * to choose how to render.
 */
sealed interface VisualElement<out G : Any, out I : Any> {
    /** A group rendered as a single visual element (e.g., cluster marker). */
    data class ClusterElement<G : Any, I : Any>(
        val key: G,
        val items: Set<I>,
        val size: Int = items.size,
    ) : VisualElement<G, I>

    /** An individual item rendered on its own (e.g., single marker). */
    data class ItemElement<G : Any, I : Any>(
        val item: I,
    ) : VisualElement<G, I>
}

/**
 * Animation instruction for one visual element: animate from [from] to [to].
 *
 * @param P Position type (e.g., LatLng, Offset)
 */
data class ElementTransition<G : Any, I : Any, P>(
    val element: VisualElement<G, I>,
    val from: P,
    val to: P,
)

/**
 * The complete animated transition between two [GroupedSnapshot] states.
 * Produced by a [TransitionResolver]; consumed by the Compose animation layer.
 */
data class TransitionPlan<G : Any, I : Any, P>(
    /** New elements appearing. Animate from [ElementTransition.from] to [ElementTransition.to]. */
    val entering: List<ElementTransition<G, I, P>>,
    /** Old elements disappearing. Animate from → to, then remove. */
    val exiting: List<ElementTransition<G, I, P>>,
    /** Elements present in both states at their final position (from == to). */
    val stable: List<ElementTransition<G, I, P>>,
)

/**
 * Remembers the previous [GroupedSnapshot] and computes a [TransitionPlan]
 * whenever the snapshot changes.
 *
 * On the first call (no previous snapshot), all elements are placed as stable
 * (no animation). On subsequent calls, the [resolver] diffs old vs. new
 * and produces entering/exiting/stable element transitions.
 */
@Composable
fun <G : Any, I : Any, P> rememberTransitionPlan(
    snapshot: GroupedSnapshot<G, I>,
    resolver: TransitionResolver<G, I, P>,
): TransitionPlan<G, I, P> {
    var previous by remember { mutableStateOf<GroupedSnapshot<G, I>?>(null) }

    return remember(snapshot) {
        val prev = previous
        previous = snapshot

        if (prev == null) {
            resolver.resolve(GroupedSnapshot.empty(), snapshot)
        } else {
            resolver.resolve(from = prev, to = snapshot)
        }
    }
}
