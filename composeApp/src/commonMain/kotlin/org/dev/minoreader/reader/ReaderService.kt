package org.dev.minoreader.reader

import org.dev.minoreader.data.ArticleDetail
import org.dev.minoreader.data.ArticleRepository

/**
 * Provides the article content as blocks:
 * 1) uses the saved HTML (RSS content:encoded or from a previous fetch);
 * 2) otherwise, downloads the page + extracts (heuristic readability) and caches it;
 * 3) otherwise, falls back to the RSS excerpt.
 */
class ReaderService(
    private val articleRepo: ArticleRepository,
    private val extractor: ArticleExtractor,
) {
    suspend fun load(detail: ArticleDetail): List<ContentBlock> {
        val saved = detail.contentHtml
        if (!saved.isNullOrBlank()) {
            val blocks = runCatching { extractor.blocksFromHtml(saved, detail.link) }.getOrDefault(emptyList())
            if (blocks.isNotEmpty()) return blocks
        }

        val link = detail.link
        if (!link.isNullOrBlank()) {
            val extracted = runCatching { extractor.fetchAndExtract(link) }.getOrNull()
            if (extracted != null && extracted.blocks.isNotEmpty()) {
                runCatching { articleRepo.setContentHtml(detail.id, extracted.cleanHtml) }
                return extracted.blocks
            }
        }

        val excerpt = detail.excerpt
        return if (!excerpt.isNullOrBlank()) {
            listOf(ContentBlock.Paragraph(listOf(TextSpan(excerpt))))
        } else {
            emptyList()
        }
    }
}
