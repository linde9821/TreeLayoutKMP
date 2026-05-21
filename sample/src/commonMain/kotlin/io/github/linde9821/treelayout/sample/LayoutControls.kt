package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Controls", style = MaterialTheme.typography.h6)

        SectionLabel("Layout Algorithm")
        ChipSelector(
            selected = state.layoutType.name,
            onClick = { layoutExpanded = true },
            expanded = layoutExpanded,
            onDismiss = { layoutExpanded = false },
            items = LayoutType.entries.map { it.name },
            onSelect = { name ->
                state.onLayoutTypeChange(LayoutType.valueOf(name))
                layoutExpanded = false
            },
        )

        when (state.layoutType) {
            LayoutType.Walker -> WalkerControls(state, compact, orientationExpanded) {
                orientationExpanded = it
            }
            LayoutType.RadialWalker -> RadialWalkerControls(state, compact)
            LayoutType.DirectAngular -> DirectAngularControls(state, compact)
        }

        SectionLabel("Node Padding")
        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledSlider("H", state.nodePaddingH, state.onNodePaddingHChange, 0f..40f, Modifier.weight(1f))
                LabeledSlider("V", state.nodePaddingV, state.onNodePaddingVChange, 0f..40f, Modifier.weight(1f))
            }
        } else {
            LabeledSlider("Horizontal", state.nodePaddingH, state.onNodePaddingHChange, 0f..40f)
            LabeledSlider("Vertical", state.nodePaddingV, state.onNodePaddingVChange, 0f..40f)
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
    SectionLabel("Spacing")
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledSlider("H", state.horizontalDistance, state.onHorizontalDistanceChange, 0f..200f, Modifier.weight(1f))
            LabeledSlider("V", state.verticalDistance, state.onVerticalDistanceChange, 0f..200f, Modifier.weight(1f))
        }
    } else {
        LabeledSlider("Horizontal", state.horizontalDistance, state.onHorizontalDistanceChange, 0f..200f)
        LabeledSlider("Vertical", state.verticalDistance, state.onVerticalDistanceChange, 0f..200f)
    }

    SectionLabel("Orientation")
    ChipSelector(
        selected = state.orientation.name,
        onClick = { onOrientationExpandedChange(true) },
        expanded = orientationExpanded,
        onDismiss = { onOrientationExpandedChange(false) },
        items = Orientation.entries.map { it.name },
        onSelect = { name ->
            state.onOrientationChange(Orientation.valueOf(name))
            onOrientationExpandedChange(false)
        },
    )
}

@Composable
private fun RadialWalkerControls(state: TreeVisualizationState, compact: Boolean) {
    SectionLabel("Radial")
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledSlider("Layer", state.layerDistance, state.onLayerDistanceChange, 10f..200f, Modifier.weight(1f))
            LabeledSlider("Margin", state.margin, state.onMarginChange, 0f..PI.toFloat(), Modifier.weight(1f), decimals = true)
        }
    } else {
        LabeledSlider("Layer Distance", state.layerDistance, state.onLayerDistanceChange, 10f..200f)
        LabeledSlider("Margin", state.margin, state.onMarginChange, 0f..PI.toFloat(), decimals = true)
    }
    LabeledSlider("Rotation", state.rotation, state.onRotationChange, 0f..2f * PI.toFloat(), decimals = true)
}

@Composable
private fun DirectAngularControls(state: TreeVisualizationState, compact: Boolean) {
    SectionLabel("Radial")
    if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledSlider("Layer", state.layerDistance, state.onLayerDistanceChange, 10f..200f, Modifier.weight(1f))
            LabeledSlider("Rotation", state.rotation, state.onRotationChange, 0f..2f * PI.toFloat(), Modifier.weight(1f), decimals = true)
        }
    } else {
        LabeledSlider("Layer Distance", state.layerDistance, state.onLayerDistanceChange, 10f..200f)
        LabeledSlider("Rotation", state.rotation, state.onRotationChange, 0f..2f * PI.toFloat(), decimals = true)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    decimals: Boolean = false,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.body2)
            Text(
                if (decimals) "${(value * 100).roundToInt() / 100f}" else "${value.toInt()}",
                style = MaterialTheme.typography.body1,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.primary,
                activeTrackColor = MaterialTheme.colors.primary,
                inactiveTrackColor = MaterialTheme.colors.surface,
            ),
        )
    }
}

@Composable
private fun ChipSelector(
    selected: String,
    onClick: () -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<String>,
    onSelect: (String) -> Unit,
) {
    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(selected, style = MaterialTheme.typography.button, color = MaterialTheme.colors.primary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            items.forEach { item ->
                DropdownMenuItem(onClick = { onSelect(item) }) {
                    Text(item, style = MaterialTheme.typography.body1)
                }
            }
        }
    }
}
