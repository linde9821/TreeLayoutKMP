package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayoutTransitionTest {

    @Test
    fun interpolateAtZeroReturnsFromPositions() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f), "b" to Point(10f, 10f)), 2)
        val to = TreeLayoutResult(mapOf("a" to Point(20f, 20f), "b" to Point(30f, 30f)), 2)
        val result = LayoutTransition(from, to).interpolate(0f)
        assertEquals(Point(0f, 0f), result.getPosition("a"))
        assertEquals(Point(10f, 10f), result.getPosition("b"))
    }

    @Test
    fun interpolateAtOneReturnsToPositions() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f)), 1)
        val to = TreeLayoutResult(mapOf("a" to Point(10f, 20f)), 1)
        val result = LayoutTransition(from, to).interpolate(1f)
        assertEquals(Point(10f, 20f), result.getPosition("a"))
    }

    @Test
    fun interpolateAtHalfReturnsMidpoint() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f)), 1)
        val to = TreeLayoutResult(mapOf("a" to Point(10f, 20f)), 1)
        val result = LayoutTransition(from, to).interpolate(0.5f)
        assertEquals(Point(5f, 10f), result.getPosition("a"))
    }

    @Test
    fun enteringNodesStayAtTargetPosition() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f)), 1)
        val to = TreeLayoutResult(mapOf("a" to Point(10f, 10f), "b" to Point(20f, 20f)), 2)
        val transition = LayoutTransition(from, to)
        assertEquals(setOf("b"), transition.enteringNodes)
        val result = transition.interpolate(0.5f)
        assertEquals(Point(20f, 20f), result.getPosition("b"))
    }

    @Test
    fun exitingNodesStayAtFromPosition() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f), "b" to Point(5f, 5f)), 2)
        val to = TreeLayoutResult(mapOf("a" to Point(10f, 10f)), 1)
        val transition = LayoutTransition(from, to)
        assertEquals(setOf("b"), transition.exitingNodes)
        val result = transition.interpolate(0.5f)
        assertEquals(Point(5f, 5f), result.getPosition("b"))
    }

    @Test
    fun allNodesContainsUnionOfBothStates() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f), "b" to Point(1f, 1f)), 1)
        val to = TreeLayoutResult(mapOf("b" to Point(2f, 2f), "c" to Point(3f, 3f)), 1)
        val transition = LayoutTransition(from, to)
        assertEquals(setOf("a", "b", "c"), transition.allNodes)
        assertEquals(setOf("b"), transition.persistentNodes)
        assertEquals(setOf("a"), transition.exitingNodes)
        assertEquals(setOf("c"), transition.enteringNodes)
    }

    @Test
    fun maxDepthIsMaxOfBothStates() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f)), 3)
        val to = TreeLayoutResult(mapOf("a" to Point(1f, 1f)), 5)
        val result = LayoutTransition(from, to).interpolate(0.5f)
        assertEquals(5, result.getMaxDepth())
    }

    @Test
    fun progressIsClampedToZeroOne() {
        val from = TreeLayoutResult(mapOf("a" to Point(0f, 0f)), 1)
        val to = TreeLayoutResult(mapOf("a" to Point(10f, 10f)), 1)
        val transition = LayoutTransition(from, to)
        assertEquals(Point(0f, 0f), transition.interpolate(-1f).getPosition("a"))
        assertEquals(Point(10f, 10f), transition.interpolate(2f).getPosition("a"))
    }
}
