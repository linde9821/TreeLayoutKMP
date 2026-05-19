package io.github.linde9821.treelayout.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import io.github.linde9821.treelayout.NodeExtentProvider
import io.github.linde9821.treelayout.Orientation
import io.github.linde9821.treelayout.Point
import io.github.linde9821.treelayout.walker.WalkerLayoutConfiguration
import io.github.linde9821.treelayout.walker.WalkerTreeLayout

public class TreeVisualizationState(
    public val input: String,
    public val onInputChange: (String) -> Unit,
    public val horizontalDistance: Float,
    public val onHorizontalDistanceChange: (Float) -> Unit,
    public val verticalDistance: Float,
    public val onVerticalDistanceChange: (Float) -> Unit,
    public val orientation: Orientation,
    public val onOrientationChange: (Orientation) -> Unit,
    public val nodePaddingH: Float,
    public val onNodePaddingHChange: (Float) -> Unit,
    public val nodePaddingV: Float,
    public val onNodePaddingVChange: (Float) -> Unit,
    public val positions: Map<PrefixNode, Point>,
    public val textLayouts: Map<PrefixNode, TextLayoutResult>,
)

@Composable
public fun rememberTreeVisualizationState(): TreeVisualizationState {
    var input by remember { mutableStateOf(DEFAULT_INPUT) }
    var horizontalDistance by remember { mutableStateOf(40f) }
    var verticalDistance by remember { mutableStateOf(60f) }
    var orientation by remember { mutableStateOf(Orientation.TopToBottom) }
    var nodePaddingH by remember { mutableStateOf(12f) }
    var nodePaddingV by remember { mutableStateOf(8f) }

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

    return TreeVisualizationState(
        input = input,
        onInputChange = { input = it },
        horizontalDistance = horizontalDistance,
        onHorizontalDistanceChange = { horizontalDistance = it },
        verticalDistance = verticalDistance,
        onVerticalDistanceChange = { verticalDistance = it },
        orientation = orientation,
        onOrientationChange = { orientation = it },
        nodePaddingH = nodePaddingH,
        onNodePaddingHChange = { nodePaddingH = it },
        nodePaddingV = nodePaddingV,
        onNodePaddingVChange = { nodePaddingV = it },
        positions = positions,
        textLayouts = textLayouts,
    )
}
