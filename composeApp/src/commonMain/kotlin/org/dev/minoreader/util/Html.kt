package org.dev.minoreader.util

private val tagRegex = Regex("<[^>]*>")
private val wsRegex = Regex("\\s+")

/** Strips HTML tags and decodes common entities, producing plain text for the excerpt. */
fun stripHtml(input: String?): String? {
    if (input == null) return null
    return tagRegex.replace(input, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&#8217;", "’")
        .replace("&hellip;", "…")
        .let { wsRegex.replace(it, " ") }
        .trim()
        .ifBlank { null }
}
