package org.dev.minoreader.platform

/** Opens a URL in the system browser (open the original article). */
interface UrlOpener {
    fun open(url: String)
}
