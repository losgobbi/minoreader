package org.dev.minoreader.reader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/** Extraction via jsoup (JVM): fetch + main-content heuristic + DOM walk → ContentBlock. */
class JsoupArticleExtractor : ArticleExtractor {

    override fun blocksFromHtml(html: String, baseUrl: String?): List<ContentBlock> {
        val doc = if (baseUrl != null) Jsoup.parse(html, baseUrl) else Jsoup.parse(html)
        val body = doc.body()
        cleanup(body)
        return walk(body)
    }

    override suspend fun fetchAndExtract(url: String): ExtractedArticle = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (minoreader RSS reader)")
            .timeout(15_000)
            .followRedirects(true)
            .get()
        val main = pickMainContent(doc)
        cleanup(main)
        ExtractedArticle(blocks = walk(main), cleanHtml = main.html())
    }

    // ---- main-content heuristic ----
    private fun pickMainContent(doc: Document): Element {
        doc.select("script, style, noscript, nav, header, footer, aside, form, iframe").remove()
        doc.select("article").maxByOrNull { it.text().length }
            ?.let { if (it.text().length > 200) return it }
        doc.selectFirst("main, [role=main]")
            ?.let { if (it.text().length > 200) return it }
        val best = doc.select("div, section").maxByOrNull { el -> el.select("p").sumOf { it.text().length } }
        if (best != null && best.select("p").sumOf { it.text().length } > 200) return best
        return doc.body()
    }

    private fun cleanup(root: Element) {
        root.select(
            "script, style, noscript, nav, header, footer, aside, form, iframe, button, " +
                ".share, .social, .newsletter, .ad, .ads, [aria-hidden=true]",
        ).remove()
    }

    // ---- walk DOM -> blocks ----
    private fun walk(root: Element): List<ContentBlock> {
        val out = mutableListOf<ContentBlock>()
        traverse(root, out)
        return out
    }

    private fun traverse(el: Element, out: MutableList<ContentBlock>) {
        for (child in el.children()) {
            when (child.normalName()) {
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val level = child.normalName().substring(1).toIntOrNull()?.coerceIn(1, 6) ?: 2
                    val t = child.text().trim()
                    if (t.isNotEmpty()) out += ContentBlock.Heading(level, t)
                }
                "p" -> {
                    val spans = inlineSpans(child)
                    if (spans.any { it.text.isNotBlank() }) out += ContentBlock.Paragraph(spans)
                }
                "ul", "ol" -> {
                    for (li in child.children().filter { it.normalName() == "li" }) {
                        val spans = inlineSpans(li)
                        if (spans.any { it.text.isNotBlank() }) out += ContentBlock.Bullet(spans)
                    }
                }
                "blockquote" -> {
                    val t = child.text().trim()
                    if (t.isNotEmpty()) out += ContentBlock.Quote(t)
                }
                "pre" -> {
                    val t = child.wholeText().trimEnd()
                    if (t.isNotBlank()) out += ContentBlock.Code(t)
                }
                "figure" -> child.selectFirst("img")?.let { imageBlock(it)?.let(out::add) }
                "img" -> imageBlock(child)?.let(out::add)
                "hr" -> out += ContentBlock.Divider
                else -> {
                    val hasBlockChildren = child.select("p, div, ul, ol, h1, h2, h3, h4, blockquote, pre, figure").isNotEmpty()
                    if (!hasBlockChildren) {
                        val spans = inlineSpans(child)
                        if (spans.any { it.text.isNotBlank() }) out += ContentBlock.Paragraph(spans)
                    } else {
                        traverse(child, out)
                    }
                }
            }
        }
    }

    private fun imageBlock(img: Element): ContentBlock.Image? {
        val src = img.absUrl("src").ifBlank { img.attr("src") }
        if (src.isBlank()) return null
        return ContentBlock.Image(url = src, alt = img.attr("alt").ifBlank { null })
    }

    // ---- inline spans ----
    private fun inlineSpans(el: Element): List<TextSpan> {
        val spans = mutableListOf<TextSpan>()
        collectInline(el, bold = false, italic = false, link = null, out = spans)
        return coalesce(spans)
    }

    private fun collectInline(node: Node, bold: Boolean, italic: Boolean, link: String?, out: MutableList<TextSpan>) {
        for (child in node.childNodes()) {
            when (child) {
                is TextNode -> {
                    val t = child.text()
                    if (t.isNotEmpty()) out += TextSpan(t, bold, italic, link)
                }
                is Element -> when (child.normalName()) {
                    "b", "strong" -> collectInline(child, true, italic, link, out)
                    "i", "em" -> collectInline(child, bold, true, link, out)
                    "a" -> collectInline(
                        child, bold, italic,
                        child.absUrl("href").ifBlank { child.attr("href") }.ifBlank { link }, out,
                    )
                    "br" -> out += TextSpan("\n", bold, italic, link)
                    else -> collectInline(child, bold, italic, link, out)
                }
            }
        }
    }

    private fun coalesce(spans: List<TextSpan>): List<TextSpan> {
        val result = mutableListOf<TextSpan>()
        for (s in spans) {
            val last = result.lastOrNull()
            if (last != null && last.bold == s.bold && last.italic == s.italic && last.link == s.link) {
                result[result.lastIndex] = last.copy(text = last.text + s.text)
            } else {
                result += s
            }
        }
        return result
    }
}
