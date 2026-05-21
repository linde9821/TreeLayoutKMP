package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point

/**
 * Simple map-backed implementation used by transformation methods.
 */
internal class MapTreeLayoutResult<T>(
    private val positions: Map<T, Point>,
    private val maxDepth: Int,
) : TreeLayoutResult<T>() {
    override fun getPosition(node: T): Point =
        positions[node] ?: throw IllegalArgumentException("Node not part of the layout")

    override fun getPositions(): Map<T, Point> = positions

    override fun getMaxDepth(): Int = maxDepth
}