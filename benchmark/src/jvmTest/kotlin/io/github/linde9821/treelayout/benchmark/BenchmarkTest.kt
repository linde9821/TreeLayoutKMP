package io.github.linde9821.treelayout.benchmark

import kotlin.test.Test
import kotlin.test.assertEquals

class BenchmarkTest {

    @Test
    fun buildTreeProducesCorrectNodeCount() {
        val sizes = listOf(1, 10, 100, 1_000)
        for (size in sizes) {
            val root = buildTreeForTest(size)
            val count = countNodes(root)
            assertEquals(count, size, "Expected $size nodes but got $count")
        }
    }

    @Test
    fun benchmarkRunsWithoutError() {
        // Smoke test: layout a small tree and verify it completes
        val root = buildTreeForTest(100)
        val adapter = TestNodeAdapter(root)
        val layout = io.github.linde9821.treelayout.walker.WalkerTreeLayout(adapter).layout()
        assertEquals(layout.getPositions().size, 100)
    }

    private fun countNodes(node: TestNode): Int =
        1 + node.children.sumOf { countNodes(it) }
}

internal class TestNode(val children: MutableList<TestNode> = mutableListOf())

internal class TestNodeAdapter(private val root: TestNode) : io.github.linde9821.treelayout.TreeAdapter<TestNode> {
    private val parentMap = HashMap<TestNode, TestNode?>()

    init {
        parentMap[root] = null
        buildParentMap(root)
    }

    private fun buildParentMap(node: TestNode) {
        for (child in node.children) {
            parentMap[child] = node
            buildParentMap(child)
        }
    }

    override fun root(): TestNode = root
    override fun children(node: TestNode): List<TestNode> = node.children
    override fun parent(node: TestNode): TestNode? = parentMap[node]
}

internal fun buildTreeForTest(targetSize: Int): TestNode {
    val root = TestNode()
    if (targetSize <= 1) return root

    val queue = ArrayDeque<TestNode>()
    queue.add(root)
    var count = 1
    val branchingFactor = 4

    while (count < targetSize && queue.isNotEmpty()) {
        val parent = queue.removeFirst()
        val childrenToAdd = minOf(branchingFactor, targetSize - count)
        repeat(childrenToAdd) {
            val child = TestNode()
            parent.children.add(child)
            queue.add(child)
            count++
        }
    }
    return root
}
