package eu.buney.maps.utils.clustering.transition

/**
 * Strategy for computing animated transitions between two [GroupedSnapshot] states.
 *
 * Implementations are domain-specific: they know how to extract positions from
 * items and groups, and decide which groups render as clusters vs. individual items.
 *
 * Swappable — different algorithms (membership-based, proximity-based, etc.)
 * implement this interface.
 *
 * @param G Group key type
 * @param I Item type
 * @param P Position type (e.g., LatLng, Offset)
 */
fun interface TransitionResolver<G : Any, I : Any, P> {
    fun resolve(
        from: GroupedSnapshot<G, I>,
        to: GroupedSnapshot<G, I>,
    ): TransitionPlan<G, I, P>
}
