package io.github.linde9821.treelayout.sample

import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.WalkerTreeLayout

internal data class SampleNode(val name: String, val children: List<SampleNode> = emptyList())

internal class SampleAdapter(private val tree: SampleNode) : TreeAdapter<SampleNode> {
    private val parentMap: Map<SampleNode, SampleNode?> = buildMap {
        fun walk(node: SampleNode, parent: SampleNode?) {
            put(node, parent)
            node.children.forEach { walk(it, node) }
        }
        walk(tree, null)
    }

    override fun root(): SampleNode = tree
    override fun children(node: SampleNode): List<SampleNode> = node.children
    override fun parent(node: SampleNode): SampleNode? = parentMap[node]
}

internal fun renderAscii(layout: WalkerTreeLayout<SampleNode>, adapter: SampleAdapter) {
    val result = layout.layout()
    val positions = result.getPositions()

    // Scale: each unit X -> 8 chars, each unit Y -> 3 rows
    val scaleX = 8
    val scaleY = 3

    data class Placed(val name: String, val col: Int, val row: Int)

    val minX = positions.values.minOf { it.x }
    val placed = positions.map { (node, pt) ->
        Placed(node.name, ((pt.x - minX) * scaleX).toInt(), (pt.y * scaleY).toInt())
    }

    val maxCol = placed.maxOf { it.col + it.name.length }
    val maxRow = placed.maxOf { it.row }

    val grid = Array(maxRow + 1) { CharArray(maxCol + 1) { ' ' } }

    for (p in placed) {
        for ((i, ch) in p.name.withIndex()) {
            if (p.col + i <= maxCol) grid[p.row][p.col + i] = ch
        }
    }

    for (row in grid) {
        println(String(row).trimEnd())
    }
}

internal class StringTreeAdapter(
    private val rootNode: String,
    private val childrenMap: Map<String, List<String>>,
) : TreeAdapter<String> {
    private val parentMap: Map<String, String?> = buildMap {
        fun walk(node: String, parent: String?) {
            put(node, parent)
            (childrenMap[node] ?: emptyList()).forEach { walk(it, node) }
        }
        walk(rootNode, null)
    }

    override fun root(): String = rootNode
    override fun children(node: String): List<String> = childrenMap[node] ?: emptyList()
    override fun parent(node: String): String? = parentMap[node]
}

public fun main(): Unit {
    // Asymmetric tree: 4 levels deep, varying widths
    val tree = SampleNode(
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

    val adapter = SampleAdapter(tree)
    val layout = WalkerTreeLayout(
        adapter = adapter,
        configuration = WalkerLayoutConfiguration(
            horizontalDistance = 5.0f,
            verticalDistance = 2.0f
        )
    )

    renderAscii(layout, adapter)

    // PNG export using String-based adapter
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
        configuration = WalkerLayoutConfiguration(
            horizontalDistance = 5.0f,
            verticalDistance = 2.0f
        )
    )
    val result = stringLayout.layout()
    val outputFile = java.io.File("tree_layout.png")
    exportLayoutToPng(result, stringAdapter, outputFile)
    println("PNG exported to: ${outputFile.absolutePath}")

    // Demo: Variable node sizes with NodeExtentProvider
    println("\n--- Variable Node Sizes Demo ---")
    val sizedExtents = object : NodeExtentProvider<SampleNode> {
        override fun width(node: SampleNode): Float = node.name.length.toFloat() * 2f
        override fun height(node: SampleNode): Float = 3f
    }
    val sizedLayout = WalkerTreeLayout(
        adapter = adapter,
        configuration = WalkerLayoutConfiguration(horizontalDistance = 2.0f, verticalDistance = 1.0f),
        nodeExtentProvider = sizedExtents,
    )
    val sizedResult = sizedLayout.layout()
    sizedResult.getPositions().forEach { (node, point) ->
        val w = sizedExtents.width(node)
        val h = sizedExtents.height(node)
        println("${node.name.padEnd(4)} -> (${"%6.1f".format(point.x)}, ${"%4.1f".format(point.y)})  size=${w}x${h}")
    }
}
