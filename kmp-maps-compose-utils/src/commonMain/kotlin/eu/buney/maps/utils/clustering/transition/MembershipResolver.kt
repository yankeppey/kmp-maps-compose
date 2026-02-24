package eu.buney.maps.utils.clustering.transition

/**
 * Resolves transitions by tracking item membership across states.
 *
 * When an item belongs to group A in the old state and group B in the new state,
 * groups A and B are considered "corresponding" — enabling split/merge animation.
 * This mirrors iOS GMUDefaultClusterRenderer's `overlappingClusterForCluster:` approach.
 *
 * Fully generic over group key, item, and position types.
 *
 * @param G Group key type
 * @param I Item type
 * @param P Position type (e.g., LatLng, Offset)
 * @param itemPosition Extract position from an item
 * @param groupPosition Extract position from a group (e.g., weighted centroid)
 * @param isCluster Whether a group should render as a single cluster element
 *                  (vs. rendering its items individually)
 */
class MembershipResolver<G : Any, I : Any, P>(
    private val itemPosition: (I) -> P,
    private val groupPosition: (Group<G, I>) -> P,
    private val isCluster: (Group<G, I>) -> Boolean,
) : TransitionResolver<G, I, P> {

    override fun resolve(
        from: GroupedSnapshot<G, I>,
        to: GroupedSnapshot<G, I>,
    ): TransitionPlan<G, I, P> {
        val entering = mutableListOf<ElementTransition<G, I, P>>()
        val exiting = mutableListOf<ElementTransition<G, I, P>>()
        val stable = mutableListOf<ElementTransition<G, I, P>>()

        val handledOldItems = mutableSetOf<I>()

        // ── NEW state: determine what enters or stays ──
        for (newGroup in to.groups) {
            if (isCluster(newGroup)) {
                processNewCluster(newGroup, from, entering, stable)
            } else {
                for (item in newGroup.items) {
                    processNewItem(item, from, entering, stable, handledOldItems)
                }
            }
        }

        // ── OLD state: determine what exits ──
        // Old clusters that split are NOT added to exiting — the entering items
        // animating outward already carry the visual effect (matches native behavior).
        for (oldGroup in from.groups) {
            if (!isCluster(oldGroup)) {
                for (item in oldGroup.items) {
                    if (item !in handledOldItems) {
                        processOldItem(item, to, exiting)
                    }
                }
            }
        }

        return TransitionPlan(entering, exiting, stable)
    }

    /** A new cluster is appearing — find where it came from. */
    private fun processNewCluster(
        newGroup: Group<G, I>,
        oldState: GroupedSnapshot<G, I>,
        entering: MutableList<ElementTransition<G, I, P>>,
        stable: MutableList<ElementTransition<G, I, P>>,
    ) {
        val element = VisualElement.ClusterElement(newGroup.key, newGroup.items)
        val targetPos = groupPosition(newGroup)

        val sourceGroup = findOverlapping(newGroup, oldState)

        if (sourceGroup != null) {
            val sourcePos = groupPosition(sourceGroup)
            if (sourcePos != targetPos) {
                entering.add(ElementTransition(element, from = sourcePos, to = targetPos))
            } else {
                stable.add(ElementTransition(element, from = targetPos, to = targetPos))
            }
        } else {
            stable.add(ElementTransition(element, from = targetPos, to = targetPos))
        }
    }

    /** A new individual item is appearing — was it inside an old cluster? */
    private fun processNewItem(
        item: I,
        oldState: GroupedSnapshot<G, I>,
        entering: MutableList<ElementTransition<G, I, P>>,
        stable: MutableList<ElementTransition<G, I, P>>,
        handledOldItems: MutableSet<I>,
    ) {
        val element = VisualElement.ItemElement<G, I>(item)
        val targetPos = itemPosition(item)
        val oldGroup = oldState.groupOf(item)

        handledOldItems.add(item)

        if (oldGroup != null && isCluster(oldGroup)) {
            // SPLIT: item emerging from an old cluster
            entering.add(ElementTransition(
                element,
                from = groupPosition(oldGroup),
                to = targetPos,
            ))
        } else {
            stable.add(ElementTransition(element, from = targetPos, to = targetPos))
        }
    }

    /** An old individual item may be merging into a new cluster. */
    private fun processOldItem(
        item: I,
        newState: GroupedSnapshot<G, I>,
        exiting: MutableList<ElementTransition<G, I, P>>,
    ) {
        val newGroup = newState.groupOf(item)

        if (newGroup != null && isCluster(newGroup)) {
            // MERGE: item being absorbed into a new cluster
            exiting.add(ElementTransition(
                VisualElement.ItemElement(item),
                from = itemPosition(item),
                to = groupPosition(newGroup),
            ))
        }
    }

    /** Find a group in [snapshot] that shares at least one item with [group]. */
    private fun findOverlapping(
        group: Group<G, I>,
        snapshot: GroupedSnapshot<G, I>,
    ): Group<G, I>? {
        for (item in group.items) {
            val match = snapshot.groupOf(item)
            if (match != null) return match
        }
        return null
    }
}
