package io.github.linde9821.treelayout

/**
 * Platform-agnostic adapter that allows the layout engine to traverse
 * any external tree structure without coupling to a specific domain model.
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
