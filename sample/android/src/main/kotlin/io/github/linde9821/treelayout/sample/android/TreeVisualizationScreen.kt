package io.github.linde9821.treelayout.sample.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Orientation
import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.TreeAdapter
import io.github.linde9821.treelayout.radial.angular.DirectAngularPlacementConfiguration
import io.github.linde9821.treelayout.radial.angular.DirectAngularPlacementLayout
import io.github.linde9821.treelayout.radial.walker.RadialWalkerLayoutConfiguration
import io.github.linde9821.treelayout.radial.walker.RadialWalkerTreeLayout
import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout
import kotlin.math.PI
import kotlin.math.roundToInt

private const val DEFAULT_INPUT: String = "Not all those who wander are lost"

private enum class LayoutType { Walker, RadialWalker, DirectAngular }

private class PrefixNode(val label: String, val children: MutableList<PrefixNode> = mutableListOf())

private fun buildPrefixTree(words: List<String>): PrefixNode {
    val root = PrefixNode("")
    for (word in words) {
        if (word.isBlank()) continue
        insertWord(root, word)
    }
    return root
}

private fun insertWord(root: PrefixNode, word: String) {
    var current = root
    var i = 0
    while (i < word.length) {
        val child = current.children.find { word.startsWith(it.label, i) }
        if (child != null) {
            i += child.label.length
            current = child
        } else {
            val partial = current.children.find { it.label.isNotEmpty() && word[i] == it.label[0] }
            if (partial != null) {
                var common = 0
                while (common < partial.label.length && i + common < word.length && partial.label[common] == word[i + common]) {
                    common++
                }
                val splitNode = PrefixNode(
                    partial.label.substring(0, common),
                    mutableListOf(PrefixNode(partial.label.substring(common), partial.children))
                )
                current.children[current.children.indexOf(partial)] = splitNode
                if (i + common < word.length) {
                    splitNode.children.add(PrefixNode(word.substring(i + common)))
                }
                return
            } else {
                current.children.add(PrefixNode(word.substring(i)))
                return
            }
        }
    }
}

@Composable
internal fun TreeVisualizationScreen() {
    var input by remember { mutableStateOf(DEFAULT_INPUT) }
    var layoutType by remember { mutableStateOf(LayoutType.Walker) }
    var horizontalDistance by remember { mutableStateOf(40f) }
    var verticalDistance by remember { mutableStateOf(60f) }
    var orientation by remember { mutableStateOf(Orientation.TopToBottom) }
    var nodePaddingH by remember { mutableStateOf(12f) }
    var nodePaddingV by remember { mutableStateOf(8f) }
    var layerDistance by remember { mutableStateOf(80f) }
    var margin by remember { mutableStateOf(0.5f) }
    var rotation by remember { mutableStateOf(0f) }
    var orientationExpanded by remember { mutableStateOf(false) }
    var layoutTypeExpanded by remember { mutableStateOf(false) }

    val words = input.lowercase().split("\\s+".toRegex())
        .map { it.filter(Char::isLetter) }
        .filter { it.isNotEmpty() }
    val tree = buildPrefixTree(words)

    val parentMap = buildMap<PrefixNode, PrefixNode?> {
        fun walk(node: PrefixNode, parent: PrefixNode?) {
            put(node, parent)
            node.children.forEach { walk(it, node) }
        }
        walk(tree, null)
    }
    val adapter = object : TreeAdapter<PrefixNode> {
        override fun root(): PrefixNode = tree
        override fun children(node: PrefixNode): List<PrefixNode> = node.children
        override fun parent(node: PrefixNode): PrefixNode? = parentMap[node]
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)

    val textLayouts = remember(tree) {
        buildMap {
            fun walk(node: PrefixNode) {
                put(node, textMeasurer.measure(node.label.ifEmpty { "·" }, textStyle))
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

    val positions: Map<PrefixNode, Point> = when (layoutType) {
        LayoutType.Walker -> {
            val config = WalkerLayoutConfiguration(
                horizontalDistance = horizontalDistance,
                verticalDistance = verticalDistance,
                orientation = orientation,
            )
            WalkerTreeLayout(adapter, config, extents).layout().getPositions()
        }
        LayoutType.RadialWalker -> {
            val config = RadialWalkerLayoutConfiguration(
                layerDistance = layerDistance,
                margin = margin,
                rotation = rotation,
            )
            RadialWalkerTreeLayout(adapter, config, extents).layout().getPositions()
        }
        LayoutType.DirectAngular -> {
            val config = DirectAngularPlacementConfiguration(
                layerDistance = layerDistance,
                rotation = rotation,
            )
            DirectAngularPlacementLayout(adapter, config).layout().getPositions()
        }
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Layout Controls", style = MaterialTheme.typography.subtitle1)

            // Layout type selector
            Box {
                OutlinedButton(onClick = { layoutTypeExpanded = true }) { Text(layoutType.name) }
                DropdownMenu(expanded = layoutTypeExpanded, onDismissRequest = { layoutTypeExpanded = false }) {
                    LayoutType.entries.forEach { lt ->
                        DropdownMenuItem(onClick = { layoutType = lt; layoutTypeExpanded = false }) { Text(lt.name) }
                    }
                }
            }

            when (layoutType) {
                LayoutType.Walker -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("H Distance: ${horizontalDistance.toInt()}", fontSize = 12.sp)
                            Slider(value = horizontalDistance, onValueChange = { horizontalDistance = it }, valueRange = 0f..200f)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("V Distance: ${verticalDistance.toInt()}", fontSize = 12.sp)
                            Slider(value = verticalDistance, onValueChange = { verticalDistance = it }, valueRange = 0f..200f)
                        }
                    }
                    Box {
                        OutlinedButton(onClick = { orientationExpanded = true }) { Text(orientation.name) }
                        DropdownMenu(expanded = orientationExpanded, onDismissRequest = { orientationExpanded = false }) {
                            Orientation.entries.forEach { o ->
                                DropdownMenuItem(onClick = { orientation = o; orientationExpanded = false }) { Text(o.name) }
                            }
                        }
                    }
                }
                LayoutType.RadialWalker -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Layer: ${layerDistance.toInt()}", fontSize = 12.sp)
                            Slider(value = layerDistance, onValueChange = { layerDistance = it }, valueRange = 10f..200f)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Margin: ${(margin * 100).roundToInt() / 100f}", fontSize = 12.sp)
                            Slider(value = margin, onValueChange = { margin = it }, valueRange = 0f..PI.toFloat())
                        }
                    }
                    Column {
                        Text("Rotation: ${(rotation * 100).roundToInt() / 100f}", fontSize = 12.sp)
                        Slider(value = rotation, onValueChange = { rotation = it }, valueRange = 0f..2f * PI.toFloat())
                    }
                }
                LayoutType.DirectAngular -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Layer: ${layerDistance.toInt()}", fontSize = 12.sp)
                            Slider(value = layerDistance, onValueChange = { layerDistance = it }, valueRange = 10f..200f)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Rotation: ${(rotation * 100).roundToInt() / 100f}", fontSize = 12.sp)
                            Slider(value = rotation, onValueChange = { rotation = it }, valueRange = 0f..2f * PI.toFloat())
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Padding H: ${nodePaddingH.toInt()}", fontSize = 12.sp)
                    Slider(value = nodePaddingH, onValueChange = { nodePaddingH = it }, valueRange = 0f..40f)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Padding V: ${nodePaddingV.toInt()}", fontSize = 12.sp)
                    Slider(value = nodePaddingV, onValueChange = { nodePaddingV = it }, valueRange = 0f..40f)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            label = { Text("Words (space-separated)") },
            textStyle = TextStyle(fontSize = 13.sp),
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        var panOffset by remember { mutableStateOf(Offset.Zero) }
        var zoom by remember { mutableStateOf(1f) }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .clipToBounds()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        panOffset += pan
                        zoom = (zoom * gestureZoom).coerceIn(0.1f, 5f)
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
