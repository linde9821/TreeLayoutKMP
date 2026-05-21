package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point
import kotlin.math.min

/**
 * The result of a tree layout computation, providing access to
 * node positions, layout metadata, and transformation utilities.
 *
 * @param T The node type of the external tree.
 */
public abstract class TreeLayoutResult<T> {

    /** Returns the position assigned to [node]. */
    public abstract fun getPosition(node: T): Point

    /** Returns all node-to-position mappings. */
    public abstract fun getPositions(): Map<T, Point>

    /** Returns the maximum depth of the tree. */
    public abstract fun getMaxDepth(): Int

    /** Returns the axis-aligned bounding box of all node positions. */
    public fun getBounds(): Bounds {
        val positions = getPositions().values
        if (positions.isEmpty()) return Bounds(0f, 0f, 0f, 0f)
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (p in positions) {
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
    public fun translated(dx: Float, dy: Float): TreeLayoutResult<T> {
        val mapped = getPositions().mapValues { (_, p) -> Point(p.x + dx, p.y + dy) }
        return MapTreeLayoutResult(mapped, getMaxDepth())
    }

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
        val mapped = getPositions().mapValues { (_, p) ->
            Point((p.x - bounds.minX) * scale, (p.y - bounds.minY) * scale)
        }
        return MapTreeLayoutResult(mapped, getMaxDepth())
    }
}