package io.github.linde9821.treelayout.radial.angular

import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.result.TreeLayoutResult
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * A radial tree layout using direct angular partitioning.
 *
 * Recursively assigns angular ranges to each subtree and places nodes
 * at the midpoint of their allocated arc on concentric circles based on depth.
 * Each child receives a share of its parent's angular sector proportional to
 * its subtree weight (number of descendants including itself).
 *
 * @param T The node type of the external tree.
 * @param adapter Adapter providing tree traversal over the external structure.
 * @param configuration Layout spacing parameters.
 */
public class DirectAngularPlacementLayout<T>(
    private val adapter: TreeAdapter<T>,
    private val configuration: DirectAngularPlacementConfiguration = DirectAngularPlacementConfiguration(),
) {
    /**
     * Executes the layout algorithm and returns the result.
     */
    public fun layout(): TreeLayoutResult<T> {
        val weight = HashMap<T, Int>()
        computeWeights(adapter.root(), weight)

        val positions = HashMap<T, Point>(weight.size)
        var maxDepth = 0

        fun assignAngles(node: T, depth: Int, start: Float, end: Float) {
            maxDepth = max(maxDepth, depth)

            if (depth == 0) {
                positions[node] = Point(0f, 0f)
            } else {
                val radius = configuration.layerDistance * depth
                val theta = (start + end) / 2f + configuration.rotation
                positions[node] = Point(radius * cos(theta), radius * sin(theta))
            }

            val children = adapter.children(node)
            if (children.isEmpty()) return

            val totalChildWeight = children.sumOf { weight[it]!! }
            val arc = end - start
            var offset = start

            for (child in children) {
                val childArc = arc * (weight[child]!!.toFloat() / totalChildWeight.toFloat())
                assignAngles(child, depth + 1, offset, offset + childArc)
                offset += childArc
            }
        }

        assignAngles(adapter.root(), 0, 0f, 2f * PI.toFloat())
        return TreeLayoutResult(positions, maxDepth)
    }

    private fun computeWeights(node: T, weight: HashMap<T, Int>): Int {
        val children = adapter.children(node)
        var w = 1
        for (child in children) {
            w += computeWeights(child, weight)
        }
        weight[node] = w
        return w
    }
}
