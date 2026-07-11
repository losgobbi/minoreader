package org.dev.minoreader.rss

import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel
import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.util.currentTimeMillis
import org.dev.minoreader.util.parseFeedDate
import org.dev.minoreader.util.stripHtml

/** Fetches and imports RSS/Atom feeds, deduping by (feedId, guid). */
class FeedService(
    private val feedRepo: FeedRepository,
    private val articleRepo: ArticleRepository,
    private val parser: RssParser = RssParser(),
) {
    /** Adds a feed by URL, fetching the title and importing its items. Returns the title. */
    suspend fun addFeedFromUrl(url: String, category: String): Result<String> = runCatching {
        val channel = parser.getRssChannel(url)
        val title = channel.title?.trim().takeUnless { it.isNullOrBlank() } ?: url
        feedRepo.addFeed(url, title, category)
        val feed = feedRepo.allFeeds().firstOrNull { it.url == url } ?: error("Failed to save feed")
        importChannel(feed.id, channel)
        feedRepo.markFetched(feed.id, title, currentTimeMillis())
        title
    }

    /** Refreshes all feeds; per-feed errors don't stop the others. Returns how many refreshed. */
    suspend fun refreshAll(): Int {
        var updated = 0
        for (feed in feedRepo.allFeeds()) {
            runCatching {
                val channel = parser.getRssChannel(feed.url)
                importChannel(feed.id, channel)
                val title = channel.title?.trim().takeUnless { it.isNullOrBlank() } ?: feed.title
                feedRepo.markFetched(feed.id, title, currentTimeMillis())
            }.onSuccess { updated++ }
        }
        return updated
    }

    internal suspend fun importChannel(feedId: Long, channel: RssChannel) {
        val now = currentTimeMillis()
        for (item in channel.items) {
            val guid = item.guid ?: item.link ?: continue
            val title = item.title?.trim().takeUnless { it.isNullOrBlank() } ?: "(untitled)"
            val excerpt = stripHtml(item.description ?: item.content)?.take(400)
            // If the feed already carries the full content (content:encoded), store it to render
            // in-app without fetching the page. Otherwise the reader fetches and extracts on demand.
            val fullContent = item.content?.takeUnless { it.isBlank() }
            articleRepo.insert(
                feedId = feedId,
                guid = guid,
                title = title,
                link = item.link,
                author = item.author,
                publishedAt = parseFeedDate(item.pubDate),
                excerpt = excerpt,
                contentHtml = fullContent,
                fetchedAt = now,
            )
        }
    }
}
