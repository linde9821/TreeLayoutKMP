package io.github.linde9821.treelayout

import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeExtentProviderTest {

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

    private class FixedExtentProvider(
        private val widths: Map<String, Float> = emptyMap(),
        private val heights: Map<String, Float> = emptyMap(),
        private val defaultWidth: Float = 1f,
        private val defaultHeight: Float = 1f,
    ) : NodeExtentProvider<String> {
        override fun width(node: String): Float = widths[node] ?: defaultWidth
        override fun height(node: String): Float = heights[node] ?: defaultHeight
    }

    @Test
    fun defaultExtentProviderPreservesBackwardCompatibility() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val result = WalkerTreeLayout(tree.buildAdapter()).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")
        // Without extent provider, spacing equals horizontalDistance
        assertEquals(1f, posB.x - posA.x)
        assertEquals(1f, posA.y)
    }

    @Test
    fun uniformExtentsIncreaseSpacing() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")

        val extents = FixedExtentProvider(defaultWidth = 2f, defaultHeight = 3f)
        val config = WalkerLayoutConfiguration(horizontalDistance = 1f, verticalDistance = 1f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config, extents).layout()

        val posA = result.getPosition("a")
        val posB = result.getPosition("b")
        // separation = width(a)/2 + hDist + width(b)/2 = 1 + 1 + 1 = 3
        assertEquals(3f, posB.x - posA.x)
        // y at depth 1 = maxHeight(depth0) + vDist = 3 + 1 = 4
        assertEquals(4f, posA.y)
    }

    @Test
    fun variableWidthsPreventOverlap() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "wide")
            .addChild("root", "narrow")

        val extents = FixedExtentProvider(
            widths = mapOf("root" to 1f, "wide" to 4f, "narrow" to 1f),
            defaultHeight = 1f,
        )
        val config = WalkerLayoutConfiguration(horizontalDistance = 0.5f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config, extents).layout()

        val posWide = result.getPosition("wide")
        val posNarrow = result.getPosition("narrow")
        // separation = wide.width/2 + hDist + narrow.width/2 = 2 + 0.5 + 0.5 = 3
        assertEquals(3f, posNarrow.x - posWide.x)
        // Bounding boxes must not overlap:
        // wide right edge = posWide.x + 2, narrow left edge = posNarrow.x - 0.5
        assertTrue(posWide.x + 2f < posNarrow.x - 0.5f)
    }

    @Test
    fun variableHeightsAffectVerticalPositioning() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "child")
            .addChild("child", "grandchild")

        val extents = FixedExtentProvider(
            heights = mapOf("root" to 5f, "child" to 2f, "grandchild" to 1f),
            defaultWidth = 0f,
        )
        val config = WalkerLayoutConfiguration(verticalDistance = 1f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config, extents).layout()

        // depth 0: root (height 5) -> y = 0
        assertEquals(0f, result.getPosition("root").y)
        // depth 1: y = maxHeight(0) + vDist = 5 + 1 = 6
        assertEquals(6f, result.getPosition("child").y)
        // depth 2: y = (5 + 1) + (2 + 1) = 9
        assertEquals(9f, result.getPosition("grandchild").y)
    }

    @Test
    fun wideNodesInSubtreesPushSiblingsApart() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "a")
            .addChild("root", "b")
            .addChild("a", "a1")
            .addChild("a", "a2")
            .addChild("b", "b1")

        val extents = FixedExtentProvider(
            widths = mapOf("a2" to 6f, "b1" to 6f),
            defaultWidth = 1f,
            defaultHeight = 0f,
        )
        val config = WalkerLayoutConfiguration(horizontalDistance = 1f, verticalDistance = 1f)
        val result = WalkerTreeLayout(tree.buildAdapter(), config, extents).layout()

        val posA2 = result.getPosition("a2")
        val posB1 = result.getPosition("b1")
        // Right edge of a2 must not overlap left edge of b1
        val a2Right = posA2.x + 3f  // half of 6
        val b1Left = posB1.x - 3f   // half of 6
        assertTrue(a2Right <= b1Left, "Wide nodes overlap: a2 right=$a2Right, b1 left=$b1Left")
    }

    @Test
    fun rootIsCenteredOverChildrenWithVariableWidths() {
        val tree = StringTreeBuilder().root("root")
            .addChild("root", "left")
            .addChild("root", "right")

        val extents = FixedExtentProvider(
            widths = mapOf("root" to 2f, "left" to 4f, "right" to 1f),
            defaultHeight = 0f,
        )
        val result = WalkerTreeLayout(tree.buildAdapter(), WalkerLayoutConfiguration(), extents).layout()

        val rootPos = result.getPosition("root")
        val leftPos = result.getPosition("left")
        val rightPos = result.getPosition("right")
        // Root should be centered over its children
        assertEquals(rootPos.x, (leftPos.x + rightPos.x) / 2f)
    }
}
