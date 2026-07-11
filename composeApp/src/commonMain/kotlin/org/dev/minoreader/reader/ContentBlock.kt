package org.dev.minoreader.reader

/** Neutral representation of the article content (no jsoup), renderable by Compose in commonMain. */
sealed interface ContentBlock {
    data class Heading(val level: Int, val text: String) : ContentBlock
    data class Paragraph(val spans: List<TextSpan>) : ContentBlock
    data class Bullet(val spans: List<TextSpan>) : ContentBlock
    data class Quote(val text: String) : ContentBlock
    data class Code(val text: String) : ContentBlock
    data class Image(val url: String, val alt: String?) : ContentBlock
    data object Divider : ContentBlock
}

/** A text span with inline style (bold/italic/link). */
data class TextSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val link: String? = null,
)
