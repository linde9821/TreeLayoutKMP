package io.github.linde9821.treelayout.result

/**
 * Axis-aligned bounding box of a tree layout.
 */
public data class Bounds(
    public val minX: Float,
    public val minY: Float,
    public val maxX: Float,
    public val maxY: Float,
) {
    public val width: Float get() = maxX - minX
    public val height: Float get() = maxY - minY
}