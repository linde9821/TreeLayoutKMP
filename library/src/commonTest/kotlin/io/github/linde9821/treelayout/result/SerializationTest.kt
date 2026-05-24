package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {

    @Test
    fun roundTripPreservesPositions() {
        val original = TreeLayoutResult(
            mapOf("a" to Point(1.5f, 2.5f), "b" to Point(-3f, 4f)),
            maxDepth = 3,
        )
        val json = original.toJson { it }
        val restored = TreeLayoutResult.fromJson(json) { it }
        assertEquals(original.getPosition("a").x, restored.getPosition("a").x)
        assertEquals(original.getPosition("a").y, restored.getPosition("a").y)
        assertEquals(original.getPosition("b").x, restored.getPosition("b").x)
        assertEquals(original.getPosition("b").y, restored.getPosition("b").y)
        assertEquals(3, restored.getMaxDepth())
    }

    @Test
    fun roundTripWithSpecialCharactersInKeys() {
        val original = TreeLayoutResult(
            mapOf("node \"A\"" to Point(1f, 2f)),
            maxDepth = 1,
        )
        val json = original.toJson { it }
        val restored = TreeLayoutResult.fromJson(json) { it }
        assertEquals(Point(1f, 2f), restored.getPosition("node \"A\""))
    }

    @Test
    fun emptyResultSerializes() {
        val original = TreeLayoutResult<String>(emptyMap(), maxDepth = 0)
        val json = original.toJson { it }
        val restored = TreeLayoutResult.fromJson(json) { it }
        assertEquals(0, restored.getMaxDepth())
        assertEquals(emptyMap(), restored.getPositions())
    }

    @Test
    fun toJsonProducesValidFormat() {
        val result = TreeLayoutResult(mapOf("x" to Point(1f, 2f)), maxDepth = 1)
        val json = result.toJson { it }
        assertTrue(json.contains("\"maxDepth\":1"))
        assertTrue(json.contains("\"k\":\"x\""))
        assertTrue(json.contains("\"px\":1.0"))
        assertTrue(json.contains("\"py\":2.0"))
    }

    @Test
    fun customNodeToKeyMapping() {
        val result = TreeLayoutResult(mapOf(42 to Point(5f, 6f)), maxDepth = 1)
        val json = result.toJson { "node_$it" }
        val restored = TreeLayoutResult.fromJson(json) { it.removePrefix("node_").toInt() }
        assertEquals(Point(5f, 6f), restored.getPosition(42))
    }

    private fun assertTrue(condition: Boolean) {
        assertEquals(true, condition)
    }
}
