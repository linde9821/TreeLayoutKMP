package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Orientation
import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout

private const val DEFAULT_INPUT: String = "Not all those who wander are lost"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeVisualization() {
    var input by remember { mutableStateOf(DEFAULT_INPUT) }
    var horizontalDistance by remember { mutableStateOf(40f) }
    var verticalDistance by remember { mutableStateOf(60f) }
    var orientation by remember { mutableStateOf(Orientation.TopToBottom) }
    var nodePaddingH by remember { mutableStateOf(12f) }
    var nodePaddingV by remember { mutableStateOf(8f) }
    var orientationExpanded by remember { mutableStateOf(false) }

    val words = input.lowercase().split("\\s+".toRegex())
        .map { it.filter(Char::isLetter) }
        .filter { it.isNotEmpty() }
    val tree = buildPrefixTree(words)
    val adapter = prefixTreeAdapter(tree)

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)

    val textLayouts = remember(tree) {
        buildMap {
            fun walk(node: PrefixNode) {
                val display = node.label.ifEmpty { "·" }
                put(node, textMeasurer.measure(display, textStyle))
                node.children.forEach { walk(it) }
            }
            walk(tree)
        }
    }

    val extents = object : NodeExtentProvider<PrefixNode> {
        override fun width(node: PrefixNode): Float =
            (textLayouts[node]?.size?.width?.toFloat() ?: 0f) + nodePaddingH * 2

        override fun height(node: PrefixNode): Float =
            (textLayouts[node]?.size?.height?.toFloat() ?: 0f) + nodePaddingV * 2
    }

    val config = WalkerLayoutConfiguration(
        horizontalDistance = horizontalDistance,
        verticalDistance = verticalDistance,
        orientation = orientation,
    )

    val result = WalkerTreeLayout(adapter, config, extents).layout()
    val positions = result.getPositions()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left: Controls
        Column(
            modifier = Modifier.widthIn(min = 180.dp).width(220.dp).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Layout Controls", style = MaterialTheme.typography.subtitle1)

            Text("Horizontal Distance: ${horizontalDistance.toInt()}")
            Slider(
                value = horizontalDistance,
                onValueChange = { horizontalDistance = it },
                valueRange = 0f..200f,
            )

            Text("Vertical Distance: ${verticalDistance.toInt()}")
            Slider(
                value = verticalDistance,
                onValueChange = { verticalDistance = it },
                valueRange = 0f..200f,
            )

            Text("Node Padding H: ${nodePaddingH.toInt()}")
            Slider(
                value = nodePaddingH,
                onValueChange = { nodePaddingH = it },
                valueRange = 0f..40f,
            )

            Text("Node Padding V: ${nodePaddingV.toInt()}")
            Slider(
                value = nodePaddingV,
                onValueChange = { nodePaddingV = it },
                valueRange = 0f..40f,
            )

            Text("Orientation:")
            Box {
                OutlinedButton(onClick = { orientationExpanded = true }) {
                    Text(orientation.name)
                }
                DropdownMenu(
                    expanded = orientationExpanded,
                    onDismissRequest = { orientationExpanded = false },
                ) {
                    Orientation.entries.forEach { o ->
                        DropdownMenuItem(onClick = {
                            orientation = o
                            orientationExpanded = false
                        }) {
                            Text(o.name)
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Middle: Input
        Column(
            modifier = Modifier.widthIn(min = 150.dp).width(200.dp).fillMaxHeight().padding(16.dp),
        ) {
            Text("Words (space-separated)", style = MaterialTheme.typography.subtitle1)
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = TextStyle(fontSize = 13.sp),
            )
        }

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right: Canvas

        var panOffset by remember { mutableStateOf(Offset.Zero) }
        var zoom by remember { mutableStateOf(1f) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .border(2.dp, Color.Gray)
                .onPointerEvent(PointerEventType.Scroll) { event ->
                    val scrollDelta = event.changes.first().scrollDelta.y
                    zoom = (zoom * if (scrollDelta > 0) 0.9f else 1.1f).coerceIn(0.1f, 5f)
                }
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
}
