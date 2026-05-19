package io.github.linde9821.treelayout.sample

import io.github.linde9821.treelayout.TreeAdapter

public class PrefixNode(
    public val label: String,
    public val children: MutableList<PrefixNode> = mutableListOf(),
)

public fun buildPrefixTree(words: List<String>): PrefixNode {
    val root = PrefixNode("")
    for (word in words) {
        if (word.isBlank()) continue
        insertWord(root, word)
    }
    return root
}

private fun insertWord(root: PrefixNode, word: String) {
    var current = root
    var i = 0
    while (i < word.length) {
        val child = current.children.find { word.startsWith(it.label, i) }
        if (child != null) {
            i += child.label.length
            current = child
        } else {
            // Check for partial match with an existing child
            val partial = current.children.find { it.label.isNotEmpty() && word[i] == it.label[0] }
            if (partial != null) {
                // Find common prefix length
                var common = 0
                while (common < partial.label.length && i + common < word.length && partial.label[common] == word[i + common]) {
                    common++
                }
                // Split the existing node
                val splitNode = PrefixNode(
                    partial.label.substring(0, common), mutableListOf(
                        PrefixNode(partial.label.substring(common), partial.children)
                    )
                )
                current.children[current.children.indexOf(partial)] = splitNode
                if (i + common < word.length) {
                    splitNode.children.add(PrefixNode(word.substring(i + common)))
                }
                return
            } else {
                current.children.add(PrefixNode(word.substring(i)))
                return
            }
        }
    }
}

public fun prefixTreeAdapter(root: PrefixNode): TreeAdapter<PrefixNode> {
    val parentMap = buildMap<PrefixNode, PrefixNode?> {
        fun walk(node: PrefixNode, parent: PrefixNode?) {
            put(node, parent)
            node.children.forEach { walk(it, node) }
        }
        walk(root, null)
    }
    return object : TreeAdapter<PrefixNode> {
        override fun root(): PrefixNode = root
        override fun children(node: PrefixNode): List<PrefixNode> = node.children
        override fun parent(node: PrefixNode): PrefixNode? = parentMap[node]
    }
}
