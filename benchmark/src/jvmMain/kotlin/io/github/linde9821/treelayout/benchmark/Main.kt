package io.github.linde9821.treelayout.benchmark

import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.scale.scaleLinetypeManual
import org.jetbrains.letsPlot.themes.themeMinimal
import kotlin.time.measureTime

private class Node(val children: MutableList<Node> = mutableListOf())

private class NodeAdapter(private val root: Node) : TreeAdapter<Node> {
    private val parentMap = HashMap<Node, Node?>()

    init {
        parentMap[root] = null
        buildParentMap(root)
    }

    private fun buildParentMap(node: Node) {
        for (child in node.children) {
            parentMap[child] = node
            buildParentMap(child)
        }
    }

    override fun root(): Node = root
    override fun children(node: Node): List<Node> = node.children
    override fun parent(node: Node): Node? = parentMap[node]
}

/**
 * Builds a balanced tree with approximately [targetSize] nodes.
 * Uses a breadth-first approach with a fixed branching factor.
 */
private fun buildTree(targetSize: Int): Node {
    val root = Node()
    if (targetSize <= 1) return root

    val queue = ArrayDeque<Node>()
    queue.add(root)
    var count = 1
    val branchingFactor = 4

    while (count < targetSize && queue.isNotEmpty()) {
        val parent = queue.removeFirst()
        val childrenToAdd = minOf(branchingFactor, targetSize - count)
        repeat(childrenToAdd) {
            val child = Node()
            parent.children.add(child)
            queue.add(child)
            count++
        }
    }
    return root
}

fun main() {
    val sizes = generateSequence(1) {
        it + 15_000
    }
    val results = mutableListOf<Pair<Int, Long>>()

    print("Heating up the jvm... ")
    for (size in sizes.take(50)) {
        val tree = buildTree(size)
        val adapter = NodeAdapter(tree)
        measureTime {
            WalkerTreeLayout(adapter).layout()
        }
    }
    println("done")

    for (size in sizes.takeWhile { it < 6_100_000 }) {
        print("Laying out tree with $size nodes... ")
        val tree = buildTree(size)
        val adapter = NodeAdapter(tree)

        val duration = measureTime {
            WalkerTreeLayout(adapter).layout()
        }
        val ms = duration.inWholeMilliseconds
        println("${ms}ms")
        results.add(size to ms)
    }

    val nodes = results.map { it.first }
    val times = results.map { it.second }

    // O(n) reference line scaled to match the last measured data point
    val maxNodes = nodes.last().toDouble()
    val maxTime = times.last().toDouble()
    val onLine = nodes.map { (it / maxNodes * maxTime).toLong() }

    val measuredData = mapOf(
        "nodes" to nodes,
        "time_ms" to times,
        "series" to List(nodes.size) { "Measured" }
    )

    val referenceData = mapOf(
        "nodes" to nodes,
        "time_ms" to onLine,
        "series" to List(nodes.size) { "O(n) reference" }
    )

    val data = mapOf(
        "nodes" to nodes + nodes,
        "time_ms" to times + onLine,
        "series" to List(nodes.size) { "Measured" } + List(nodes.size) { "O(n) reference" }
    )

    val jvmVersion = System.getProperty("java.version")
    val os = System.getProperty("os.name")

    val plot = letsPlot(data) { x = "nodes"; y = "time_ms" } +
            geomLine(data = referenceData) { color = "series"; linetype = "series" } +
            geomLine(data = measuredData) { color = "series"; linetype = "series" } +
            geomPoint(data = measuredData, size = 1.5) { color = "series" } +
            scaleColorManual(values = listOf("#999999", "#2166AC")) +
            scaleLinetypeManual(values = listOf("dashed", "solid")) +
            xlab("Number of Nodes") +
            ylab("Time (ms)") +
            labs(subtitle = "JVM $jvmVersion · $os") +
            ggtitle("TreeLayoutKMP Benchmark") +
            themeMinimal()

    ggsave(plot, "benchmark_results.png", dpi = 150, w = 10.0, h = 6.0)
    println("\nChart written to benchmark_results.png")
}
