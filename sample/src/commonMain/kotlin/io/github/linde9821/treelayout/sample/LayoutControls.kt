package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.linde9821.treelayout.Orientation
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
public fun LayoutControls(
    state: TreeVisualizationState,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var layoutExpanded by remember { mutableStateOf(false) }
    var orientationExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Layout Controls", style = MaterialTheme.typography.subtitle1)

        // Layout type selector
        Box {
            OutlinedButton(onClick = { layoutExpanded = true }) {
                Text(state.layoutType.name)
            }
            DropdownMenu(
                expanded = layoutExpanded,
                onDismissRequest = { layoutExpanded = false },
            ) {
                LayoutType.entries.forEach { type ->
                    DropdownMenuItem(onClick = {
                        state.onLayoutTypeChange(type)
                        layoutExpanded = false
                    }) { Text(type.name) }
                }
            }
        }

        when (state.layoutType) {
            LayoutType.Walker -> WalkerControls(state, compact, orientationExpanded) {
                orientationExpanded = it
            }
            LayoutType.RadialWalker -> RadialWalkerControls(state, compact)
            LayoutType.DirectAngular -> DirectAngularControls(state, compact)
        }

        // Node padding (shared across all layouts)
        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Padding H: ${state.nodePaddingH.toInt()}", fontSize = 12.sp)
                    Slider(value = state.nodePaddingH, onValueChange = state.onNodePaddingHChange, valueRange = 0f..40f)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Padding V: ${state.nodePaddingV.toInt()}", fontSize = 12.sp)
                    Slider(value = state.nodePaddingV, onValueChange = state.onNodePaddingVChange, valueRange = 0f..40f)
                }
            }
        } else {
            Text("Node Padding H: ${state.nodePaddingH.toInt()}")
            Slider(value = state.nodePaddingH, onValueChange = state.onNodePaddingHChange, valueRange = 0f..40f)
            Text("Node Padding V: ${state.nodePaddingV.toInt()}")
            Slider(value = state.nodePaddingV, onValueChange = state.onNodePaddingVChange, valueRange = 0f..40f)
        }
    }
}

@Composable
private fun WalkerControls(
    state: TreeVisualizationState,
    compact: Boolean,
    orientationExpanded: Boolean,
    onOrientationExpandedChange: (Boolean) -> Unit,
) {
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("H Distance: ${state.horizontalDistance.toInt()}", fontSize = 12.sp)
                Slider(value = state.horizontalDistance, onValueChange = state.onHorizontalDistanceChange, valueRange = 0f..200f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("V Distance: ${state.verticalDistance.toInt()}", fontSize = 12.sp)
                Slider(value = state.verticalDistance, onValueChange = state.onVerticalDistanceChange, valueRange = 0f..200f)
            }
        }
    } else {
        Text("Horizontal Distance: ${state.horizontalDistance.toInt()}")
        Slider(value = state.horizontalDistance, onValueChange = state.onHorizontalDistanceChange, valueRange = 0f..200f)
        Text("Vertical Distance: ${state.verticalDistance.toInt()}")
        Slider(value = state.verticalDistance, onValueChange = state.onVerticalDistanceChange, valueRange = 0f..200f)
    }

    Box {
        OutlinedButton(onClick = { onOrientationExpandedChange(true) }) {
            Text(state.orientation.name)
        }
        DropdownMenu(
            expanded = orientationExpanded,
            onDismissRequest = { onOrientationExpandedChange(false) },
        ) {
            Orientation.entries.forEach { o ->
                DropdownMenuItem(onClick = {
                    state.onOrientationChange(o)
                    onOrientationExpandedChange(false)
                }) { Text(o.name) }
            }
        }
    }
}

@Composable
private fun RadialWalkerControls(state: TreeVisualizationState, compact: Boolean) {
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Layer Dist: ${state.layerDistance.toInt()}", fontSize = 12.sp)
                Slider(value = state.layerDistance, onValueChange = state.onLayerDistanceChange, valueRange = 10f..200f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Margin: ${(state.margin * 100).roundToInt() / 100f}", fontSize = 12.sp)
                Slider(value = state.margin, onValueChange = state.onMarginChange, valueRange = 0f..PI.toFloat())
            }
        }
    } else {
        Text("Layer Distance: ${state.layerDistance.toInt()}")
        Slider(value = state.layerDistance, onValueChange = state.onLayerDistanceChange, valueRange = 10f..200f)
        Text("Margin: ${(state.margin * 100).roundToInt() / 100f}")
        Slider(value = state.margin, onValueChange = state.onMarginChange, valueRange = 0f..PI.toFloat())
    }
    Text("Rotation: ${(state.rotation * 100).roundToInt() / 100f}")
    Slider(value = state.rotation, onValueChange = state.onRotationChange, valueRange = 0f..2f * PI.toFloat())
}

@Composable
private fun DirectAngularControls(state: TreeVisualizationState, compact: Boolean) {
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Layer Dist: ${state.layerDistance.toInt()}", fontSize = 12.sp)
                Slider(value = state.layerDistance, onValueChange = state.onLayerDistanceChange, valueRange = 10f..200f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Rotation: ${(state.rotation * 100).roundToInt() / 100f}", fontSize = 12.sp)
                Slider(value = state.rotation, onValueChange = state.onRotationChange, valueRange = 0f..2f * PI.toFloat())
            }
        }
    } else {
        Text("Layer Distance: ${state.layerDistance.toInt()}")
        Slider(value = state.layerDistance, onValueChange = state.onLayerDistanceChange, valueRange = 10f..200f)
        Text("Rotation: ${(state.rotation * 100).roundToInt() / 100f}")
        Slider(value = state.rotation, onValueChange = state.onRotationChange, valueRange = 0f..2f * PI.toFloat())
    }
}
