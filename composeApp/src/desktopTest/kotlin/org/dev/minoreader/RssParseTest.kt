package org.dev.minoreader

import com.prof18.rssparser.RssParser
import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.rss.FeedService
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the real RssParser XML integration + import/dedup. Desktop/JVM only: on Android,
 * RssParser uses the framework's XmlPullParser, which is a non-functional stub under local
 * unit tests (would need an instrumented/Robolectric runtime). The import/dedup, search, DB,
 * extraction and date/HTML logic are covered cross-platform in commonTest.
 */
class RssParseTest {

    private val sampleRss = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
          <channel>
            <title>Example</title>
            <link>https://ex.com</link>
            <item>
              <title>Article A</title>
              <link>https://ex.com/a</link>
              <guid>a</guid>
              <pubDate>Wed, 02 Oct 2024 13:00:00 GMT</pubDate>
              <description>&lt;p&gt;Summary &amp;amp; test&lt;/p&gt;</description>
            </item>
            <item>
              <title>Article B</title>
              <link>https://ex.com/b</link>
              <guid>b</guid>
              <pubDate>Thu, 03 Oct 2024 09:30:00 +0000</pubDate>
              <description>text B</description>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    @Test
    fun parseAndImport() = runTestBlocking {
        val db = newInMemoryDb()
        val feeds = FeedRepository(db)
        val articles = ArticleRepository(db)

        feeds.addFeed("https://ex.com/rss", "Example", "tech")
        val feedId = feeds.allFeeds().first().id

        val channel = RssParser().parse(sampleRss)
        assertEquals("Example", channel.title)
        assertEquals(2, channel.items.size)

        val service = FeedService(feeds, articles)
        service.importChannel(feedId, channel)
        // re-importing does not duplicate (dedup by guid)
        service.importChannel(feedId, channel)

        val unread = articles.unread(null).first()
        assertEquals(2, unread.size, "dedup should keep 2 items")

        val a = articles.detail(unread.first { it.title == "Article A" }.id)!!
        assertEquals("Summary & test", a.excerpt, "excerpt without HTML/entities")
        assertTrue((a.publishedAt ?: 0L) > 0L, "RFC-822 pubDate parsed")
    }
}
