package org.dev.minoreader

import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DbTest {

    @Test
    fun feedAndArticleRoundtrip() = runTestBlocking {
        val db = newInMemoryDb()
        val feeds = FeedRepository(db)
        val articles = ArticleRepository(db)

        feeds.addFeed("https://example.com/rss", "Example", "tech")
        val allFeeds = feeds.allFeeds()
        assertEquals(1, allFeeds.size)
        val feedId = allFeeds.first().id

        articles.insert(feedId, "guid-1", "First", "https://example.com/1", null, 1000L, "summary", null, 5000L)
        // dedup: same guid is ignored
        articles.insert(feedId, "guid-1", "First (dup)", "https://example.com/1", null, 1000L, "summary", null, 5000L)
        articles.insert(feedId, "guid-2", "Second", "https://example.com/2", null, 2000L, null, null, 5000L)

        val unread = articles.unread(null).first()
        assertEquals(2, unread.size, "dedup should keep 2 articles")
        assertEquals("Second", unread.first().title, "ordered by publishedAt desc")

        assertEquals(2, articles.unread("tech").first().size, "category filter")
        assertEquals(0, articles.unread("news").first().size)

        // marking as read removes it from the feed
        articles.setRead(unread.first().id, true)
        assertEquals(1, articles.unread(null).first().size)

        // favorite
        val secondId = unread[1].id
        articles.setFavorite(secondId, true)
        assertEquals(1, articles.favorites().first().size)

        assertTrue(feeds.categories().first().contains("tech"))
    }
}
