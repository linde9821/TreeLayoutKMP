package io.github.linde9821.treelayout

import kotlin.math.max

/**
 * Configuration for the Walker layout algorithm.
 *
 * @property horizontalDistance Minimum horizontal spacing between sibling nodes.
 * @property verticalDistance Vertical spacing between depth levels.
 * @property orientation Direction in which the tree grows from root to leaves.
 */
public data class WalkerLayoutConfiguration(
    public val horizontalDistance: Float = 1.0f,
    public val verticalDistance: Float = 1.0f,
    public val orientation: Orientation = Orientation.TopToBottom,
)

/**
 * Computes a tidy tree layout using the Walker algorithm (Buchheim variant).
 * Runs in O(n) time complexity.
 *
 * This is a pure, platform-agnostic implementation with no JVM dependencies.
 *
 * @param T The node type of the external tree.
 * @param adapter Adapter providing tree traversal over the external structure.
 * @param configuration Layout spacing parameters.
 * @param nodeExtentProvider Provides node dimensions. Defaults to dimensionless points (0×0).
 */
public class WalkerTreeLayout<T>(
    private val adapter: TreeAdapter<T>,
    private val configuration: WalkerLayoutConfiguration = WalkerLayoutConfiguration(),
    private val nodeExtentProvider: NodeExtentProvider<T> = UniformNodeExtentProvider(),
) {
    /**
     * Executes the layout algorithm and returns the result.
     */
    public fun layout(): TreeLayoutResult<T> {
        val ctx = LayoutContext(adapter, configuration, nodeExtentProvider)
        ctx.execute()
        return ctx.buildResult()
    }
}

/**
 * Default extent provider that returns zero dimensions for all nodes,
 * treating them as dimensionless points for backward compatibility.
 */
private class UniformNodeExtentProvider<T> : NodeExtentProvider<T> {
    override fun width(node: T): Float = 0.0f
    override fun height(node: T): Float = 0.0f
}

/**
 * Internal mutable context for a single layout computation.
 */
private class LayoutContext<T>(
    private val adapter: TreeAdapter<T>,
    private val config: WalkerLayoutConfiguration,
    private val extents: NodeExtentProvider<T>,
) {
    private val prelim = HashMap<T, Float>()
    private val modifiers = HashMap<T, Float>()
    private val threads = HashMap<T, T?>()
    private val ancestor = HashMap<T, T>()
    private val shifts = HashMap<T, Float>()
    private val changes = HashMap<T, Float>()
    private val positions = HashMap<T, Point>()
    private var maxDepth = 0

    // Precomputed structural info
    private val depthOf = HashMap<T, Int>()
    private val indexAmongSiblings = HashMap<T, Int>()

    // Precomputed max height per depth level for vertical positioning
    private val maxHeightAtDepth = HashMap<Int, Float>()

    fun execute() {
        val root = adapter.root()
        initNodes(root, 0)
        firstWalk(root)
        secondWalk(root, -prelim[root]!!)
    }

    fun buildResult(): TreeLayoutResult<T> {
        return LayoutResultImpl(positions.toMap(), maxDepth)
    }

    private fun initNodes(node: T, depth: Int) {
        depthOf[node] = depth
        modifiers[node] = 0f
        threads[node] = null
        ancestor[node] = node
        shifts[node] = 0f
        changes[node] = 0f

        // Track max height at each depth level
        val h = extents.height(node)
        maxHeightAtDepth[depth] = max(maxHeightAtDepth[depth] ?: 0f, h)

        val children = adapter.children(node)
        children.forEachIndexed { index, child ->
            indexAmongSiblings[child] = index
            initNodes(child, depth + 1)
        }
    }

    /** Computes the required distance between centers of two horizontally adjacent nodes. */
    private fun horizontalSeparation(left: T, right: T): Float {
        return extents.width(left) / 2f + config.horizontalDistance + extents.width(right) / 2f
    }

    private fun firstWalk(v: T) {
        val children = adapter.children(v)
        if (children.isEmpty()) {
            prelim[v] = 0f
            val w = leftSibling(v)
            if (w != null) {
                prelim[v] = prelim[w]!! + horizontalSeparation(w, v)
            }
        } else {
            var defaultAncestor = children.first()

            for (w in children) {
                firstWalk(w)
                defaultAncestor = apportion(w, defaultAncestor)
            }

            executeShifts(v)

            val midpoint = 0.5f * (prelim[children.first()]!! + prelim[children.last()]!!)
            val w = leftSibling(v)
            if (w != null) {
                prelim[v] = prelim[w]!! + horizontalSeparation(w, v)
                modifiers[v] = prelim[v]!! - midpoint
            } else {
                prelim[v] = midpoint
            }
        }
    }

    private fun apportion(v: T, defaultAncestor: T): T {
        val w = leftSibling(v) ?: return defaultAncestor

        var viPlus: T = v
        var voPlus: T = v
        var viMinus: T = w
        var voMinus: T = leftmostSibling(viPlus)!!

        var siPlus = modifiers[viPlus]!!
        var soPlus = modifiers[voPlus]!!
        var siMinus = modifiers[viMinus]!!
        var soMinus = modifiers[voMinus]!!

        while (nextRight(viMinus) != null && nextLeft(viPlus) != null) {
            viMinus = nextRight(viMinus)!!
            viPlus = nextLeft(viPlus)!!
            voMinus = nextLeft(voMinus)!!
            voPlus = nextRight(voPlus)!!
            ancestor[voPlus] = v

            val shift = (prelim[viMinus]!! + siMinus) - (prelim[viPlus]!! + siPlus) +
                    horizontalSeparation(viMinus, viPlus)
            if (shift > 0) {
                moveSubtree(ancestorOf(viMinus, v, defaultAncestor), v, shift)
                siPlus += shift
                soPlus += shift
            }
            siMinus += modifiers[viMinus]!!
            siPlus += modifiers[viPlus]!!
            soMinus += modifiers[voMinus]!!
            soPlus += modifiers[voPlus]!!
        }

        if (nextRight(viMinus) != null && nextRight(voPlus) == null) {
            threads[voPlus] = nextRight(viMinus)
            modifiers[voPlus] = modifiers[voPlus]!! + siMinus - soPlus
        }

        if (nextLeft(viPlus) != null && nextLeft(voMinus) == null) {
            threads[voMinus] = nextLeft(viPlus)
            modifiers[voMinus] = modifiers[voMinus]!! + siPlus - soMinus
            return v
        }

        return defaultAncestor
    }

    private fun executeShifts(v: T) {
        var shift = 0f
        var change = 0f
        for (w in adapter.children(v).reversed()) {
            prelim[w] = prelim[w]!! + shift
            modifiers[w] = modifiers[w]!! + shift
            change += changes[w]!!
            shift += shifts[w]!! + change
        }
    }

    private fun moveSubtree(wMinus: T, wPlus: T, shift: Float) {
        val subtrees = numberOf(wPlus) - numberOf(wMinus)
        changes[wPlus] = changes[wPlus]!! - shift / subtrees.toFloat()
        shifts[wPlus] = shifts[wPlus]!! + shift
        changes[wMinus] = changes[wMinus]!! + shift / subtrees.toFloat()
        prelim[wPlus] = prelim[wPlus]!! + shift
        modifiers[wPlus] = modifiers[wPlus]!! + shift
    }

    private fun secondWalk(v: T, m: Float) {
        val depth = depthOf[v]!!
        val across = prelim[v]!! + m
        // Compute depth-axis position by accumulating max heights of preceding levels + gaps
        var along = 0f
        for (d in 0 until depth) {
            along += maxHeightAtDepth[d]!! + config.verticalDistance
        }
        positions[v] = when (config.orientation) {
            Orientation.TopToBottom -> Point(across, along)
            Orientation.BottomToTop -> Point(across, -along)
            Orientation.LeftToRight -> Point(along, across)
            Orientation.RightToLeft -> Point(-along, across)
        }
        maxDepth = max(maxDepth, depth)

        for (w in adapter.children(v)) {
            secondWalk(w, m + modifiers[v]!!)
        }
    }

    private fun ancestorOf(viMinus: T, v: T, defaultAncestor: T): T {
        val a = ancestor[viMinus]!!
        return if (adapter.parent(a) == adapter.parent(v)) a else defaultAncestor
    }

    private fun nextLeft(v: T): T? {
        val children = adapter.children(v)
        return if (children.isNotEmpty()) children.first() else threads[v]
    }

    private fun nextRight(v: T): T? {
        val children = adapter.children(v)
        return if (children.isNotEmpty()) children.last() else threads[v]
    }

    private fun leftSibling(v: T): T? {
        val parent = adapter.parent(v) ?: return null
        val siblings = adapter.children(parent)
        val idx = indexAmongSiblings[v] ?: return null
        return if (idx > 0) siblings[idx - 1] else null
    }

    private fun leftmostSibling(v: T): T? {
        val parent = adapter.parent(v) ?: return null
        return adapter.children(parent).first()
    }

    private fun numberOf(v: T): Int = (indexAmongSiblings[v] ?: 0) + 1
}

private class LayoutResultImpl<T>(
    private val positions: Map<T, Point>,
    private val maxDepth: Int,
) : TreeLayoutResult<T> {
    override fun getPosition(node: T): Point =
        positions[node] ?: throw IllegalArgumentException("Node not part of the layout")

    override fun getPositions(): Map<T, Point> = positions

    override fun getMaxDepth(): Int = maxDepth
}
