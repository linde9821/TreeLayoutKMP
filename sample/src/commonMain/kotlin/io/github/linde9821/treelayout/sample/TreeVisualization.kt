package io.github.linde9821.treelayout.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public const val DEFAULT_INPUT: String = "Not all those who wander are lost"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun TreeVisualization() {
    TreeLayoutTheme {
        val state = rememberTreeVisualizationState()
        var zoom by remember { mutableStateOf(1f) }

        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            Column(
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                LayoutControls(state = state)
            }

            Column(
                modifier = Modifier
                    .widthIn(min = 150.dp)
                    .width(200.dp)
                    .fillMaxHeight()
                    .padding(16.dp),
            ) {
                Text("Words", style = MaterialTheme.typography.subtitle1)
                OutlinedTextField(
                    value = state.input,
                    onValueChange = state.onInputChange,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
                    textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colors.onBackground),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.surface,
                        cursorColor = MaterialTheme.colors.primary,
                    ),
                )
            }

            TreeCanvas(
                positions = state.positions,
                textLayouts = state.textLayouts,
                nodePaddingH = state.nodePaddingH,
                nodePaddingV = state.nodePaddingV,
                zoom = zoom,
                onZoomChange = { zoom = it },
                modifier = Modifier
                    .fillMaxSize()
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        val scrollDelta = event.changes.first().scrollDelta.y
                        zoom = (zoom * if (scrollDelta > 0) 0.9f else 1.1f).coerceIn(0.1f, 5f)
                    },
            )
        }
    }
}
