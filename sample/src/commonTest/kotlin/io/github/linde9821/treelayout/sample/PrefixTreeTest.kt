package io.github.linde9821.treelayout.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class PrefixTreeTest {

    @Test
    public fun emptyInputProducesRootOnly() {
        val tree = buildPrefixTree(emptyList())
        assertEquals("", tree.label)
        assertTrue(tree.children.isEmpty())
    }

    @Test
    public fun singleWordCreatesSingleChild() {
        val tree = buildPrefixTree(listOf("cat"))
        assertEquals(1, tree.children.size)
        assertEquals("cat", tree.children[0].label)
    }

    @Test
    public fun sharedPrefixSplitsNode() {
        val tree = buildPrefixTree(listOf("car", "cat"))
        // root -> "ca" -> ("r", "t")
        val ca = tree.children[0]
        assertEquals("ca", ca.label)
        assertEquals(2, ca.children.size)
        assertEquals("r", ca.children[0].label)
        assertEquals("t", ca.children[1].label)
    }

    @Test
    public fun disjointWordsCreateSeparateBranches() {
        val tree = buildPrefixTree(listOf("ab", "cd"))
        assertEquals(2, tree.children.size)
        assertEquals("ab", tree.children[0].label)
        assertEquals("cd", tree.children[1].label)
    }

    @Test
    public fun threWordsWithSharedPrefix() {
        val tree = buildPrefixTree(listOf("tree", "trie", "try"))
        // root -> "tr" -> ("ee", "ie", "y")
        val tr = tree.children[0]
        assertEquals("tr", tr.label)
        assertEquals(3, tr.children.size)
    }

    @Test
    public fun adapterRootMatchesTree() {
        val tree = buildPrefixTree(listOf("hi"))
        val adapter = prefixTreeAdapter(tree)
        assertEquals(tree, adapter.root())
    }

    @Test
    public fun adapterParentOfRootIsNull() {
        val tree = buildPrefixTree(listOf("hi"))
        val adapter = prefixTreeAdapter(tree)
        assertNull(adapter.parent(tree))
    }

    @Test
    public fun adapterParentIsCorrect() {
        val tree = buildPrefixTree(listOf("hi"))
        val adapter = prefixTreeAdapter(tree)
        val child = tree.children[0]
        assertEquals(tree, adapter.parent(child))
    }

    @Test
    public fun blankWordsAreIgnored() {
        val tree = buildPrefixTree(listOf("", "  ", "a"))
        assertEquals(1, tree.children.size)
    }
}
