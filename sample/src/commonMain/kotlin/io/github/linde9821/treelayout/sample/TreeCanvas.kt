package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
    var internalZoom by remember { mutableStateOf(zoom) }
    val effectiveZoom = if (onZoomChange != null) zoom else internalZoom
    val currentZoom by rememberUpdatedState(effectiveZoom)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)

    val nodeColor = MaterialTheme.colors.primary
    val edgeColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    val canvasBackground = MaterialTheme.colors.background

    Canvas(
        modifier = modifier
            .clipToBounds()
            .background(canvasBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    panOffset += pan
                    val newZoom = (currentZoom * gestureZoom).coerceIn(0.1f, 5f)
                    if (currentOnZoomChange != null) {
                        currentOnZoomChange?.invoke(newZoom)
                    } else {
                        internalZoom = newZoom
                    }
                }
            }
    ) {
        val centerX = size.width / 2f + panOffset.x
        val centerY = size.height / 2f + panOffset.y

        scale(effectiveZoom, pivot = Offset(size.width / 2f, size.height / 2f)) {
            positions.forEach { (node, pos) ->
                node.children.forEach { child ->
                    val childPos = positions[child] ?: return@forEach
                    drawLine(
                        color = edgeColor,
                        start = Offset(pos.x + centerX, pos.y + centerY),
                        end = Offset(childPos.x + centerX, childPos.y + centerY),
                        strokeWidth = 1.5f / effectiveZoom,
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
                    color = nodeColor,
                    topLeft = Offset(x - rectW / 2f, y - rectH / 2f),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(8f, 8f),
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x - textLayout.size.width / 2f,
                        y - textLayout.size.height / 2f,
                    ),
                    color = Color(0xFF0F172A),
                )
            }
        }
    }
}
