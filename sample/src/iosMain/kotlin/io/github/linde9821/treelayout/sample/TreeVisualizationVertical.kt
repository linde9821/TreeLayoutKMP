package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun TreeVisualizationVertical() {
    val state = rememberTreeVisualizationState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                focusManager.clearFocus()
            }
            .padding(16.dp)
    ) {
        LayoutControls(
            state = state,
            modifier = Modifier.weight(0.4f).verticalScroll(rememberScrollState()),
            compact = true,
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(
            value = state.input,
            onValueChange = state.onInputChange,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            label = { Text("Words (space-separated)") },
            textStyle = TextStyle(fontSize = 13.sp),
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        TreeCanvas(
            positions = state.positions,
            textLayouts = state.textLayouts,
            nodePaddingH = state.nodePaddingH,
            nodePaddingV = state.nodePaddingV,
            zoom = 1f,
            onZoomChange = null,
            modifier = Modifier.fillMaxWidth().weight(0.6f),
        )
    }
}
