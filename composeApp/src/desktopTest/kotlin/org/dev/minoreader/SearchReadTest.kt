package org.dev.minoreader

import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchReadTest {

    @Test
    fun readHistoryAndSearch() = runBlocking {
        val db = newInMemoryDb()
        val feeds = FeedRepository(db)
        val articles = ArticleRepository(db)

        feeds.addFeed("https://ex.com/rss", "Source", "tech")
        val feedId = feeds.allFeeds().first().id

        articles.insert(feedId, "g1", "Kotlin Multiplatform rocks", "l1", null, 100L, "about kmp", null, 1L)
        articles.insert(feedId, "g2", "Rust news", "l2", null, 200L, "about rust", null, 1L)

        // nothing read yet
        assertEquals(0, articles.read().first().size)

        // mark the Kotlin one as read -> shows in Read, leaves the feed
        val kotlinItem = articles.unread(null).first().first { it.title.contains("Kotlin") }
        articles.setRead(kotlinItem.id, true)
        assertEquals(1, articles.read().first().size)
        assertEquals(1, articles.unread(null).first().size)

        // search by title (case-insensitive) and by excerpt
        assertEquals(1, articles.search("kotlin").first().size)
        assertEquals(1, articles.search("rust").first().size)
        assertEquals(2, articles.search("about").first().size)
        assertEquals(0, articles.search("zzz").first().size)
    }
}
