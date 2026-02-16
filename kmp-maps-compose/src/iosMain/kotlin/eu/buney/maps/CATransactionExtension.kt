package eu.buney.maps

import platform.QuartzCore.CATransaction

/**
 * Wraps [block] in a [CATransaction] begin/commit pair.
 *
 * Inside [block], `this` is [CATransaction]'s companion, so transaction
 * properties like [setAnimationDuration] can be called directly.
 */
internal inline fun CATransaction.Companion.withTransaction(
    block: CATransaction.Companion.() -> Unit
) {
    begin()
    try {
        block()
    } finally {
        commit()
    }
}
