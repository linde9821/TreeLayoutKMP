package io.github.linde9821.treelayout

import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.test.Test
import kotlin.test.assertEquals

class OrientationTest {

    private fun buildTree(): TreeAdapter<String> {
        val childrenMap = mapOf("root" to listOf("a", "b"), "a" to emptyList(), "b" to emptyList())
        val parentMap = mapOf("root" to null, "a" to "root", "b" to "root")
        return object : TreeAdapter<String> {
            override fun root(): String = "root"
            override fun children(node: String): List<String> = childrenMap[node] ?: emptyList()
            override fun parent(node: String): String? = parentMap[node]
        }
    }

    @Test
    fun topToBottomIsDefault() {
        val result = WalkerTreeLayout(buildTree()).layout()
        val root = result.getPosition("root")
        val a = result.getPosition("a")
        assertEquals(0f, root.y)
        assertEquals(1f, a.y) // depth 1 * verticalDistance
    }

    @Test
    fun bottomToTopNegatesY() {
        val config = WalkerLayoutConfiguration(orientation = Orientation.BottomToTop)
        val result = WalkerTreeLayout(buildTree(), config).layout()
        val a = result.getPosition("a")
        assertEquals(-1f, a.y)
    }

    @Test
    fun leftToRightSwapsAxes() {
        val config = WalkerLayoutConfiguration(orientation = Orientation.LeftToRight)
        val result = WalkerTreeLayout(buildTree(), config).layout()
        val root = result.getPosition("root")
        val a = result.getPosition("a")
        val b = result.getPosition("b")
        // Root at x=0, children at x=verticalDistance
        assertEquals(0f, root.x)
        assertEquals(1f, a.x)
        assertEquals(1f, b.x)
        // Siblings spread along y axis
        assertEquals(root.y, (a.y + b.y) / 2f)
    }

    @Test
    fun rightToLeftNegatesDepthAxis() {
        val config = WalkerLayoutConfiguration(orientation = Orientation.RightToLeft)
        val result = WalkerTreeLayout(buildTree(), config).layout()
        val a = result.getPosition("a")
        assertEquals(-1f, a.x)
    }

    @Test
    fun orientationPreservesSiblingOrder() {
        val config = WalkerLayoutConfiguration(orientation = Orientation.LeftToRight)
        val result = WalkerTreeLayout(buildTree(), config).layout()
        val a = result.getPosition("a")
        val b = result.getPosition("b")
        // "a" is first child -> should have smaller y than "b"
        assertEquals(true, a.y < b.y)
    }
}
