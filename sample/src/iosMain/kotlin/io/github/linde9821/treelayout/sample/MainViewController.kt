package io.github.linde9821.treelayout.sample

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
public fun MainViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme {
        TreeVisualizationVertical()
    }
}
