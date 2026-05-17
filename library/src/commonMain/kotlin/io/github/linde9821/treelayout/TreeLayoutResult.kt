package io.github.linde9821.treelayout

/**
 * A 2D coordinate representing a node's position in the layout.
 */
public data class Point(public val x: Float, public val y: Float)

/**
 * The result of a tree layout computation, providing access to
 * node positions and layout metadata.
 *
 * @param T The node type of the external tree.
 */
public interface TreeLayoutResult<T> {
    /** Returns the position assigned to [node]. */
    public fun getPosition(node: T): Point

    /** Returns all node-to-position mappings. */
    public fun getPositions(): Map<T, Point>

    /** Returns the maximum depth of the tree. */
    public fun getMaxDepth(): Int
}
