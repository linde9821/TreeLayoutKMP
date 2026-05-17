package io.github.linde9821.treelayout.sample

import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.TreeLayoutResult
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private const val SCALE = 50
private const val PADDING = 100
private const val NODE_RADIUS = 22

public fun exportLayoutToPng(
    result: TreeLayoutResult<String>,
    adapter: TreeAdapter<String>,
    outputFile: File,
): Unit {
    val positions = result.getPositions()
    val minX = positions.values.minOf { it.x }
    val maxX = positions.values.maxOf { it.x }
    val minY = positions.values.minOf { it.y }
    val maxY = positions.values.maxOf { it.y }

    val width = ((maxX - minX) * SCALE).toInt() + PADDING * 2
    val height = ((maxY - minY) * SCALE).toInt() + PADDING * 2

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.color = Color.WHITE
    g.fillRect(0, 0, width, height)

    fun px(x: Float): Int = ((x - minX) * SCALE).toInt() + PADDING
    fun py(y: Float): Int = ((y - minY) * SCALE).toInt() + PADDING

    // Draw edges
    g.color = Color(70, 90, 140)
    g.stroke = BasicStroke(2f)
    fun drawEdges(node: String) {
        val pos = result.getPosition(node)
        for (child in adapter.children(node)) {
            val cpos = result.getPosition(child)
            g.drawLine(px(pos.x), py(pos.y), px(cpos.x), py(cpos.y))
            drawEdges(child)
        }
    }
    drawEdges(adapter.root())

    // Draw nodes
    val font = Font("SansSerif", Font.BOLD, 12)
    g.font = font
    val fm = g.fontMetrics
    for ((node, pt) in positions) {
        val cx = px(pt.x)
        val cy = py(pt.y)
        g.color = Color(220, 235, 255)
        g.fillOval(cx - NODE_RADIUS, cy - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2)
        g.color = Color(70, 90, 140)
        g.drawOval(cx - NODE_RADIUS, cy - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2)
        g.color = Color.BLACK
        val tw = fm.stringWidth(node)
        g.drawString(node, cx - tw / 2, cy + fm.ascent / 2)
    }

    g.dispose()
    ImageIO.write(image, "png", outputFile)
}
