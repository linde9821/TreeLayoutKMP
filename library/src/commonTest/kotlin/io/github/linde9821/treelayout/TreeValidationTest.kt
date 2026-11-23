package io.github.linde9821.treelayout

import io.github.linde9821.treelayout.radial.angular.DirectAngularPlacementLayout
import io.github.linde9821.treelayout.radial.walker.RadialWalkerTreeLayout
import io.github.linde9821.treelayout.result.TreeLayoutResult
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TreeValidationTest {

    /** Adapter backed by an explicit child map, allowing malformed (non-tree) structures. */
    private class MapAdapter(
        private val root: String,
        private val children: Map<String, List<String>>,
    ) : TreeAdapter<String> {
        override fun root(): String = root
        override fun children(node: String): List<String> = children[node] ?: emptyList()
        override fun parent(node: String): String? =
            children.entries.firstOrNull { node in it.value }?.key
    }

    private val layouts: List<Pair<String, (TreeAdapter<String>) -> TreeLayoutResult<String>>> = listOf(
        "Walker" to { a -> WalkerTreeLayout(a).layout() },
        "RadialWalker" to { a -> RadialWalkerTreeLayout(a).layout() },
        "DirectAngular" to { a -> DirectAngularPlacementLayout(a).layout() },
    )

    private fun assertRejected(adapter: TreeAdapter<String>, expectedDepth: Int) {
        for ((name, layout) in layouts) {
            val error = assertFailsWith<IllegalArgumentException>("$name should reject the tree") {
                layout(adapter)
            }
            val message = error.message ?: ""
            assertTrue("Invalid tree" in message, "$name: unexpected message '$message'")
            assertTrue("depth $expectedDepth" in message, "$name: unexpected message '$message'")
        }
    }

    @Test
    fun selfLoopIsRejected() {
        assertRejected(MapAdapter("a", mapOf("a" to listOf("a"))), expectedDepth = 1)
    }

    @Test
    fun cycleBackToRootIsRejected() {
        val adapter = MapAdapter(
            "a",
            mapOf("a" to listOf("b"), "b" to listOf("c"), "c" to listOf("a")),
        )
        assertRejected(adapter, expectedDepth = 3)
    }

    @Test
    fun nodeSharedBetweenParentsIsRejected() {
        val adapter = MapAdapter(
            "root",
            mapOf("root" to listOf("a", "b"), "a" to listOf("shared"), "b" to listOf("shared")),
        )
        assertRejected(adapter, expectedDepth = 2)
    }

    @Test
    fun duplicateSiblingIsRejected() {
        assertRejected(MapAdapter("root", mapOf("root" to listOf("x", "x"))), expectedDepth = 1)
    }

    @Test
    fun validTreeIsAccepted() {
        val adapter = MapAdapter(
            "root",
            mapOf("root" to listOf("a", "b"), "a" to listOf("c", "d")),
        )
        for ((name, layout) in layouts) {
            assertEquals(5, layout(adapter).getPositions().size, "$name lost nodes")
        }
    }
}
