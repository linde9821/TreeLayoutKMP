package io.github.linde9821.treelayout.walker

import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * A simple tree builder that uses String IDs as nodes.
 */
private class StringTreeBuilder {
    val childrenMap = mutableMapOf<String, MutableList<String>>()
    val parentMap = mutableMapOf<String, String?>()
    var root: String = ""

    fun root(id: String): StringTreeBuilder {
        root = id
        parentMap[id] = null
        childrenMap[id] = mutableListOf()
        return this
    }

    fun addChild(parent: String, child: String): StringTreeBuilder {
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

class WalkerTreeLayoutTest {

    @Test
    fun singleNodeTreeHasPositionAtOrigin() {
        val tree = StringTreeBuilder().root("root")
        val layout = WalkerTreeLayout(tree.buildAdapter())
        val result = layout.layout()

        assertEquals(Point(0f, 0f), result.getPosition("root"))
        assertEquals(0, result.getMaxDepth())
    }

    @Test
    fun rootWithTwoChildrenIsCentered() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "left")
            .addChild("root", "right")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        val rootPos = result.getPosition("root")
        val leftPos = result.getPosition("left")
        val rightPos = result.getPosition("right")

        assertEquals(rootPos.x, (leftPos.x + rightPos.x) / 2f)
        assertEquals(1f, leftPos.y)
        assertEquals(1f, rightPos.y)
        assertEquals(0f, rootPos.y)
    }

    @Test
    fun siblingsAreSpacedByHorizontalDistance() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("root", "c")

        val config = WalkerLayoutConfiguration(horizontalDistance = 2f, verticalDistance = 3f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")
        val posC = result.getPosition("c")

        assertEquals(2f, posB.x - posA.x)
        assertEquals(2f, posC.x - posB.x)
        assertEquals(3f, posA.y)
    }

    @Test
    fun subtreesDoNotOverlap() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a").addChild("root", "b")
            .addChild("a", "c").addChild("a", "d")
            .addChild("b", "e").addChild("b", "f")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        val posD = result.getPosition("d")
        val posE = result.getPosition("e")

        assertTrue(posD.x < posE.x, "Subtrees overlap: d.x=${posD.x} >= e.x=${posE.x}")
    }

    @Test
    fun deepTreeMaxDepthIsCorrect() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("a", "b")
            .addChild("b", "c")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        assertEquals(3, result.getMaxDepth())
        assertEquals(3f, result.getPosition("c").y)
    }

    @Test
    fun getPositionsReturnsAllNodes() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()
        val positions = result.getPositions()

        assertEquals(3, positions.size)
        assertTrue(positions.containsKey("root"))
        assertTrue(positions.containsKey("a"))
        assertTrue(positions.containsKey("b"))
    }

    @Test
    fun unknownNodeThrowsException() {
        val tree = StringTreeBuilder().root("root")
        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        assertFailsWith<IllegalArgumentException> {
            result.getPosition("unknown")
        }
    }

    @Test
    fun asymmetricTreeDoesNotOverlap() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a").addChild("root", "b")
            .addChild("a", "c").addChild("a", "d").addChild("a", "e")
            .addChild("d", "f")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")
        // b (right sibling) must be strictly right of a (left sibling)
        assertTrue(posB.x > posA.x, "b should be right of a: b.x=${posB.x}, a.x=${posA.x}")
    }

    @Test
    fun symmetricTreeIsSymmetric() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a").addChild("root", "b")
            .addChild("a", "c").addChild("a", "d")
            .addChild("b", "e").addChild("b", "f")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        val rootPos = result.getPosition("root")
        val posA = result.getPosition("a")
        val posB = result.getPosition("b")

        val distA = rootPos.x - posA.x
        val distB = posB.x - rootPos.x
        assertEquals(distA, distB, "Tree should be symmetric")
    }

    @Test
    fun customConfigurationIsRespected() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val config = WalkerLayoutConfiguration(horizontalDistance = 5f, verticalDistance = 10f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")

        assertEquals(5f, posB.x - posA.x)
        assertEquals(10f, posA.y)
    }

    @Test
    fun largeTreeDoesNotCrash() {
        val tree = StringTreeBuilder().root("root")
        (0 until 100).forEach { tree.addChild("root", "child_$it") }

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        assertEquals(101, result.getPositions().size)
        assertEquals(1, result.getMaxDepth())

        val positions = (0 until 100).map { result.getPosition("child_$it").x }
        for (i in 1 until positions.size) {
            assertTrue(positions[i] > positions[i - 1], "Children should be ordered left to right")
        }
    }

    @Test
    fun nonDeterministicAdapterProducesCorrectLayout() {
        // Adapter that returns a new list instance on every children() call.
        // Before the childrenOf cache fix, this would cause index-out-of-bounds or wrong results.
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("a", "c")
            .addChild("a", "d")

        val adapter = object : TreeAdapter<String> {
            override fun root(): String = tree.root
            override fun children(node: String): List<String> =
                tree.childrenMap[node]?.toList() ?: emptyList() // new list each call
            override fun parent(node: String): String? = tree.parentMap[node]
        }

        val result = WalkerTreeLayout(adapter).layout()

        assertEquals(5, result.getPositions().size)
        val posC = result.getPosition("c")
        val posD = result.getPosition("d")
        assertTrue(posD.x > posC.x, "Siblings should be ordered: c.x=${posC.x}, d.x=${posD.x}")
    }
}
