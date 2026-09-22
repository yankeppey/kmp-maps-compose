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

/**
 * A group of items sharing one key.
 *
 * Note: intentionally a class and not a data class, so a later property stays binary
 * compatible. See https://jakewharton.com/public-api-challenges-in-kotlin/
 */
class Group<G : Any, I : Any>(
    val key: G,
    val items: Set<I>
) {
    override fun equals(other: Any?): Boolean = other is Group<*, *> &&
        key == other.key &&
        items == other.items

    override fun hashCode(): Int = 31 * key.hashCode() + items.hashCode()

    override fun toString(): String = "Group(key=$key, items=$items)"
}
