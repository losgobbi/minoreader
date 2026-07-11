package org.dev.minoreader

import org.dev.minoreader.reader.ContentBlock
import org.dev.minoreader.reader.JsoupArticleExtractor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Mirrors the Linux/desktop ExtractorTest: JsoupArticleExtractor lives in the JVM-shared
// source set (jvmShared), which is on the Android compilation classpath too.
class ExtractorTest {

    private val extractor = JsoupArticleExtractor()

    @Test
    fun walksHtmlToBlocks() {
        val html = """
            <h2>Title</h2>
            <p>A <b>paragraph</b> with a <a href="https://x.com/a">link</a>.</p>
            <ul><li>item 1</li><li>item 2</li></ul>
            <blockquote>a quote</blockquote>
            <img src="/photo.jpg" alt="photo">
        """.trimIndent()

        val blocks = extractor.blocksFromHtml(html, "https://ex.com")

        assertTrue(blocks.any { it is ContentBlock.Heading && it.text == "Title" }, "heading")

        val paragraph = blocks.filterIsInstance<ContentBlock.Paragraph>().first()
        assertTrue(paragraph.spans.any { it.bold && it.text.contains("paragraph") }, "bold")
        assertTrue(paragraph.spans.any { it.link == "https://x.com/a" }, "resolved link")

        assertEquals(2, blocks.count { it is ContentBlock.Bullet }, "2 list items")
        assertTrue(blocks.any { it is ContentBlock.Quote }, "quote")

        val image = blocks.filterIsInstance<ContentBlock.Image>().firstOrNull()
        assertTrue(image != null && image.url == "https://ex.com/photo.jpg", "img with absolute URL")
    }
}
