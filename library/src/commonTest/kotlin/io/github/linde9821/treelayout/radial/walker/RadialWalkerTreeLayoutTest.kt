package io.github.linde9821.treelayout.radial.walker

import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class RadialStringTreeBuilder {
    val childrenMap = mutableMapOf<String, MutableList<String>>()
    val parentMap = mutableMapOf<String, String?>()
    var root: String = ""

    fun root(id: String): RadialStringTreeBuilder {
        root = id
        parentMap[id] = null
        childrenMap[id] = mutableListOf()
        return this
    }

    fun addChild(parent: String, child: String): RadialStringTreeBuilder {
        childrenMap.getOrPut(parent) { mutableListOf() }.add(child)
        childrenMap.getOrPut(child) { mutableListOf() }
        parentMap[child] = parent
        return this
    }

    fun buildAdapter(): TreeAdapter<String> = object : TreeAdapter<String> {
        override fun root(): String = root
        override fun children(node: String): List<String> = childrenMap[node] ?: emptyList()
        override fun parent(node: String): String? = parentMap[node]
    }
}

class RadialWalkerTreeLayoutTest {

    @Test
    fun singleNodeIsAtOrigin() {
        val tree = RadialStringTreeBuilder().root("root")
        val result = RadialWalkerTreeLayout(tree.buildAdapter()).layout()

        val pos = result.getPosition("root")
        assertEquals(0.0, pos.x.toDouble(), 0.0001)
        assertEquals(0.0, pos.y.toDouble(), 0.0001)
    }

    @Test
    fun childrenAreAtLayerDistance() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val config = RadialWalkerLayoutConfiguration(layerDistance = 5f)
        val result = RadialWalkerTreeLayout(tree.buildAdapter(), config).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")

        val radiusA = sqrt(posA.x * posA.x + posA.y * posA.y)
        val radiusB = sqrt(posB.x * posB.x + posB.y * posB.y)

        assertEquals(5.0, radiusA.toDouble(), 0.0001)
        assertEquals(5.0, radiusB.toDouble(), 0.0001)
    }

    @Test
    fun maxDepthIsCorrect() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")
            .addChild("b", "c")

        val result = RadialWalkerTreeLayout(tree.buildAdapter()).layout()
        assertEquals(3, result.getMaxDepth())
    }

    @Test
    fun getPositionsReturnsAllNodes() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val result = RadialWalkerTreeLayout(tree.buildAdapter()).layout()
        assertEquals(4, result.getPositions().size)
    }

    @Test
    fun unknownNodeThrowsException() {
        val tree = RadialStringTreeBuilder().root("root")
        val result = RadialWalkerTreeLayout(tree.buildAdapter()).layout()

        assertFailsWith<IllegalArgumentException> {
            result.getPosition("unknown")
        }
    }

    @Test
    fun nodesAtSameDepthHaveSameRadius() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val config = RadialWalkerLayoutConfiguration(layerDistance = 3f)
        val result = RadialWalkerTreeLayout(tree.buildAdapter(), config).layout()

        val positions = listOf("a", "b", "c").map { result.getPosition(it) }
        val radii = positions.map { sqrt(it.x * it.x + it.y * it.y) }

        for (r in radii) {
            assertEquals(3.0, r.toDouble(), 0.0001)
        }
    }

    @Test
    fun marginReducesAngularSpread() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val smallMargin = RadialWalkerTreeLayout(
            tree.buildAdapter(),
            RadialWalkerLayoutConfiguration(layerDistance = 1f, margin = 0.5f),
        ).layout()

        val largeMargin = RadialWalkerTreeLayout(
            tree.buildAdapter(),
            RadialWalkerLayoutConfiguration(layerDistance = 1f, margin = PI.toFloat()),
        ).layout()

        // Adjacent siblings (a-b) should be closer with larger margin
        val distSmall = distance(smallMargin.getPosition("a"), smallMargin.getPosition("b"))
        val distLarge = distance(largeMargin.getPosition("a"), largeMargin.getPosition("b"))

        assertTrue(distLarge < distSmall, "Larger margin should reduce spread between adjacent nodes")
    }

    @Test
    fun deeperNodesHaveLargerRadius() {
        val tree = RadialStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")

        val result = RadialWalkerTreeLayout(tree.buildAdapter()).layout()

        val radiusA = radius(result.getPosition("a"))
        val radiusB = radius(result.getPosition("b"))

        assertTrue(radiusB > radiusA, "Deeper nodes should have larger radius")
    }

    private fun distance(a: Point, b: Point): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun radius(p: Point): Float = sqrt(p.x * p.x + p.y * p.y)
}
