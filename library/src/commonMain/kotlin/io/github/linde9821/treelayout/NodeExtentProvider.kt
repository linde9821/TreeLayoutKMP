package io.github.linde9821.treelayout

/**
 * Provides the dimensions of each node in the tree.
 * Used by the layout algorithm to prevent overlap between nodes of varying sizes.
 *
 * @param T The node type of the external tree.
 */
public interface NodeExtentProvider<T> {
    /** Returns the width of [node]. */
    public fun width(node: T): Float

    /** Returns the height of [node]. */
    public fun height(node: T): Float
}
