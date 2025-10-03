package at.meks.pv.forecast.battery

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kotlin_compose_web_desktop",
    ) {
        App()
    }
}