package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import io.github.linde9821.treelayout.Point

@Composable
public fun TreeCanvas(
    positions: Map<PrefixNode, Point>,
    textLayouts: Map<PrefixNode, TextLayoutResult>,
    nodePaddingH: Float,
    nodePaddingV: Float,
    zoom: Float,
    onZoomChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffset += dragAmount
                }
            }
    ) {
        val centerX = size.width / 2f + panOffset.x
        val centerY = size.height / 2f + panOffset.y

        scale(zoom, pivot = Offset(size.width / 2f, size.height / 2f)) {
            positions.forEach { (node, pos) ->
                node.children.forEach { child ->
                    val childPos = positions[child] ?: return@forEach
                    drawLine(
                        color = Color.Gray,
                        start = Offset(pos.x + centerX, pos.y + centerY),
                        end = Offset(childPos.x + centerX, childPos.y + centerY),
                        strokeWidth = 2f / zoom,
                    )
                }
            }

            positions.forEach { (node, pos) ->
                val textLayout = textLayouts[node] ?: return@forEach
                val x = pos.x + centerX
                val y = pos.y + centerY
                val rectW = textLayout.size.width + nodePaddingH * 2
                val rectH = textLayout.size.height + nodePaddingV * 2

                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(x - rectW / 2f, y - rectH / 2f),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(6f, 6f),
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x - textLayout.size.width / 2f,
                        y - textLayout.size.height / 2f,
                    ),
                )
            }
        }
    }
}
