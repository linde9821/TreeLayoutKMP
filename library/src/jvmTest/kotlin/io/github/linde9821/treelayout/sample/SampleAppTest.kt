package io.github.linde9821.treelayout.sample

import io.github.linde9821.treelayout.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.WalkerTreeLayout
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SampleAppTest {

    private val tree = SampleNode(
        "root", listOf(
            SampleNode(
                "A", listOf(
                    SampleNode("A1"),
                    SampleNode(
                        "A2", listOf(
                            SampleNode("A2a"),
                            SampleNode("A2b"),
                            SampleNode("A2c")
                        )
                    ),
                    SampleNode("A3")
                )
            ),
            SampleNode("B"),
            SampleNode(
                "C", listOf(
                    SampleNode(
                        "C1", listOf(
                            SampleNode("C1x")
                        )
                    ),
                    SampleNode("C2")
                )
            )
        )
    )

    private val adapter = SampleAdapter(tree)
    private val layout = WalkerTreeLayout(
        adapter = adapter,
        configuration = WalkerLayoutConfiguration(
            horizontalDistance = 5.0f,
            verticalDistance = 2.0f
        )
    )

    @Test
    fun layoutProducesPositionsForAllNodes(): Unit {
        val result = layout.layout()
        assertEquals(13, result.getPositions().size)
    }

    @Test
    fun maxDepthIsThree(): Unit {
        val result = layout.layout()
        assertEquals(3, result.getMaxDepth())
    }

    @Test
    fun noNodesOverlap(): Unit {
        val result = layout.layout()
        val positions = result.getPositions().values.toList()
        for (i in positions.indices) {
            for (j in i + 1 until positions.size) {
                val a = positions[i]
                val b = positions[j]
                assertTrue(a.x != b.x || a.y != b.y, "Two nodes share position ($a)")
            }
        }
    }

    @Test
    fun rootIsAtDepthZero(): Unit {
        val result = layout.layout()
        assertEquals(0.0f, result.getPosition(tree).y)
    }

    @Test
    fun exportLayoutToPngCreatesFile(): Unit {
        val childrenMap = mapOf(
            "root" to listOf("A", "B", "C"),
            "A" to listOf("A1", "A2", "A3"),
            "A2" to listOf("A2a", "A2b", "A2c"),
            "C" to listOf("C1", "C2"),
            "C1" to listOf("C1x"),
        )
        val stringAdapter = StringTreeAdapter("root", childrenMap)
        val stringLayout = WalkerTreeLayout(
            adapter = stringAdapter,
            configuration = WalkerLayoutConfiguration(horizontalDistance = 5.0f, verticalDistance = 2.0f)
        )
        val result = stringLayout.layout()
        val tmpFile = File.createTempFile("tree_layout_test", ".png")
        tmpFile.deleteOnExit()
        exportLayoutToPng(result, stringAdapter, tmpFile)
        assertTrue(tmpFile.exists(), "PNG file should exist")
        assertTrue(tmpFile.length() > 0, "PNG file should not be empty")
    }
}
