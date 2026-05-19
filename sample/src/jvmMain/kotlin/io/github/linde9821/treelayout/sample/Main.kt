package io.github.linde9821.treelayout.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main(): Unit = application {
    Window(onCloseRequest = ::exitApplication, title = "TreeLayoutKMP Sample") {
        TreeVisualization()
    }
}
