package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point
import kotlin.math.min

/**
 * The result of a tree layout computation, providing access to
 * node positions, layout metadata, and transformation utilities.
 *
 * @param T The node type of the external tree.
 */
public class TreeLayoutResult<T>(
    private val positions: Map<T, Point>,
    private val maxDepth: Int,
) {
    public companion object {}

    /** Returns the position assigned to [node]. */
    public fun getPosition(node: T): Point =
        positions[node] ?: throw IllegalArgumentException("Node not part of the layout")

    /** Returns all node-to-position mappings. */
    public fun getPositions(): Map<T, Point> = positions

    /** Returns the maximum depth of the tree. */
    public fun getMaxDepth(): Int = maxDepth

    /** Returns the axis-aligned bounding box of all node positions. */
    public fun getBounds(): Bounds {
        val values = positions.values
        if (values.isEmpty()) return Bounds(0f, 0f, 0f, 0f)
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        for (p in values) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y
            if (p.x > maxX) maxX = p.x
            if (p.y > maxY) maxY = p.y
        }
        return Bounds(minX, minY, maxX, maxY)
    }

    /** Returns a new result with all positions shifted so the minimum corner is at the origin. */
    public fun normalized(): TreeLayoutResult<T> {
        val bounds = getBounds()
        return translated(-bounds.minX, -bounds.minY)
    }

    /** Returns a new result with all positions shifted by [dx] and [dy]. */
    public fun translated(dx: Float, dy: Float): TreeLayoutResult<T> =
        TreeLayoutResult(positions.mapValues { (_, p) -> Point(p.x + dx, p.y + dy) }, maxDepth)

    /** Returns a new result with all positions transformed by [transform]. */
    public fun mapped(transform: (Point) -> Point): TreeLayoutResult<T> =
        TreeLayoutResult(positions.mapValues { (_, p) -> transform(p) }, maxDepth)

    /**
     * Returns a new result uniformly scaled to fit within [width]×[height],
     * preserving aspect ratio. Positions are normalized to origin before scaling.
     */
    public fun scaledTo(width: Float, height: Float): TreeLayoutResult<T> {
        val bounds = getBounds()
        val bw = bounds.width
        val bh = bounds.height
        val scale = when {
            bw == 0f && bh == 0f -> 1f
            bw == 0f -> height / bh
            bh == 0f -> width / bw
            else -> min(width / bw, height / bh)
        }
        return TreeLayoutResult(
            positions.mapValues { (_, p) ->
                Point((p.x - bounds.minX) * scale, (p.y - bounds.minY) * scale)
            },
            maxDepth,
        )
    }

    /**
     * Returns a new result centered within a viewport of [width]×[height].
     * The layout is first scaled to fit (preserving aspect ratio), then offset
     * so that the bounding box is centered in the viewport.
     */
    public fun centered(width: Float, height: Float): TreeLayoutResult<T> {
        val scaled = scaledTo(width, height)
        val bounds = scaled.getBounds()
        val dx = (width - bounds.width) / 2f - bounds.minX
        val dy = (height - bounds.height) / 2f - bounds.minY
        return scaled.translated(dx, dy)
    }
}
