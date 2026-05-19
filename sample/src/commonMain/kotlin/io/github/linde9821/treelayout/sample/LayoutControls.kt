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

@Composable
public fun LayoutControls(
    state: TreeVisualizationState,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var orientationExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Layout Controls", style = MaterialTheme.typography.subtitle1)

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
            Text("Horizontal Distance: ${state.horizontalDistance.toInt()}")
            Slider(value = state.horizontalDistance, onValueChange = state.onHorizontalDistanceChange, valueRange = 0f..200f)
            Text("Vertical Distance: ${state.verticalDistance.toInt()}")
            Slider(value = state.verticalDistance, onValueChange = state.onVerticalDistanceChange, valueRange = 0f..200f)
            Text("Node Padding H: ${state.nodePaddingH.toInt()}")
            Slider(value = state.nodePaddingH, onValueChange = state.onNodePaddingHChange, valueRange = 0f..40f)
            Text("Node Padding V: ${state.nodePaddingV.toInt()}")
            Slider(value = state.nodePaddingV, onValueChange = state.onNodePaddingVChange, valueRange = 0f..40f)
        }

        Box {
            OutlinedButton(onClick = { orientationExpanded = true }) {
                Text(state.orientation.name)
            }
            DropdownMenu(
                expanded = orientationExpanded,
                onDismissRequest = { orientationExpanded = false },
            ) {
                Orientation.entries.forEach { o ->
                    DropdownMenuItem(onClick = {
                        state.onOrientationChange(o)
                        orientationExpanded = false
                    }) { Text(o.name) }
                }
            }
        }
    }
}
