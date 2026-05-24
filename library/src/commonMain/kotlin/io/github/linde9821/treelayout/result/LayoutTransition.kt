package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point

/**
 * Represents a transition between two layout states, enabling animation
 * by interpolating node positions at a given progress fraction.
 *
 * Nodes present only in [from] are treated as exiting (animate to their last known position).
 * Nodes present only in [to] are treated as entering (animate from their target position).
 *
 * @param T The node type of the external tree.
 * @param from The starting layout state.
 * @param to The ending layout state.
 */
public class LayoutTransition<T>(
    private val from: TreeLayoutResult<T>,
    private val to: TreeLayoutResult<T>,
) {
    /** All nodes involved in either the start or end state. */
    public val allNodes: Set<T> = from.getPositions().keys + to.getPositions().keys

    /** Nodes present in both states. */
    public val persistentNodes: Set<T> = from.getPositions().keys.intersect(to.getPositions().keys)

    /** Nodes only in [from] (removed in the new layout). */
    public val exitingNodes: Set<T> = from.getPositions().keys - to.getPositions().keys

    /** Nodes only in [to] (added in the new layout). */
    public val enteringNodes: Set<T> = to.getPositions().keys - from.getPositions().keys

    /**
     * Returns an interpolated layout at the given [progress] (0.0 = [from], 1.0 = [to]).
     *
     * - Persistent nodes are linearly interpolated between their start and end positions.
     * - Exiting nodes remain at their [from] position.
     * - Entering nodes remain at their [to] position.
     */
    public fun interpolate(progress: Float): TreeLayoutResult<T> {
        val clamped = progress.coerceIn(0f, 1f)
        val positions = HashMap<T, Point>(allNodes.size)

        for (node in persistentNodes) {
            val a = from.getPosition(node)
            val b = to.getPosition(node)
            positions[node] = Point(
                x = a.x + (b.x - a.x) * clamped,
                y = a.y + (b.y - a.y) * clamped,
            )
        }
        for (node in exitingNodes) {
            positions[node] = from.getPosition(node)
        }
        for (node in enteringNodes) {
            positions[node] = to.getPosition(node)
        }

        val maxDepth = maxOf(from.getMaxDepth(), to.getMaxDepth())
        return TreeLayoutResult(positions, maxDepth)
    }
}
