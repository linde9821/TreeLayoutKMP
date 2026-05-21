package io.github.linde9821.treelayout.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val root = document.getElementById("root") ?: return
    ComposeViewport(root) {
        TreeVisualization()
    }
}
