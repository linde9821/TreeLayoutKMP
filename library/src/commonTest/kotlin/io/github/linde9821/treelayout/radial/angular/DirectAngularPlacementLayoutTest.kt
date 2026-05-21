package io.github.linde9821.treelayout.radial.angular

import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class AngularStringTreeBuilder {
    val childrenMap = mutableMapOf<String, MutableList<String>>()
    val parentMap = mutableMapOf<String, String?>()
    var root: String = ""

    fun root(id: String): AngularStringTreeBuilder {
        root = id
        parentMap[id] = null
        childrenMap[id] = mutableListOf()
        return this
    }

    fun addChild(parent: String, child: String): AngularStringTreeBuilder {
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

class DirectAngularPlacementLayoutTest {

    @Test
    fun rootIsAtOrigin() {
        val tree = AngularStringTreeBuilder().root("root")
        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()

        val pos = result.getPosition("root")
        assertEquals(0.0, pos.x.toDouble(), 0.0001)
        assertEquals(0.0, pos.y.toDouble(), 0.0001)
    }

    @Test
    fun childrenAreAtLayerDistance() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val config = DirectAngularPlacementConfiguration(layerDistance = 4f)
        val result = DirectAngularPlacementLayout(tree.buildAdapter(), config).layout()

        val radiusA = radius(result.getPosition("a"))
        val radiusB = radius(result.getPosition("b"))

        assertEquals(4.0, radiusA.toDouble(), 0.0001)
        assertEquals(4.0, radiusB.toDouble(), 0.0001)
    }

    @Test
    fun maxDepthIsCorrect() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")
            .addChild("b", "c")

        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()
        assertEquals(3, result.getMaxDepth())
    }

    @Test
    fun getPositionsReturnsAllNodes() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()
        assertEquals(4, result.getPositions().size)
    }

    @Test
    fun unknownNodeThrowsException() {
        val tree = AngularStringTreeBuilder().root("root")
        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()

        assertFailsWith<IllegalArgumentException> {
            result.getPosition("unknown")
        }
    }

    @Test
    fun siblingsAtSameDepthHaveSameRadius() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()

        val radii = listOf("a", "b", "c").map { radius(result.getPosition(it)) }
        assertEquals(radii[0].toDouble(), radii[1].toDouble(), 0.0001)
        assertEquals(radii[1].toDouble(), radii[2].toDouble(), 0.0001)
    }

    @Test
    fun deeperNodesHaveLargerRadius() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")

        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()

        assertTrue(radius(result.getPosition("b")) > radius(result.getPosition("a")))
    }

    @Test
    fun singleChildChainPlacesNodesOnSameRay() {
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")

        val config = DirectAngularPlacementConfiguration(layerDistance = 2f)
        val result = DirectAngularPlacementLayout(tree.buildAdapter(), config).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")

        assertEquals(posA.x.toDouble() * 2, posB.x.toDouble(), 0.0001)
        assertEquals(posA.y.toDouble() * 2, posB.y.toDouble(), 0.0001)
    }

    @Test
    fun heavierSubtreeGetsMoreAngularSpace() {
        // "a" has 2 children (weight=3), "b" is a leaf (weight=1)
        val tree = AngularStringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("a", "c")
            .addChild("a", "d")

        val result = DirectAngularPlacementLayout(tree.buildAdapter()).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")
        val posC = result.getPosition("c")
        val posD = result.getPosition("d")

        val arcCD = angularDistance(posC, posD)
        val angleA = angle(posA)
        val angleB = angle(posB)
        val angleDiff = angularDiff(angleA, angleB)

        assertTrue(angleDiff > arcCD, "Heavier subtree should occupy more angular space")
    }

    private fun radius(p: Point): Float = sqrt(p.x * p.x + p.y * p.y)

    private fun angle(p: Point): Float = kotlin.math.atan2(p.y, p.x)

    private fun angularDistance(a: Point, b: Point): Float = angularDiff(angle(a), angle(b))

    private fun angularDiff(a: Float, b: Float): Float {
        val diff = kotlin.math.abs(a - b)
        return if (diff > kotlin.math.PI.toFloat()) 2f * kotlin.math.PI.toFloat() - diff else diff
    }
}
