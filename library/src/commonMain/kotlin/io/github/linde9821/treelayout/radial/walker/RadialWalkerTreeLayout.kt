package io.github.linde9821.treelayout.radial.walker

import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.UniformNodeExtentProvider
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
        val (linearResult, depthOf) = WalkerTreeLayout(adapter, walkerConfig, nodeExtentProvider).layoutWithDepths()
        val linearPositions = linearResult.getPositions()

        var xMin = Float.POSITIVE_INFINITY
        var xMax = Float.NEGATIVE_INFINITY
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
}
