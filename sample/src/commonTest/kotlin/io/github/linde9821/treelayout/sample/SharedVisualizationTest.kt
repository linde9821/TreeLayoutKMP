package io.github.linde9821.treelayout.sample

import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Orientation
import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class SharedVisualizationTest {

    private val fixedExtents: NodeExtentProvider<PrefixNode> = object : NodeExtentProvider<PrefixNode> {
        override fun width(node: PrefixNode): Float = 40f
        override fun height(node: PrefixNode): Float = 20f
    }

    @Test
    public fun sharedLayoutProducesPositionsForAllNodes() {
        val tree = buildPrefixTree(listOf("cat", "car"))
        val adapter = prefixTreeAdapter(tree)
        val config = WalkerLayoutConfiguration(
            horizontalDistance = 40f,
            verticalDistance = 60f,
            orientation = Orientation.TopToBottom,
        )
        val result = WalkerTreeLayout(adapter, config, fixedExtents).layout()
        val positions = result.getPositions()

        // root + "ca" + "t" + "r" = 4 nodes
        assertEquals(4, positions.size)
    }

    @Test
    public fun sharedLayoutWorksWithAllOrientations() {
        val tree = buildPrefixTree(listOf("ab", "cd"))
        val adapter = prefixTreeAdapter(tree)

        Orientation.entries.forEach { orientation ->
            val config = WalkerLayoutConfiguration(
                horizontalDistance = 40f,
                verticalDistance = 60f,
                orientation = orientation,
            )
            val result = WalkerTreeLayout(adapter, config, fixedExtents).layout()
            assertTrue(result.getPositions().isNotEmpty(), "No positions for $orientation")
        }
    }

    @Test
    public fun defaultInputProducesNonEmptyTree() {
        val words = DEFAULT_INPUT.lowercase().split("\\s+".toRegex())
            .map { it.filter(Char::isLetter) }
            .filter { it.isNotEmpty() }
        val tree = buildPrefixTree(words)
        assertTrue(tree.children.isNotEmpty())
    }
}
