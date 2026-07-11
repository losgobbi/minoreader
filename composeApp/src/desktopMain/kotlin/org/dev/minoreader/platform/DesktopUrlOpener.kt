package org.dev.minoreader.platform

import java.awt.Desktop
import java.net.URI

class DesktopUrlOpener : UrlOpener {
    override fun open(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }
}
