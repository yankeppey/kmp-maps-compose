package eu.buney.maps.utils.clustering.transition

/**
 * An immutable snapshot of items organized into groups.
 * Content-agnostic: knows nothing about positions, maps, or rendering.
 *
 * @param G Group key type (must have stable identity for diffing)
 * @param I Item type (must have stable identity for membership tracking)
 */
class GroupedSnapshot<G : Any, I : Any>(
    val groups: List<Group<G, I>>
) {
    private val itemToGroup: Map<I, Group<G, I>> by lazy {
        buildMap {
            for (group in groups) {
                for (item in group.items) put(item, group)
            }
        }
    }

    /** O(1) lookup: which group does this item belong to? */
    fun groupOf(item: I): Group<G, I>? = itemToGroup[item]

    val allItems: Set<I> by lazy {
        groups.flatMapTo(linkedSetOf()) { it.items }
    }

    companion object {
        fun <G : Any, I : Any> empty(): GroupedSnapshot<G, I> =
            GroupedSnapshot(emptyList())
    }
}

data class Group<G : Any, I : Any>(
    val key: G,
    val items: Set<I>
)
