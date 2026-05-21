package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeLayoutResultTest {

    private fun result(vararg entries: Pair<String, Point>): TreeLayoutResult<String> =
        MapTreeLayoutResult(entries.toMap(), maxDepth = 1)

    @Test
    fun getBoundsReturnsCorrectBoundingBox() {
        val r = result("a" to Point(-5f, 2f), "b" to Point(10f, 8f), "c" to Point(3f, -1f))
        val b = r.getBounds()
        assertEquals(-5f, b.minX)
        assertEquals(-1f, b.minY)
        assertEquals(10f, b.maxX)
        assertEquals(8f, b.maxY)
        assertEquals(15f, b.width)
        assertEquals(9f, b.height)
    }

    @Test
    fun getBoundsEmptyPositionsReturnsZero() {
        val r = result()
        val b = r.getBounds()
        assertEquals(Bounds(0f, 0f, 0f, 0f), b)
    }

    @Test
    fun normalizedShiftsMinCornerToOrigin() {
        val r = result("a" to Point(5f, 10f), "b" to Point(15f, 30f))
        val n = r.normalized()
        assertEquals(Point(0f, 0f), n.getPosition("a"))
        assertEquals(Point(10f, 20f), n.getPosition("b"))
    }

    @Test
    fun translatedShiftsAllPositions() {
        val r = result("a" to Point(1f, 2f), "b" to Point(3f, 4f))
        val t = r.translated(10f, -5f)
        assertEquals(Point(11f, -3f), t.getPosition("a"))
        assertEquals(Point(13f, -1f), t.getPosition("b"))
    }

    @Test
    fun scaledToFitsWithinTargetDimensions() {
        // Layout spans 0..100 x 0..50 → scale to 200x200 → limited by width ratio (2.0) vs height ratio (4.0) → uses 2.0
        val r = result("a" to Point(0f, 0f), "b" to Point(100f, 50f))
        val s = r.scaledTo(200f, 200f)
        assertEquals(Point(0f, 0f), s.getPosition("a"))
        assertEquals(Point(200f, 100f), s.getPosition("b"))
    }

    @Test
    fun scaledToPreservesAspectRatio() {
        // Layout spans 0..10 x 0..20 → scale to 100x100 → limited by height (5.0) vs width (10.0) → uses 5.0
        val r = result("a" to Point(0f, 0f), "b" to Point(10f, 20f))
        val s = r.scaledTo(100f, 100f)
        assertEquals(Point(0f, 0f), s.getPosition("a"))
        assertEquals(Point(50f, 100f), s.getPosition("b"))
    }

    @Test
    fun scaledToNormalizesBeforeScaling() {
        // Layout at offset: 10..30 x 5..15 → width=20, height=10 → scale to 40x40 → factor=2.0
        val r = result("a" to Point(10f, 5f), "b" to Point(30f, 15f))
        val s = r.scaledTo(40f, 40f)
        assertEquals(Point(0f, 0f), s.getPosition("a"))
        assertEquals(Point(40f, 20f), s.getPosition("b"))
    }

    @Test
    fun scaledToSingleNodeReturnsOrigin() {
        val r = result("a" to Point(42f, 99f))
        val s = r.scaledTo(800f, 600f)
        assertEquals(Point(0f, 0f), s.getPosition("a"))
    }

    @Test
    fun chainingNormalizedAndScaledTo() {
        val r = result("a" to Point(-10f, -20f), "b" to Point(10f, 20f))
        val s = r.normalized().scaledTo(100f, 100f)
        // After normalized: a=(0,0), b=(20,40). scaledTo 100x100 → factor=min(5, 2.5)=2.5
        assertEquals(Point(0f, 0f), s.getPosition("a"))
        assertEquals(Point(50f, 100f), s.getPosition("b"))
    }

    @Test
    fun maxDepthIsPreservedThroughTransformations() {
        val r = MapTreeLayoutResult(mapOf("a" to Point(0f, 0f)), maxDepth = 5)
        assertEquals(5, r.normalized().getMaxDepth())
        assertEquals(5, r.translated(1f, 1f).getMaxDepth())
        assertEquals(5, r.scaledTo(100f, 100f).getMaxDepth())
    }
}