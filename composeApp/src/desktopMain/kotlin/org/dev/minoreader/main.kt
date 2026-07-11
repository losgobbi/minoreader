package org.dev.minoreader

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.dev.minoreader.di.appModules
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModules())
    }
    application {
        val density = LocalDensity.current
        val icon = remember(density) {
            useResource("minoreader.svg") { loadSvgPainter(it, density) }
        }
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 1000.dp, height = 720.dp),
            title = "minoreader",
            icon = icon,
        ) {
            App()
        }
    }
}
