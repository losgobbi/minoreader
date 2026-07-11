package org.dev.minoreader.reader

data class ExtractedArticle(
    val blocks: List<ContentBlock>,
    val cleanHtml: String,
)

/** Extracts the article content. Implemented per platform (JVM/jsoup). */
interface ArticleExtractor {
    /** Converts already-available HTML (e.g. RSS content:encoded) into blocks. */
    fun blocksFromHtml(html: String, baseUrl: String?): List<ContentBlock>

    /** Downloads the article page and extracts the main content (heuristic readability). */
    suspend fun fetchAndExtract(url: String): ExtractedArticle
}
