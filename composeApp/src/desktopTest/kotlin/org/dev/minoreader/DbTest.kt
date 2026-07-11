package org.dev.minoreader

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.db.MinoDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DbTest {

    private fun newDb(): MinoDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MinoDb.Schema.create(driver)
        driver.execute(null, "PRAGMA foreign_keys=ON;", 0)
        return MinoDb(driver)
    }

    @Test
    fun feedAndArticleRoundtrip() = runBlocking {
        val db = newDb()
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
