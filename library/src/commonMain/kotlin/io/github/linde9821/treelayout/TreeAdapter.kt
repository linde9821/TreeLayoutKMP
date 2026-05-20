package io.github.linde9821.treelayout

/**
 * Platform-agnostic adapter that allows the layout engine to traverse
 * any external tree structure without coupling to a specific domain model.
 *
 * ## Identity contract
 *
 * The layout algorithm relies on consistent node identity. Implementations **must** satisfy:
 *
 * 1. **Deterministic children**: [children] must return the same elements in the same order
 *    every time it is called for a given node. Returning freshly allocated wrapper objects
 *    that are not equal to previously returned nodes violates this contract.
 * 2. **Consistent parent–child relationship**: For every child `c` returned by `children(node)`,
 *    `parent(c)` must return `node` (the same instance or an equal object).
 * 3. **Unique membership**: Each node must appear exactly once in the tree. Sharing a node
 *    across multiple parents or including it in multiple sibling lists is not supported.
 *
 * Violating these constraints leads to undefined layout results or runtime exceptions.
 *
 * @param T The node type of the external tree.
 */
public interface TreeAdapter<T> {
    /** Returns the root node of the tree. */
    public fun root(): T

    /** Returns the children of [node] in left-to-right order. */
    public fun children(node: T): List<T>

    /** Returns the parent of [node], or null if [node] is the root. */
    public fun parent(node: T): T?

    /** Returns true if [node] has no children. */
    public fun isLeaf(node: T): Boolean = children(node).isEmpty()
}
