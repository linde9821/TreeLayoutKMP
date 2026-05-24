package io.github.linde9821.treelayout.radial.walker

import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.result.TreeLayoutResult
import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * A radial variant of the Walker tree layout algorithm.
 *
 * Nodes are arranged in concentric circles around the root. Each depth level
 * forms a ring, and sibling nodes are spaced proportionally along the arc.
 * Internally delegates to [WalkerTreeLayout] for the initial linear positioning,
 * then transforms the across-axis into angular coordinates.
 *
 * @param T The node type of the external tree.
 * @param adapter Adapter providing tree traversal over the external structure.
 * @param configuration Radial layout spacing parameters.
 * @param nodeExtentProvider Provides node dimensions. Defaults to dimensionless points (0×0).
 */
public class RadialWalkerTreeLayout<T>(
    private val adapter: TreeAdapter<T>,
    private val configuration: RadialWalkerLayoutConfiguration = RadialWalkerLayoutConfiguration(),
    private val nodeExtentProvider: NodeExtentProvider<T> = UniformNodeExtentProvider(),
) {
    /**
     * Executes the layout algorithm and returns the result with polar-to-Cartesian positions.
     */
    public fun layout(): TreeLayoutResult<T> {
        val walkerConfig = WalkerLayoutConfiguration(
            horizontalDistance = 1.0f,
            verticalDistance = configuration.layerDistance,
        )
        val linearResult = WalkerTreeLayout(adapter, walkerConfig, nodeExtentProvider).layout()
        val linearPositions = linearResult.getPositions()

        val depthOf = HashMap<T, Int>(linearPositions.size)
        computeDepths(adapter.root(), 0, depthOf)

        var xMin = Float.MAX_VALUE
        var xMax = Float.MIN_VALUE
        for ((_, pos) in linearPositions) {
            xMin = min(xMin, pos.x)
            xMax = max(xMax, pos.x)
        }

        val radialPositions = HashMap<T, Point>(linearPositions.size)
        val xRange = xMax - xMin

        for ((node, pos) in linearPositions) {
            val depth = depthOf[node]!!
            val radius = depth * configuration.layerDistance

            val theta = if (xRange == 0f) {
                configuration.rotation
            } else {
                val normalizedX = (pos.x - xMin) / xRange
                normalizedX * configuration.usableAngle + configuration.rotation
            }

            radialPositions[node] = Point(
                x = radius * cos(theta),
                y = radius * sin(theta),
            )
        }

        return TreeLayoutResult(radialPositions, linearResult.getMaxDepth())
    }

    private fun computeDepths(node: T, depth: Int, depthOf: HashMap<T, Int>) {
        depthOf[node] = depth
        for (child in adapter.children(node)) {
            computeDepths(child, depth + 1, depthOf)
        }
    }
}

private class UniformNodeExtentProvider<T> : NodeExtentProvider<T> {
    override fun width(node: T): Float = 0.0f
    override fun height(node: T): Float = 0.0f
}
