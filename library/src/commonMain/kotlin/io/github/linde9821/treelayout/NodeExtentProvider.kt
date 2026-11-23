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

/**
 * Default extent provider that returns zero dimensions for all nodes,
 * treating them as dimensionless points.
 */
internal class UniformNodeExtentProvider<T> : NodeExtentProvider<T> {
    override fun width(node: T): Float = 0.0f
    override fun height(node: T): Float = 0.0f
}
