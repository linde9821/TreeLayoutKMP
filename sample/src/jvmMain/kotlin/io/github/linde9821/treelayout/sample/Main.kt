package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.linde9821.treelayout.*

data class SampleNode(val label: String, val children: List<SampleNode> = emptyList())

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "TreeLayoutKMP Sample") {
        TreeVisualization()
    }
}

@Composable
fun TreeVisualization() {
    val tree = SampleNode(
        "CEO", listOf(
            SampleNode("Engineering", listOf(
                SampleNode("Frontend"),
                SampleNode("Backend"),
                SampleNode("Infra"),
            )),
            SampleNode("Design"),
            SampleNode("Marketing", listOf(
                SampleNode("Growth"),
                SampleNode("Brand"),
            )),
        )
    )

    val adapter = remember {
        object : TreeAdapter<SampleNode> {
            private val parentMap = buildMap<SampleNode, SampleNode?> {
                fun walk(node: SampleNode, parent: SampleNode?) {
                    put(node, parent)
                    node.children.forEach { walk(it, node) }
                }
                walk(tree, null)
            }

            override fun root(): SampleNode = tree
            override fun children(node: SampleNode): List<SampleNode> = node.children
            override fun parent(node: SampleNode): SampleNode? = parentMap[node]
        }
    }

    var horizontalDistance by remember { mutableStateOf(40f) }
    var verticalDistance by remember { mutableStateOf(60f) }
    var orientation by remember { mutableStateOf(Orientation.TopToBottom) }
    var nodePaddingH by remember { mutableStateOf(12f) }
    var nodePaddingV by remember { mutableStateOf(8f) }
    var orientationExpanded by remember { mutableStateOf(false) }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)

    // Pre-measure all labels to get actual text sizes
    val textLayouts = remember(tree) {
        buildMap<SampleNode, androidx.compose.ui.text.TextLayoutResult> {
            fun walk(node: SampleNode) {
                put(node, textMeasurer.measure(node.label, textStyle))
                node.children.forEach { walk(it) }
            }
            walk(tree)
        }
    }

    val extents = object : NodeExtentProvider<SampleNode> {
        override fun width(node: SampleNode): Float =
            textLayouts[node]!!.size.width.toFloat() + nodePaddingH * 2
        override fun height(node: SampleNode): Float =
            textLayouts[node]!!.size.height.toFloat() + nodePaddingV * 2
    }

    val config = WalkerLayoutConfiguration(
        horizontalDistance = horizontalDistance,
        verticalDistance = verticalDistance,
        orientation = orientation,
    )

    val result = WalkerTreeLayout(adapter, config, extents).layout()
    val positions = result.getPositions()

    Row(modifier = Modifier.fillMaxSize()) {
        // Controls panel
        Column(
            modifier = Modifier.width(260.dp).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Layout Controls", style = MaterialTheme.typography.h6)

            Text("Horizontal Distance: ${"%.0f".format(horizontalDistance)}")
            Slider(
                value = horizontalDistance,
                onValueChange = { horizontalDistance = it },
                valueRange = 0f..200f,
            )

            Text("Vertical Distance: ${"%.0f".format(verticalDistance)}")
            Slider(
                value = verticalDistance,
                onValueChange = { verticalDistance = it },
                valueRange = 0f..200f,
            )

            Text("Node Padding H: ${"%.0f".format(nodePaddingH)}")
            Slider(
                value = nodePaddingH,
                onValueChange = { nodePaddingH = it },
                valueRange = 0f..40f,
            )

            Text("Node Padding V: ${"%.0f".format(nodePaddingV)}")
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

        // Tree canvas
        var panOffset by remember { mutableStateOf(Offset.Zero) }
        var zoom by remember { mutableStateOf(1f) }

        @OptIn(ExperimentalComposeUiApi::class)
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
                // Draw edges
                positions.forEach { (node, pos) ->
                    node.children.forEach { child ->
                        val childPos = positions[child]!!
                        drawLine(
                            color = Color.Gray,
                            start = Offset(pos.x + centerX, pos.y + centerY),
                            end = Offset(childPos.x + centerX, childPos.y + centerY),
                            strokeWidth = 2f / zoom,
                        )
                    }
                }

                // Draw nodes
                positions.forEach { (node, pos) ->
                    val x = pos.x + centerX
                    val y = pos.y + centerY
                    val textLayout = textLayouts[node]!!
                    val rectW = textLayout.size.width + nodePaddingH * 2
                    val rectH = textLayout.size.height + nodePaddingV * 2

                    drawRoundRect(
                        color = Color(0xFF4CAF50),
                        topLeft = Offset(x - rectW / 2f, y - rectH / 2f),
                        size = Size(rectW, rectH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(x - textLayout.size.width / 2f, y - textLayout.size.height / 2f),
                    )
                }
            }
        }
    }
}
